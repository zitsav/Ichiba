package com.example.ichiba.activites

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
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
        
        binding.editAdBtn.setOnClickListener {
            if (binding.priceEt.text.toString().isNotEmpty() && binding.titleEt.text.toString().isNotEmpty() && binding.descEt.text.toString().isNotEmpty()) {
                updateProduct(productId)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
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

    private fun updateProduct(field: String) {
        val updateRequest = UpdateProductRequest(
                description = binding.descEt.text.toString(),
                name = binding.titleEt.text.toString(),
                price = binding.priceEt.text.toString().toIntOrNull(),
                isSold = false
        )

        productApi.updateProduct("Bearer $accessToken", productId, updateRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditAdActivity, "$field updated successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@EditAdActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@EditAdActivity, "Failed to update $field", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@EditAdActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
