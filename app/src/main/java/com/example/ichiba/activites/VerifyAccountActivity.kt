package com.example.ichiba.activites

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ichiba.interfaces.AuthApi
import com.example.ichiba.RetrofitInstance
import org.json.JSONException
import org.json.JSONObject
import androidx.lifecycle.lifecycleScope
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthToken
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.ActivityVerifyAccountBinding
import com.example.ichiba.dataclass.VerifyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class VerifyAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyAccountBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var authApi: AuthApi
    private lateinit var authTokenRepository: AuthTokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please Wait...")
            setCanceledOnTouchOutside(true)
        }

        val retrofit = RetrofitInstance.getRetrofitInstance()
        authApi = retrofit.create(AuthApi::class.java)

        binding.verifyButton.setOnClickListener {
            lifecycleScope.launch {
                verifyAccount()
            }
        }
    }

    private suspend fun verifyAccount() {
        val phoneNumber = binding.contact.text.toString().trim()
        val upiId = binding.upiId.text.toString().trim()

        if (phoneNumber.isEmpty()) {
            binding.contact.error = "Enter Contact Number"
            binding.contact.requestFocus()
            return
        }

        if (upiId.isEmpty()) {
            binding.upiId.error = "Enter UPI ID"
            binding.upiId.requestFocus()
            return
        }

        val request = VerifyRequest(phoneNumber, upiId)

        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())
        val authToken = authTokenRepository.getAuthToken()
        val accessToken = "Bearer " + (authToken?.token ?: "")
        val userId = authToken?.userId.toString()

        progressDialog.show()

        try {
            val response = withContext(Dispatchers.IO) {
                authApi.verify(accessToken, request, userId).execute()
            }

            progressDialog.dismiss()

            if (response.isSuccessful) {
                authToken?.let {
                    updateAuthToken(it, phoneNumber, upiId)
                }

                val intent = Intent(this@VerifyAccountActivity, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody.isNullOrEmpty()) {
                    "Unknown error occurred"
                } else {
                    try {
                        JSONObject(errorBody).getString("message")
                    } catch (e: JSONException) {
                        "Unknown error occurred"
                    }
                }
                Toast.makeText(this@VerifyAccountActivity, "Unsuccessful: $errorMessage", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            progressDialog.dismiss()
            Toast.makeText(this@VerifyAccountActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun updateAuthToken(authToken: AuthToken, phoneNumber: String, upiId: String) {
        val updatedAuthToken = authToken.copy(
            isVerified = true,
            phoneNumber = phoneNumber,
            upiId = upiId
        )
        authTokenRepository.updateAuthToken(updatedAuthToken)
    }
}