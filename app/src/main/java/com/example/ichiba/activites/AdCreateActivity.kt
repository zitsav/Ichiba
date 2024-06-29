package com.example.ichiba.activites

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ichiba.R
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.ActivityAdCreateBinding
import com.example.ichiba.dataclass.Product
import com.example.ichiba.dataclass.ProductRequest
import com.example.ichiba.interfaces.ProductApi
import com.example.ichiba.models.ModelImagePicked
import com.example.ichiba.utils.Utils
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AdCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdCreateBinding
    private lateinit var progressDialog: ProgressDialog

    private val categories = listOf(
        "Choose",
        "ELECTRONICS",
        "COOLER",
        "BOOKS",
        "MATTRESS",
        "OTHERS"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.categorySpinner.adapter = adapter

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please Wait")
            setCanceledOnTouchOutside(false)
        }

        binding.postAdBtn.setOnClickListener {
            postAd()
        }
    }

    private fun postAd() {
        val category = binding.categorySpinner.selectedItem.toString()
        val price = binding.priceEt.text.toString()
        val title = binding.titleEt.text.toString()
        val description = binding.descEt.text.toString()

        if (category != "Choose a category" && price.isNotEmpty() && title.isNotEmpty() && description.isNotEmpty()) {
            val intent = Intent(this, UploadAdImagesActivity::class.java)
            intent.putExtra("category", category)
            intent.putExtra("price", price)
            intent.putExtra("title", title)
            intent.putExtra("description", description)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
        }
    }
}