package com.example.ichiba.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.activites.EditPhoneActivity
import com.example.ichiba.activites.EditUpiActivity
import com.example.ichiba.activites.LoginActivity
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.FragmentAccountBinding
import com.example.ichiba.dataclass.UpdateProfile
import com.example.ichiba.interfaces.AuthApi
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID


class AccountFragment : Fragment() {
    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1
    }

    private lateinit var binding: FragmentAccountBinding
    private lateinit var authTokenRepository: AuthTokenRepository
    private var imageUri: Uri? = null
    private lateinit var authApi: AuthApi
    private lateinit var accessToken: String
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(requireContext()).authTokenDao())
        authApi = RetrofitInstance.getRetrofitInstance().create(AuthApi::class.java)

        progressDialog = ProgressDialog(context).apply {
            setMessage("Updating profile picture...")
            setCancelable(false)
        }

        lifecycleScope.launch {
            val account = authTokenRepository.getAuthToken()
            account?.let {
                accessToken = it.token!!
                binding.nameTv.text = it.name
                binding.enrollNoTv.text = it.enrollmentNumber
                binding.batchTv.text = it.batch
                binding.programTv.text = it.program
                binding.contactNoTv.text = it.phoneNumber ?: "Not Set"
                binding.upiIdTv.text = it.upiId ?: "Not Set"
                binding.fullNameTv.text = it.name
                binding.emailTv.text = buildString {
                    append(it.enrollmentNumber?.toLowerCase(Locale.getDefault()))
                    append("@iiita.ac.in")
                }

                it.profilePicture?.let { profilePictureUrl ->
                    Glide.with(requireContext())
                        .load(profilePictureUrl)
                        .into(binding.profileIv)
                }
            }
        }

        binding.imagePicker.setOnClickListener {
            chooseImage()
        }

        binding.editUPIButton.setOnClickListener {
            val intent = Intent(context, EditUpiActivity::class.java)
            startActivity(intent)
        }

        binding.editPhoneButton.setOnClickListener {
            val intent = Intent(context, EditPhoneActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                logout()
            }
        }
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
            imageUri?.let {
                lifecycleScope.launch {
                    compressAndUploadImage(it)
                }
            }
        }
    }

    private suspend fun compressAndUploadImage(uri: Uri) {
        progressDialog.show()
        val compressedImageFile = compressImage(uri)
        val compressedUri = Uri.fromFile(compressedImageFile)

        uploadImageToFirestore(compressedUri)
    }

    private fun compressImage(imageUri: Uri): File {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context?.contentResolver?.query(imageUri, filePathColumn, null, null, null)
        cursor?.moveToFirst()

        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val picturePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()

        val bitmap = BitmapFactory.decodeFile(picturePath)
        val compressedFile = File(context?.cacheDir, "compressed_image.jpg")
        val outputStream = FileOutputStream(compressedFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.close()

        return compressedFile
    }

    private fun uploadImageToFirestore(uri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference.child("profilePictures/${UUID.randomUUID()}")
        storageReference.putFile(uri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val profilePictureUrl = uri.toString()
                    lifecycleScope.launch {
                        updateProfilePicture(profilePictureUrl)
                    }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun updateProfilePicture(profilePictureUrl: String) {
        val updateProfile = UpdateProfile(profilePictureUrl)
        val account = authTokenRepository.getAuthToken()
        val userId = account?.userId.toString()
        authApi.updateProfilePicture("Bearer $accessToken", userId, updateProfile)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        lifecycleScope.launch {
                            updateProfileLocal(profilePictureUrl)
                        }
                        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private suspend fun updateProfileLocal(profilePictureUrl: String) {
        authTokenRepository.getAuthToken()?.let { authToken ->
            val updatedAuthToken = authToken.copy(profilePicture = profilePictureUrl)
            authTokenRepository.updateAuthToken(updatedAuthToken)
        }
    }

    private suspend fun logout() {
        val account = authTokenRepository.getAuthToken()
        if (account != null) {
            authTokenRepository.deleteAuthToken(account)
        }
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
    }
}