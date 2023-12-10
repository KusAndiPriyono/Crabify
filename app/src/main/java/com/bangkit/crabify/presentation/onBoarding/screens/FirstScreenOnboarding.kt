package com.bangkit.crabify.presentation.onBoarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentFirstScreenOnboardingBinding
import com.bangkit.crabify.presentation.onBoarding.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstScreenOnboarding : Fragment() {

    private var _binding: FragmentFirstScreenOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnBoardingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstScreenOnboardingBinding.inflate(inflater, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.view_pager_onBoarding)

        binding.btnNext.setOnClickListener {
            viewModel.getSession { user ->
                if (user != null) {
                    viewPager?.currentItem = 0
                } else {
                    viewPager?.currentItem = 1
                }
            }
        }
        return binding.root
    }
}