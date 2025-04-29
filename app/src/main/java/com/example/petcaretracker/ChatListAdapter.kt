package com.example.petcaretracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val items: List<Chat>,
    private val currentUserId: String,
    private val onClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val chat = items[pos]

        // Identificamos al otro participante comparando con currentUserId
        val otherId = chat.participants.first { it != currentUserId }

        // Placeholder mientras carga
        holder.tvName.text = "Cargando..."

        // Pedimos los datos del usuario otherId
        FirebaseService.getUsuarioById(otherId) { user: User? ->
            if (user != null) {
                Log.d("ChatListAdapter", "Usuario para chat[$pos]: $user")
                val nombre = user.nombreCompleto.trim()
                val rolRaw = user.rol.trim()
                val rolCap = rolRaw
                    .lowercase(Locale.getDefault())
                    .replaceFirstChar { it.uppercase(Locale.getDefault()) }
                holder.tvName.text = "$nombre ($rolCap)"
            } else {
                holder.tvName.text = otherId
            }
        }

        // Último mensaje + hora
        holder.tvLast.text = chat.lastMessage
        val time = chat.timestamp ?: Date()
        holder.tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)

        holder.itemView.setOnClickListener { onClick(chat) }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView    = view.findViewById(R.id.tvChatName)
        val tvLast: TextView    = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView    = view.findViewById(R.id.tvTimestamp)
    }
}
