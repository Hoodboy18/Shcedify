package com.example.shcedify.home.horario

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.shcedify.R
import com.example.shcedify.core.HorarioGenerator
import com.example.shcedify.core.MateriaSeleccionada
import com.example.shcedify.core.SeleccionManager
import com.example.shcedify.databinding.FragmentHorarioBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar
import com.example.shcedify.core.HorarioGenerado


class HorarioFragment : Fragment() {

    private var _binding: FragmentHorarioBinding? = null
    private val binding get() = _binding!!

    private val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    private val horas = listOf("07:00","08:00","09:00","10:00","11:00",
        "12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHorarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarEstadoInicial()
        binding.btnGenerar.setOnClickListener { generarHorario() }
    }

    override fun onResume() {
        super.onResume()
        mostrarEstadoInicial()
    }

    private fun mostrarEstadoInicial() {
        val seleccionadas = SeleccionManager.getSeleccionadas()
        val total = seleccionadas.size
        val importantes = SeleccionManager.getImportantes().size

        binding.tvResumen.text = if (total == 0) "Sin materias seleccionadas"
        else "$total materia${if (total != 1) "s" else ""} · $importantes importante${if (importantes != 1) "s" else ""}"

        binding.containerHorarios.removeAllViews()

        if (total == 0) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.btnGenerar.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility = View.GONE
        binding.btnGenerar.visibility = View.VISIBLE

        // Mostrar lista de materias seleccionadas
        val card = buildResumenSeleccionCard(seleccionadas)
        binding.containerHorarios.addView(card)
    }

    private fun generarHorario() {
        val importantes = SeleccionManager.getImportantes()
        val normales    = SeleccionManager.getNormales()
        val horarios = HorarioGenerator.generarHorarios(importantes, normales)
            .sortedWith(compareBy(
                { it.tieneTraslape },
                { -it.materias.count { m -> m.esImportante } }
            ))

        binding.containerHorarios.removeAllViews()

        if (horarios.isEmpty()) {
            binding.tvEmpty.text = "No se pudieron generar horarios.\nIntenta cambiar tu selección."
            binding.layoutEmpty.visibility = View.VISIBLE
            return
        }

        horarios.forEachIndexed { index, horario ->
            val dp = resources.displayMetrics.density

            // Título de la opción
            val tvTitulo = TextView(requireContext()).apply {
                text = if (horarios.size == 1) "Tu horario"
                else "Opción ${horario.id}: ${horario.descripcion}"
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.primary))
                setPadding(0,
                    if (index > 0) (16 * dp).toInt() else 0,
                    0, (8 * dp).toInt())
            }
            binding.containerHorarios.addView(tvTitulo)

