package com.example.ichiba.activites

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ichiba.R
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.adapters.AdapterImageSlider
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.ActivityAdDetailsBinding
import com.example.ichiba.dataclass.Product
import com.example.ichiba.dataclass.User
import com.example.ichiba.interfaces.ProductApi
import com.example.ichiba.interfaces.AuthApi
import com.example.ichiba.models.ModelImageSlider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding
    private lateinit var productApi: ProductApi
    private lateinit var authApi: AuthApi
    private lateinit var authTokenRepository: AuthTokenRepository
    private lateinit var imageSliderAdapter: AdapterImageSlider
    private val imageList = ArrayList<ModelImageSlider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra("product_id") ?: ""

        val retrofit = RetrofitInstance.getRetrofitInstance()
        productApi = retrofit.create(ProductApi::class.java)
        authApi = retrofit.create(AuthApi::class.java)
        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())

        lifecycleScope.launch {
            val authToken = authTokenRepository.getAuthToken()
            val accessToken = "Bearer " + (authToken?.token ?: "")

            val call = productApi.getProductById(accessToken, productId)

            try {
                val response = withContext(Dispatchers.IO) {
                    call.execute()
                }

                if (response.isSuccessful) {
                    val product = response.body()
                    if (product != null) {
                        displayProductData(product)
                    } else {
                        showToast("Failed to fetch product details")
                    }
                } else {
                    showToast("Failed to fetch product details")
                    Log.e("AdDetailsActivity", "API call failed with code: ${response.code()}")
                }
            } catch (e: IOException) {
                showToast("Network request failed. Please try again.")
                Log.e("AdDetailsActivity", "Network error: ${e.message}")
            }
        }
    }

    private fun displayProductData(product: Product) {
        binding.apply {
            priceTv.text = product.price.toString()
            dateTv.text = product.createdAt.take(10)
            categoryTv.text = product.category.name
            titleTv.text = product.name
            descTv.text = product.description
            if (product.isSold) {
                isSoldTv.text = "SOLD OUT"
                isSoldTv.setTextColor(getColor(R.color.red))
            } else {
                isSoldTv.text = "FOR SALE"
                isSoldTv.setTextColor(getColor(R.color.green))
            }

            sellerNameTv.text = product.owner.name
            enrollNoTv.text = product.owner.enrollmentNumber
            if (product.owner.phoneNumber?.isNotEmpty() == true) {
                contactNoTv.text = product.owner.phoneNumber
            }

            if (product.owner.profilePicture != null) {
                Glide.with(this@AdDetailsActivity)
                    .load(product.owner.profilePicture)
                    .into(sellerProfileTv)
            }

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
}