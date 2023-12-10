package com.bangkit.crabify.presentation.auth.register

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.databinding.FragmentRegisterBinding
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvRegisterHere.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.btnRegister.setOnClickListener {
            if (validation()) {
                viewModel.register(
                    fullName = binding.etFullName.text.toString(),
                    email = binding.etEmailRegister.text.toString(),
                    password = binding.etPassword.text.toString(),
                    user = getUserObject()
                )
            }
        }

        viewModel.register.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Loading..."
                }

                is UiState.Success -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                    val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                    findNavController().navigate(action)
                }

                is UiState.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
            }
        }
    }

    private fun getUserObject(): User {
        return User(
            id = "",
            fullName = binding.etFullName.text.toString(),
            email = binding.etEmailRegister.text.toString(),
            password = binding.etPassword.text.toString(),
        )
    }

    private fun validation(): Boolean {
        var isValid = true

        if (binding.etFullName.text.isNullOrEmpty()) {
            binding.etFullName.error = "Full Name is required"
            isValid = false
        }
        if (binding.etEmailRegister.text.isNullOrEmpty()) {
            binding.etEmailRegister.error = "Email is required"
            isValid = false
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmailRegister.text.toString())
                    .matches()
            ) {
                binding.etEmailRegister.error = "Invalid email address"
                isValid = false
            }
        }
        if (binding.etPassword.text.isNullOrEmpty()) {
            binding.etPassword.error = "Password is required"
            isValid = false
        } else {
            if (binding.etPassword.text.toString().length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                isValid = false
            }
        }
        return isValid
    }
}