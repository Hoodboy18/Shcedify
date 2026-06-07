package com.example.shcedify.home.materias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shcedify.core.model.Materia
import com.example.shcedify.databinding.ItemMateriaBinding

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