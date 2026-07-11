package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String, // "Echo", "Light", or "Flow"
    val mediaUri: String? = null,
    val mediaType: String? = null, // "image", "video", "audio", "animation", "music"
    val promptText: String? = null
)
