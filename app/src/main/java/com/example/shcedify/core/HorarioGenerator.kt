package com.example.shcedify.core

import com.example.shcedify.core.model.Grupo
import com.example.shcedify.core.model.Materia
import com.example.shcedify.core.model.Sesion

data class HorarioGenerado(
    val id: Int,
    val materias: List<MateriaSeleccionada>,
    val tieneTraslape: Boolean = false,
    val descripcion: String = ""
)

object HorarioGenerator {

    fun generarHorarios(
        importantes: List<MateriaSeleccionada>,
        normales: List<MateriaSeleccionada>
    ): List<HorarioGenerado> {
        val todas = importantes + normales
        if (todas.isEmpty()) return emptyList()

        if (!tieneTraslape(todas)) {
            return listOf(
                HorarioGenerado(1, todas, false, "Tu horario sin traslapes")
            )
        }

        // Hay traslape — generar alternativas
        val resultados = mutableListOf<HorarioGenerado>()

        // Estrategia 1: cambiar grupos de las materias en conflicto
        // manteniendo siempre las importantes con su grupo original
        val opcionesCambiandoGrupos = intentarResolverCambiandoGrupos(importantes, normales)
        resultados.addAll(opcionesCambiandoGrupos)

        // Estrategia 2: si no se resolvió con cambio de grupos,
        // quitar la normal en conflicto menos prioritaria
        if (resultados.none { !it.tieneTraslape }) {
            val opcionesQuitando = intentarResolverQuitandoNormales(importantes, normales)
            resultados.addAll(opcionesQuitando)
        }

        // Si aún no hay opciones sin traslape, mostrar el horario original con aviso
        if (resultados.isEmpty()) {
            resultados.add(HorarioGenerado(1, todas, true, "Horario con traslape"))
        }

        val ordenadas = resultados
            .distinctBy { opcion -> opcion.materias.map { it.materia.id + it.grupo.id }.sorted().joinToString() }
            .sortedWith(compareBy(
                { it.tieneTraslape },
                { -it.materias.count { m -> m.esImportante } }
            ))

        return ordenadas.take(4).mapIndexed { i, h -> h.copy(id = i + 1) }
    }

    private fun intentarResolverCambiandoGrupos(
        importantes: List<MateriaSeleccionada>,
        normales: List<MateriaSeleccionada>
    ): List<HorarioGenerado> {
        val resultados = mutableListOf<HorarioGenerado>()
        val todas = importantes + normales

        // Encontrar materias en conflicto
        val enConflicto = encontrarMateriasEnConflicto(todas)


        val normalesEnConflicto = normales.filter { n ->
            enConflicto.any { it.materia.id == n.materia.id }
        }
        val normalesLibres = normales.filter { n ->
            !enConflicto.any { it.materia.id == n.materia.id }
        }

        for (normal in normalesEnConflicto) {
            for (grupoAlt in normal.materia.grupos) {
                if (grupoAlt.id == normal.grupo.id) continue // saltar el actual
                val candidato = normal.copy(grupo = grupoAlt)
                val nuevaLista = importantes + normalesLibres + listOf(candidato) +
                        normalesEnConflicto.filter { it.materia.id != normal.materia.id }
                if (!tieneTraslape(nuevaLista)) {
                    val nombreCorto = normal.materia.nombre.split(" ").take(3).joinToString(" ")
                    resultados.add(
                        HorarioGenerado(
                            id = resultados.size + 1,
                            materias = ordenarMaterias(nuevaLista, importantes),
                            tieneTraslape = false,
                            descripcion = "\"$nombreCorto\" en grupo ${grupoAlt.numeroGrupo}"
                        )
                    )
                }
                if (resultados.size >= 3) return resultados
            }
        }

        // También intentar cambiar grupos de las importantes en conflicto
        val importantesEnConflicto = importantes.filter { imp ->
            enConflicto.any { it.materia.id == imp.materia.id }
        }
        val importantesLibres = importantes.filter { imp ->
            !enConflicto.any { it.materia.id == imp.materia.id }
        }

        for (imp in importantesEnConflicto) {
            for (grupoAlt in imp.materia.grupos) {
                if (grupoAlt.id == imp.grupo.id) continue
                val candidato = imp.copy(grupo = grupoAlt, esImportante = true)
                val nuevaLista = importantesLibres + listOf(candidato) +
                        importantesEnConflicto.filter { it.materia.id != imp.materia.id } +
                        normales
                if (!tieneTraslape(nuevaLista)) {
                    val nombreCorto = imp.materia.nombre.split(" ").take(3).joinToString(" ")
                    resultados.add(
                        HorarioGenerado(
                            id = resultados.size + 1,
                            materias = ordenarMaterias(nuevaLista, importantes),
                            tieneTraslape = false,
                            descripcion = "\"$nombreCorto\" ⭐ en grupo ${grupoAlt.numeroGrupo}"
                        )
                    )
                }
                if (resultados.size >= 3) return resultados
            }
        }

        return resultados
    }

