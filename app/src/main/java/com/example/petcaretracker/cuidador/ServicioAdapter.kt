package com.example.petcaretracker.cuidador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.R

class ServicioAdapter(private val servicios: List<Map<String, Any>>) :
    RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder>() {

    class ServicioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipo: TextView = view.findViewById(R.id.tvTipoServicio)
        val tvComentario: TextView = view.findViewById(R.id.tvComentarioServicio)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaServicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        return ServicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        val servicio = servicios[position]
        holder.tvTipo.text = "Tipo: ${servicio["tipo"] ?: "Desconocido"}"
        holder.tvComentario.text = "Comentario: ${servicio["comentario"] ?: "Sin comentario"}"
        holder.tvFecha.text = "Fecha: ${servicio["fecha"] ?: "Sin fecha"}"
    }

    override fun getItemCount(): Int = servicios.size
}
