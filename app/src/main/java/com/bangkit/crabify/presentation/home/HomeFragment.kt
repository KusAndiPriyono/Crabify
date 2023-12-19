package com.bangkit.crabify.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentHomeBinding
import com.bangkit.crabify.presentation.auth.login.LoginViewModel
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: LoginViewModel by viewModels()
    private val sensorViewModel: HomeViewModel by viewModels()
    private val adapter by lazy {
        SensorListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val greetingMessage = when (LocalTime.now()) {
            in LocalTime.parse("05:00:00")..LocalTime.parse("11:59:59") -> "Selamat Pagi,"
            in LocalTime.parse("12:00:00")..LocalTime.parse("16:59:59") -> "Selamat Siang,"
            in LocalTime.parse("17:00:00")..LocalTime.parse("20:59:59") -> "Selamat Sore,"
            else -> "Selamat malam"
        }
        binding.tvWelcome.text = greetingMessage

        val currentTimeMillis = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("EEE dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(currentTimeMillis))
        binding.tvDate.text = formattedDate

        observeSensorList()
        startSensorList()
        startAnalysis()

        binding.rvSensor.adapter = adapter

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSensor.layoutManager = gridLayoutManager
        authViewModel.getSession { user ->
            user?.let {
                binding.tvName.text = user.fullName
                sensorViewModel.getSensorData(user)
            }
        }
    }

    private fun startAnalysis() {
        binding.ivAnalytics.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_analisis)
        }
    }

    private fun startSensorList() {
        binding.ivList.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_crab_list)
        }
    }

    private fun observeSensorList() {
        sensorViewModel.sensor.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    adapter.updateList(state.data.toMutableList())
                }

                is UiState.Error -> {
                }
            }
        }
    }
}