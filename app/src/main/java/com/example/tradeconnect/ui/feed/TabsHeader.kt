package com.example.tradeconnect.ui.feed.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.DrawerState
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun TabsHeader(
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

        // ------------------------------
        //  BURGER + TITLE
        // ------------------------------
        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = textColor,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { scope.launch { drawerState.open() } }
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

        // ------------------------------
        //  TABS TITLE : Pour vous / Abonnements
        // ------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // POUR VOUS
            Text(
                text = "Pour vous",
                fontSize = 16.sp,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 0) tabColor else tabColor.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onTabSelected(0) }
            )

            // ABONNEMENTS
            Text(
                text = "Abonnements",
                fontSize = 16.sp,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 1) tabColor else tabColor.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onTabSelected(1) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Distance horizontale jusqu'au 2nd tab (ajuste si besoin)
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
