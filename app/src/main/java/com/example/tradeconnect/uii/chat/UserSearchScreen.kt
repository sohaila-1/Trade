package com.example.tradeconnect.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.UserSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
    navController: NavController,
    viewModel: UserSearchViewModel
) {
    // ✅ PAS de collectAsState
    val searchResults by viewModel.searchResults
    val searchQuery = viewModel.searchQuery
    val isLoading = viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn {
                items(searchResults) { user ->
                    UserItem(
                        user = user,
                        onClick = {
                            navController.navigate("chat/${user.uid}/${user.username}")
                        }
                    )
                }

                if (!isLoading && searchResults.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No users found")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (user.profileImageUrl.isNotEmpty()) {
            if (user.profileImageUrl.startsWith("data:image")) {
                Base64ProfileImage(
                    base64String = user.profileImageUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            DefaultAvatar(
                letter = user.username.firstOrNull()?.uppercase() ?: "?",
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = user.username,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

