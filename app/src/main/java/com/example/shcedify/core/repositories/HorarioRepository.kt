package com.example.shcedify.core.repositories

import com.example.shcedify.core.ResponseService
import com.example.shcedify.core.network.ApiClient
import com.example.shcedify.core.network.HorarioService
import com.example.shcedify.home.materias.model.Materia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HorarioRepository: HorarioService {

    private val api = ApiClient.HorarioApi

    override suspend fun getTracks(): ResponseService<List<Materia>> = withContext(Dispatchers.IO){
        try {
            val response
        }
    }

}