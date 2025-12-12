package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.ui.components.rememberFormattedTime

@Composable
fun TweetItem(
    tweet: Tweet,
    isDarkMode: Boolean,
    currentUserId: String,
    onMoreClick: () -> Unit
) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondary = if (isDarkMode) Color.LightGray else Color.Gray

    val formattedTime = rememberFormattedTime(tweet.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        // ---- Avatar ----
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Color(0xFF444444)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tweet.username.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            // ---- Username + Time ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tweet.username,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedTime,
                    color = secondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tweet.content,
                color = textColor,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Icons ----
            Row {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(20.dp))

                Icon(Icons.Filled.ModeComment, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(20.dp))

                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = textColor)
            }
        }

        // ⭐ MENU visible seulement pour l'auteur du tweet
        if (tweet.userId == currentUserId) {
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options",
                    tint = textColor
                )
            }
        }
    }
}
