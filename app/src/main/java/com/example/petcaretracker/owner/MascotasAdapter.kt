package com.example.petcaretracker.owner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcaretracker.Mascota
import com.example.petcaretracker.R

class MascotasAdapter(
    private val mascotasList: List<Mascota>,
    private val context: Context,
    private val onItemClick: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotasAdapter.MascotaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = mascotasList[position]
        holder.nombre.text = mascota.nombre
        holder.raza.text   = mascota.raza
        holder.tipo.text   = mascota.tipo

        // Mostrar foto
        if (mascota.fotoUrl.isNotEmpty()) {
            Glide.with(context)
                .load(mascota.fotoUrl)
                .into(holder.foto)
        }

        // Mostrar cuidador si existe
        if (!mascota.cuidadorNombre.isNullOrEmpty()) {
            holder.cuidador.text = "Cuidador: ${mascota.cuidadorNombre}"
            holder.cuidador.visibility = View.VISIBLE
        } else {
            holder.cuidador.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(mascota)
        }
    }

    override fun getItemCount(): Int = mascotasList.size

    class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView      = itemView.findViewById(R.id.textNombreMascota)
        val raza: TextView        = itemView.findViewById(R.id.textRazaMascota)
        val tipo: TextView        = itemView.findViewById(R.id.textTipoMascota)
        val foto: ImageView       = itemView.findViewById(R.id.imageMascota)
        val cuidador: TextView    = itemView.findViewById(R.id.textCuidador)
    }
}

