package com.example.ichiba.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ichiba.R
import com.example.ichiba.activites.ProfileEditActivity
import com.example.ichiba.data.AuthDatabase
import com.example.ichiba.data.AuthTokenRepository
import com.example.ichiba.databinding.FragmentAccountBinding
import com.example.ichiba.dataclass.User
import com.example.ichiba.utils.Utils
import kotlinx.coroutines.launch
import java.util.Locale


class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var authTokenRepository: AuthTokenRepository

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

        lifecycleScope.launch {
            val account = authTokenRepository.getAuthToken()

            account?.let {
                binding.nameTvAcc.text = it.name
                binding.rollNoTvAcc.text = it.enrollmentNumber
                binding.batchTvAcc.text = it.batch
                binding.programTvAcc.text = it.program
                binding.ContactTvAcc.text = it.phoneNumber
                binding.UpiIdAcc.text = it.upiId
                binding.textView4.text = it.name
                binding.textView5.text = buildString {
                    append(it.enrollmentNumber?.toLowerCase(Locale.getDefault()))
                    append("@iiita.ac.in")
                }
                binding.editProfileCvAcc.setOnClickListener {
                    val intent = Intent(requireContext(), ProfileEditActivity::class.java)
                    startActivity(intent)
                }

                it.profilePicture?.let { profilePictureUrl ->
                    Glide.with(requireContext())
                        .load(profilePictureUrl)
                        .placeholder(R.drawable.i2)
                        .into(binding.profileTvAcc)
                }
            }
        }
    }
}