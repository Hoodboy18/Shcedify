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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shcedify.core.FragmentCommunicator
import com.example.shcedify.core.ResponseService
import com.example.shcedify.databinding.FragmentAccountBinding
import com.example.shcedify.onboarding.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class accountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var communicator: FragmentCommunicator

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
        communicator = requireActivity() as FragmentCommunicator
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres salir de Schedify?")
                .setPositiveButton("Sí, salir") { _, _ -> logout() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        observeState()
        viewModel.load()
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.accountState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            bindData(state.data)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun bindData(data: AccountData) {
        val p = data.profile
        val fullName = "${p.firstName} ${p.lastName}".trim()
        binding.tvNombre.text    = fullName.ifEmpty { "—" }
        binding.tvCarrera.text   = p.carrera.ifEmpty { "—" }
        binding.tvNumCuenta.text = p.numCuenta.ifEmpty { "—" }
        binding.tvEmail.text     = data.email
        binding.tvPhone.text     = p.phone.ifEmpty { "—" }
        binding.tvBirthdate.text = p.birthDate.ifEmpty { "—" }

        val initials = buildString {
            if (p.firstName.isNotEmpty()) append(p.firstName.first().uppercaseChar())
            if (p.lastName.isNotEmpty())  append(p.lastName.first().uppercaseChar())
        }
        binding.tvInitials.text = initials.ifEmpty { "?" }

        if (data.horarioGuardado.isNotEmpty()) {
            mostrarHorarioGuardado(data.horarioGuardado, data.descripcionHorario)
        } else {
            binding.cardHorario.isVisible = false
        }
    }

    private fun mostrarHorarioGuardado(
        materiasData: List<Map<String, Any>>,
        descripcion: String
    ) {
        binding.cardHorario.isVisible = true
        binding.tvHorarioDescripcion.text = descripcion.ifEmpty { "Horario seleccionado" }

        binding.containerGridHorario.removeAllViews()
        val table = buildGridDesdeFirestore(materiasData)
        binding.containerGridHorario.addView(table)

        binding.containerMateriasHorario.removeAllViews()
        materiasData.forEachIndexed { index, m ->
            binding.containerMateriasHorario.addView(buildMateriaItem(m, index))
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
            setPadding((4*dp).toInt(), (4*dp).toInt(), (4*dp).toInt(), (4*dp).toInt())
        }

        val headerRow = TableRow(requireContext())
        headerRow.addView(makeCell("", 52, true, "#FFFFFF"))
        diasPresentes.forEach { dia ->
            headerRow.addView(makeCell(dia.take(3), 62, true, "#1A3A6B", "#FFFFFF"))
        }
        table.addView(headerRow)

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
    private fun buildMateriaItem(m: Map<String, Any>, index: Int): View {
        val dp = resources.displayMetrics.density
        val color = colores[index % colores.size]

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor(color))
                cornerRadius = 12 * dp
            }
            val mb = (10 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, mb) }
            setPadding((12*dp).toInt(), (12*dp).toInt(), (12*dp).toInt(), (12*dp).toInt())
        }

        val esImportante = m["esImportante"] as? Boolean ?: false
        val nombre = (m["nombre"] as? String ?: "") + if (esImportante) " ⭐" else ""

        listOf(
            nombre to Pair(14f, Typeface.BOLD),
            "Clave: ${m["clave"]}  ·  ${m["creditos"]} créditos  ·  ${m["area"]}" to Pair(12f, Typeface.NORMAL),
            "Grupo ${m["numeroGrupo"]}  ·  ${m["profesor"]}" to Pair(12f, Typeface.NORMAL)
        ).forEachIndexed { i, (text, style) ->
            container.addView(TextView(requireContext()).apply {
                this.text = text
                textSize = style.first
                setTypeface(null, style.second)
                setTextColor(android.graphics.Color.parseColor("#1A3A6B"))
                if (i > 0) setPadding(0, (3*dp).toInt(), 0, 0)
            })
        }

        val sesiones = m["sesiones"] as? List<Map<String, Any>> ?: emptyList()
        container.addView(TextView(requireContext()).apply {
            text = sesiones.joinToString("  |  ") { s ->
                "${(s["dia"] as? String ?: "").take(3)} ${s["horaInicio"]}–${s["horaFin"]}"
            }
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextColor(android.graphics.Color.parseColor("#555555"))
            setPadding(0, (4*dp).toInt(), 0, 0)
        })

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
            layoutParams = TableRow.LayoutParams((widthDp*dp).toInt(), (28*dp).toInt())
            setPadding((2*dp).toInt(), (2*dp).toInt(), (2*dp).toInt(), (2*dp).toInt())
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