package com.example.ichiba.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.ichiba.databinding.RowImageSliderBinding
import com.example.ichiba.models.ModelImageSlider
import com.google.android.material.imageview.ShapeableImageView

class AdapterImageSlider(private val imageList: List<ModelImageSlider>) : PagerAdapter() {

    override fun getCount(): Int = imageList.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context)
        Glide.with(container)
            .load(imageList[position].imageUrl)
            .into(imageView)
        container.addView(imageView)
        return imageView
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}