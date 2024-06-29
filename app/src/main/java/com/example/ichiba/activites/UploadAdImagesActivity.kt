package com.example.ichiba.activites

import AdapterImagePicked
import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ichiba.databinding.ActivityUploadAdImagesBinding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ichiba.R
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.dataclass.ProductRequest
import com.example.ichiba.interfaces.ProductApi
import com.example.ichiba.models.ModelImagePicked
import com.example.ichiba.utils.Utils
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.UUID

class UploadAdImagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadAdImagesBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var productApi: ProductApi
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private lateinit var adapterImagePicked: AdapterImagePicked
    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>
    private lateinit var authTokenRepository: AuthTokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadAdImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please Wait")
            setCanceledOnTouchOutside(false)
        }

        val retrofit = RetrofitInstance.getRetrofitInstance()
        productApi = retrofit.create(ProductApi::class.java)

        initializeFirebaseStorage()
        setupImageRecyclerView()

        binding.addImage.setOnClickListener {
            showImagePickOptions()
        }

        binding.postAdBtn.setOnClickListener {
            lifecycleScope.launch {
                postAd()
            }
        }
    }

    private fun initializeFirebaseStorage() {
        FirebaseStorage.getInstance().apply {
            storageReference = reference
        }
    }

    private fun setupImageRecyclerView() {
        imagePickedArrayList = ArrayList()
        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList)

        binding.imagesRv.apply {
            layoutManager =
                LinearLayoutManager(this@UploadAdImagesActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterImagePicked
        }
    }

    private fun showImagePickOptions() {
        val popupMenu = PopupMenu(this, binding.addImage)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
                        requestCameraPermission.launch(cameraPermissions)
                    } else {
                        val cameraPermissions = arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        requestCameraPermission.launch(cameraPermissions)
                    }
                }

                2 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery()
                    } else {
                        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                        requestStoragePermission.launch(storagePermission)
                    }
                }
            }
            true
        }
    }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImageGallery()
            } else {
                Utils.toast(this, "Storage Permission Denied")
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val areAllGranted = result.values.all { it }
            if (areAllGranted) {
                pickImageCamera()
            } else {
                Utils.toast(this, "Camera or Storage Permissions Both Denied")
            }
        }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
            put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                Log.d("AdCreateActivity", "Gallery Image URI: $imageUri")
                imageUri?.let {
                    val timestamp = Utils.getTimestamp().toString()
                    val modelImagePicked = ModelImagePicked(timestamp, it, null, false)
                    imagePickedArrayList.add(modelImagePicked)
                    adapterImagePicked.notifyDataSetChanged()
                }
            } else {
                Utils.toast(this, "Cancelled..!")
            }
        }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("AdCreateActivity", "Camera Image URI: $imageUri")
                imageUri?.let {
                    val timestamp = Utils.getTimestamp().toString()
                    val modelImagePicked = ModelImagePicked(timestamp, it, null, false)
                    imagePickedArrayList.add(modelImagePicked)
                    adapterImagePicked.notifyDataSetChanged()
                }
            } else {
                Utils.toast(this, "Cancelled!")
            }
        }

    private suspend fun createProduct(createProductRequest: ProductRequest) {
        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())
        val authToken = authTokenRepository.getAuthToken()
        val accessToken = "Bearer " + (authToken?.token ?: "")

        try {
            val response = withContext(Dispatchers.IO) {
                productApi.createProduct(accessToken, createProductRequest).execute()
            }
            progressDialog.dismiss()
            if (response.isSuccessful) {
                val intent = Intent(this, AdPostedActivity::class.java)
                startActivity(intent)
            } else {
                val errorMessage = response.errorBody()?.string()
                Toast.makeText(this, "Product creation failed: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Product creation failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun postAd() {
        val category = intent.getStringExtra("category") ?: ""
        val price = intent.getStringExtra("price") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        if (imagePickedArrayList.isNotEmpty()) {
            progressDialog.setMessage("Uploading Image...")
            progressDialog.show()

            val uploadedImageUrls = ArrayList<String>()
            val fileNames = imagePickedArrayList.map { UUID.randomUUID().toString() }

            imagePickedArrayList.forEachIndexed { index, modelImagePicked ->
                val imageUri = modelImagePicked.imageUri
                val fileName = fileNames[index]
                val imageRef = storageReference.child("images/$fileName.jpg")

                imageUri?.let {
                    lifecycleScope.launch {
                        val compressedUri = compressImage(it)
                        val uploadTask = imageRef.putFile(compressedUri)

                        uploadTask.addOnSuccessListener { taskSnapshot ->
                            progressDialog.setMessage("Uploading Image... ${index + 1}/${imagePickedArrayList.size}")

                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                                uploadedImageUrls.add(downloadUri.toString())

                                if (uploadedImageUrls.size == imagePickedArrayList.size) {
                                    val createProductRequest = ProductRequest(
                                        name = title,
                                        description = description,
                                        category = category,
                                        price = price.toInt(),
                                        images = uploadedImageUrls
                                    )

                                    lifecycleScope.launch {
                                        createProduct(createProductRequest)
                                    }
                                }
                            }
                        }.addOnFailureListener { exception ->
                            progressDialog.dismiss()
                            Toast.makeText(this@UploadAdImagesActivity, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }.addOnProgressListener { taskSnapshot ->
                            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                            progressDialog.setMessage("Uploading Image... $progress%")
                        }
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please upload images first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressImage(imageUri: Uri): Uri {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(imageUri, filePathColumn, null, null, null)
        cursor?.moveToFirst()

        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val picturePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()

        val bitmap = BitmapFactory.decodeFile(picturePath)
        val compressedFile = File(cacheDir, "compressed_image_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(compressedFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.close()

        return Uri.fromFile(compressedFile)
    }
}