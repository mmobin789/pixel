/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This is a modern variant of Jake Wharton's Disk LRU Cache developed by Colin White
 *  which is translated to Kotlin with improved I/O by Okio by him.
 * (Using this to not reinvent the wheel)
 */

package io.pixel.loader.cache.disk

import io.pixel.loader.cache.disk.DiskLRUCache.Editor
import okio.*
import java.io.*
import java.io.EOFException
import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * A cache that uses a bounded amount of space on a filesystem. Each cache
 * entry has a string key and a fixed number of values. Each key must match
 * the regex **[a-z0-9_-]{1,120}**. Values are byte sequences,
 * accessible as streams or files. Each value must be between `0` and
 * `Integer.MAX_VALUE` bytes in length.
 *
 *
 * The cache stores its data in a directory on the filesystem. This
 * directory must be exclusive to the cache; the cache may delete or overwrite
 * files from its directory. It is an error for multiple processes to use the
 * same cache directory at the same time.
 *
 *
 * This cache limits the number of bytes that it will store on the
 * filesystem. When the number of stored bytes exceeds the limit, the cache will
 * remove entries in the background until the limit is satisfied. The limit is
 * not strict: the cache may temporarily exceed it while waiting for files to be
 * deleted. The limit does not include filesystem overhead or the cache
 * journal so space-sensitive applications should set a conservative limit.
 *
 *
 * Clients call [.edit] to create or update the values of an entry. An
 * entry may have only one editor at one time; if a value is not available to be
 * edited then [.edit] will return null.
 *
 *  * When an entry is being **created** it is necessary to
 * supply a full set of values; the empty value should be used as a
 * placeholder if necessary.
 *  * When an entry is being **edited**, it is not necessary
 * to supply data for every value; values default to their previous
 * value.
 *
 * Every [.edit] call must be matched by a call to [Editor.commit]
 * or [Editor.abort]. Committing is atomic: a read observes the full set
 * of values as they were before or after the commit, but never a mix of values.
 *
 *
 * Clients call [.get] to read a snapshot of an entry. The read will
 * observe the value at the time that [.get] was called. Updates and
 * removals after the call do not impact ongoing reads.
 *
 *
 * This class is tolerant of some I/O errors. If files are missing from the
 * filesystem, the corresponding entries will be dropped from the cache. If
 * an error occurs while writing a cache value, the edit will fail silently.
 * Callers should handle other problems by catching `IOException` and
 * responding appropriately.
 */
