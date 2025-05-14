package com.example.petcaretracker.veterinario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.R
import com.example.petcaretracker.model.Cita

class CitaAdapter(
    private val listaCitas: List<Cita>,
    private val onMarcarRealizada: (Cita) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDuenio: TextView = view.findViewById(R.id.tvDuenio)
        val tvHora: TextView = view.findViewById(R.id.tvHora)
        val tvMotivo: TextView = view.findViewById(R.id.tvMotivo)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val btnRealizada: Button = view.findViewById(R.id.btnRealizada)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]
        holder.tvDuenio.text = "Dueño: ${cita.duenioNombre}"
        holder.tvHora.text = "Hora: ${cita.hora}"
        holder.tvMotivo.text = "Motivo: ${cita.motivo}"
        holder.tvEstado.text = "Estado: ${cita.estado}"

        if (cita.estado == "Realizada") {
            holder.btnRealizada.visibility = View.GONE
        } else {
            holder.btnRealizada.visibility = View.VISIBLE
            holder.btnRealizada.setOnClickListener {
                onMarcarRealizada(cita)
            }
        }
    }

    override fun getItemCount(): Int = listaCitas.size
}
