package com.bangkit.crabify.presentation.forgot

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bangkit.crabify.databinding.FragmentForgotPasswordBinding
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPassword : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgotPasswordViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeForgotPassword()
        binding.btnForgotPassword.setOnClickListener {
            if (validation()) {
                viewModel.forgotPassword(binding.etForgotPasswordEmail.text.toString())
            }
        }
    }

    private fun observeForgotPassword() {
        viewModel.forgotPassword.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.btnForgotPassword.text = ""
                    binding.pbForgotPassword.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    binding.btnForgotPassword.text = "Send"
                    binding.pbForgotPassword.visibility = View.GONE
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                }

                is UiState.Error -> {
                    binding.btnForgotPassword.text = "Send"
                    binding.pbForgotPassword.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validation(): Boolean {
        var valid = true
        if (binding.etForgotPasswordEmail.text.isNullOrEmpty()) {
            valid = false
            Toast.makeText(requireContext(), "Email email address", Toast.LENGTH_SHORT).show()
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(binding.etForgotPasswordEmail.text.toString())
                    .matches()
            ) {
                valid = false
                Toast.makeText(requireContext(), "Email is not valid", Toast.LENGTH_SHORT).show()
            }
        }
        return valid
    }

}