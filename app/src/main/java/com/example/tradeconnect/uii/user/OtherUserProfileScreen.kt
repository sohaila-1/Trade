package com.example.tradeconnect.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    userId: String
) {
    val selectedUser by userViewModel.selectedUser.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val followingStatus by userViewModel.followingStatus.collectAsState()

    val isFollowing = followingStatus[userId] ?: false
    val isOwnProfile = userViewModel.isCurrentUser(userId)
    val isLoading = userViewModel.isLoading
    val isFollowLoading = userViewModel.isFollowLoading == userId

    // Charger le profil au lancement
    LaunchedEffect(userId) {
        userViewModel.loadUserProfile(userId)
    }

    // Nettoyer à la sortie
    DisposableEffect(Unit) {
        onDispose {
            userViewModel.clearSelectedUser()
            userViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedUser?.username ?: "Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (isLoading && selectedUser == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            selectedUser?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Photo de profil
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profileImageUrl.isNotEmpty()) {
                            if (user.profileImageUrl.startsWith("data:image")) {
                                Base64ProfileImage(
                                    base64String = user.profileImageUrl,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                AsyncImage(
                                    model = user.profileImageUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            DefaultAvatar(
                                letter = user.username.firstOrNull()?.uppercase() ?: "?",
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nom d'utilisateur
                    Text(
                        text = user.username,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Email
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    // Statut en ligne
                    if (user.isOnline) {
                        Text(
                            text = "🟢 En ligne",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    } else {
                        Text(
                            text = user.getLastSeenText(),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats followers/following
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            count = user.followersCount,
                            label = "Followers",
                            onClick = { navController.navigate("followers/$userId") }
                        )
                        StatItem(
                            count = user.followingCount,
                            label = "Abonnements",
                            onClick = { navController.navigate("following/$userId") }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Boutons d'action
                    if (!isOwnProfile) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Bouton Follow/Unfollow
                            Button(
                                onClick = { userViewModel.toggleFollow(userId) },
                                enabled = !isFollowLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) Color.LightGray else TBlue,
                                    contentColor = if (isFollowing) Color.Black else Color.White
                                )
                            ) {
                                if (isFollowLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = if (isFollowing) Color.Black else Color.White
                                    )
                                } else {
                                    Text(
                                        text = if (isFollowing) "Ne plus suivre" else "Suivre",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Bouton Message
                            OutlinedButton(
                                onClick = {
                                    navController.navigate("chat/${user.uid}/${user.username}")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Message")
                            }
                        }
                    } else {
                        // C'est son propre profil
                        OutlinedButton(
                            onClick = { navController.navigate("userprofile") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text("Modifier le profil")
                        }
                    }

                    // Indicateur "Vous suit"
                    if (!isOwnProfile && currentUser?.followers?.contains(userId) == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vous suit",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Messages d'erreur
                    userViewModel.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }

                    // Message de succès
                    userViewModel.successMessage?.let { success ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = success,
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
            } ?: run {
                // User not found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Utilisateur non trouvé",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}