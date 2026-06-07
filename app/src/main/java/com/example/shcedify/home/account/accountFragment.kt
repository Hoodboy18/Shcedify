package com.example.shcedify.home.account

import android.content.Intent
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.shcedify.databinding.FragmentAccountBinding
import com.example.shcedify.onboarding.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class accountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val dias = listOf("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado")
    private val horas = listOf("07:00","08:00","09:00","10:00","11:00",
        "12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00")
    private val colores = listOf(
        "#E3F2FD","#F3E5F5","#E8F5E9","#FFF3E0","#FCE4EC","#E0F7FA","#F9FBE7"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email ?: "—"

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val firstName  = doc.getString("firstName")  ?: ""
                    val lastName   = doc.getString("lastName")   ?: ""
                    val numCuenta  = doc.getString("numCuenta")  ?: "—"
                    val carrera    = doc.getString("carrera")    ?: "—"
                    val phone      = doc.getString("phone")      ?: "—"
                    val birthDate  = doc.getString("birthDate")  ?: "—"

                    val fullName = "$firstName $lastName".trim()
                    binding.tvNombre.text    = fullName.ifEmpty { "—" }
                    binding.tvCarrera.text   = carrera
                    binding.tvNumCuenta.text = numCuenta
                    binding.tvPhone.text     = phone
                    binding.tvBirthdate.text = birthDate

                    val initials = buildString {
                        if (firstName.isNotEmpty()) append(firstName.first().uppercaseChar())
                        if (lastName.isNotEmpty())  append(lastName.first().uppercaseChar())
                    }
                    binding.tvInitials.text = initials.ifEmpty { "?" }

                    // Cargar horario guardado si existe
                    @Suppress("UNCHECKED_CAST")
                    val horarioData = doc.get("horarioGuardado") as? List<Map<String, Any>>
                    val descripcion = doc.getString("descripcion") ?: ""
                    if (!horarioData.isNullOrEmpty()) {
                        mostrarHorarioGuardado(horarioData, descripcion)
                    }
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mostrarHorarioGuardado(
        materiasData: List<Map<String, Any>>,
        descripcion: String
    ) {
        binding.cardHorario.isVisible = true
        binding.tvHorarioDescripcion.text = descripcion.ifEmpty { "Horario seleccionado" }

        // Construir grid
        binding.containerGridHorario.removeAllViews()
        val table = buildGridDesdeFirestore(materiasData)
        binding.containerGridHorario.addView(table)

        // Construir lista de materias
        binding.containerMateriasHorario.removeAllViews()
        materiasData.forEachIndexed { index, m ->
            val itemView = buildMateriaItemCuenta(m, index)
            binding.containerMateriasHorario.addView(itemView)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildGridDesdeFirestore(materiasData: List<Map<String, Any>>): TableLayout {
        val dp = resources.displayMetrics.density
        val colorMap = materiasData.mapIndexed { i, m ->
            (m["materiaId"] as? String ?: "") to colores[i % colores.size]
        }.toMap()

        val diasPresentes = dias.filter { dia ->
            materiasData.any { m ->
                val sesiones = m["sesiones"] as? List<Map<String, Any>> ?: emptyList()
                sesiones.any { it["dia"] == dia }
            }
        }

        val table = TableLayout(requireContext()).apply {
            setPadding((4 * dp).toInt(), (4 * dp).toInt(), (4 * dp).toInt(), (4 * dp).toInt())
        }

        // Header
        val headerRow = TableRow(requireContext())
        headerRow.addView(makeCell("", 52, true, "#FFFFFF"))
        diasPresentes.forEach { dia ->
            headerRow.addView(makeCell(dia.take(3), 62, true, "#1A3A6B", "#FFFFFF"))
        }
        table.addView(headerRow)

        // Filas de horas
        for (i in 0 until horas.size - 1) {
            val horaInicio = horas[i]
            val row = TableRow(requireContext())
            row.addView(makeCell(horaInicio, 52, false, "#F5F7FA"))

            diasPresentes.forEach { dia ->
                val materia = materiasData.firstOrNull { m ->
                    val sesiones = m["sesiones"] as? List<Map<String, Any>> ?: emptyList()
                    sesiones.any { s ->
                        s["dia"] == dia &&
                                horaAMinutos(s["horaInicio"] as? String ?: "00:00") <= horaAMinutos(horaInicio) &&
                                horaAMinutos(s["horaFin"] as? String ?: "00:00") > horaAMinutos(horaInicio)
                    }
                }
                if (materia != null) {
                    val id = materia["materiaId"] as? String ?: ""
                    val color = colorMap[id] ?: "#E3F2FD"
                    val abrev = (materia["nombre"] as? String ?: "")
                        .split(" ").take(2).joinToString(" ") { it.take(4) }
                    row.addView(makeCell(abrev, 62, false, color, "#1A3A6B"))
                } else {
                    row.addView(makeCell("", 62, false, "#FFFFFF"))
                }
            }
            table.addView(row)
        }

        return table
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildMateriaItemCuenta(m: Map<String, Any>, index: Int): View {
        val dp = resources.displayMetrics.density
        val color = colores[index % colores.size]

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor(color))
            val r = (12 * dp).toInt()
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor(color))
                cornerRadius = 12 * dp
            }
            val mb = (10 * dp).toInt()
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, mb) }
            layoutParams = params
            setPadding((12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt())
        }

        val esImportante = m["esImportante"] as? Boolean ?: false
        val nombre = (m["nombre"] as? String ?: "") + if (esImportante) " ⭐" else ""

        val tvNombre = TextView(requireContext()).apply {
            text = nombre
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#1A3A6B"))
        }
        container.addView(tvNombre)

        val tvClave = TextView(requireContext()).apply {
            text = "Clave: ${m["clave"] as? String ?: "—"}  ·  ${m["creditos"]} créditos  ·  ${m["area"] as? String ?: "—"}"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#444444"))
            setPadding(0, (4 * dp).toInt(), 0, 0)
        }
        container.addView(tvClave)

        val tvGrupo = TextView(requireContext()).apply {
            text = "Grupo ${m["numeroGrupo"] as? String ?: "—"}  ·  ${m["profesor"] as? String ?: "—"}"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#444444"))
            setPadding(0, (2 * dp).toInt(), 0, 0)
        }
        container.addView(tvGrupo)

        val sesiones = m["sesiones"] as? List<Map<String, Any>> ?: emptyList()
        val sesionesText = sesiones.joinToString("  |  ") { s ->
            "${(s["dia"] as? String ?: "").take(3)} ${s["horaInicio"]}–${s["horaFin"]}"
        }
        val tvSesiones = TextView(requireContext()).apply {
            text = sesionesText
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextColor(android.graphics.Color.parseColor("#555555"))
            setPadding(0, (4 * dp).toInt(), 0, 0)
        }
        container.addView(tvSesiones)

        return container
    }

    private fun makeCell(
        text: String, widthDp: Int, isHeader: Boolean,
        bgColor: String, textColor: String = "#333333"
    ): TextView {
        val dp = resources.displayMetrics.density
        return TextView(requireContext()).apply {
            this.text = text
            textSize = if (isHeader) 10f else 9f
            setTypeface(null, if (isHeader) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(android.graphics.Color.parseColor(textColor))
            setBackgroundColor(android.graphics.Color.parseColor(bgColor))
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(
                (widthDp * dp).toInt(),
                (28 * dp).toInt()
            )
            setPadding((2 * dp).toInt(), (2 * dp).toInt(), (2 * dp).toInt(), (2 * dp).toInt())
        }
    }

    private fun horaAMinutos(hora: String): Int {
        val p = hora.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}