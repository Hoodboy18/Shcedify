package com.example.shcedify.home.materias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shcedify.R
import com.example.shcedify.core.SeleccionManager
import com.example.shcedify.core.model.Grupo
import com.example.shcedify.core.model.Materia
import com.example.shcedify.databinding.FragmentMateriaDetailBinding
import com.google.android.material.snackbar.Snackbar

class MateriaDetailFragment : Fragment() {

    private var _binding: FragmentMateriaDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var materia: Materia
    private var grupoSeleccionado: Grupo? = null

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

        // Preseleccionar el grupo que ya tenía elegido (si existe)
        grupoSeleccionado = SeleccionManager.getGrupoSeleccionado(materia.id)
            ?: materia.grupos.firstOrNull()

        bindMateriaInfo()
        setupButtons()
        updateUI()
    }

    private fun bindMateriaInfo() {
        binding.tvNombre.text      = materia.nombre
        binding.tvDescripcion.text = materia.descripcion
        binding.tvClave.text       = "Clave: ${materia.clave}"
        binding.tvCreditos.text    = "${materia.creditos} créditos"
        binding.tvSemestre.text    = "Semestre ${materia.semestrePlan}"
        binding.tvModalidad.text   = materia.modalidad
        binding.tvArea.text        = materia.area
        binding.tvTipo.text        = materia.tipo

        binding.containerGrupos.removeAllViews()
        materia.grupos.forEach { grupo ->
            val grupoView = buildGrupoView(grupo)
            binding.containerGrupos.addView(grupoView)
        }
    }

    private fun buildGrupoView(grupo: Grupo): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_grupo, binding.containerGrupos, false)

        view.findViewById<TextView>(R.id.tvGrupoNum).text  = "Grupo ${grupo.numeroGrupo}"
        view.findViewById<TextView>(R.id.tvEstado).text    = grupo.estado
        view.findViewById<TextView>(R.id.tvProfesorGrupo).text = grupo.profesor
        view.findViewById<TextView>(R.id.tvCupoGrupo).text =
            "Cupo: ${grupo.cupoDisponible}/${grupo.cupoMaximo} disponibles"

        val sesionesText = grupo.sesiones.joinToString("\n") { s ->
            "${s.dia.padEnd(10)} ${s.horaInicio}–${s.horaFin}  |  ${s.salon}, Edif. ${s.edificio}"
        }
        view.findViewById<TextView>(R.id.tvSesionesGrupo).text = sesionesText

        // Resaltar el grupo seleccionado
        updateGrupoViewSelection(view, grupo.id == grupoSeleccionado?.id)

        // Click para seleccionar este grupo
        view.setOnClickListener {
            grupoSeleccionado = grupo
            // Actualizar visual de todos los grupos
            for (i in 0 until binding.containerGrupos.childCount) {
                val child = binding.containerGrupos.getChildAt(i)
                val isSelected = materia.grupos[i].id == grupo.id
                updateGrupoViewSelection(child, isSelected)
            }
            // Si ya estaba agregada, actualizar el grupo
            if (SeleccionManager.estaAgregada(materia.id)) {
                SeleccionManager.quitarMateria(materia.id)
                SeleccionManager.agregarMateria(materia, grupo)
            }
            updateUI()
        }

        return view
    }

    private fun updateGrupoViewSelection(view: View, selected: Boolean) {
        view.setBackgroundResource(
            if (selected) R.drawable.bg_grupo_selected else R.drawable.bg_grupo_item
        )
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.chipImportante.isChecked = SeleccionManager.esImportante(materia.id)
        binding.chipImportante.setOnCheckedChangeListener { _, isChecked ->
            val grupo = grupoSeleccionado ?: materia.grupos.firstOrNull() ?: return@setOnCheckedChangeListener
            if (isChecked) {
                val ok = SeleccionManager.marcarImportante(materia, grupo)
                if (!ok) {
                    binding.chipImportante.isChecked = false
                    Snackbar.make(binding.root, "Máximo 3 materias importantes", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                SeleccionManager.desmarcarImportante(materia.id)
            }
            updateUI()
        }

        binding.btnAgregar.setOnClickListener {
            val grupo = grupoSeleccionado ?: materia.grupos.firstOrNull() ?: return@setOnClickListener
            if (SeleccionManager.estaAgregada(materia.id)) {
                SeleccionManager.quitarMateria(materia.id)
                Snackbar.make(binding.root, "Materia eliminada de tu selección", Snackbar.LENGTH_SHORT).show()
            } else {
                when (SeleccionManager.agregarMateria(materia, grupo)) {
                    SeleccionManager.ResultadoAgregar.OK ->
                        Snackbar.make(binding.root, "Materia agregada ✓", Snackbar.LENGTH_SHORT).show()
                    SeleccionManager.ResultadoAgregar.LIMITE_TOTAL ->
                        Snackbar.make(binding.root, "Ya seleccionaste 7 materias", Snackbar.LENGTH_SHORT).show()
                    SeleccionManager.ResultadoAgregar.YA_EXISTE -> { }
                }
            }
            updateUI()
        }
    }

    private fun updateUI() {
        val agregada = SeleccionManager.estaAgregada(materia.id)
        binding.chipImportante.isChecked = SeleccionManager.esImportante(materia.id)

        if (agregada) {
            binding.btnAgregar.text = "Quitar de mi selección"
            binding.btnAgregar.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.error)
                )
        } else {
            binding.btnAgregar.text = "Agregar a mi selección"
            binding.btnAgregar.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            val total = SeleccionManager.totalSeleccionadas()
            binding.btnAgregar.isEnabled = total < 7
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}