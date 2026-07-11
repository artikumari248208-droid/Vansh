package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Message
import com.example.data.model.User
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)

    // User State
    val userState: StateFlow<User?> = repository.userFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Chat History State
    val messagesState: StateFlow<List<Message>> = repository.allMessagesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected Mode State ("Echo", "Light", "Flow")
    private val _selectedMode = MutableStateFlow("Echo")
    val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()

    // Is Generative Model Active (Loading state)
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Active Creation Request (from '+' Menu)
    private val _activeCreationType = MutableStateFlow<String?>(null)
    val activeCreationType: StateFlow<String?> = _activeCreationType.asStateFlow()

    fun selectMode(mode: String) {
        _selectedMode.value = mode
    }

    fun setCreationType(type: String?) {
        _activeCreationType.value = type
        if (type != null) {
            // Creation automatically switches to Flow Mode as requested in prompt!
            _selectedMode.value = "Flow"
        }
    }

    fun login(name: String, phoneNumber: String) {
        viewModelScope.launch {
            val user = User(
                name = name.ifBlank { "User" },
                phoneNumber = phoneNumber.ifBlank { "9999999999" },
                isLoggedIn = true
            )
            repository.saveUser(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearUser()
            repository.clearChatHistory()
            _selectedMode.value = "Echo"
            _activeCreationType.value = null
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() && _activeCreationType.value == null) return

        val currentMode = _selectedMode.value
        val creationType = _activeCreationType.value
        val userPrompt = if (text.isNotBlank()) text else {
            when (creationType) {
                "image" -> "Create an AI image representing futuristic Indian art"
                "video" -> "Generate a text-to-video of a neon tiger walking"
                "animation" -> "Animate a revolving 3D wireframe grid"
                "music" -> "Compose a deep classical sitar fusion track"
                else -> "Generate creative asset"
            }
        }

        viewModelScope.launch {
            _isGenerating.value = true

            // 1. Add User Message
            val userMessage = Message(
                role = "user",
                text = userPrompt,
                mode = currentMode,
                mediaType = creationType, // Store if user triggered a media creation
                promptText = userPrompt
            )
            repository.addMessage(userMessage)

            // Clear active creation state in input bar
            _activeCreationType.value = null

            // 2. Fetch history for context
            val history = repository.allMessagesFlow.stateIn(viewModelScope).value

            // 3. Request Gemini AI response
            val aiResponse = repository.generateResponse(userPrompt, currentMode, history, creationType)

            // 4. If it was a creation task, attach simulated rich media assets
            val mediaUri = when (creationType) {
                "image" -> {
                    // Premium monochrome abstract visuals from Unsplash
                    val images = listOf(
                        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                        "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=600",
                        "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=600"
                    )
                    images.random()
                }
                "video" -> "simulated_video"
                "animation" -> "simulated_animation"
                "music" -> "simulated_music"
                else -> null
            }

            // 5. Add AI Response Message
            val modelMessage = Message(
                role = "model",
                text = aiResponse,
                mode = currentMode,
                mediaType = creationType,
                mediaUri = mediaUri,
                promptText = userPrompt
            )
            repository.addMessage(modelMessage)

            _isGenerating.value = false
        }
    }
}
