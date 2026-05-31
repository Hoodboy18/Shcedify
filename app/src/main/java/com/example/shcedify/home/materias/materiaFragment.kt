package com.example.shcedify.home.materias

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shcedify.core.FragmentCommunicator
import com.example.shcedify.core.ResponseService
import com.example.shcedify.databinding.FragmentMateriaBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class materiaFragment : Fragment() {

    private var _binding: FragmentMateriaBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MateriasViewModel>()
    private lateinit var communicator: FragmentCommunicator
    private val adapter = MateriasAdapter { materia -> }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMateriaBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        binding.rvMaterias.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMaterias.adapter = adapter
        observeState()
        viewModel.loadMaterias()
        return binding.root
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.materiaState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            adapter.submitList(state.data)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}