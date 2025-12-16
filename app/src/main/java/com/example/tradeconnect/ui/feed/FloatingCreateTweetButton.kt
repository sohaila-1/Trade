package com.example.tradeconnect.ui.feed

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.ui.theme.TwitterBlue

@Composable
fun FloatingCreateTweetButton(onClick: () -> Unit) {
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = TwitterBlue,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
            .size(60.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = TwitterBlue.copy(alpha = 0.4f),
                spotColor = TwitterBlue.copy(alpha = 0.4f)
            )
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Nouveau Tweet",
            modifier = Modifier.size(28.dp)
        )
    }
}