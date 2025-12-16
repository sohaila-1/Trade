package com.example.tradeconnect.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.ui.theme.*
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar

@Composable
fun SidebarMenu(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogoutClick: () -> Unit,
    currentUser: User? = null
) {
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider

    // Récupérer les données de l'utilisateur
    val username = currentUser?.username ?: "Utilisateur"
    val email = currentUser?.email ?: "@utilisateur"
    val followersCount = currentUser?.followersCount ?: 0
    val followingCount = currentUser?.followingCount ?: 0
    val profileImageUrl = currentUser?.profileImageUrl ?: ""
    val profileInitial = username.firstOrNull()?.uppercase() ?: "?"

    ModalDrawerSheet(
        drawerContainerColor = bgColor,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // ==================== HEADER PROFIL ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 🆕 Avatar avec photo de profil
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigate("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        // Si c'est une image base64
                        profileImageUrl.startsWith("data:image") ||
                                profileImageUrl.length > 200 -> {
                            Base64ProfileImage(
                                base64String = profileImageUrl,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                            )
                        }
                        // Si c'est une URL normale
                        profileImageUrl.isNotEmpty() &&
                                (profileImageUrl.startsWith("http") || profileImageUrl.startsWith("https")) -> {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Photo de profil",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Sinon afficher l'avatar par défaut
                        else -> {
                            DefaultAvatar(
                                letter = profileInitial,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nom
                Text(
                    text = username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                // Email
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = secondaryColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats followers/following
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatItem(
                        count = followingCount.toString(),
                        label = "Abonnements",
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        onClick = {
                            currentUser?.uid?.let { uid ->
                                navController.navigate("following/$uid")
                            }
                        }
                    )

                    StatItem(
                        count = followersCount.toString(),
                        label = "Followers",
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        onClick = {
                            currentUser?.uid?.let { uid ->
                                navController.navigate("followers/$uid")
                            }
                        }
                    )
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // ==================== MENU ITEMS ====================

            MenuItem(
                icon = Icons.Outlined.Person,
                title = "Profil",
                textColor = textColor,
                onClick = { navController.navigate("profile") }
            )

            MenuItem(
                icon = Icons.Outlined.Bookmark,
                title = "Signets",
                textColor = textColor,
                onClick = { navController.navigate("bookmarks") }
            )

            MenuItem(
                icon = Icons.Outlined.Email,
                title = "Messages",
                textColor = textColor,
                onClick = { navController.navigate("chat") }
            )

            MenuItem(
                icon = Icons.Outlined.PersonSearch,
                title = "Découvrir",
                textColor = textColor,
                onClick = { navController.navigate("discover") }
            )

            MenuItem(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                textColor = textColor,
                onClick = { navController.navigate("notifications") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // ==================== PARAMÈTRES ====================

            MenuItem(
                icon = Icons.Outlined.Settings,
                title = "Paramètres",
                textColor = textColor,
                onClick = { navController.navigate("settings") }
            )

            // Theme Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleTheme() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Theme",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = if (isDarkMode) "Mode clair" else "Mode sombre",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TwitterBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = secondaryColor.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            // ==================== LOGOUT ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogoutClick() }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFFE0245E),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Déconnexion",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE0245E)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        badge?.let {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TwitterBlue)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    count: String,
    label: String,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = count,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = secondaryColor
        )
    }
}