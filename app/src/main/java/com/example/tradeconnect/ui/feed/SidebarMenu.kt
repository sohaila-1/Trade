package com.example.tradeconnect.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.theme.TBlue

@Composable
fun SidebarMenu(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val bgColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Titre du menu
        Text(
            text = "Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        // 🆕 DÉCOUVRIR
        MenuItemRow(
            icon = Icons.Default.PersonSearch,
            label = "Découvrir",
            textColor = TBlue,
            onClick = { navController.navigate("discover") }
        )

        Spacer(Modifier.height(22.dp))

        // PROFIL
        MenuItemRow(
            icon = Icons.Default.Person,
            label = "Mon Profil",
            textColor = textColor,
            onClick = { navController.navigate("profile") }
        )

        Spacer(Modifier.height(22.dp))

        // MESSAGES
        MenuItemRow(
            icon = Icons.Default.Email,
            label = "Messages",
            textColor = textColor,
            onClick = { navController.navigate("chat") }
        )

        Spacer(Modifier.height(22.dp))

        // BOOKMARKS
        MenuItemRow(
            icon = Icons.Default.Bookmark,
            label = "Bookmarks",
            textColor = textColor,
            onClick = { navController.navigate("bookmarks") }
        )

        Spacer(Modifier.height(22.dp))

        // SETTINGS
        MenuItemRow(
            icon = Icons.Default.Settings,
            label = "Paramètres",
            textColor = textColor,
            onClick = { navController.navigate("settings") }
        )

        Spacer(Modifier.height(22.dp))

        // THEME
        MenuItemRow(
            icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            label = if (isDarkMode) "Mode Clair" else "Mode Sombre",
            textColor = textColor,
            onClick = onToggleTheme
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        // LOGOUT
        MenuItemRow(
            icon = Icons.Default.ExitToApp,
            label = "Déconnexion",
            textColor = Color.Red,
            onClick = onLogoutClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}