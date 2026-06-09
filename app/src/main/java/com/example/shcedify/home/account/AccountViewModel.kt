package com.example.shcedify.home.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shcedify.core.ResponseService
import com.example.shcedify.core.repositories.UserRepository
import com.example.shcedify.onboarding.personal.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AccountData(
    val profile: UserProfile,
    val email: String,
    val horarioGuardado: List<Map<String, Any>> = emptyList(),
    val descripcionHorario: String = ""
)

class AccountViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _accountState = MutableStateFlow<ResponseService<AccountData>?>(null)
    val accountState: StateFlow<ResponseService<AccountData>?> = _accountState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _accountState.value = ResponseService.Loading
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _accountState.value = ResponseService.Error("Sesión inválida")
                return@launch
            }
            try {
                val doc = withContext(Dispatchers.IO) {
                    firestore.collection("users").document(uid).get().await()
                }
                val profile = doc.toObject(UserProfile::class.java) ?: UserProfile()

                @Suppress("UNCHECKED_CAST")
                val horario = try {
                    doc.get("horarioGuardado") as? List<Map<String, Any>> ?: emptyList()
                } catch (e: Exception) { emptyList() }

                val descripcion = doc.getString("descripcion") ?: ""

                _accountState.value = ResponseService.Success(
                    AccountData(
                        profile = profile,
                        email = auth.currentUser?.email ?: "",
                        horarioGuardado = horario,
                        descripcionHorario = descripcion
                    )
                )
            } catch (e: Exception) {
                _accountState.value = ResponseService.Error("No se pudo cargar el perfil: ${e.localizedMessage}")
            }
        }
    }
}