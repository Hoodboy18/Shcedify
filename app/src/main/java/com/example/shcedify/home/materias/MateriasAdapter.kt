package com.example.shcedify.home.materias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shcedify.core.model.Materia
import com.example.shcedify.databinding.ItemMateriaBinding

class MateriasAdapter(
    private val onItemClick: (Materia) -> Unit = {}
): ListAdapter<Materia, MateriasAdapter.MateriaViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MateriaViewHolder {
        val binding = ItemMateriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MateriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MateriaViewHolder(
        private val binding: ItemMateriaBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(materia: Materia) {
            binding.tvNombre.text = materia.nombre
            binding.tvClave.text = materia.clave
            binding.root.setOnClickListener { onItemClick(materia) }
        }
    }

    companion object {
        private val DIFF = object: DiffUtil.ItemCallback<Materia>() {
            override fun areItemsTheSame(oldItem: Materia, newItem: Materia) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Materia, newItem: Materia) =
                oldItem == newItem
        }
    }
}