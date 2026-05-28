package com.example.shcedify.core.network

import retrofit2.http.GET
import com.example.shcedify.core.model.MateriaResponse
import retrofit2.Response

interface HorarioAPI {
    @GET("9c95b9dbe5424b85ae131e631d3d3a82")
    suspend fun getMaterias(

    ): Response<MateriaResponse>
}
