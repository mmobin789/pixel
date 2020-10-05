package io.pixel.sample

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kc.unsplash.models.Collection
import io.pixel.android.Pixel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.row_center_cropped.*

class RVUnsplashAdapter(private val list: MutableList<Collection>) :
    RecyclerView.Adapter<RVUnsplashAdapter.VH>() {

    private lateinit var recyclerView: RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_center_cropped,
                parent,
                false
            )
        )
    }

    fun addAll(list: MutableList<Collection>) {
        this.list.addAll(list)
        notifyItemRangeInserted(itemCount, list.size)
        //   recyclerView.setItemViewCacheSize(itemCount)

    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val collection = list[position]
        val url = collection.coverPhoto.urls.small

        Log.d("Url position: $position", url)

        Pixel.load(
            url,
            imageView = holder.iv
        )

// coil
        //  holder.iv.load(url)

        //    Glide.with(holder.itemView).load(url).into(holder.iv)


    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        this.recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
    }

    class VH(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer
}