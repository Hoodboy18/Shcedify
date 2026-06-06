package com.example.shcedify.home.materias

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.shcedify.core.model.Materia
import com.example.shcedify.databinding.FragmentMateriaDetailBinding

class MateriaDetailFragment : Fragment() {

    private var _binding: FragmentMateriaDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var materia: Materia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        materia = requireArguments().getParcelable("materia")
            ?: error("Materia argument required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMateriaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindMateriaInfo()
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun bindMateriaInfo() {
        binding.tvNombre.text = materia.nombre
        binding.tvClave.text = "Clave: ${materia.clave}"
        binding.tvCreditos.text = "Créditos: ${materia.creditos}"
        binding.tvArea.text = "Área: ${materia.area}"
        binding.tvSemestre.text = "Semestre plan: ${materia.semestrePlan}"

        if (materia.grupos.isNotEmpty()) {
            val grupo = materia.grupos.first()
            binding.tvProfesor.text = "Profesor: ${grupo.profesor}"
            binding.tvGrupo.text = "Grupo: ${grupo.numeroGrupo}"
            binding.tvCupo.text = "Cupo disponible: ${grupo.cupoDisponible}/${grupo.cupoMaximo}"

            val sesionesText = grupo.sesiones.joinToString("\n") { s ->
                "${s.dia}  ${s.horaInicio}–${s.horaFin}  |  Salón ${s.salon}, Edif. ${s.edificio}"
            }
            binding.tvSesiones.text = if (sesionesText.isNotEmpty()) sesionesText else "Sin sesiones registradas"
        } else {
            binding.tvProfesor.text = "Profesor: —"
            binding.tvGrupo.text = "Grupo: —"
            binding.tvCupo.text = "Cupo: —"
            binding.tvSesiones.text = "Sin grupos registrados"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}