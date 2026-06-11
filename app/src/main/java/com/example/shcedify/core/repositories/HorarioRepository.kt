package com.example.shcedify.core.repositories

import com.example.shcedify.core.ResponseService
import com.example.shcedify.core.network.ApiClient
import com.example.shcedify.core.network.HorarioService
import com.example.shcedify.core.model.Materia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HorarioRepository : HorarioService {

    private val api = ApiClient.HorarioApi

    override suspend fun getTracks(): ResponseService<List<Materia>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getMaterias()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ResponseService.Success(body.materias)
                } else {
                    ResponseService.Error("Respuesta vacía del servidor")
                }
            } else {
                ResponseService.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            ResponseService.Error("No se pudieron cargar las materias: ${e.localizedMessage}")
        }
    }
}