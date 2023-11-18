package com.bangkit.crabify.presentation.onBoarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bangkit.crabify.databinding.FragmentOnBoardingBinding
import com.bangkit.crabify.presentation.onBoarding.screens.FirstScreenOnboarding
import com.bangkit.crabify.presentation.onBoarding.screens.SecondScreenOnboarding
import com.bangkit.crabify.presentation.onBoarding.screens.ThirdScreenOnboarding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFragment : Fragment() {

    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)


        val fragmentList = arrayListOf(
            FirstScreenOnboarding(),
            SecondScreenOnboarding(),
            ThirdScreenOnboarding()
        )

        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        binding.viewPagerOnBoarding.adapter = adapter
        binding.viewPagerOnBoarding.isUserInputEnabled = false

        val dotsIndicator = binding.dotsIndicator
        dotsIndicator.attachTo(binding.viewPagerOnBoarding)

        return binding.root
    }


}