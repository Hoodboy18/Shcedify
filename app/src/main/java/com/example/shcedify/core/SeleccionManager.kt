package com.example.shcedify.core

import com.example.shcedify.core.model.Grupo
import com.example.shcedify.core.model.Materia

data class MateriaSeleccionada(
    val materia: Materia,
    val grupo: Grupo,
    val esImportante: Boolean = false
)

object SeleccionManager {

    private val seleccionadas = mutableListOf<MateriaSeleccionada>()
    private val importantesIds = mutableSetOf<String>()

    enum class ResultadoAgregar { OK, LIMITE_TOTAL, YA_EXISTE }

    fun agregarMateria(materia: Materia, grupo: Grupo): ResultadoAgregar {
        if (seleccionadas.any { it.materia.id == materia.id }) return ResultadoAgregar.YA_EXISTE
        if (seleccionadas.size >= 7) return ResultadoAgregar.LIMITE_TOTAL
        seleccionadas.add(MateriaSeleccionada(materia, grupo, importantesIds.contains(materia.id)))
        return ResultadoAgregar.OK
    }

    fun quitarMateria(id: String) {
        seleccionadas.removeAll { it.materia.id == id }
        importantesIds.remove(id)
    }

    fun marcarImportante(materia: Materia, grupo: Grupo): Boolean {
        if (importantesIds.size >= 3) return false
        importantesIds.add(materia.id)
        // Si ya está agregada, actualizar su estado
        val idx = seleccionadas.indexOfFirst { it.materia.id == materia.id }
        if (idx >= 0) {
            seleccionadas[idx] = seleccionadas[idx].copy(esImportante = true)
        } else {
            // Agregar automáticamente si no estaba
            if (seleccionadas.size < 7) {
                seleccionadas.add(MateriaSeleccionada(materia, grupo, true))
            } else {
                importantesIds.remove(materia.id)
                return false
            }
        }
        return true
    }

    fun desmarcarImportante(id: String) {
        importantesIds.remove(id)
        val idx = seleccionadas.indexOfFirst { it.materia.id == id }
        if (idx >= 0) {
            seleccionadas[idx] = seleccionadas[idx].copy(esImportante = false)
        }
    }

    fun esImportante(id: String) = importantesIds.contains(id)
    fun estaAgregada(id: String) = seleccionadas.any { it.materia.id == id }
    fun totalSeleccionadas() = seleccionadas.size
    fun importantesCount() = importantesIds.size

    fun getSeleccionadas(): List<MateriaSeleccionada> = seleccionadas.toList()
    fun getImportantes(): List<MateriaSeleccionada> = seleccionadas.filter { it.esImportante }
    fun getNormales(): List<MateriaSeleccionada> = seleccionadas.filter { !it.esImportante }

    fun getGrupoSeleccionado(materiaId: String): Grupo? =
        seleccionadas.firstOrNull { it.materia.id == materiaId }?.grupo

    fun limpiar() {
        seleccionadas.clear()
        importantesIds.clear()
    }
}