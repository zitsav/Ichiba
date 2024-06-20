import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ichiba.R
import com.example.ichiba.databinding.RowImagesPickedBinding
import com.example.ichiba.models.ModelImagePicked

class AdapterImagePicked(
    private val context: Context,
    private val imagesPickedArrayList: ArrayList<ModelImagePicked>
) : RecyclerView.Adapter<AdapterImagePicked.HolderImagePicked>() {

    private var onImageLoadedListener: OnImageLoadedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        val binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagePicked(binding)
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        val model = imagesPickedArrayList[position]
        val imageUri: Uri = model.imageUri ?: Uri.EMPTY

        try {
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.baseline_image_24)
                .error(com.google.android.material.R.drawable.mtrl_ic_error)
                .centerCrop()
                .into(holder.binding.imageTv)
        } catch (e: Exception) {
            Log.e("IMAGES_TAG", "onBindViewHolder", e)
        }

        holder.binding.closeBtn.setOnClickListener {
            imagesPickedArrayList.remove(model)
            notifyDataSetChanged()
        }

        onImageLoadedListener?.onImageLoaded(imageUri, holder.binding.imageTv)
    }

    override fun getItemCount(): Int {
        return imagesPickedArrayList.size
    }

    inner class HolderImagePicked(val binding: RowImagesPickedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
            }
        }
    }

    interface OnImageLoadedListener {
        fun onImageLoaded(imageUri: Uri, imageView: ImageView)
    }

    fun setOnImageLoadedListener(listener: OnImageLoadedListener) {
        this.onImageLoadedListener = listener
    }
}
