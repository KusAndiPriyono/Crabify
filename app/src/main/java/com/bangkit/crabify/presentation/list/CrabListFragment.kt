package com.bangkit.crabify.presentation.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.crabify.databinding.FragmentCrabListBinding
import com.bangkit.crabify.presentation.auth.login.LoginViewModel
import com.bangkit.crabify.presentation.upload.ClassificationViewModel
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrabListFragment : Fragment() {

    private var _binding: FragmentCrabListBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: LoginViewModel by viewModels()
    private val homeViewModel: ClassificationViewModel by viewModels()

    private val adapter by lazy {
        CrabListAdapter(
            onItemClicked = { pos, item ->
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrabListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCrabList()
        binding.rvCrabList.adapter = adapter
        binding.rvCrabList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        authViewModel.getSession { uid ->
            homeViewModel.getCrabs(uid)
            Log.d("CrabListFragment", "onViewCreated: $uid")
        }
    }

    private fun observeCrabList() {
        homeViewModel.crab.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    adapter.updateList(state.data.toMutableList())
                    Log.d("update", "observeCrabList: ${state.data}")
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