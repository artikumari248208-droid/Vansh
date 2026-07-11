package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.model.Message
import com.example.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val messageDao = db.messageDao()

    val userFlow: Flow<User?> = userDao.getUserFlow()
    val allMessagesFlow: Flow<List<Message>> = messageDao.getAllMessagesFlow()

    suspend fun getActiveUser(): User? = withContext(Dispatchers.IO) {
        userDao.getUser()
    }

    suspend fun saveUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun clearUser() = withContext(Dispatchers.IO) {
        userDao.clearUser()
    }

    suspend fun addMessage(message: Message) = withContext(Dispatchers.IO) {
        messageDao.insertMessage(message)
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        messageDao.clearAllMessages()
    }

    suspend fun generateResponse(
        prompt: String,
        mode: String,
        recentMessages: List<Message>,
        mediaType: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing. Please configure it in the AI Studio Secrets Panel."
        }

        // 1. Setup System Instructions for each Mode
        val systemInstructionText = when (mode) {
            "Echo" -> {
                "You are 'Hello', a premium, minimalist Indian AI assistant. Act as a helpful, general-purpose conversational AI. Keep your answers clear, professional, concise, and optimized for the Indian context. Speak in a friendly, balanced mix of Hindi and English (Hinglish) or pure English as appropriate to the user's queries."
            }
            "Light" -> {
                "You are 'Hello', a friendly and gentle study helper specialized in Indian education (NCERT Class 1-12) and basic personal mentorship. Provide verified, simplified, step-by-step answers. Use easy explanations, simple analogies, and exam-oriented bullet points. If asked study doubts or personal advice, act as a wise, encouraging elder sibling or mentor."
            }
            "Flow" -> {
                "You are 'Hello', an advanced AI solver and media creator. You are in 'Flow Mode', optimized for tricky math, complex physics/STEM questions, and creative generation (images, videos, music, animations). For math and STEM, solve step-by-step with high mathematical accuracy. When a user requests content creation (like creating an image, video, music, or animation), compose a detailed narrative/script/lyrics and write a structured generation description in rich, vivid detail so they can visualize it perfectly."
            }
            else -> "You are 'Hello', a premium Indian AI Assistant."
        }

        // 2. Prepare Conversation History
        // Map last 10 messages for context window efficiency and cost savings
        val contentsList = recentMessages.takeLast(10).map { msg ->
            Content(
                role = if (msg.role == "user") "user" else "model",
                parts = listOf(Part(text = msg.text))
            )
        }.toMutableList()

        // Append the current prompt
        contentsList.add(
            Content(
                role = "user",
                parts = listOf(Part(text = prompt))
            )
        )

        // 3. Create Request
        val request = GenerateContentRequest(
            contents = contentsList,
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No response received. Please try again."
        } catch (e: Exception) {
            "Error: ${e.message ?: "An unknown network error occurred."}"
        }
    }
}
