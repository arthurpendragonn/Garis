package com.tech4everyone.garis.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tech4everyone.garis.MainActivity
import com.tech4everyone.garis.R
import com.tech4everyone.garis.auth.AuthState
import com.tech4everyone.garis.auth.AuthViewModel
import com.tech4everyone.garis.databinding.BottomSheetLoginBinding

class LoginBottomSheet: BottomSheetDialogFragment() {

    private var _binding: BottomSheetLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.authState.observe(this){

            when(it){
                AuthState.Idle -> {
                    binding.progressbar.visibility = View.GONE
                }
                AuthState.Loading -> {
                    binding.progressbar.visibility = View.VISIBLE
                }
                AuthState.Success -> {
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_LONG).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    dialog?.dismiss()
                    activity?.finish()
                }
                else -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    binding.progressbar.visibility = View.GONE
                }
            }
        }

        binding.login.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email ?: "").matches()) {
                binding.email.error = resources.getString(R.string.invalidEmail)
            } else if (password.isEmpty() && password.length < 6) {
                binding.password.error = resources.getString(R.string.invalidPassword)
            } else {
               viewModel.handleSignIn(email, password)
            }
        }

        binding.resetPass.setOnClickListener {
            DialogController.showDialogConfirmation(requireContext(), "Reset Password?", "We will send reset password request into your email") {
                val email = binding.email.text.toString()

                if (email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email ?: "").matches()) {
                    binding.email.error = resources.getString(R.string.invalidEmail)
                } else {
                    viewModel.resetPassword(email)
                    viewModel.authState.observe(this) {
                        when(it) {
                            AuthState.Error(it.message) -> Toast.makeText(requireContext(),it.message, Toast.LENGTH_LONG).show()
                            else -> Toast.makeText(requireContext(),"Reset password request have been sent into your email", Toast.LENGTH_LONG).show()
                        }
                    }
                }

            }
        }
    }

    object DialogController
    {
        private lateinit var dialogAlert: AlertDialog
        private lateinit var dialogConfirmation: AlertDialog

        private fun cantShowDialog(): Boolean =
            this::dialogAlert.isInitialized && dialogAlert.isShowing ||
                    this::dialogConfirmation.isInitialized && dialogConfirmation.isShowing

        fun showDialogAlert(context: Context, title: String, msg: String)
        {
            if (cantShowDialog()) { return }

            dialogAlert = MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Close", null)
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }

        fun showDialogConfirmation(
            context: Context,
            title: String,
            msg: String,
            callback: () -> Unit
        ) {
            if (cantShowDialog()) { return }

            dialogConfirmation = MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm") { _, _ -> callback() }
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }
}
