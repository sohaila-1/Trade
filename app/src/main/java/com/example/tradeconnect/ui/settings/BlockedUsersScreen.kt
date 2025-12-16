package com.example.tradeconnect.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.ui.theme.*
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.FollowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    navController: NavController,
    followViewModel: FollowViewModel,
    isDarkMode: Boolean
) {
    // Couleurs
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider
    val cardColor = if (isDarkMode) DarkSurface else LightSurface

    // États
    val blockedUsers by followViewModel.blockedUsers.collectAsState()
    val isLoading by followViewModel.isBlockedLoading.collectAsState()

    // Dialog de confirmation déblocage
    var userToUnblock by remember { mutableStateOf<User?>(null) }

    // Charger les utilisateurs bloqués
    LaunchedEffect(Unit) {
        followViewModel.loadBlockedUsers()
    }

    // Dialog de confirmation
    userToUnblock?.let { user ->
        AlertDialog(
            onDismissRequest = { userToUnblock = null },
            containerColor = cardColor,
            title = {
                Text(
                    "Débloquer ${user.username} ?",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Text(
                    "Cette personne pourra à nouveau voir votre profil, vos tweets et vous envoyer des messages.",
                    color = secondaryColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        followViewModel.unblockUser(user.uid)
                        userToUnblock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
                ) {
                    Text("Débloquer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToUnblock = null }) {
                    Text("Annuler", color = TwitterBlue)
                }
            }
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgColor,
                shadowElevation = 2.dp
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = textColor
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "Comptes bloqués",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            if (blockedUsers.isNotEmpty()) {
                                Text(
                                    text = "${blockedUsers.size} compte${if (blockedUsers.size > 1) "s" else ""}",
                                    fontSize = 13.sp,
                                    color = secondaryColor
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                }
            }
        }
    ) { padding ->
        when {
            // Loading
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TwitterBlue)
                }
            }

            // Empty state
            blockedUsers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.PersonOff,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Aucun compte bloqué",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Quand vous bloquez quelqu'un, cette personne ne pourra plus voir votre profil, vos tweets ni vous envoyer de messages.",
                            fontSize = 14.sp,
                            color = secondaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Liste des utilisateurs bloqués
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor)
                ) {
                    // Info header
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = cardColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Block,
                                    contentDescription = null,
                                    tint = secondaryColor,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Les personnes bloquées ne peuvent pas vous suivre, voir vos tweets ou vous envoyer des messages.",
                                    fontSize = 13.sp,
                                    color = secondaryColor,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Liste
                    items(
                        items = blockedUsers,
                        key = { it.uid }
                    ) { user ->
                        BlockedUserItem(
                            user = user,
                            isDarkMode = isDarkMode,
                            onUnblock = { userToUnblock = user }
                        )

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = dividerColor,
                            modifier = Modifier.padding(start = 84.dp)
                        )
                    }

                    // Espace en bas
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedUserItem(
    user: User,
    isDarkMode: Boolean,
    onUnblock: () -> Unit
) {
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                user.profileImageUrl.startsWith("data:image") ||
                        user.profileImageUrl.length > 200 -> {
                    Base64ProfileImage(
                        base64String = user.profileImageUrl,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                }
                user.profileImageUrl.isNotEmpty() &&
                        (user.profileImageUrl.startsWith("http") ||
                                user.profileImageUrl.startsWith("https")) -> {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    DefaultAvatar(
                        letter = user.username.firstOrNull()?.uppercase() ?: "?",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // User info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = "@${user.email.substringBefore("@")}",
                fontSize = 14.sp,
                color = secondaryColor
            )
        }

        // Bouton débloquer
        OutlinedButton(
            onClick = onUnblock,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE0245E)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFFE0245E), Color(0xFFE0245E))
                )
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                "Débloquer",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}