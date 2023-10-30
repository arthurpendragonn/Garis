package com.tech4everyone.garis.start

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tech4everyone.garis.R
import com.tech4everyone.garis.auth.AuthState
import com.tech4everyone.garis.auth.AuthViewModel
import com.tech4everyone.garis.databinding.BottomSheetRegisterBinding


class RegisterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetRegisterBinding.inflate(inflater, container, false)
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

                    Toast.makeText(requireContext(), "Registered", Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                }
                else -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    binding.progressbar.visibility = View.GONE
                }
            }
        }

        binding.btnRegister.setOnClickListener {

            val email = _binding?.email?.text.toString()
            val password = _binding?.password?.text.toString()
            val name = _binding?.name?.text.toString()

            if (email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email ?: "").matches()) {
                _binding?.email?.error = resources.getString(R.string.invalidEmail)
            } else if (password.isEmpty() && password.length < 6) {
                _binding?.password?.error = resources.getString(R.string.invalidPassword)
            } else {
                viewModel.handleSignUp(email,password,name)
            }
        }
    }
}
