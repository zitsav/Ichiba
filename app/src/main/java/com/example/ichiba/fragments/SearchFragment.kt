package com.example.ichiba.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ichiba.R
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.activites.AdDetailsActivity
import com.example.ichiba.adapters.AdapterAd
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.FragmentHomeBinding
import com.example.ichiba.databinding.FragmentSearchBinding
import com.example.ichiba.dataclass.ProductResponse
import com.example.ichiba.dataclass.SearchRequest
import com.example.ichiba.interfaces.ProductApi
import com.example.ichiba.models.ModelAd
import com.example.ichiba.utils.SortDialogFragment
import com.example.ichiba.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var mContext: Context
    private lateinit var productApi: ProductApi
    private lateinit var adapterAd: AdapterAd
    private lateinit var authTokenRepository: AuthTokenRepository
    private var adArrayList: ArrayList<ModelAd> = ArrayList()
    private var sortOption: String = "Date: Newest First"
    private var selectedCategory: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
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

        binding.filterButton.setOnClickListener {
            showSortOptionsBottomSheet()
        }

        binding.searchEt.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun showSortOptionsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mContext)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val categoryRadioGroup = bottomSheetView.findViewById<RadioGroup>(R.id.categoryOptionsRg)
        val sortRadioGroup = bottomSheetView.findViewById<RadioGroup>(R.id.sortOptionsRg)

        when (selectedCategory) {
            "ELECTRONICS" -> categoryRadioGroup.check(R.id.electronicsBtn)
            "COOLER" -> categoryRadioGroup.check(R.id.coolerBtn)
            "BOOKS" -> categoryRadioGroup.check(R.id.booksBtn)
            "MATTRESS" -> categoryRadioGroup.check(R.id.mattressBtn)
            "OTHERS" -> categoryRadioGroup.check(R.id.othersBtn)
            else -> categoryRadioGroup.check(R.id.anyCategory)
        }

        when (sortOption) {
            "Price: Low to High" -> sortRadioGroup.check(R.id.sortPriceLowToHigh)
            "Price: High to Low" -> sortRadioGroup.check(R.id.sortPriceHighToLow)
            "Date: Newest First" -> sortRadioGroup.check(R.id.sortDateNewestFirst)
            "Date: Oldest First" -> sortRadioGroup.check(R.id.sortDateOldestFirst)
        }

        categoryRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.electronicsBtn -> "ELECTRONICS"
                R.id.coolerBtn -> "COOLER"
                R.id.booksBtn -> "BOOKS"
                R.id.othersBtn -> "OTHERS"
                R.id.mattressBtn -> "MATTRESS"
                else -> ""
            }
        }

        sortRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            sortOption = when (checkedId) {
                R.id.sortPriceLowToHigh -> "Price: Low to High"
                R.id.sortPriceHighToLow -> "Price: High to Low"
                R.id.sortDateNewestFirst -> "Date: Newest First"
                R.id.sortDateOldestFirst -> "Date: Oldest First"
                else -> sortOption
            }
        }

        bottomSheetDialog.show()
    }

    private fun performSearch() {
        val searchQuery = binding.searchEt.text.toString().trim()
        val order = when (sortOption) {
            "Price: Low to High" -> "asc"
            "Price: High to Low" -> "desc"
            "Date: Newest First" -> "desc"
            "Date: Oldest First" -> "asc"
            else -> ""
        }
        val sortBy = when (sortOption) {
            "Price: Low to High", "Price: High to Low" -> "price"
            "Date: Newest First", "Date: Oldest First" -> "time"
            else -> ""
        }
        val searchRequest = SearchRequest(
            category = selectedCategory,
            order = order,
            search = searchQuery,
            sortBy = sortBy
        )

        lifecycleScope.launch {
            val authToken = authTokenRepository.getAuthToken()
            val accessToken = "Bearer " + (authToken?.token ?: "")
            fetchProducts(accessToken, searchRequest)
        }
    }

    private fun fetchProducts(accessToken: String, searchRequest: SearchRequest? = null) {
        com.example.ichiba.utils.AdLoader.showDialog(mContext, isCancelable = true)
        val call = if (searchRequest == null) {
            productApi.getAllProducts(accessToken)
        } else {
            productApi.searchProducts(accessToken, searchRequest)
        }
        call.enqueue(object : Callback<ProductResponse> {
            @SuppressLint("NotifyDataSetChanged", "NewApi")
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
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
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
                Toast.makeText(mContext, "Failed to fetch products: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}