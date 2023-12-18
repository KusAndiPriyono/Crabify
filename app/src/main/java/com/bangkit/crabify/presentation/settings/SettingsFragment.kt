package com.bangkit.crabify.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentSettingsBinding
import com.bangkit.crabify.presentation.auth.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {
            viewModel.logout {
                requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility =
                    View.GONE
                findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
                requireActivity().finish()
            }
        }

        viewModel.getSession { user ->
            user?.let {
                binding.tvUsername.text = it.fullName
                binding.tvEmail.text = it.email
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}