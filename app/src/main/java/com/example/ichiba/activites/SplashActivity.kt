package com.example.ichiba.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.ichiba.R
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.dataclass.VerifyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class SplashActivity : AppCompatActivity() {

    private lateinit var authTokenRepository: AuthTokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        authTokenRepository = AuthTokenRepository(AuthDatabase.getDatabase(this).authTokenDao())

        lifecycleScope.launch {
            val authToken = withContext(Dispatchers.IO) {
                authTokenRepository.getAuthToken()
            }
            val currentTime = Date(System.currentTimeMillis())

            val resultIntent = if (authToken != null && currentTime.before(authToken.expirationTime)) {
                Intent(this@SplashActivity, MainActivity::class.java)
            }
            else {
                if (authToken != null) {
                    authTokenRepository.deleteAuthToken(authToken)
                }
                Intent(this@SplashActivity, LoginActivity::class.java)
            }
            Handler().postDelayed({
                startActivity(resultIntent)
                finish()
            }, 3000)
        }
    }
}