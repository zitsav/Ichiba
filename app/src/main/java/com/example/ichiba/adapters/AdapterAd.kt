package com.example.ichiba.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ichiba.R
import com.example.ichiba.databinding.RowAdBinding
import com.example.ichiba.models.ModelAd
import com.example.ichiba.utils.Utils

class AdapterAd(private val context: Context, private val adArrayList: List<ModelAd>) :
    RecyclerView.Adapter<AdapterAd.HolderAd>() {

    interface OnAdClickListener {
        fun onAdClick(productId: Int)
    }

    private var onAdClickListener: OnAdClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {
        val binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAd(binding)
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        val modelAd = adArrayList[position]

        loadAdFirstImage(modelAd, holder)

        holder.bind(modelAd)

        holder.itemView.setOnClickListener {
            val productId = modelAd.id
            if (productId != -1) {
                onAdClickListener?.onAdClick(productId)
            }
        }
    }

    fun setOnAdClickListener(listener: OnAdClickListener?) {
        this.onAdClickListener = listener
    }

    private fun loadAdFirstImage(modelAd: ModelAd, holder: HolderAd) {
        val imageList = modelAd.imageList

        if (imageList.isNotEmpty()) {
            val firstImageUrl = imageList[0]
            Glide.with(context)
                .load(firstImageUrl)
                .placeholder(R.drawable.ic_person)
                .into(holder.binding.imageTv)
        } else {
            holder.binding.imageTv.setImageResource(R.drawable.ic_person)
        }
    }

    override fun getItemCount(): Int = adArrayList.size

    inner class HolderAd(val binding: RowAdBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(modelAd: ModelAd) {
            binding.apply {
                titleTv.text = modelAd.title
                descriptionTv.text = modelAd.description
                priceTv.text = modelAd.price.toString()
                dateTv.text = Utils.formatTimestampDate(modelAd.timestamp)
            }
        }
    }
}