package com.tech4everyone.garis

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tech4everyone.garis.auth.AuthViewModel
import com.tech4everyone.garis.databinding.ActivityStartBinding
import com.tech4everyone.garis.start.LoginBottomSheet
import com.tech4everyone.garis.start.RegisterBottomSheet


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        viewModel.getLoginState()

        viewModel.loginState.observe(this) {
            if (it) {
                Toast.makeText(this@StartActivity, "Logged in", Toast.LENGTH_LONG).show()
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else Toast.makeText(this@StartActivity, "No Auth Data", Toast.LENGTH_LONG).show()
        }

        binding.buttonRegister.setOnClickListener {
            val bottomSheetFragmentRegister = RegisterBottomSheet()
            bottomSheetFragmentRegister.show(supportFragmentManager, "Register BottomSheet Dialog")
        }

        binding.buttonLogin.setOnClickListener {
            val bottomSheetFragmentLogin = LoginBottomSheet()
            bottomSheetFragmentLogin.show(supportFragmentManager, "Login ButtomSheet Dialog")
        }

        binding.btnConnectGoogle.setOnClickListener{
            Toast.makeText(this@StartActivity, "Not available yet", Toast.LENGTH_SHORT).show()
        }
    }
}