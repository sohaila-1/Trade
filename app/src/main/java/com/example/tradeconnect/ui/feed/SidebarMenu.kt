package com.example.tradeconnect.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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

        Row(Modifier.clickable { navController.navigate("profile") }) {
            Icon(Icons.Default.Person, contentDescription = null, tint = textColor)
            Spacer(Modifier.width(12.dp))
            Text("Profile", color = textColor)
        }

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.clickable {
                navController.navigate("home") {
                    popUpTo("home") {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = textColor
            )
            Spacer(Modifier.width(12.dp))
            Text("Home", color = textColor)
        }




        Spacer(Modifier.height(22.dp))

        Row(Modifier.clickable { navController.navigate("bookmarks") }) {
            Icon(Icons.Default.Bookmark, contentDescription = null, tint = textColor)
            Spacer(Modifier.width(12.dp))
            Text("Bookmarks", color = textColor)
        }

        Spacer(Modifier.height(22.dp))

        Row(Modifier.clickable { onToggleTheme() }) {
            Icon(Icons.Default.LightMode, contentDescription = null, tint = textColor)
            Spacer(Modifier.width(12.dp))
            Text("Light / Dark", color = textColor)
        }

        Spacer(Modifier.height(22.dp))

        Row(Modifier.clickable { onLogoutClick() }) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(12.dp))
            Text("Logout", color = Color.Red)
        }
    }
}
