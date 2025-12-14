package com.example.tradeconnect.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*

@Composable
fun BottomNavBar(navController: NavController, isDarkMode: Boolean) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        FooterIcon(
            icon = Icons.Default.Home,
            onClick = { navController.navigate("feed") }
        )

        FooterIcon(
            icon = Icons.AutoMirrored.Default.Message,
            onClick = { navController.navigate("chat") }
        )

        FooterIcon(
            icon = Icons.Default.Search,
            onClick = { /* Explore */ }
        )

        FooterIcon(
            icon = Icons.Default.Notifications,
            onClick = { /* Notifs */ }
        )

        FooterIcon(
            icon = Icons.Default.Person,
            onClick = { navController.navigate("profile") }
        )
    }
}

@Composable
private fun FooterIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier.size(26.dp)
        )
    }
}
