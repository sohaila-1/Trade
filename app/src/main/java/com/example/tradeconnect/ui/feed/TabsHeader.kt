package com.example.tradeconnect.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.tradeconnect.ui.theme.*

@Composable
fun TabsHeader(
    isDarkMode: Boolean,
    textColor: Color,
    drawerState: DrawerState,
    scope: CoroutineScope,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider
    val secondaryText = if (isDarkMode) DarkGrayText else LightGrayText

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        // Top Bar avec Avatar et Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar / Menu
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) DarkSurface else LightSurface)
                    .clickable { scope.launch { drawerState.open() } },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Logo / Titre
            Text(
                text = "TradeConnect",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TwitterBlue
            )

            // Spacer pour équilibrer
            Spacer(modifier = Modifier.size(36.dp))
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabItem(
                title = "Pour vous",
                isSelected = selectedTab == 0,
                isDarkMode = isDarkMode,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            TabItem(
                title = "Découvrir",  // 🆕 Changé de "Abonnements" à "Découvrir"
                isSelected = selectedTab == 1,
                isDarkMode = isDarkMode,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }

        // Divider
        HorizontalDivider(
            thickness = 0.5.dp,
            color = dividerColor
        )
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryText = if (isDarkMode) DarkGrayText else LightGrayText

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) textColor else secondaryText
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Indicateur
        Box(
            modifier = Modifier
                .width(if (isSelected) 60.dp else 0.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) TwitterBlue else Color.Transparent)
        )
    }
}