package com.example.tradeconnect.ui.feed.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tradeconnect.ui.theme.*

@Composable
fun BottomNavBar(
    navController: NavController,
    isDarkMode: Boolean
) {
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider
    val iconColor = if (isDarkMode) DarkText else LightText
    val inactiveColor = if (isDarkMode) DarkGrayText else LightGrayText

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column {
        // Top divider
        HorizontalDivider(
            thickness = 0.5.dp,
            color = dividerColor
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HOME
            NavItem(
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = currentRoute == "feed",
                activeColor = iconColor,
                inactiveColor = inactiveColor,
                onClick = {
                    if (currentRoute != "feed") {
                        navController.navigate("feed") {
                            popUpTo("feed") { inclusive = true }
                        }
                    }
                }
            )

            // SEARCH → Recherche d'utilisateurs
            NavItem(
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                isSelected = currentRoute == "user_search",
                activeColor = iconColor,
                inactiveColor = inactiveColor,
                onClick = {
                    if (currentRoute != "user_search") {
                        navController.navigate("user_search")
                    }
                }
            )

            // MESSAGES / CHAT
            NavItem(
                selectedIcon = Icons.Filled.Email,
                unselectedIcon = Icons.Outlined.Email,
                isSelected = currentRoute == "chat",
                activeColor = iconColor,
                inactiveColor = inactiveColor,
                onClick = {
                    if (currentRoute != "chat") {
                        navController.navigate("chat")
                    }
                }
            )

            // NOTIFICATIONS
            NavItem(
                selectedIcon = Icons.Filled.Notifications,
                unselectedIcon = Icons.Outlined.Notifications,
                isSelected = currentRoute == "notifications",
                activeColor = iconColor,
                inactiveColor = inactiveColor,
                onClick = {
                    if (currentRoute != "notifications") {
                        navController.navigate("notifications")
                    }
                }
            )

            // PROFILE
            NavItem(
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                isSelected = currentRoute == "profile",
                activeColor = iconColor,
                inactiveColor = inactiveColor,
                onClick = {
                    if (currentRoute != "profile") {
                        navController.navigate("profile")
                    }
                }
            )
        }
    }
}

@Composable
private fun NavItem(
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val icon = if (isSelected) selectedIcon else unselectedIcon
    val color by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "navItemColor"
    )

    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
    }
}