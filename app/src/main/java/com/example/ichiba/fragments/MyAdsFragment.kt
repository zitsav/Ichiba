package com.example.ichiba.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.activites.AdDetailsActivity
import com.example.ichiba.activites.EditAdActivity
import com.example.ichiba.activites.UserAdDetailsActivity
import com.example.ichiba.adapters.AdapterAd
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.dataclass.Product
import com.example.ichiba.databinding.FragmentMyAdsBinding
import com.example.ichiba.dataclass.ProductResponse
import com.example.ichiba.interfaces.ProductApi
import com.example.ichiba.models.ModelAd
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

class MyAdsFragment : Fragment(), AdapterAd.OnAdClickListener {

    private lateinit var binding: FragmentMyAdsBinding
    private lateinit var mContext: Context
    private lateinit var productApi: ProductApi
    private lateinit var adapterAd: AdapterAd
    private lateinit var authTokenRepository: AuthTokenRepository
    private var adArrayList: ArrayList<ModelAd> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = RetrofitInstance.getRetrofitInstance()
        productApi = retrofit.create(ProductApi::class.java)

        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(requireContext()).authTokenDao())

        adapterAd = AdapterAd(mContext, adArrayList)
        binding.adsRv.layoutManager = LinearLayoutManager(mContext)
        binding.adsRv.adapter = adapterAd

        adapterAd.setOnAdClickListener(this@MyAdsFragment)

        fetchMyAds()
    }

    override fun onAdClick(productId: Int) {
        val intent = Intent(mContext, UserAdDetailsActivity::class.java)
        intent.putExtra("product_id", productId.toString())
        startActivity(intent)
    }

    private fun fetchMyAds() {
        com.example.ichiba.utils.AdLoader.showDialog(mContext, isCancelable = true)

        lifecycleScope.launch {
            val authToken = authTokenRepository.getAuthToken()
            val accessToken = "Bearer " + (authToken?.token ?: "")

            val call = productApi.getAllProductsOfUser(accessToken)
            call.enqueue(object : Callback<ProductResponse> {
                @SuppressLint("NotifyDataSetChanged", "NewApi", "SimpleDateFormat")
                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    com.example.ichiba.utils.AdLoader.hideDialog()
                    if (response.isSuccessful) {
                        val productResponse = response.body()
                        productResponse?.let { productResp ->
                            adArrayList.clear()
                            for (product in productResp.products) {
                                val ad = ModelAd().apply {
                                    id = product.productId
                                    uid = product.ownerId
                                    category = product.category.name
                                    price = product.price.toString()
                                    title = product.name
                                    description = product.description
                                    isSold = product.isSold
                                    timestamp = product.createdAt.let {
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                                        dateFormat.parse(it)?.time ?: 0L
                                    }
                                    imageList = ArrayList(product.images)
                                }
                                adArrayList.add(ad)
                            }
                            adapterAd.notifyDataSetChanged()
                        }
                    } else {
                        val errorMessage = response.message()
                        Log.e("Fetch", "Failed to fetch products: $errorMessage")
                        Toast.makeText(mContext, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    com.example.ichiba.utils.AdLoader.hideDialog()
                    Log.e("Fetch", "Failed to fetch products: ${t.message}")
                    Toast.makeText(
                        mContext,
                        "Failed to fetch products: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}