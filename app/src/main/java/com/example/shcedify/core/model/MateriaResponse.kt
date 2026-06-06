package com.example.shcedify.core.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sesion(
    @SerializedName("dia")         val dia: String,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin")    val horaFin: String,
    @SerializedName("salon")       val salon: String,
    @SerializedName("edificio")    val edificio: String
) : Parcelable

@Parcelize
data class Grupo(
    @SerializedName("id")              val id: String,
    @SerializedName("numero_grupo")    val numeroGrupo: String,
    @SerializedName("profesor")        val profesor: String,
    @SerializedName("cupo_maximo")     val cupoMaximo: Int,
    @SerializedName("cupo_disponible") val cupoDisponible: Int,
    @SerializedName("sesiones")        val sesiones: List<Sesion>
) : Parcelable

@Parcelize
data class Materia(
    @SerializedName("id")            val id: String,
    @SerializedName("clave")         val clave: String,
    @SerializedName("nombre")        val nombre: String,
    @SerializedName("creditos")      val creditos: Int,
    @SerializedName("semestre_plan") val semestrePlan: Int,
    @SerializedName("area")          val area: String,
    @SerializedName("grupos")        val grupos: List<Grupo>
) : Parcelable

data class MateriaResponse(
    @SerializedName("semestre")    val semestre: String,
    @SerializedName("facultad")    val facultad: String,
    @SerializedName("universidad") val universidad: String,
    @SerializedName("materias")    val materias: List<Materia>
)