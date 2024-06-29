package com.example.ichiba.activites

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.ichiba.RetrofitInstance
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.ActivityLoginBinding
import com.example.ichiba.dataclass.AuthResponse
import com.example.ichiba.dataclass.LoginRequest
import com.example.ichiba.dataclass.User
import com.example.ichiba.interfaces.AuthApi
import com.example.ichiba.utils.AdLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.sql.Date
import java.sql.Time
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var authApi: AuthApi
    private lateinit var authTokenRepository: AuthTokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(true)

        val retrofit = RetrofitInstance.getRetrofitInstance()
        authApi = retrofit.create(AuthApi::class.java)

        binding.loginButton.setOnClickListener {
            lifecycleScope.launch {
                validateData()
            }
        }
    }

    private var username = ""
    private var password = ""

    private suspend fun validateData() {
        username = binding.loginEmail.text.toString().toUpperCase(Locale.getDefault()).trim()
        password = binding.loginPassword.text.toString().trim()

        if (username.isEmpty()) {
            binding.loginEmail.error = "Enter Username"
            binding.loginEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.loginPassword.error = "Enter Password"
            binding.loginPassword.requestFocus()
        } else {
            loginUser()
        }
    }

    private suspend fun loginUser() {
        AdLoader.showDialog(this, isCancelable = true)
        val request = LoginRequest(username, password)
        try {
            val response = withContext(Dispatchers.IO) {
                authApi.login(request).execute()
            }
            AdLoader.hideDialog()
            if (response.isSuccessful) {
                val authToken = response.body()?.token
                val user = response.body()?.user
                if (authToken != null && user != null) {
                    saveAuthToken(authToken, user)
                    navigateToNextActivity(user)
                }
            } else {
                handleLoginError(response)
            }
        } catch (e: IOException) {
            AdLoader.hideDialog()
            Toast.makeText(
                this@LoginActivity,
                "Network error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private suspend fun saveAuthToken(authToken: String, user: User) {
        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())
        val currentTime = Time(System.currentTimeMillis())
        val expirationTime = Time(currentTime.time + TimeUnit.DAYS.toMillis(30))
        authTokenRepository.saveAuthToken(authToken, expirationTime, user)
    }

    private fun navigateToNextActivity(user: User) {
        val intent = if (user.phoneNumber.isNullOrEmpty()) {
            Intent(this@LoginActivity, VerifyPhoneActivity::class.java)
        }
        else if  (user.upiId.isNullOrEmpty()){
            Intent(this@LoginActivity, VerifyUPIActivity::class.java)
        }
        else {
            Intent(this@LoginActivity, MainActivity::class.java)
        }

        startActivity(intent)
        finishAffinity()
    }

    private fun handleLoginError(response: Response<AuthResponse>) {
        val errorBody = response.errorBody()?.string()
        val errorMessage = if (errorBody.isNullOrEmpty()) {
            "Unknown error occurred"
        } else {
            try {
                val errorJson = JSONObject(errorBody)
                errorJson.getString("message")
            } catch (e: JSONException) {
                "Unknown error occurred"
            }
        }
        if (errorBody != null) {
            Log.e("LoginActivity", errorBody)
        }
        Toast.makeText(
            this@LoginActivity,
            "Unsuccessful: $errorMessage",
            Toast.LENGTH_LONG
        ).show()
    }
}