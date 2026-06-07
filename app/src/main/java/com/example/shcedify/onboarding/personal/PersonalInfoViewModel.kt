package com.example.shcedify.onboarding.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shcedify.core.ResponseService
import com.example.shcedify.core.repositories.UserRepository
import com.example.shcedify.onboarding.personal.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalInfoViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _saveState = MutableStateFlow<ResponseService<Unit>?>(null)
    val saveState: StateFlow<ResponseService<Unit>?> = _saveState.asStateFlow()

    fun validateFirstName(value: String): String? {
        if (value.isBlank()) return "El nombre es requerido"
        if (value.length < 2) return "Mínimo 2 caracteres"
        return null
    }

    fun validateLastName(value: String): String? {
        if (value.isBlank()) return "El primer apellido es requerido"
        if (value.length < 2) return "Mínimo 2 caracteres"
        return null
    }

    fun validateNumCuenta(value: String): String? {
        if (value.isBlank()) return "El número de cuenta es requerido"
        if (!value.all { it.isDigit() }) return "Solo números"
        if (value.length != 9) return "Debe tener 9 dígitos"
        return null
    }

    fun validatePhone(value: String): String? {
        if (value.isBlank()) return "El teléfono es requerido"
        if (!value.all { it.isDigit() }) return "Solo números"
        if (value.length !in 10..15) return "Entre 10 y 15 dígitos"
        return null
    }

    fun validateBirthDate(value: String): String? {
        if (value.isBlank()) return "Selecciona tu fecha de nacimiento"
        return null
    }

    fun validateCarrera(value: String): String? {
        if (value.isBlank()) return "Selecciona tu carrera"
        return null
    }

    fun isFormValid(
        firstName: String, lastName: String,
        numCuenta: String, phone: String,
        birthDate: String, carrera: String
    ): Boolean {
        return validateFirstName(firstName) == null &&
                validateLastName(lastName) == null &&
                validateNumCuenta(numCuenta) == null &&
                validatePhone(phone) == null &&
                validateBirthDate(birthDate) == null &&
                validateCarrera(carrera) == null
    }

    fun saveProfile(
        uid: String, firstName: String, secondName: String,
        lastName: String, secondLastName: String,
        numCuenta: String, carrera: String,
        phone: String, birthDate: String
    ) {
        viewModelScope.launch {
            _saveState.value = ResponseService.Loading
            val user = UserProfile(
                id             = uid,
                firstName      = firstName,
                secondName     = secondName,
                lastName       = lastName,
                secondLastName = secondLastName,
                numCuenta      = numCuenta,
                carrera        = carrera,
                phone          = phone,
                birthDate      = birthDate
            )
            _saveState.value = repository.saveUserInfo(user)
        }
    }
}