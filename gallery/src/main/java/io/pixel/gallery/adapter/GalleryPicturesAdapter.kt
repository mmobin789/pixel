package io.pixel.gallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import io.pixel.Pixel
import io.pixel.config.PixelOptions
import io.pixel.gallery.R
import io.pixel.gallery.model.GalleryPicture
import kotlinx.android.synthetic.main.multi_gallery_listitem.*
import java.io.File

class GalleryPicturesAdapter(private val list: List<GalleryPicture>) : RecyclerView.Adapter<GVH>() {

    init {
        initSelectedIndexList()
    }

    constructor(list: List<GalleryPicture>, selectionLimit: Int) : this(list) {
        setSelectionLimit(selectionLimit)
    }

    private lateinit var onClick: (GalleryPicture) -> Unit
    private lateinit var afterSelectionCompleted: () -> Unit
    private var isSelectionEnabled = false
    private lateinit var selectedIndexList: ArrayList<Int> // only limited items are selectable.
    private var selectionLimit = 0

    private fun initSelectedIndexList() {
        selectedIndexList = ArrayList(selectionLimit)
    }

    fun setSelectionLimit(selectionLimit: Int) {
        this.selectionLimit = selectionLimit
        removedSelection()
        initSelectedIndexList()
    }

    fun setOnClickListener(onClick: (GalleryPicture) -> Unit) {
        this.onClick = onClick
    }

    fun setAfterSelectionListener(afterSelectionCompleted: () -> Unit) {
        this.afterSelectionCompleted = afterSelectionCompleted
    }

    private fun checkSelection(position: Int) {
        if (isSelectionEnabled) {
            if (getItem(position).isSelected)
                selectedIndexList.add(position)
            else {
                selectedIndexList.remove(position)
                isSelectionEnabled = selectedIndexList.isNotEmpty()
            }
        }
    }

    //    Useful Methods to provide delete feature.

//    fun deletePicture(picture: GalleryPicture) {
//        deletePicture(list.indexOf(picture))
//    }
//
//    fun deletePicture(position: Int) {
//        if (File(getItem(position).path).delete()) {
//            list.removeAt(position)
//            notifyItemRemoved(position)
//        } else {
//            Log.e("GalleryPicturesAdapter", "Deletion Failed")
//        }
//    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): GVH {
        val vh =
            GVH(LayoutInflater.from(p0.context).inflate(R.layout.multi_gallery_listitem, p0, false))
        vh.containerView.setOnClickListener {
            val position = vh.adapterPosition
            val picture = getItem(position)
            if (isSelectionEnabled) {
                handleSelection(position, it.context)
                notifyItemChanged(position)
                checkSelection(position)
                afterSelectionCompleted()
            } else
                onClick(picture)
        }
        vh.containerView.setOnLongClickListener {
            val position = vh.adapterPosition
            isSelectionEnabled = true
            handleSelection(position, it.context)
            notifyItemChanged(position)
            checkSelection(position)
            afterSelectionCompleted()

            isSelectionEnabled
        }
        return vh
    }

    private fun handleSelection(position: Int, context: Context) {

        val picture = getItem(position)

        picture.isSelected = if (picture.isSelected) {
            false
        } else {
            val selectionCriteriaSuccess = getSelectedItems().size < selectionLimit
            if (!selectionCriteriaSuccess)
                selectionLimitReached(context)

            selectionCriteriaSuccess
        }
    }

    fun getSelectionLimit() = selectionLimit

    private fun selectionLimitReached(context: Context) {
        Toast.makeText(
            context,
            "${getSelectedItems().size}/$selectionLimit selection limit reached.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getItem(position: Int) = list[position]

    override fun onBindViewHolder(p0: GVH, p1: Int) {
        val picture = list[p1]
        //  Glide.with(p0.containerView).load(picture.path).into(p0.ivImg)
        Pixel.load(
            file = File(picture.path),
            imageView = p0.ivImg,
            pixelOptions = PixelOptions.Builder()
                .setPlaceholderResource(R.drawable.ic_loading_android)
                // .setRequest(Request.Builder().url(url).tag("Test Image Load at $position").build())
                .setImageFormat(PixelOptions.ImageFormat.JPEG).build()
        )

        if (picture.isSelected) {
            p0.vSelected.visibility = View.VISIBLE
        } else {
            p0.vSelected.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size

    fun getSelectedItems() = selectedIndexList.map {
        list[it]
    }

    fun removedSelection(): Boolean {
        return if (isSelectionEnabled) {
            selectedIndexList.forEach {
                list[it].isSelected = false
            }
            isSelectionEnabled = false
            selectedIndexList.clear()
            notifyDataSetChanged()
            true
        } else false
    }
}
