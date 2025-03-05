package com.example.petcaretracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FuncionesAdapter(private val listaFunciones: List<String>) :
    RecyclerView.Adapter<FuncionesAdapter.FuncionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuncionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_funcion, parent, false)
        return FuncionViewHolder(view)
    }

    override fun onBindViewHolder(holder: FuncionViewHolder, position: Int) {
        val funcion = listaFunciones[position]
        holder.textFuncion.text = funcion

        // Asignar imágenes según la funcionalidad
        val imagenRes = when (funcion) {
            "Trackeo de Recorridos" -> R.drawable.ic_location // Cambia esto por tus íconos
            "Alimentación Proporcional a la mascota" -> R.drawable.ic_food // Cambia esto por tus íconos
            "Mejores Estéticas y Veterinarias Certificadas" -> R.drawable.ic_veterinaria // Cambia esto por tus íconos
            "Compartir ubicación" -> R.drawable.ic_share_location // Cambia esto por tus íconos
            "Historial Médico" -> R.drawable.ic_historial
            "Recordatorio Vacunas" -> R.drawable.ic_vacunas
            "Registro Médico" -> R.drawable.ic_registro_medico
            "Carnet de Vacunacion" -> R.drawable.ic_carnet_vacunacion
            else -> R.drawable.ic_food  // Predeterminado
        }
        holder.imagenFuncion.setImageResource(imagenRes)

        // Clic en cada tarjeta
        holder.itemView.setOnClickListener {
            val contexto = holder.itemView.context
            val intent = when (funcion) {
                "Historial Médico" -> Intent(contexto, HistorialMedicoActivity::class.java)
                "Registro Médico" -> Intent(contexto, RegistroMedicoActivity::class.java)
                else -> null
            }
            intent?.let { contexto.startActivity(it) }
        }
    }

    override fun getItemCount(): Int = listaFunciones.size

    class FuncionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textFuncion: TextView = view.findViewById(R.id.textFuncion)
        val imagenFuncion: ImageView = view.findViewById(R.id.imagenFuncion)
    }
}

