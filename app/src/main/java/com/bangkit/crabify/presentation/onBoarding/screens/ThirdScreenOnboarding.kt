package com.bangkit.crabify.presentation.onBoarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentThirdScreenOnboardingBinding
import com.bangkit.crabify.presentation.onBoarding.OnBoardingEvent
import com.bangkit.crabify.presentation.onBoarding.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThirdScreenOnboarding : Fragment() {

    private var _binding: FragmentThirdScreenOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<OnBoardingViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThirdScreenOnboardingBinding.inflate(inflater, container, false)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.view_pager_onBoarding)

        binding.tvBack.setOnClickListener {
            viewPager?.currentItem = 1
        }
        return binding.root
    }

    private fun sendSaveAppEntryEvent() {
        viewModel.onEvent(OnBoardingEvent.SaveAppEntry)
    }
}