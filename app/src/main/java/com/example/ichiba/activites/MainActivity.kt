package com.example.ichiba.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.ichiba.fragments.AccountFragment
import com.example.ichiba.fragments.HomeFragment
import com.example.ichiba.fragments.MyAdsFragment
import com.example.ichiba.R
import com.example.ichiba.fragments.SearchFragment
import com.example.ichiba.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    showHomeFragment()
                    true
                }
                R.id.menu_account -> {
                    showAccountFragment()
                    true
                }
                R.id.menu_my_ads -> {
                    showMyAdsFragment()
                    true
                }
                R.id.menu_settings -> {
                    showSettingsFragment()
                    true
                }
                R.id.menu_sell -> {
                    startActivity(Intent(this, AdCreateActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.button.setOnClickListener {
            startActivity(Intent(this, AdCreateActivity::class.java))
        }
        showHomeFragment()
    }

    private fun showHomeFragment() {
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "HomeFragment")
        fragmentTransaction.commit()
    }

    private fun showAccountFragment() {
        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "AccountFragment")
        fragmentTransaction.commit()
    }

    private fun showMyAdsFragment() {
        val fragment = MyAdsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "MyAdsFragment")
        fragmentTransaction.commit()
    }

    private fun showSettingsFragment() {
        val fragment = SearchFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "SettingsFragment")
        fragmentTransaction.commit()
    }
}