package com.example.petcaretracker

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.firestore.ListenerRegistration

class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter            // <— Tipo correcto
    private var listener: ListenerRegistration? = null

    private val userId by lazy {
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .getString("userId", "") ?: ""
    }

    private val chatId by lazy {
        intent.getStringExtra("chatId")
            ?: throw IllegalStateException("chatId missing")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvMessages = findViewById(R.id.rvMessages)
        etMessage  = findViewById(R.id.etMessage)
        btnSend    = findViewById(R.id.btnSend)

        // **OJO**: pasamos messages y userId
        adapter = MessageAdapter(messages, userId)
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = adapter

        btnSend.setOnClickListener {
            val texto = etMessage.text.toString().trim()
            if (texto.isNotEmpty()) {
                FirebaseService.sendMessage(chatId, userId, "owner", texto)
                etMessage.text.clear()
            }
        }

        listener = FirebaseService.listenMessages(chatId) { lista ->
            messages.clear()
            messages.addAll(lista)
            adapter.notifyDataSetChanged()
            rvMessages.scrollToPosition(messages.size - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}