internal class DiskLRUCache private constructor(
    val directory: File,
    private val appVersion: Int,
    private val valueCount: Int,
    private var maxSize: Long
) : Closeable {

    /*
     * This cache uses a journal file named "journal". A typical journal file
     * looks like this:
     *     libcore.io.DiskLruCache
     *     1
     *     100
     *     2
     *
     *     CLEAN 3400330d1dfc7f3f7f4b8d4d803dfcf6 832 21054
     *     DIRTY 335c4c6028171cfddfbaae1a9c313c52
     *     CLEAN 335c4c6028171cfddfbaae1a9c313c52 3934 2342
     *     REMOVE 335c4c6028171cfddfbaae1a9c313c52
     *     DIRTY 1ab96a171faeeee38496d8b330771a7a
     *     CLEAN 1ab96a171faeeee38496d8b330771a7a 1600 234
     *     READ 335c4c6028171cfddfbaae1a9c313c52
     *     READ 3400330d1dfc7f3f7f4b8d4d803dfcf6
     *
     * The first five lines of the journal form its header. They are the
     * constant string "libcore.io.DiskLruCache", the disk cache's version,
     * the application's version, the value count, and a blank line.
     *
     * Each of the subsequent lines in the file is a record of the state of a
     * cache entry. Each line contains space-separated values: a state, a key,
     * and optional state-specific values.
     *   o DIRTY lines track that an entry is actively being created or updated.
     *     Every successful DIRTY action should be followed by a CLEAN or REMOVE
     *     action. DIRTY lines without a matching CLEAN or REMOVE indicate that
     *     temporary files may need to be deleted.
     *   o CLEAN lines track a cache entry that has been successfully published
     *     and may be read. A publish line is followed by the lengths of each of
     *     its values.
     *   o READ lines track accesses for LRU.
     *   o REMOVE lines track entries that have been deleted.
     *
     * The journal file is appended to as cache operations occur. The journal may
     * occasionally be compacted by dropping redundant lines. A temporary file named
     * "journal.tmp" will be used during compaction; that file should be deleted if
     * it exists when the cache is opened.
     */
    private val journalFile = File(directory, JOURNAL_FILE)
    private val journalFileTmp = File(directory, JOURNAL_FILE_TEMP)
    private val journalFileBackup = File(directory, JOURNAL_FILE_BACKUP)

    private val lruEntries = LinkedHashMap<String, Entry>(0, 0.75f, true)

    /** This cache uses a single background thread to evict entries. */
    private val executorService =
        ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, LinkedBlockingQueue())

    private var journalWriter: BufferedSink? = null
    private var redundantOpCount = 0
    private var size = 0L

    /**
     * To differentiate between old and current snapshots, each entry is given
     * a sequence number each time an edit is committed. A snapshot is stale if
     * its sequence number is not equal to its entry's sequence number.
     */
    private var nextSequenceNumber = 0L

    @Throws(IOException::class)
    private fun readJournal() {
        journalFile.source().buffer().use { reader ->
            val magic = reader.readUtf8LineStrict()
            val version = reader.readUtf8LineStrict()
            val appVersionString = reader.readUtf8LineStrict()
            val valueCountString = reader.readUtf8LineStrict()
            val blank = reader.readUtf8LineStrict()

            if (MAGIC != magic
                || VERSION_1 != version
                || appVersion.toString() != appVersionString
                || valueCount.toString() != valueCountString
                || blank.isNotBlank()
            ) {
                throw IOException("Unexpected journal header: [$magic, $version, $valueCountString, $blank]")
            }

            var lineCount = 0
            while (true) {
                try {
                    readJournalLine(reader.readUtf8LineStrict())
                    lineCount++
                } catch (ignored: EOFException) {
                    break
                }
            }
            redundantOpCount = lineCount - lruEntries.count()

            // If we ended on a truncated line, rebuild the journal before appending to it.
            if (!reader.exhausted()) {
                rebuildJournal()
            } else {
                journalWriter = journalFile.appendingSink().buffer()
            }
        }
    }

    @Throws(IOException::class)
    private fun readJournalLine(line: String) {
        val firstSpace = line.indexOf(' ')
        if (firstSpace == -1) {
            throw IOException("Unexpected journal line: $line")
        }

        val keyBegin = firstSpace + 1
        val secondSpace = line.indexOf(' ', keyBegin)
        val key: String
        if (secondSpace == -1) {
            key = line.substring(keyBegin)
            if (firstSpace == REMOVE.count() && line.startsWith(REMOVE)) {
                lruEntries.remove(key)
                return
            }
        } else {
            key = line.substring(keyBegin, secondSpace)
        }

        val entry = lruEntries[key] ?: Entry(key).also { lruEntries[key] = it }

        if (secondSpace != -1 && firstSpace == CLEAN.count() && line.startsWith(CLEAN)) {
            val parts = line
                .substring(secondSpace + 1)
                .split(" ")
                .dropLastWhile { it.isEmpty() }
            entry.readable = true
            entry.currentEditor = null
            entry.setLengths(parts)
        } else if (secondSpace == -1 && firstSpace == DIRTY.count() && line.startsWith(DIRTY)) {
            entry.currentEditor = Editor(entry)
        } else if (secondSpace == -1 && firstSpace == READ.count() && line.startsWith(READ)) {
            // This work was already done by calling lruEntries.get().
        } else {
            throw IOException("Unexpected journal line: $line")
        }
    }

    /**
     * Computes the initial size and collects garbage as a part of opening the
     * cache. Dirty entries are assumed to be inconsistent and will be deleted.
     */
    @Throws(IOException::class)
    private fun processJournal() {
        journalFileTmp.deleteIfExists()

        val iterator = lruEntries.values.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.currentEditor == null) {
                for (i in 0 until valueCount) {
                    size += entry.lengths[i]
                }
            } else {
                entry.currentEditor = null
                for (i in 0 until valueCount) {
                    entry.getCleanFile(i).deleteIfExists()
                    entry.getDirtyFile(i).deleteIfExists()
                }
                iterator.remove()
            }
        }
    }

    /**
     * Creates a new journal that omits redundant information. This replaces the
     * current journal if it exists.
     */
    @Synchronized
    @Throws(IOException::class)
    private fun rebuildJournal() {
        journalWriter?.close()

        journalFileTmp.sink().buffer().use { writer ->
            writer.writeUtf8(MAGIC)
            writer.writeUtf8("\n")
            writer.writeUtf8(VERSION_1)
            writer.writeUtf8("\n")
            writer.writeUtf8(appVersion.toString())
            writer.writeUtf8("\n")
            writer.writeUtf8(valueCount.toString())
            writer.writeUtf8("\n")
            writer.writeUtf8("\n")

            lruEntries.values.forEach { entry ->
                if (entry.currentEditor != null) {
                    writer.writeUtf8(DIRTY)
                    writer.writeUtf8(" ")
                    writer.writeUtf8(entry.key)
                    writer.writeUtf8("\n")
                } else {
                    writer.writeUtf8(CLEAN)
                    writer.writeUtf8(" ")
                    writer.writeUtf8(entry.key)
                    writer.writeUtf8(" ")
                    writer.writeUtf8(entry.getLengthsString())
                    writer.writeUtf8("\n")
                }
            }
        }

        if (journalFile.exists()) {
            journalFile.renameTo(journalFileBackup, true)
        }
        journalFileTmp.renameTo(journalFile, false)
        journalFileBackup.delete()

        journalWriter = journalFile.appendingSink().buffer()
    }

    @Synchronized
    @Throws(IOException::class)
    private fun cleanupJournal() {
        journalWriter ?: return // Closed.
        trimToSize()

        if (journalRebuildRequired()) {
            rebuildJournal()
            redundantOpCount = 0
        }
    }

    /**
     * Returns a snapshot of the entry named `key`, or null if it doesn't
     * exist is not currently readable. If a value is returned, it is moved to
     * the head of the LRU queue.
     */
    @Synchronized
    @Throws(IOException::class)
    operator fun get(key: String): Snapshot? {
        ensureNotClosed()
        validateKey(key)

        val entry = lruEntries[key]?.takeIf { it.readable } ?: return null

        // Open all streams eagerly to guarantee that we see a single published
        // snapshot. If we opened streams lazily then the streams could come
        // from different edits.
        val inputs = arrayOfNulls<Source>(valueCount)
        try {
            for (index in 0 until valueCount) {
                inputs[index] = entry.getCleanFile(index).source()
            }
        } catch (e: FileNotFoundException) {
            // A file must have been deleted manually!
            for (input in inputs) {
                input?.closeQuietly() ?: break
            }
            return null
        }

        redundantOpCount++
        journalWriter?.apply {
            writeUtf8(READ)
            writeUtf8(" ")
            writeUtf8(key)
            writeUtf8("\n")
        }

        if (journalRebuildRequired()) {
            executorService.submit(this::cleanupJournal)
        }

        @Suppress("UNCHECKED_CAST")
        return Snapshot(
            key = key,
            sequenceNumber = entry.sequenceNumber,
            inputs = inputs as Array<Source>,
            lengths = entry.lengths
        )
    }

    @Throws(IOException::class)
    fun edit(key: String): Editor? {
        return edit(key, ANY_SEQUENCE_NUMBER)
    }

    /**
     * Returns an editor for the entry named `key`, or null if another
     * edit is in progress.
     */
    @Synchronized
    @Throws(IOException::class)
    private fun edit(key: String, expectedSequenceNumber: Long): Editor? {
        ensureNotClosed()
        validateKey(key)

        var entry: Entry? = lruEntries[key]
        if (expectedSequenceNumber != ANY_SEQUENCE_NUMBER && (entry == null || entry.sequenceNumber != expectedSequenceNumber)) {
            return null // Snapshot is stale.
        }
        if (entry == null) {
            entry = Entry(key)
            lruEntries[key] = entry
        } else if (entry.currentEditor != null) {
            return null // Another edit is in progress.
        }

        val editor = Editor(entry)
        entry.currentEditor = editor

        // Flush the journal before creating files to prevent file leaks.
        journalWriter?.apply {
            writeUtf8(DIRTY)
            writeUtf8(" ")
            writeUtf8(key)
            writeUtf8("\n")
            flush()
        }
        return editor
    }

    /**
     * Returns the maximum number of bytes that this cache should use to store
     * its data.
     */
    @Synchronized
    fun getMaxSize(): Long {
        return maxSize
    }

    /**
     * Changes the maximum number of bytes the cache can store and queues a job
     * to trim the existing store, if necessary.
     */
    @Synchronized
    fun setMaxSize(maxSize: Long) {
        this.maxSize = maxSize
        executorService.submit(this::cleanupJournal)
    }

    /**
     * Returns the number of bytes currently being used to store the values in
     * this cache. This may be greater than the max size if a background
     * deletion is pending.
     */
    @Synchronized
    fun size(): Long {
        return size
    }

    /** Returns true if this cache has been closed. */
    @Synchronized
    fun isClosed(): Boolean {
        return journalWriter == null
    }

    @Synchronized
    @Throws(IOException::class)
    private fun completeEdit(editor: Editor, success: Boolean) {
        val entry = editor.entry
        if (entry.currentEditor != editor) {
            throw IllegalStateException()
        }

        // If this edit is creating the entry for the first time, every index must have a value.
        if (success && !entry.readable) {
            for (i in 0 until valueCount) {
                if (editor.written?.get(i) != true) {
                    editor.abort()
                    throw IllegalStateException("Newly created entry didn't create value for index $i.")
                }
                if (!entry.getDirtyFile(i).exists()) {
                    editor.abort()
                    return
                }
            }
        }

        for (i in 0 until valueCount) {
            val dirty = entry.getDirtyFile(i)
            if (success) {
                if (dirty.exists()) {
                    val clean = entry.getCleanFile(i)
                    dirty.renameTo(clean)
                    val oldLength = entry.lengths[i]
                    val newLength = clean.length()
                    entry.lengths[i] = newLength
                    size = size - oldLength + newLength
                }
            } else {
                dirty.deleteIfExists()
            }
        }

        redundantOpCount++
        entry.currentEditor = null

        if (entry.readable or success) {
            entry.readable = true
            journalWriter?.apply {
                writeUtf8(CLEAN)
                writeUtf8(" ")
                writeUtf8(entry.key)
                writeUtf8(" ")
                writeUtf8(entry.getLengthsString())
                writeUtf8("\n")
            }
            if (success) {
                entry.sequenceNumber = nextSequenceNumber++
            }
        } else {
            lruEntries.remove(entry.key)
            journalWriter?.apply {
                writeUtf8(REMOVE)
                writeUtf8(" ")
                writeUtf8(entry.key)
                writeUtf8("\n")
            }
        }
        journalWriter?.flush()

        if (size > maxSize || journalRebuildRequired()) {
            executorService.submit(this::cleanupJournal)
        }
    }

    /**
     * Drops the entry for `key` if it exists and can be removed. Entries
     * actively being edited cannot be removed.
     *
     * @return true if an entry was removed.
     */
    @Synchronized
    @Throws(IOException::class)
    fun remove(key: String): Boolean {
        ensureNotClosed()
        validateKey(key)

        val entry = lruEntries[key]
        if (entry == null || entry.currentEditor != null) {
            return false
        }

        for (i in 0 until valueCount) {
            val file = entry.getCleanFile(i)
            if (file.exists() && !file.delete()) {
                throw IOException("Failed to delete $file!")
            }
            size -= entry.lengths[i]
            entry.lengths[i] = 0
        }

        redundantOpCount++
        journalWriter?.apply {
            writeUtf8(REMOVE)
            writeUtf8(" ")
            writeUtf8(key)
            writeUtf8("\n")
        }
        lruEntries.remove(key)

        if (journalRebuildRequired()) {
            executorService.submit(this::cleanupJournal)
        }

        return true
    }

    /** Force buffered operations to the filesystem. */
    @Synchronized
    @Throws(IOException::class)
    fun flush() {
        ensureNotClosed()
        trimToSize()
        journalWriter?.flush()
    }

    /** Closes this cache. Stored values will remain on the filesystem. */
    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        journalWriter ?: return // Already closed.
        lruEntries.values.forEach { entry ->
            entry.currentEditor?.abort()
        }
        trimToSize()
        journalWriter?.close()
        journalWriter = null
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete
     * all files in the cache directory including files that weren't created by
     * the cache.
     */
    @Throws(IOException::class)
    fun delete() {
        close()
        directory.deleteDirectory()
    }

    private fun ensureNotClosed() {
        journalWriter ?: throw IllegalStateException("Cache is closed!")
    }

    @Throws(IOException::class)
    private fun trimToSize() {
        while (size > maxSize) {
            remove(lruEntries.entries.iterator().next().key)
        }
    }

    /**
     * We only rebuild the journal when it will halve the size of the journal
     * and eliminate at least 2000 ops.
     */
    private fun journalRebuildRequired(): Boolean {
        return redundantOpCount >= 2000 && redundantOpCount >= lruEntries.count()
    }

    private fun validateKey(key: String) {
        if (!LEGAL_KEY_PATTERN.matches(key)) {
            throw IllegalArgumentException("Keys must match regex $STRING_KEY_PATTERN: \"$key\"")
        }
    }

    /** A snapshot of the values for an entry. */
    inner class Snapshot(
        private val key: String,
        private val sequenceNumber: Long,
        private val inputs: Array<Source>,
        private val lengths: LongArray
    ) : Closeable {

        /**
         * Returns an editor for this snapshot's entry, or null if either the
         * entry has changed since this snapshot was created or if another edit
         * is in progress.
         */
        @Throws(IOException::class)
        fun edit(): Editor? {
            return this@DiskLRUCache.edit(key, sequenceNumber)
        }

        /** Returns the unbuffered stream with the value for `index`. */
        fun getSource(index: Int): Source {
            return inputs[index]
        }

        /** Returns the string value for `index`. */
        @Throws(IOException::class)
        fun getString(index: Int): String {
            return getSource(index).buffer().readString(Charsets.UTF_8)
        }

        /** Returns the byte length of the value for `index`. */
        fun getLength(index: Int): Long {
            return lengths[index]
        }

        override fun close() {
            inputs.forEach { it.closeQuietly() }
        }
    }

    /** Edits the values for an entry. */
    inner class Editor internal constructor(
        internal val entry: Entry
    ) {

        internal var written = if (entry.readable) null else BooleanArray(valueCount)
        internal var hasErrors = false
        internal var committed = false

        /**
         * Returns the last committed value as a string, or null if no value
         * has been committed.
         */
        @Throws(IOException::class)
        fun getString(index: Int): String? {
            return newSource(index)?.buffer()?.readString(Charsets.UTF_8)
        }

        /** Sets the value at `index` to `value`. */
        @Throws(IOException::class)
        fun setString(index: Int, value: String) {
            newSink(index).buffer().use { it.writeUtf8(value) }
        }

        /**
         * Returns an unbuffered input stream to read the last committed value,
         * or null if no value has been committed.
         */
        @Throws(IOException::class)
        fun newSource(index: Int): Source? {
            validateIndex(index)

            synchronized(this@DiskLRUCache) {
                if (entry.currentEditor != this) {
                    throw IllegalStateException()
                }
                if (!entry.readable) {
                    return null
                }

                return try {
                    entry.getCleanFile(index).source()
                } catch (e: FileNotFoundException) {
                    null
                }
            }
        }

        /**
         * Returns a new unbuffered output stream to write the value at
         * `index`. If the underlying output stream encounters errors
         * when writing to the filesystem, this edit will be aborted when
         * [.commit] is called. The returned output stream does not throw
         * IOExceptions.
         */
        @Throws(IOException::class)
        fun newSink(index: Int): Sink {
            validateIndex(index)

            synchronized(this@DiskLRUCache) {
                if (entry.currentEditor != this) {
                    throw IllegalStateException()
                }
                if (!entry.readable) {
                    written?.set(index, true)
                }

                val dirtyFile = entry.getDirtyFile(index)
                val sink = try {
                    dirtyFile.sink()
                } catch (ignored: FileNotFoundException) {
                    // Attempt to recreate the cache directory.
                    directory.mkdirs()
                    try {
                        dirtyFile.sink()
                    } catch (ignored: FileNotFoundException) {
                        // We are unable to recover. Silently eat the writes.
                        return blackholeSink()
                    }
                }

                return FaultHidingSink(sink)
            }
        }

        /**
         * Commits this edit so it is visible to readers.  This releases the
         * edit lock so another edit may be started on the same key.
         */
        @Throws(IOException::class)
        fun commit() {
            if (hasErrors) {
                completeEdit(this, false)
                remove(entry.key) // The previous entry is stale.
            } else {
                completeEdit(this, true)
            }
            committed = true
        }

        /**
         * Aborts this edit. This releases the edit lock so another edit may be
         * started on the same key.
         */
        @Throws(IOException::class)
        fun abort() {
            completeEdit(this, false)
        }

        fun abortUnlessCommitted() {
            if (!committed) {
                try {
                    abort()
                } catch (ignored: IOException) {
                }
            }
        }

        private fun validateIndex(index: Int) {
            if (index < 0 || index >= valueCount) {
                throw IllegalArgumentException("Expected index $index to be greater than 0 and less than the maximum value count of $valueCount.")
            }
        }

        private inner class FaultHidingSink(private val output: Sink) : Sink by output {

            override fun write(source: Buffer, byteCount: Long) {
                try {
                    output.write(source, byteCount)
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun flush() {
                try {
                    output.flush()
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun close() {
                try {
                    output.close()
                } catch (e: IOException) {
                    hasErrors = true
                }
            }
        }
    }

    internal inner class Entry(val key: String) {

        /** Lengths of this entry's files. */
        val lengths = LongArray(valueCount)

        /** True if this entry has ever been published. */
        var readable = false

        /** The ongoing edit or null if this entry is not being edited. */
        var currentEditor: Editor? = null

        /** The sequence number of the most recently committed edit to this entry. */
        var sequenceNumber = 0L

        @Throws(IOException::class)
        fun getLengthsString(): String {
            return lengths.joinToString(" ")
        }

        /** Set lengths using decimal numbers like "10123". */
        @Throws(IOException::class)
        fun setLengths(strings: List<String>) {
            if (strings.count() != valueCount) {
                throw invalidLengths(strings)
            }

            try {
                strings.forEachIndexed { index, value ->
                    lengths[index] = value.toLong()
                }
            } catch (e: NumberFormatException) {
                throw invalidLengths(strings)
            }
        }

        fun getCleanFile(index: Int): File {
            return File(directory, "$key.$index")
        }

        fun getDirtyFile(index: Int): File {
            return File(directory, "$key.$index.tmp")
        }

        @Throws(IOException::class)
        private fun invalidLengths(strings: List<String>): IOException {
            throw IOException(
                "Unexpected journal line: ${strings.toTypedArray().contentToString()}"
            )
        }
    }

    companion object {
        private const val CLEAN = "CLEAN"
        private const val DIRTY = "DIRTY"
        private const val REMOVE = "REMOVE"
        private const val READ = "READ"

        internal const val JOURNAL_FILE = "journal"
        internal const val JOURNAL_FILE_TEMP = "journal.tmp"
        internal const val JOURNAL_FILE_BACKUP = "journal.bkp"
        internal const val MAGIC = "libcore.io.DiskLruCache"
        internal const val VERSION_1 = "1"
        internal const val ANY_SEQUENCE_NUMBER = -1L
        internal const val STRING_KEY_PATTERN = "[a-z0-9_-]{1,120}"

        private val LEGAL_KEY_PATTERN = STRING_KEY_PATTERN.toRegex()

        /**
         * Opens the cache in `directory`, creating a cache if none exists
         * there.
         *
         * @param directory a writable directory
         * @param valueCount the number of values per cache entry. Must be positive.
         * @param maxSize the maximum number of bytes this cache should use to store
         * @throws IOException if reading or writing the cache directory fails
         */
        @Throws(IOException::class)
        fun open(directory: File, appVersion: Int, valueCount: Int, maxSize: Long): DiskLRUCache {
            if (maxSize <= 0) {
                throw IllegalArgumentException("maxSize <= 0")
            }
            if (valueCount <= 0) {
                throw IllegalArgumentException("valueCount <= 0")
            }

            // If a bkp file exists, use it instead.
            val backupFile = File(directory, JOURNAL_FILE_BACKUP)
            if (backupFile.exists()) {
                val journalFile = File(directory, JOURNAL_FILE)
                // If journal file also exists just delete backup file.
                if (journalFile.exists()) {
                    backupFile.delete()
                } else {
                    backupFile.renameTo(journalFile, false)
                }
            }

            // Prefer to pick up where we left off.
            var cache = DiskLRUCache(directory, appVersion, valueCount, maxSize)
            if (cache.journalFile.exists()) {
                try {
                    cache.readJournal()
                    cache.processJournal()
                    return cache
                } catch (journalIsCorrupt: IOException) {
                    println("DiskLruCache $directory is corrupt: ${journalIsCorrupt.message}, removing.")
                    cache.delete()
                }
            }

            // Create a new empty cache.
            directory.mkdirs()
            cache = DiskLRUCache(directory, appVersion, valueCount, maxSize)
            cache.rebuildJournal()
            return cache
        }
    }
}