    private fun intentarResolverQuitandoNormales(
        importantes: List<MateriaSeleccionada>,
        normales: List<MateriaSeleccionada>
    ): List<HorarioGenerado> {
        val resultados = mutableListOf<HorarioGenerado>()
        val todas = importantes + normales
        val enConflicto = encontrarMateriasEnConflicto(todas)

        val normalesConflicto = normales.filter { n ->
            enConflicto.any { it.materia.id == n.materia.id }
        }

        for (aQuitar in normalesConflicto) {
            val sinEsta = todas.filter { it.materia.id != aQuitar.materia.id }
            if (!tieneTraslape(sinEsta)) {
                val nombreCorto = aQuitar.materia.nombre.split(" ").take(3).joinToString(" ")
                resultados.add(
                    HorarioGenerado(
                        id = resultados.size + 1,
                        materias = ordenarMaterias(sinEsta, importantes),
                        tieneTraslape = false,
                        descripcion = "Sin \"$nombreCorto\""
                    )
                )
            }
            if (resultados.size >= 2) break
        }

        return resultados
    }

    private fun ordenarMaterias(
        materias: List<MateriaSeleccionada>,
        importantes: List<MateriaSeleccionada>
    ): List<MateriaSeleccionada> {
        val importantesIds = importantes.map { it.materia.id }.toSet()
        return materias.sortedByDescending { importantesIds.contains(it.materia.id) }
    }

    private fun encontrarMateriasEnConflicto(
        materias: List<MateriaSeleccionada>
    ): List<MateriaSeleccionada> {
        val enConflicto = mutableSetOf<String>()
        for (i in materias.indices) {
            for (j in i + 1 until materias.size) {
                val a = materias[i]
                val b = materias[j]
                val choca = a.grupo.sesiones.any { sa ->
                    b.grupo.sesiones.any { sb -> sesionesChocan(sa, sb) }
                }
                if (choca) {
                    enConflicto.add(a.materia.id)
                    enConflicto.add(b.materia.id)
                }
            }
        }
        return materias.filter { enConflicto.contains(it.materia.id) }
    }

    fun tieneTraslape(materias: List<MateriaSeleccionada>): Boolean {
        for (i in materias.indices) {
            for (j in i + 1 until materias.size) {
                if (materias[i].materia.id == materias[j].materia.id) continue
                val choca = materias[i].grupo.sesiones.any { sa ->
                    materias[j].grupo.sesiones.any { sb -> sesionesChocan(sa, sb) }
                }
                if (choca) return true
            }
        }
        return false
    }

    private fun sesionesChocan(a: Sesion, b: Sesion): Boolean {
        if (a.dia != b.dia) return false
        val aInicio = horaAMinutos(a.horaInicio)
        val aFin    = horaAMinutos(a.horaFin)
        val bInicio = horaAMinutos(b.horaInicio)
        val bFin    = horaAMinutos(b.horaFin)
        return aInicio < bFin && bInicio < aFin
    }

    private fun horaAMinutos(hora: String): Int {
        val p = hora.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }
}