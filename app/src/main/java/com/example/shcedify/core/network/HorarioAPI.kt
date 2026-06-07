package com.example.shcedify.core.network

import retrofit2.http.GET
import com.example.shcedify.core.model.MateriaResponse
import retrofit2.Response

interface HorarioAPI {
    @GET("3874fbe50e1c48f08d427e600f16aaae")
    suspend fun getMaterias(

    ): Response<MateriaResponse>
}
