package com.example.shcedify.home.materias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shcedify.R
import com.example.shcedify.core.model.Materia
import com.example.shcedify.databinding.ItemMateriaBinding
import androidx.core.content.ContextCompat
import com.example.shcedify.core.SeleccionManager

class MateriasAdapter(
    private val onClick: (Materia) -> Unit
) : ListAdapter<Materia, MateriasAdapter.MateriaViewHolder>(DiffCallback) {

    inner class MateriaViewHolder(private val binding: ItemMateriaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(materia: Materia) {
            binding.tvNombre.text   = materia.nombre
            binding.tvClave.text    = materia.clave
            binding.tvArea.text     = materia.area
            binding.tvCreditos.text = "${materia.creditos} créditos"
            binding.root.setOnClickListener { onClick(materia) }

            val badgeColor = if (materia.tipo == "Optativa") {
                android.graphics.Color.parseColor("#2E7D32")
            } else {
                binding.root.context.getColor(R.color.primary)
            }
            binding.badgeContainer.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(badgeColor)
            )

            // Color de fondo si está seleccionada
            if (SeleccionManager.estaAgregada(materia.id)) {
                binding.cardMateria.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#E8F5E9")
                )
                binding.cardMateria.strokeColor =
                    android.graphics.Color.parseColor("#2E7D32")
                binding.cardMateria.strokeWidth = 2
            } else {
                binding.cardMateria.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.surface)
                )
                binding.cardMateria.strokeWidth = 0
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val binding = ItemMateriaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MateriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Materia>() {
        override fun areItemsTheSame(a: Materia, b: Materia) = a.id == b.id
        override fun areContentsTheSame(a: Materia, b: Materia) = a == b
    }
}