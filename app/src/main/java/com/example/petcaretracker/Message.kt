package com.example.petcaretracker

import java.security.Timestamp
import java.util.Date

data class Message(
    var id: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val text: String = "",
    val timestamp: Date?   = null
)