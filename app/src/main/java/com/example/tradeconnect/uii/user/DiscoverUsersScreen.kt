// app/src/main/java/com/example/tradeconnect/ui/user/DiscoverUsersScreen.kt
package com.example.tradeconnect.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverUsersScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val allUsers by userViewModel.allUsers.collectAsState()
    val followingStatus by userViewModel.followingStatus.collectAsState()
    val isLoading = userViewModel.isLoading
    val followLoadingUserId = userViewModel.isFollowLoading

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Découvrir") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    userViewModel.searchUsers(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher un utilisateur...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                shape = RoundedCornerShape(25.dp),
                singleLine = true
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                allUsers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Aucun utilisateur trouvé"
                            else
                                "Aucun utilisateur disponible",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(allUsers) { user ->
                            FollowUserItem(
                                user = user,
                                isFollowing = followingStatus[user.uid] ?: false,
                                isLoading = followLoadingUserId == user.uid,
                                isCurrentUser = false,
                                onUserClick = {
                                    navController.navigate("other_profile/${user.uid}")
                                },
                                onFollowClick = {
                                    userViewModel.toggleFollow(user.uid)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}