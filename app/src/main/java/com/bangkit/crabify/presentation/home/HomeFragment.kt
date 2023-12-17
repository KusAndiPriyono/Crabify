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

        observeSensorList()

        binding.ivList.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_crabListFragment)
        }

        binding.rvSensor.adapter = adapter

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSensor.layoutManager = gridLayoutManager

        authViewModel.getSession { uid ->
            sensorViewModel.getSensorData(uid)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}