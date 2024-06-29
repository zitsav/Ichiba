package com.example.ichiba.activites

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.ichiba.R
import com.example.ichiba.databinding.ActivityAdPostedBinding
import com.example.ichiba.fragments.HomeFragment
import kotlinx.coroutines.launch

class AdPostedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdPostedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdPostedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.lottieAnimationView.apply {
            repeatCount = 1
            playAnimation()
        }

        binding.postAdBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}