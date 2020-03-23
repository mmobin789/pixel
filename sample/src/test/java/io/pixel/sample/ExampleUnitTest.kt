//package workspace.android.sample
//
//import org.junit.Assert.assertArrayEquals
//import org.junit.Assert.assertEquals
//import org.junit.Test
//
///**
// * Example local unit test, which will execute on the development machine (host).
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//class ExampleUnitTest {
//    @Test
//    fun mergeSort() {
//
//        val d = 4.6
//        assertEquals(4, d.toInt())
//    }
//
//    @Test
//    fun insertionSort() {
//        val data = arrayOf(5, 4, 2, 10, 12, 1, 0, 5, 6)
//
//        data.forEachIndexed { index, item ->
//            if (index == 0)
//                return@forEachIndexed
//
//            var item1Index = index - 1
//
//            while (item1Index >= 0 && data[item1Index] > item) // if previous item is bigger than current.
//            {
//                data[item1Index + 1] = data[item1Index]     // replace current item with previous.
//                item1Index--                                 // decrement previous index by 1
//
//
//            }
//            data[item1Index + 1] = item          // keep current item at position by default.
//
//        }
//        println(data.contentToString())
//
//        assertArrayEquals(arrayOf(0, 1, 2, 3, 6, 7), data)
//    }
//
//    @Test
//    fun insertionSortDescending() {
//        val data = arrayOf(1, 2, 3, 4)
//
//        data.forEachIndexed { index, item ->
//            if (index == 0)
//                return@forEachIndexed
//
//            var item1Index = index - 1
//
//            while (item1Index >= 0 && data[item1Index] < item) // if previous item is smaller than current.
//            {
//                data[item1Index + 1] = data[item1Index]     // replace current item with previous.
//                item1Index--                                 // decrement previous index by 1
//
//
//            }
//            data[item1Index + 1] =
//                item          // keep current item at current position by default.
//
//        }
//        println(data.contentToString())
//
//        assertArrayEquals(arrayOf(0, 1, 2, 3, 6, 7), data)
//    }
//}
