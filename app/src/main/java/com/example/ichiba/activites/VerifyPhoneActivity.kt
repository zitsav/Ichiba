package com.example.ichiba.activites

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.ichiba.R
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.ActivityEditPhoneBinding
import com.example.ichiba.databinding.ActivityVerifyPhoneBinding
import com.example.ichiba.dataclass.UpdatePhone
import com.example.ichiba.interfaces.AuthApi
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyPhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyPhoneBinding
    private lateinit var authApi: AuthApi
    private lateinit var authTokenRepository: AuthTokenRepository
    private lateinit var accessToken: String
    private lateinit var userId: String
    private lateinit var upiId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerifyPhoneBinding.inflate(layoutInflater)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        window.statusBarColor = Color.TRANSPARENT
        setContentView(binding.root)

        supportActionBar?.hide()

        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        authApi = RetrofitInstance.getRetrofitInstance().create(AuthApi::class.java)
        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())

        lifecycleScope.launch {
            val account = authTokenRepository.getAuthToken()
            account?.let {
                accessToken = it.token?: ""
                userId = it.userId.toString()
                upiId = it.upiId?:""
            }
        }

        binding.loginButton.setOnClickListener {
            updatePhoneNumber()
        }

        binding.skipTv.setOnClickListener{
            if (upiId.isEmpty()){
                val intent = Intent(this@VerifyPhoneActivity, VerifyUPIActivity::class.java)
                startActivity(intent)
            }
            else{
                val intent = Intent(this@VerifyPhoneActivity, MainActivity::class.java)
                startActivity(intent)
            }
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
                        Toast.makeText(this@VerifyPhoneActivity, "Phone number updated", Toast.LENGTH_SHORT).show()
                        if (upiId.isEmpty()){
                            val intent = Intent(this@VerifyPhoneActivity, VerifyUPIActivity::class.java)
                            startActivity(intent)
                        }
                        else{
                            val intent = Intent(this@VerifyPhoneActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@VerifyPhoneActivity, "Failed to update phone number", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@VerifyPhoneActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private suspend fun updatePhoneLocal(newPhoneNumber: String) {
        authTokenRepository.getAuthToken()?.let { authToken ->
            val updatedAuthToken = authToken.copy(phoneNumber = newPhoneNumber)
            authTokenRepository.updateAuthToken(updatedAuthToken)
        }
    }
}