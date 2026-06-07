package com.example.shcedify.home.materias

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shcedify.R
import com.example.shcedify.core.FragmentCommunicator
import com.example.shcedify.core.ResponseService
import com.example.shcedify.core.model.Materia
import com.example.shcedify.databinding.FragmentMateriaBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class materiaFragment : Fragment() {

    private var _binding: FragmentMateriaBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MateriasViewModel>()
    private lateinit var communicator: FragmentCommunicator
    private var allMaterias: List<Materia> = emptyList()
    private var selectedSemestre: Int? = null
    private var selectedCarrera: String? = null

    private val adapter = MateriasAdapter { materia ->
        val bundle = Bundle().apply { putParcelable("materia", materia) }
        findNavController().navigate(
            R.id.action_materiaFragment_to_materiaDetailFragment, bundle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMateriaBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        binding.rvMaterias.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMaterias.adapter = adapter
        setupSearch()
        setupChips()
        observeState()
        viewModel.loadMaterias()
        return binding.root
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { applyFilters() }
    }

    private fun setupChips() {
        val chipMap = mapOf(
            binding.chipTodos to null,
            binding.chip1 to 1,
            binding.chip2 to 2,
            binding.chip3 to 3,
            binding.chip4 to 4,
            binding.chip5 to 5,
            binding.chip6 to 6,
            binding.chip7 to 7,
            binding.chip8 to 8
        )
        binding.chipGroupSemestre.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedSemestre = if (checkedIds.isEmpty() ||
                checkedIds.contains(R.id.chipTodos)) {
                null
            } else {
                chipMap.entries.firstOrNull {
                    it.key.id == checkedIds.first()
                }?.value
            }
            applyFilters()
        }

        val carreraMap = mapOf(
            binding.chipCarreraTodas to null,
            binding.chipINF to "INF",
            binding.chipADM to "ADM",
            binding.chipCON to "CON",
            binding.chipNI to "NI"
        )
        binding.chipGroupCarrera.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCarrera = if (checkedIds.isEmpty() ||
                checkedIds.contains(R.id.chipCarreraTodas)) {
                null
            } else {
                carreraMap.entries.firstOrNull {
                    it.key.id == checkedIds.first()
                }?.value
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val filtered = allMaterias.filter { materia ->
            val matchesSearch = query.isEmpty() ||
                    materia.nombre.lowercase().contains(query) ||
                    materia.clave.lowercase().contains(query)
            val matchesSemestre = selectedSemestre == null ||
                    materia.semestrePlan == selectedSemestre
            val matchesCarrera = selectedCarrera == null ||
                    materia.carreraId == selectedCarrera ||
                    materia.carreraId == "GLOBAL"
            matchesSearch && matchesSemestre && matchesCarrera
        }
        adapter.submitList(filtered)
        binding.tvResultCount.text =
            "${filtered.size} materia${if (filtered.size != 1) "s" else ""} encontrada${if (filtered.size != 1) "s" else ""}"
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.materiaState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            allMaterias = state.data
                            applyFilters()
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(
                                binding.root, state.error, Snackbar.LENGTH_LONG
                            ).show()
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