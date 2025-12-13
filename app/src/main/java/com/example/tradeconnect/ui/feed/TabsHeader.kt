package com.example.tradeconnect.ui.feed.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TabsHeader(
    navController: NavController,
    isDarkMode: Boolean,
    textColor: Color,
    drawerState: DrawerState,
    scope: CoroutineScope,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabColor = if (isDarkMode) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {

        // 🔝 HEADER (Menu + Home + Chat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Menu + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { scope.launch { drawerState.open() } }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = textColor
                    )
                }

                Text(
                    text = "Home",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // 💬 ICON MESSAGE
            IconButton(
                onClick = {
                    val partnerId = "test_user_id" // TODO remplacer plus tard
                    navController.navigate("chatList")
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Messages",
                    tint = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🗂️ TABS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Pour vous",
                fontSize = 16.sp,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 0) tabColor else tabColor.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onTabSelected(0) }
            )

            Text(
                text = "Abonnements",
                fontSize = 16.sp,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 1) tabColor else tabColor.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onTabSelected(1) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔽 INDICATOR
        val targetOffset = if (selectedTab == 0) 0.dp else 250.dp

        val indicatorOffset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = tween(
                durationMillis = 350,
                easing = FastOutSlowInEasing
            ),
            label = ""
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .height(3.dp)
                .width(90.dp)
                .background(
                    color = tabColor,
                    shape = RoundedCornerShape(50.dp)
                )
        )
    }
}
