package com.example.petcaretracker.veterinario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcaretracker.R

class MascotasVeterinarioAdapter(
    private val listaMascotas: List<MascotaVeterinario>
) : RecyclerView.Adapter<MascotasVeterinarioAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreMascota)
        val tvTipo: TextView = itemView.findViewById(R.id.tvTipoMascota)
        val tvRaza: TextView = itemView.findViewById(R.id.tvRazaMascota)
        val tvDueno: TextView = itemView.findViewById(R.id.tvDuenoMascota)
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFotoMascota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mascota_veterinario, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = listaMascotas[position]
        holder.tvNombre.text = "Nombre: ${mascota.nombre}"
        holder.tvTipo.text = "Tipo: ${mascota.tipo}"
        holder.tvRaza.text = "Raza: ${mascota.raza}"
        holder.tvDueno.text = "Dueño: ${mascota.nombreDueno}"

        if (mascota.foto.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(mascota.foto)
                .into(holder.ivFoto)
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_pet)
        }
    }

    override fun getItemCount(): Int = listaMascotas.size
}
