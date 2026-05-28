package com.example.shcedify.core.network

import com.example.shcedify.core.ResponseService
import com.example.shcedify.core.model.Materia

interface HorarioService {

    suspend fun getTracks(): ResponseService<List<Materia>>
}