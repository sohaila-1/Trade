// app/src/main/java/com/example/tradeconnect/ui/follow/DiscoverUsersScreen.kt
package com.example.tradeconnect.ui.follow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.FollowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverUsersScreen(
    navController: NavController,
    followViewModel: FollowViewModel,
    isDarkMode: Boolean
) {
    val allUsers by followViewModel.allUsers.collectAsState()
    val followingStatus by followViewModel.followingStatus.collectAsState()
    val isLoading = followViewModel.isLoading
    val followLoadingUserId = followViewModel.isFollowLoading

    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
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
            if (allUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = TBlue)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Aucun utilisateur trouvé", fontSize = 16.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allUsers) { user ->
                        FollowUserItem(
                            user = user,
                            isFollowing = followingStatus[user.uid] ?: false,
                            isLoading = followLoadingUserId == user.uid,
                            isCurrentUser = followViewModel.isCurrentUser(user.uid),
                            onUserClick = {
                                navController.navigate("user_profile/${user.uid}")
                            },
                            onFollowClick = {
                                followViewModel.toggleFollow(user.uid)
                            }
                        )
                    }
                }
            }
        }
    }
}