package io.pixel.sample

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.pixel.android.Pixel
import io.pixel.android.config.PixelOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.row.*
import org.json.JSONArray

class RVAdapter(private val jsonArray: JSONArray) : RecyclerView.Adapter<RVAdapter.VH>() {

    private lateinit var recyclerView: RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false))

    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = jsonArray.getJSONObject(position).getJSONObject("urls").optString("regular")
        Log.d("Url position: $position", url)
        val pixelOptionsBuilder = PixelOptions.Builder()
            .setPlaceholderResource(R.drawable.ic_loading_android)

        if (position == 3)
            pixelOptionsBuilder.setImageSize(30, 30)

        Pixel.load(
            url,
            imageView = holder.iv,
            pixelOptions = pixelOptionsBuilder.build()
        )


        /*  Glide.with(holder.itemView).applyDefaultRequestOptions(
              RequestOptions.centerCropTransform().placeholder(R.drawable.ic_loading_android)).load(url).into(holder.iv)*/


    }

    override fun getItemCount(): Int {
        return jsonArray.length()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
    }

    class VH(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer
}