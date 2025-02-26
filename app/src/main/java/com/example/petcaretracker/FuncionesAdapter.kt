package com.example.petcaretracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        holder.textFuncion.text = listaFunciones[position]

        // Agregar el clic para abrir la actividad correspondiente
        holder.itemView.setOnClickListener {
            val contexto = holder.itemView.context
            val intent = when (listaFunciones[position]) {
                "Historial Médico" -> Intent(contexto, MedicoActivity::class.java)

                "Registro Médico" -> Intent(contexto, RegistroMedicoActivity::class.java)
                else -> null
            }
            intent?.let { contexto.startActivity(it) }
        }
    }

    override fun getItemCount(): Int = listaFunciones.size

    class FuncionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textFuncion: TextView = view.findViewById(R.id.textFuncion)
    }
}

