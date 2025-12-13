package com.example.tradeconnect.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.Message
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.util.NetworkObserver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

class ChatViewModel(
    private val repository: MessageRepository,
    private val networkObserver: NetworkObserver,
    private val partnerId: String
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _partner = MutableStateFlow<User?>(null)
    val partner: StateFlow<User?> = _partner.asStateFlow()

    var messageText by mutableStateOf("")
        private set

    var isOnline by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var messagesJob: Job? = null
    private var networkJob: Job? = null

    init {
        Log.d("ChatViewModel", "=== ChatViewModel CREATED for partner: $partnerId ===")
    }

    // Called from the UI with LaunchedEffect to ensure it runs after composition
    fun startObserving() {
        if (messagesJob?.isActive == true) {
            Log.d("ChatViewModel", "Already observing, skipping")
            return
        }

        Log.d("ChatViewModel", "Starting observation for partner: $partnerId")

        // Observe network connectivity
        networkJob = viewModelScope.launch {
            try {
                networkObserver.isConnected.collect { connected ->
                    val wasOffline = !isOnline
                    isOnline = connected
                    Log.d("ChatViewModel", "Network status: $connected")

                    if (connected && wasOffline) {
                        Log.d("ChatViewModel", "Coming online, syncing pending messages")
                        try {
                            repository.syncPendingMessages()
                        } catch (e: Exception) {
                            if (e !is CancellationException) {
                                Log.e("ChatViewModel", "Error syncing pending messages", e)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                Log.d("ChatViewModel", "Network observer stopped")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error observing network", e)
            }
        }

        // Load messages
        loadMessages()

        // Load partner info
        loadPartner()

        // Sync message history from Firebase if online
        viewModelScope.launch {
            try {
                if (isOnline) {
                    repository.syncConversationHistory(partnerId, limit = 50)
                }
            } catch (e: CancellationException) {
                // Ignore
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to sync conversation history", e)
            }
        }

        // Mark messages as seen
        viewModelScope.launch {
            try {
                repository.markMessagesAsSeen(partnerId)
            } catch (e: CancellationException) {
                // Ignore
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error marking messages as seen", e)
            }
        }
    }

    private fun loadMessages() {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Starting to load messages for $partnerId")
                repository.getMessagesForChat(partnerId)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        Log.e("ChatViewModel", "Error in messages flow", e)
                        errorMessage = "Failed to load messages: ${e.message}"
                        isLoading = false
                    }
                    .collect { msgs ->
                        _messages.value = msgs
                        isLoading = false
                        Log.d("ChatViewModel", "Messages updated: ${msgs.size} total" +
                                if (msgs.isNotEmpty()) ", last: ${msgs.last().text.take(20)}" else "")

                        if (msgs.isNotEmpty() && errorMessage != null) {
                            errorMessage = null
                        }
                    }
            } catch (e: CancellationException) {
                Log.d("ChatViewModel", "Message loading stopped")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception loading messages", e)
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        }
    }

    private fun loadPartner() {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(partnerId)
                _partner.value = user
                Log.d("ChatViewModel", "Loaded partner: ${user?.username ?: "null"}")
            } catch (e: CancellationException) {
                // Ignore
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error loading partner info", e)
            }
        }
    }

    fun updateMessageText(text: String) {
        messageText = text
    }

    fun sendMessage() {
        val text = messageText.trim()
        if (text.isEmpty()) {
            Log.d("ChatViewModel", "Empty message, not sending")
            return
        }

        Log.d("ChatViewModel", "Sending message: '${text.take(20)}...' (online: $isOnline)")

        viewModelScope.launch {
            // Clear input immediately for better UX
            messageText = ""

            try {
                // Send message (will save locally first for optimistic UI)
                val result = repository.sendMessage(partnerId, text, isOnline)

                if (result.isSuccess) {
                    Log.d("ChatViewModel", "Message sent successfully")
                } else {
                    Log.e("ChatViewModel", "Failed to send message: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: CancellationException) {
                // Restore message text if cancelled
                messageText = text
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception sending message", e)
            }
        }
    }

    override fun onCleared() {
        Log.d("ChatViewModel", "=== ChatViewModel DESTROYED for partner: $partnerId ===")
        messagesJob?.cancel()
        networkJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val repository: MessageRepository,
        private val networkObserver: NetworkObserver,
        private val partnerId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository, networkObserver, partnerId) as T
        }
    }
}