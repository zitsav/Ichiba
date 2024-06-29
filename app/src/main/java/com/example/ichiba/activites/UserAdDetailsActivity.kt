package com.example.ichiba.activites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ichiba.R
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.adapters.AdapterImageSlider
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.ActivityAdDetailsBinding
import com.example.ichiba.databinding.ActivityUserAdDetailsBinding
import com.example.ichiba.dataclass.Product
import com.example.ichiba.dataclass.UpdateProductRequest
import com.example.ichiba.interfaces.AuthApi
import com.example.ichiba.interfaces.ProductApi
import com.example.ichiba.models.ModelImageSlider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class UserAdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAdDetailsBinding
    private lateinit var productApi: ProductApi
    private lateinit var authTokenRepository: AuthTokenRepository
    private lateinit var accessToken: String
    private lateinit var productId: String
    private lateinit var imageSliderAdapter: AdapterImageSlider
    private val imageList = ArrayList<ModelImageSlider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAdDetailsBinding.inflate(layoutInflater)
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

        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditAdActivity::class.java)
            intent.putExtra("product_id", productId)
            startActivity(intent)
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.markAsSoldBtn.setOnClickListener {
            updateIsSold(true)
        }
    }

    private fun fetchProductDetails() {
        productApi.getProductById("Bearer $accessToken", productId)
            .enqueue(object : Callback<Product> {
                override fun onResponse(call: Call<Product>, response: Response<Product>) {
                    if (response.isSuccessful) {
                        response.body()?.let { product ->
                            displayProductData(product)
                        }
                    } else {
                        showToast("Failed to fetch product details")
                    }
                }

                override fun onFailure(call: Call<Product>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
    }

    private fun displayProductData(product: Product) {
        binding.apply {
            priceTv.text = product.price.toString()
            dateTv.text = product.createdAt.take(10)
            categoryTv.text = product.category.name
            titleTv.text = product.name
            descTv.text = product.description

            if (product.images.isNotEmpty()) {
                imageList.clear()
                product.images.forEach { imageUrl ->
                    imageList.add(ModelImageSlider("", imageUrl))
                }
                setupImageSlider()
            }
        }
    }

    private fun setupImageSlider() {
        imageSliderAdapter = AdapterImageSlider(imageList)
        binding.imageSliderVp.adapter = imageSliderAdapter
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                        showToast("Product deleted successfully")
                        val intent = Intent(this@UserAdDetailsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showToast("Failed to delete product")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
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
                        showToast("Product status updated")
                        val intent = Intent(this@UserAdDetailsActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        showToast("Failed to update product status")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
    }
}