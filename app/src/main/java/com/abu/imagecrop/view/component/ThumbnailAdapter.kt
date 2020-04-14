package com.abu.imagecrop.view.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.abu.imagecrop.R
import com.abu.imagecrop.extension.byteArrayToBitmap
import com.abu.imagecrop.model.db.ImageEntity

class ThumbnailAdapter(val thumbnailList: MutableLiveData<List<ImageEntity>>) :
    RecyclerView.Adapter<ThumbnailAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.thumbnail_circle, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return thumbnailList.value?.size ?: 0
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        thumbnailList.value?.get(position)?.let { holder.bind(it) }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var ivThumbnail: ImageView? = null

        init {
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail)
        }

        fun bind(imageEntity: ImageEntity) {
            if (imageEntity.thumbnail.isNotEmpty()) {
                ivThumbnail?.setImageBitmap(imageEntity.thumbnail.byteArrayToBitmap())
            }
        }
    }
}