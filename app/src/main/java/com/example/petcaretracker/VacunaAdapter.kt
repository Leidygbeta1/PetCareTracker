package com.example.petcaretracker



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VacunaAdapter(private val listaVacunas: List<Map<String, Any>>) :
    RecyclerView.Adapter<VacunaAdapter.VacunaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacunaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vacuna, parent, false)
        return VacunaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VacunaViewHolder, position: Int) {
        val vacuna = listaVacunas[position]
        holder.nombre.text = vacuna["nombre"].toString()
        holder.fechaAplicacion.text = "Aplicada: ${vacuna["fechaAplicacion"]}"
        holder.proximaDosis.text = "Próxima dosis: ${vacuna["proximaDosis"]}"
        holder.veterinario.text = "Veterinario: ${vacuna["veterinario"]}"
    }

    override fun getItemCount(): Int = listaVacunas.size

    class VacunaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreVacuna)
        val fechaAplicacion: TextView = view.findViewById(R.id.tvFechaAplicacion)
        val proximaDosis: TextView = view.findViewById(R.id.tvProximaDosis)
        val veterinario: TextView = view.findViewById(R.id.tvVeterinario)
    }
}