            if (horario.tieneTraslape) {
                val tvAviso = TextView(requireContext()).apply {
                    text = "⚠️ Esta opción tiene traslape. Considera cambiar grupos."
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(context, R.color.error))
                    setPadding(0, 0, 0, (8 * dp).toInt())
                }
                binding.containerHorarios.addView(tvAviso)
            }

            val gridCard = buildGridCalendario(horario.materias)
            binding.containerHorarios.addView(gridCard)

            // Botón Seleccionar Horario
            val btnSeleccionar = com.google.android.material.button.MaterialButton(requireContext()).apply {
                text = "Seleccionar este horario"
                textSize = 14f
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (48 * dp).toInt()
                ).apply { setMargins(0, (8 * dp).toInt(), 0, (4 * dp).toInt()) }
                layoutParams = params
                cornerRadius = (12 * dp).toInt()
                setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
                setOnClickListener { guardarHorario(horario) }
            }
            binding.containerHorarios.addView(btnSeleccionar)
        }
    }

    private fun guardarHorario(horario: HorarioGenerado) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val materiasData = ArrayList<HashMap<String, Any>>()

        for (ms in horario.materias) {
            val sesionesData = ArrayList<HashMap<String, Any>>()
            for (s in ms.grupo.sesiones) {
                val sesion = HashMap<String, Any>()
                sesion["dia"]        = s.dia
                sesion["horaInicio"] = s.horaInicio
                sesion["horaFin"]    = s.horaFin
                sesion["salon"]      = s.salon
                sesion["edificio"]   = s.edificio
                sesionesData.add(sesion)
            }

            val materiaMap = HashMap<String, Any>()
            materiaMap["materiaId"]    = ms.materia.id
            materiaMap["clave"]        = ms.materia.clave
            materiaMap["nombre"]       = ms.materia.nombre
            materiaMap["creditos"]     = ms.materia.creditos
            materiaMap["area"]         = ms.materia.area
            materiaMap["tipo"]         = ms.materia.tipo
            materiaMap["esImportante"] = ms.esImportante
            materiaMap["grupoId"]      = ms.grupo.id
            materiaMap["numeroGrupo"]  = ms.grupo.numeroGrupo
            materiaMap["profesor"]     = ms.grupo.profesor
            materiaMap["sesiones"]     = sesionesData
            materiasData.add(materiaMap)
        }

        val data = HashMap<String, Any>()
        data["horarioGuardado"] = materiasData
        data["descripcion"]     = horario.descripcion
        data["fechaGuardado"]   = com.google.firebase.Timestamp.now()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(data as Map<String, Any>, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Snackbar.make(
                    binding.root,
                    "✓ Horario guardado en tu cuenta",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    "Error al guardar: ${e.localizedMessage}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun buildResumenSeleccionCard(seleccionadas: List<MateriaSeleccionada>): View {
        val card = MaterialCardView(requireContext()).apply {
            radius = 16f * resources.displayMetrics.density
            cardElevation = 4f * resources.displayMetrics.density
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
            }
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvTitulo = TextView(requireContext()).apply {
            text = "Materias seleccionadas"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
        }
        container.addView(tvTitulo)

        seleccionadas.forEach { ms ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, (10 * resources.displayMetrics.density).toInt())
            }

            val badge = TextView(requireContext()).apply {
                text = if (ms.esImportante) "⭐" else "•"
                textSize = 14f
                setPadding(0, 0, (8 * resources.displayMetrics.density).toInt(), 0)
            }
            row.addView(badge)

            val info = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvNombre = TextView(requireContext()).apply {
                text = ms.materia.nombre
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.on_surface))
            }
            info.addView(tvNombre)

            val tvGrupo = TextView(requireContext()).apply {
                text = "Grupo ${ms.grupo.numeroGrupo} · ${ms.grupo.profesor}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
            info.addView(tvGrupo)

            row.addView(info)
            container.addView(row)
        }

        card.addView(container)
        return card
    }

    private fun buildGridCalendario(materias: List<MateriaSeleccionada>): View {
        val dp = resources.displayMetrics.density

        val card = MaterialCardView(requireContext()).apply {
            radius = 16f * dp
            cardElevation = 4f * dp
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val scroll = android.widget.HorizontalScrollView(requireContext()).apply {
            isHorizontalScrollBarEnabled = false
        }

        val table = TableLayout(requireContext()).apply {
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
        }

        // Colores por materia
        val colores = listOf(
            "#E3F2FD", "#F3E5F5", "#E8F5E9", "#FFF3E0", "#FCE4EC",
            "#E0F7FA", "#F9FBE7"
        )
        val colorMap = materias.mapIndexed { i, ms ->
            ms.materia.id to colores[i % colores.size]
        }.toMap()

        // Header de días
        val headerRow = TableRow(requireContext())
        headerRow.addView(makeCell("", 60, true, "#FFFFFF"))
        val diasPresentes = dias.filter { dia ->
            materias.any { ms -> ms.grupo.sesiones.any { it.dia == dia } }
        }
        diasPresentes.forEach { dia ->
            headerRow.addView(makeCell(dia.take(3), 70, true, "#1A3A6B", "#FFFFFF"))
        }
        table.addView(headerRow)

        // Filas de horas
        for (i in 0 until horas.size - 1) {
            val horaInicio = horas[i]
            val horaFin    = horas[i + 1]
            val row = TableRow(requireContext())
            row.addView(makeCell("$horaInicio", 60, false, "#F5F7FA"))

            diasPresentes.forEach { dia ->
                val materiasEnSlot = materias.filter { ms ->
                    ms.grupo.sesiones.any { s ->
                        s.dia == dia &&
                                horaAMinutos(s.horaInicio) <= horaAMinutos(horaInicio) &&
                                horaAMinutos(s.horaFin) > horaAMinutos(horaInicio)
                    }
                }
                if (materiasEnSlot.isNotEmpty()) {
                    val ms = materiasEnSlot.first()
                    val color = colorMap[ms.materia.id] ?: "#E3F2FD"
                    val abrev = ms.materia.nombre.split(" ")
                        .take(2).joinToString(" ") { it.take(4) }
                    row.addView(makeCell(abrev, 70, false, color, "#1A3A6B"))
                } else {
                    row.addView(makeCell("", 70, false, "#FFFFFF"))
                }
            }
            table.addView(row)
        }

        scroll.addView(table)
        card.addView(scroll)
        return card
    }

    private fun makeCell(
        text: String, widthDp: Int, isHeader: Boolean,
        bgColor: String, textColor: String = "#333333"
    ): TextView {
        val dp = resources.displayMetrics.density
        return TextView(requireContext()).apply {
            this.text = text
            textSize = if (isHeader) 11f else 10f
            setTypeface(null, if (isHeader) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(android.graphics.Color.parseColor(textColor))
            setBackgroundColor(android.graphics.Color.parseColor(bgColor))
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(
                (widthDp * dp).toInt(),
                (32 * dp).toInt()
            )
            setPadding((2 * dp).toInt(), (2 * dp).toInt(), (2 * dp).toInt(), (2 * dp).toInt())
        }
    }

    private fun horaAMinutos(hora: String): Int {
        val p = hora.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}