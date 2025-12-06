package com.example.tradeconnect.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.DrawerState

@Composable
fun TabsHeader(
    isDarkMode: Boolean,
    textColor: Color,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val tabColor = if (isDarkMode) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {

        // 🔥 ROW WITH BURGER + TITLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = textColor,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        scope.launch { drawerState.open() }
                    }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Home",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔵Pour vous / Abonnements
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Pour vous",
                fontSize = 16.sp,
                color = tabColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Abonnements",
                fontSize = 16.sp,
                color = tabColor.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .height(2.dp)
                .width(90.dp)
                .background(tabColor)
        )
    }
}
