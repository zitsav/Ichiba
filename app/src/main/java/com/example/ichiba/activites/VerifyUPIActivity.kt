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
import com.example.ichiba.databinding.ActivityEditUpiBinding
import com.example.ichiba.databinding.ActivityVerifyUpiactivityBinding
import com.example.ichiba.dataclass.UpdateUPI
import com.example.ichiba.interfaces.AuthApi
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyUPIActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyUpiactivityBinding
    private lateinit var authApi: AuthApi
    private lateinit var authTokenRepository: AuthTokenRepository
    private lateinit var accessToken: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerifyUpiactivityBinding.inflate(layoutInflater)

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
            }
        }

        binding.loginButton.setOnClickListener {
            updateUPI()
        }

        binding.skipTv.setOnClickListener{
            val intent = Intent(this@VerifyUPIActivity, MainActivity::class.java)
            startActivity(intent)
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
                        Toast.makeText(this@VerifyUPIActivity, "UPI ID updated", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@VerifyUPIActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@VerifyUPIActivity, "Failed to update UPI ID", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@VerifyUPIActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private suspend fun updateUPILocal(newUPI: String) {
        authTokenRepository.getAuthToken()?.let { authToken ->
            val updatedAuthToken = authToken.copy(upiId = newUPI)
            authTokenRepository.updateAuthToken(updatedAuthToken)
        }
    }
}