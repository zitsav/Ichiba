package com.example.ichiba.activites

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ichiba.R
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.adapters.AdapterImageSlider
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.ActivityEditAdBinding
import com.example.ichiba.dataclass.Product
import com.example.ichiba.dataclass.UpdateProductRequest
import com.example.ichiba.interfaces.ProductApi
import com.example.ichiba.models.ModelImageSlider
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditAdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditAdBinding
    private lateinit var productApi: ProductApi
    private lateinit var authTokenRepository: AuthTokenRepository
    private lateinit var accessToken: String
    private lateinit var productId: String
    private lateinit var imageSliderAdapter: AdapterImageSlider
    private val imageList = ArrayList<ModelImageSlider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())

        val retrofit = RetrofitInstance.getRetrofitInstance()
        productApi = retrofit.create(ProductApi::class.java)

        productId = intent.getStringExtra("product_id") ?: return

        lifecycleScope.launch {
            val account = authTokenRepository.getAuthToken()

            account?.let {
                accessToken = it.token!!
                fetchProductDetails()
            }
        }

        binding.priceUpdateBtn.setOnClickListener { showConfirmationDialog("price") }
        binding.titleUpdateBtn.setOnClickListener { showConfirmationDialog("title") }
        binding.descUpdateBtn.setOnClickListener { showConfirmationDialog("description") }
        binding.toggleButton.setOnClickListener { showToggleConfirmationDialog(binding.toggleButton.isChecked) }
        binding.toolBarDeleteBtn.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun fetchProductDetails() {
        productApi.getProductById("Bearer $accessToken", productId)
            .enqueue(object : Callback<Product> {
                override fun onResponse(call: Call<Product>, response: Response<Product>) {
                    if (response.isSuccessful) {
                        response.body()?.let { product ->
                            binding.priceEt.setText(product.price.toString())
                            binding.titleEt.setText(product.name)
                            binding.descEt.setText(product.description)
                            binding.toggleButton.isChecked = product.isSold
                            binding.categoryTv.text = product.category.name

                            if (product.images.isNotEmpty()) {
                                imageList.clear()
                                product.images.forEach { imageUrl ->
                                    imageList.add(ModelImageSlider("", imageUrl))
                                }
                                setupImageSlider()
                            }
                        }
                    } else {
                        Toast.makeText(this@EditAdActivity, "Failed to fetch product details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Product>, t: Throwable) {
                    Toast.makeText(this@EditAdActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupImageSlider() {
        imageSliderAdapter = AdapterImageSlider(imageList)
        binding.imageSliderVp.adapter = imageSliderAdapter
    }

    private fun showConfirmationDialog(field: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to update the $field?")
            .setPositiveButton("Yes") { _, _ -> updateProduct(field) }
            .setNegativeButton("No", null)
        builder.create().show()
    }

    private fun updateProduct(field: String) {
        val updateRequest = when (field) {
            "price" -> UpdateProductRequest(
                description = null,
                name = null,
                price = binding.priceEt.text.toString().toIntOrNull(),
                isSold = null
            )
            "title" -> UpdateProductRequest(
                description = null,
                name = binding.titleEt.text.toString(),
                price = null,
                isSold = null
            )
            "description" -> UpdateProductRequest(
                description = binding.descEt.text.toString(),
                name = null,
                price = null,
                isSold = null
            )
            else -> return
        }

        productApi.updateProduct("Bearer $accessToken", productId, updateRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditAdActivity, "$field updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditAdActivity, "Failed to update $field", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@EditAdActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showToggleConfirmationDialog(isChecked: Boolean) {
        val status = if (isChecked) "sold" else "for sale"
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to mark this product as $status?")
            .setPositiveButton("Yes") { _, _ -> updateIsSold(isChecked) }
            .setNegativeButton("No") { _, _ -> binding.toggleButton.isChecked = !isChecked }
        builder.create().show()
    }

    private fun updateIsSold(isSold: Boolean) {
        val updateRequest = UpdateProductRequest(
            description = null,
            name = null,
            price = null,
            isSold = isSold
        )

        productApi.updateProduct("Bearer $accessToken", productId, updateRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditAdActivity, "Product status updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditAdActivity, "Failed to update product status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@EditAdActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Yes") { _, _ -> deleteProduct() }
            .setNegativeButton("No", null)
        builder.create().show()
    }

    private fun deleteProduct() {
        productApi.deleteProduct("Bearer $accessToken", productId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditAdActivity, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditAdActivity, "Failed to delete product", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@EditAdActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
