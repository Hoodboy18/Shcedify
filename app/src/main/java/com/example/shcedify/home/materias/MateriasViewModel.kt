package com.example.shcedify.home.materias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shcedify.core.ResponseService
import com.example.shcedify.core.model.Materia
import com.example.shcedify.core.network.HorarioService
import com.example.shcedify.core.repositories.HorarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MateriasViewModel(
    private val service: HorarioService = HorarioRepository()
): ViewModel() {

    private val _materiaState = MutableStateFlow<ResponseService<List<Materia>>?>(null)
    val materiaState: StateFlow<ResponseService<List<Materia>>?> = _materiaState.asStateFlow()

    fun loadMaterias() {
        viewModelScope.launch {
            _materiaState.value = ResponseService.Loading
            _materiaState.value = service.getTracks()
        }
    }
}