package io.pixel.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.pixel.android.Pixel
import io.pixel.android.config.PixelOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.row_horizontal.*
import org.json.JSONArray

class RVAdapterHorizontal(private val jsonArray: JSONArray) :
    RecyclerView.Adapter<RVAdapterHorizontal.VH>() {

    private lateinit var recyclerView: RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_horizontal,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = jsonArray.getJSONObject(position).getJSONObject("urls").optString("small")
        //  Log.d("Url position: $position", url)

        Pixel.load(
            url,
            imageView = holder.iv,
            pixelOptions = PixelOptions.Builder()
                .setPlaceholderResource(R.drawable.ic_loading_android).build()
        )


        /*  Glide.with(holder.itemView).applyDefaultRequestOptions(
              RequestOptions.diskCacheStrategyOf(
                  DiskCacheStrategy.NONE
              ).centerCrop()
          ).load(url).into(holder.iv)*/


    }

    override fun getItemCount(): Int {
        return jsonArray.length()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView

        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
    }

    class VH(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer
}