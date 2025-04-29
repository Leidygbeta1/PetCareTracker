package com.example.petcaretracker  // o el paquete que uses

import java.util.Date

data class Chat(
    var id: String               = "",
    val participants: List<String> = emptyList(),
    val petId: String?           = null,
    val lastMessage: String      = "",
    val timestamp: Date?         = null   // <-- Date, no Timestamp
)