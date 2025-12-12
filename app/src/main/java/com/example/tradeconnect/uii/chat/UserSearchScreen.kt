package com.example.tradeconnect.ui.chat

// Android imports
import android.graphics.BitmapFactory
import android.util.Base64

// Compose imports
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Navigation
import androidx.navigation.NavController

// Coil for image loading
import coil.compose.AsyncImage

// Your models
import com.example.tradeconnect.data.model.User

// Utils
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.UserSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
    navController: NavController,
    viewModel: UserSearchViewModel
) {
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            // Search bar
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search")
                },
                singleLine = true
            )

            // Loading indicator
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Search results
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { user ->
                    UserItem(
                        user = user,
                        onClick = {
                            navController.navigate("chat/${user.uid}/${user.username}")
                        }
                    )
                }

                // Empty state
                if (!viewModel.isLoading && searchResults.isEmpty() && viewModel.searchQuery.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile image - handles both base64 and URL
        if (user.profileImageUrl.isNotEmpty()) {
            if (user.profileImageUrl.startsWith("data:image")) {
                // Handle base64 image
                Base64ProfileImage(
                    base64String = user.profileImageUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                // Handle regular URL
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // Default avatar with first letter
            DefaultAvatar(
                letter = user.username.firstOrNull()?.uppercase() ?: "?",
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // User info
        Column {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Base64ProfileImage and DefaultAvatar are now imported from util package