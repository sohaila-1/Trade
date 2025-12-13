package com.example.tradeconnect.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 🏠 HOME
        FooterIcon(
            icon = Icons.Default.Home,
            isSelected = currentRoute == "feed",
            onClick = {
                navController.navigate("feed") {
                    launchSingleTop = true
                }
            }
        )

        // 💬 MESSAGES
        FooterIcon(
            icon = Icons.Outlined.ChatBubbleOutline,
            isSelected = currentRoute == "chatList",
            onClick = {
                navController.navigate("chatList") {
                    launchSingleTop = true
                }
            }
        )

        // 🔔 NOTIFICATIONS
        FooterIcon(
            icon = Icons.Default.Notifications,
            isSelected = currentRoute == "notifications",
            onClick = {
                navController.navigate("notifications") {
                    launchSingleTop = true
                }
            }
        )

        // 👤 PROFILE
        FooterIcon(
            icon = Icons.Default.Person,
            isSelected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun FooterIcon(
    icon: ImageVector,
    isSelected: Boolean,
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
            modifier = Modifier.size(26.dp),
            tint = if (isSelected)
                Color(0xFF1D9BF0) // 🔵 Twitter blue
            else
                Color.Black.copy(alpha = 0.75f)
        )
    }
}
