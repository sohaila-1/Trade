package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.repository.UserRepository

class UserSearchViewModel(
    private val repository: UserRepository
) : ViewModel() {

    // 🔹 Texte de recherche
    var searchQuery by mutableStateOf("")
        private set

    // 🔹 Résultats
    private val _searchResults = mutableStateOf<List<User>>(emptyList())
    val searchResults: State<List<User>> = _searchResults

    // 🔹 Loading
    var isLoading by mutableStateOf(false)
        private set

    // ----------------------------
    // 🔥 UPDATE QUERY + SEARCH
    // ----------------------------
    fun updateSearchQuery(query: String) {
        searchQuery = query

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            isLoading = false
            return
        }

        isLoading = true

        repository.searchUsers(query) { users ->
            val currentUid = repository.getCurrentUserId()

            _searchResults.value = users.filter {
                it.uid != currentUid
            }

            isLoading = false
        }
    }

    // ----------------------------
    // 🔹 FACTORY
    // ----------------------------
    class Factory(
        private val repository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserSearchViewModel(repository) as T
        }
    }
}
