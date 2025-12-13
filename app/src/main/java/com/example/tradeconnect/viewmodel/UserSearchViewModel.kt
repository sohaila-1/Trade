package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.data.repository.MessageRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class UserSearchViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            snapshotFlow { searchQuery }
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        return@collectLatest
                    }

                    isLoading = true
                    repository.searchUsers(query)
                        .catch {
                            _searchResults.value = emptyList()
                        }
                        .collect { users ->
                            _searchResults.value = users.filter {
                                it.uid != repository.getCurrentUserId()
                            }
                            isLoading = false
                        }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    class Factory(
        private val repository: MessageRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserSearchViewModel(repository) as T
        }
    }
}