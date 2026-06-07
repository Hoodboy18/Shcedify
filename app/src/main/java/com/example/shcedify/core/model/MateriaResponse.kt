package com.example.shcedify.core.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class MateriaResponse(
    @SerializedName("universidad")    val universidad: String,
    @SerializedName("facultad")       val facultad: String,
    @SerializedName("periodo_actual") val periodoActual: String,
    @SerializedName("carreras")       val carreras: List<Carrera>,
    @SerializedName("materias")       val materias: List<Materia>
)

@Parcelize
data class Carrera(
    @SerializedName("id")     val id: String,
    @SerializedName("nombre") val nombre: String
) : Parcelable

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
    @SerializedName("estado")          val estado: String,
    @SerializedName("cupo_maximo")     val cupoMaximo: Int,
    @SerializedName("cupo_disponible") val cupoDisponible: Int,
    @SerializedName("sesiones")        val sesiones: List<Sesion>
) : Parcelable

@Parcelize
data class Materia(
    @SerializedName("id")           val id: String,
    @SerializedName("clave")        val clave: String,
    @SerializedName("nombre")       val nombre: String,
    @SerializedName("descripcion")  val descripcion: String,
    @SerializedName("carrera_id")   val carreraId: String,
    @SerializedName("tipo")         val tipo: String,
    @SerializedName("creditos")     val creditos: Int,
    @SerializedName("horas_semana") val horasSemana: Int,
    @SerializedName("semestre_plan")val semestrePlan: Int,
    @SerializedName("area")         val area: String,
    @SerializedName("modalidad")    val modalidad: String,
    @SerializedName("seriacion")    val seriacion: List<String>,
    @SerializedName("grupos")       val grupos: List<Grupo>
) : Parcelable