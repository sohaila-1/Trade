package com.example.tradeconnect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.ChatPreview
import com.example.tradeconnect.data.repository.MessageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatPreviews: StateFlow<List<ChatPreview>> = _chatPreviews.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // First sync from Firebase, then load from local DB
        syncAndLoadChats()
    }

    private fun syncAndLoadChats() {
        viewModelScope.launch {
            try {
                // Start syncing from Firebase
                _isSyncing.value = true
                Log.d("ChatListViewModel", "Starting to sync conversations from Firebase")

                try {
                    repository.syncAllConversations()
                    Log.d("ChatListViewModel", "Sync completed")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("ChatListViewModel", "Error syncing conversations", e)
                    // Continue to load local data even if sync fails
                }

                _isSyncing.value = false
            } catch (e: CancellationException) {
                Log.d("ChatListViewModel", "Sync cancelled")
                _isSyncing.value = false
            }
        }

        // Load chat previews from local DB (will update as sync populates it)
        loadChatPreviews()
    }

    private fun loadChatPreviews() {
        viewModelScope.launch {
            try {
                repository.getChatPreviews()
                    .catch { e ->
                        if (e is CancellationException) throw e
                        Log.e("ChatListViewModel", "Error loading chat previews", e)
                        _isLoading.value = false
                    }
                    .collect { previews ->
                        _chatPreviews.value = previews
                        _isLoading.value = false
                        Log.d("ChatListViewModel", "Loaded ${previews.size} chat previews")
                    }
            } catch (e: CancellationException) {
                Log.d("ChatListViewModel", "Chat previews loading cancelled")
            }
        }
    }

    /**
     * Manually trigger a refresh/sync of conversations.
     * Useful for pull-to-refresh functionality.
     */
    fun refresh() {
        if (_isSyncing.value) {
            Log.d("ChatListViewModel", "Already syncing, skipping refresh")
            return
        }

        viewModelScope.launch {
            try {
                _isSyncing.value = true
                repository.syncAllConversations()
                _isSyncing.value = false
            } catch (e: CancellationException) {
                _isSyncing.value = false
            } catch (e: Exception) {
                Log.e("ChatListViewModel", "Error refreshing", e)
                _isSyncing.value = false
            }
        }
    }

    class Factory(
        private val repository: MessageRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatListViewModel(repository) as T
        }
    }
}