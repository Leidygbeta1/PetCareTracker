package com.example.petcaretracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.owner.AlimentacionActivity
import com.example.petcaretracker.owner.CarnetVacunacionActivity
import com.example.petcaretracker.owner.HistorialMedicoActivity
import com.example.petcaretracker.owner.RecordatorioVacunasActivity
import com.example.petcaretracker.owner.RegistroMedicoActivity
import com.example.petcaretracker.veterinario.EstadisticasVeterinarioActivity
import com.example.petcaretracker.veterinario.HorariosVeterinarioActivity
import com.example.petcaretracker.veterinario.MensajesVeterinarioActivity
import com.example.petcaretracker.veterinario.ReputacionVeterinarioActivity
import com.example.petcaretracker.veterinario.ActualizarCarnetActivity
import com.example.petcaretracker.veterinario.AgendaCitasActivity
import com.example.petcaretracker.veterinario.ConsultarHistorialActivity
import com.example.petcaretracker.veterinario.RegistrarAtencionActivity

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
            "Trackeo de Recorridos" -> R.drawable.ic_location
            "Alimentación Proporcional a la mascota" -> R.drawable.ic_food
            "Mejores Estéticas y Veterinarias Certificadas" -> R.drawable.ic_veterinaria
            "Compartir ubicación" -> R.drawable.ic_share_location
            "Historial Médico" -> R.drawable.ic_historial
            "Recordatorio Vacunas" -> R.drawable.ic_vacunas
            "Registro Médico" -> R.drawable.ic_registro_medico
            "Carnet de Vacunacion" -> R.drawable.ic_carnet_vacunacion

            // Nuevas funciones veterinario
            "Estadísticas de Atenciones" -> R.drawable.ic_estadisticas
            "Mensajes de Propietarios" -> R.drawable.ic_mensajes
            "Horarios de Atención" -> R.drawable.ic_horario
            "Reputación y Comentarios" -> R.drawable.ic_reputacion

            // Médicas veterinario
            "Consultar Historial Médico" -> R.drawable.ic_historial2
            "Registrar Atención Médica" -> R.drawable.ic_registro_medico2
            "Ver Agenda de Citas" -> R.drawable.ic_agenda
            "Actualizar Carnet de Vacunación" -> R.drawable.ic_carnet_vacunacion

            else -> R.drawable.ic_food
        }
        holder.imagenFuncion.setImageResource(imagenRes)

        // Clic en cada tarjeta
        holder.itemView.setOnClickListener {
            val contexto = holder.itemView.context
            val intent = when (funcion) {
                "Historial Médico" -> Intent(contexto, HistorialMedicoActivity::class.java)
                "Registro Médico" -> Intent(contexto, RegistroMedicoActivity::class.java)
                "Recordatorio Vacunas" -> Intent(contexto, RecordatorioVacunasActivity::class.java)
                "Carnet de Vacunacion" -> Intent(contexto, CarnetVacunacionActivity::class.java)
                "Alimentación Proporcional a la mascota" -> Intent(contexto, AlimentacionActivity::class.java)

                // Nuevas funciones veterinario
                "Estadísticas de Atenciones" -> Intent(contexto, EstadisticasVeterinarioActivity::class.java)
                "Mensajes de Propietarios" -> Intent(contexto, MensajesVeterinarioActivity::class.java)
                "Horarios de Atención" -> Intent(contexto, HorariosVeterinarioActivity::class.java)
                "Reputación y Comentarios" -> Intent(contexto, ReputacionVeterinarioActivity::class.java)

                // Médicas veterinario
                "Consultar Historial Médico" -> Intent(contexto, ConsultarHistorialActivity::class.java)
                "Registrar Atención Médica" -> Intent(contexto, RegistrarAtencionActivity::class.java)
                "Ver Agenda de Citas" -> Intent(contexto, AgendaCitasActivity::class.java)
                "Actualizar Carnet de Vacunación" -> Intent(contexto, ActualizarCarnetActivity::class.java)

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
