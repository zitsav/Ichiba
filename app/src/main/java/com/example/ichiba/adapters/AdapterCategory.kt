package com.example.ichiba.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.ichiba.models.ModelCategory
import com.example.ichiba.interfaces.RvListenerCategory
import com.example.ichiba.databinding.RowCategoryBinding

class AdapterCategory(
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory
) : RecyclerView.Adapter<AdapterCategory.HolderCategory>() {

    private lateinit var binding: RowCategoryBinding

    private companion object {
        private const val TAG = "ADAPTER_CATEGORY_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val modelCategory = categoryArrayList[position]
        val icon = modelCategory.icon
        val category = modelCategory.name
        val random = java.util.Random()
        val color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255))

        holder.categoryTv.text = category
        holder.categoryIconTv.setBackgroundColor(color)

        // Handle item click events
        holder.itemView.setOnClickListener {
            rvListenerCategory.onCategoryClick(modelCategory)
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    inner class HolderCategory(itemView: View) : ViewHolder(itemView) {
        var categoryIconTv = binding.categoryIconTv
        var categoryTv = binding.categoryTv
    }
}
