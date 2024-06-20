package com.example.ichiba.activites

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ichiba.R
import com.example.ichiba.utils.Utils
import com.example.ichiba.databinding.ActivityProfileEditBinding
import com.example.ichiba.dataclass.User
import com.example.ichiba.interfaces.AuthApi
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthToken
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.dataclass.UpdatePhone
import com.example.ichiba.dataclass.UpdateProfile
import com.example.ichiba.dataclass.UpdateUPI
import com.example.ichiba.dataclass.UpdateUserRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale
import java.util.UUID

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    private var imageUri: Uri? = null
    private lateinit var authApi: AuthApi
    private lateinit var authTokenRepository: AuthTokenRepository
    private lateinit var accessToken: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditBinding.inflate(layoutInflater)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        window.statusBarColor = Color.TRANSPARENT
        setContentView(binding.root)

        supportActionBar?.hide()

        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        authApi = RetrofitInstance.getRetrofitInstance().create(AuthApi::class.java)

        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())

        lifecycleScope.launch {
            val account = authTokenRepository.getAuthToken()

            account?.let {
                accessToken = it.token?:""
                userId = it.userId.toString()
                binding.phoneEt.setText(it.phoneNumber)
                binding.upiEt.setText(it.upiId)

                it.profilePicture?.let { profilePictureUrl ->
                    Glide.with(this@ProfileEditActivity)
                        .load(profilePictureUrl)
                        .placeholder(R.drawable.i2)
                        .into(binding.shapeableImageView)
                }
            }
        }

        binding.changeImage.setOnClickListener { chooseImage() }
        binding.changePhoneNumber.setOnClickListener { updatePhoneNumber() }
        binding.changeUpi.setOnClickListener { updateUPI() }
    }

    private fun initViews() {
        binding.changeImage.setOnClickListener { chooseImage() }
        binding.changePhoneNumber.setOnClickListener { updatePhoneNumber() }
        binding.changeUpi.setOnClickListener { updateUPI() }
    }

    private fun setupActionBar() {
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let { uploadImageToFirestore(it) }
        }
    }

    private fun uploadImageToFirestore(uri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference.child("profilePictures/${UUID.randomUUID()}")
        storageReference.putFile(uri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val profilePictureUrl = uri.toString()
                    updateProfilePicture(profilePictureUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfilePicture(profilePictureUrl: String) {
        val updateProfile = UpdateProfile(profilePictureUrl)
        authApi.updateProfilePicture("Bearer $accessToken", userId, updateProfile)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        lifecycleScope.launch {
                            updateProfileLocal(profilePictureUrl)
                        }
                        Toast.makeText(this@ProfileEditActivity, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileEditActivity, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ProfileEditActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private suspend fun updateProfileLocal(profilePictureUrl: String) {
        authTokenRepository.getAuthToken()?.let { authToken ->
            val updatedAuthToken = authToken.copy(profilePicture = profilePictureUrl)
            authTokenRepository.updateAuthToken(updatedAuthToken)
        }
    }

    private fun updatePhoneNumber() {
        val newPhoneNumber = binding.phoneEt.text.toString()
        val updatePhone = UpdatePhone(newPhoneNumber)
        authApi.updatePhoneNumber("Bearer $accessToken", userId, updatePhone)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        lifecycleScope.launch {
                            updatePhoneLocal(newPhoneNumber)
                        }
                        Toast.makeText(this@ProfileEditActivity, "Phone number updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileEditActivity, "Failed to update phone number", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ProfileEditActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private suspend fun updatePhoneLocal(newPhoneNumber: String) {
        authTokenRepository.getAuthToken()?.let { authToken ->
            val updatedAuthToken = authToken.copy(phoneNumber = newPhoneNumber)
            authTokenRepository.updateAuthToken(updatedAuthToken)
        }
    }

    private fun updateUPI() {
        val newUPI = binding.upiEt.text.toString()
        val updateUPI = UpdateUPI(newUPI)
        authApi.updateUPI("Bearer $accessToken", userId, updateUPI)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        lifecycleScope.launch {
                            updateUPILocal(newUPI)
                        }
                        Toast.makeText(this@ProfileEditActivity, "UPI ID updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileEditActivity, "Failed to update UPI ID", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ProfileEditActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private suspend fun updateUPILocal(newUPI: String) {
        authTokenRepository.getAuthToken()?.let { authToken ->
            val updatedAuthToken = authToken.copy(upiId = newUPI)
            authTokenRepository.updateAuthToken(updatedAuthToken)
        }
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 1
    }
}