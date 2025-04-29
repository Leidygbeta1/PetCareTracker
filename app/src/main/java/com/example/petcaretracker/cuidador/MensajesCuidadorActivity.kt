package com.example.petcaretracker.cuidador

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcaretracker.Chat
import com.example.petcaretracker.ChatActivity
import com.example.petcaretracker.ChatListAdapter
import com.example.petcaretracker.FirebaseService
import com.example.petcaretracker.LoginActivity
import com.example.petcaretracker.R

import com.google.android.material.floatingactionbutton.FloatingActionButton

class MensajesCuidadorActivity : AppCompatActivity() {

    private lateinit var rvChats: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var fabNewChat: FloatingActionButton
    private lateinit var userId: String
    private val chats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)

        // 1) Recuperar userId
        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = prefs.getString("userId","") ?: ""
        if (userId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish(); return
        }

        // 2) Vistas
        rvChats    = findViewById(R.id.rvChats)
        tvEmpty    = findViewById(R.id.tvEmpty)
        fabNewChat = findViewById(R.id.fabNewChat)

        // 3) RecyclerView + Adapter
        adapter = ChatListAdapter(chats, userId) { chat ->
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("chatId", chat.id)
            })
        }
        rvChats.layoutManager = LinearLayoutManager(this)
        rvChats.adapter       = adapter

        // 4) Nuevo chat (con dueños)
        fabNewChat.setOnClickListener { showOwnerPicker() }
    }

    override fun onResume() {
        super.onResume()
        loadChats()
    }

    private fun loadChats() {
        FirebaseService.getChatsForUser(userId) { list ->
            chats.clear()
            chats.addAll(list)
            adapter.notifyDataSetChanged()
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showOwnerPicker() {
        FirebaseService.getOwnersDisponibles { owners ->
            if (owners.isEmpty()) {
                tvEmpty.text = "No hay dueños disponibles"
                tvEmpty.visibility = View.VISIBLE
                return@getOwnersDisponibles
            }
            val names = owners.map { it["nombre_completo"] ?: "" }.toTypedArray()
            val ids   = owners.map { it["id"]!! }

            AlertDialog.Builder(this)
                .setTitle("Nuevo chat con dueño:")
                .setItems(names) { _, idx ->
                    val otherId = ids[idx]
                    FirebaseService.getOrCreateChat(userId, otherId, null) { chatId ->
                        startActivity(Intent(this, ChatActivity::class.java).apply {
                            putExtra("chatId", chatId)
                        })
                    }
                }
                .show()
        }
    }
}
