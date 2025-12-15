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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.theme.*

@Composable
fun SidebarMenu(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val surfaceColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider

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
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TwitterBlue)
                        .clickable { navController.navigate("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nom et username
                Text(
                    text = "Mon Profil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Text(
                    text = "@utilisateur",
                    fontSize = 14.sp,
                    color = secondaryColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats followers/following
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatItem(
                        count = "0",
                        label = "Abonnements",
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        onClick = { /* TODO */ }
                    )

                    StatItem(
                        count = "0",
                        label = "Followers",
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        onClick = { /* TODO */ }
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

            // Spacer pour pousser le logout en bas
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

        // Badge optionnel (pour notifications, etc.)
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