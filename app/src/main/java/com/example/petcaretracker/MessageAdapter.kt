package com.example.petcaretracker


import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.Message
import com.example.petcaretracker.R
import android.text.format.DateFormat

class MessageAdapter(
    private val items: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val msg = items[pos]
        holder.tvText.text = msg.text
        msg.timestamp?.let { date ->
            holder.tvTime.text = DateFormat.format("HH:mm", date)
        }

        // Como el contenedor es un LinearLayout hijo de un FrameLayout,
        // su layoutParams son FrameLayout.LayoutParams
        val params = holder.container.layoutParams as FrameLayout.LayoutParams

        if (msg.senderId == currentUserId) {
            params.gravity = Gravity.END
            holder.container.setBackgroundResource(R.drawable.bg_message_self)
        } else {
            params.gravity = Gravity.START
            holder.container.setBackgroundResource(R.drawable.bg_message_other)
        }
        holder.container.layoutParams = params
    }

    override fun getItemCount() = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        // Aquí cambiamos FrameLayout a LinearLayout
        val container: LinearLayout = view.findViewById(R.id.containerMessage)
        val tvText: TextView       = view.findViewById(R.id.tvMessageText)
        val tvTime: TextView       = view.findViewById(R.id.tvMessageTime)
    }
}
