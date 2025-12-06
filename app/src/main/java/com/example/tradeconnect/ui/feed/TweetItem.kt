package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.model.Tweet

@Composable
fun TweetItem(
    tweet: Tweet,
    isDarkMode: Boolean,
    onMoreClick: () -> Unit
) {
    val textColor = if (isDarkMode) Color.White else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tweet.username.firstOrNull()?.uppercase() ?: "?",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tweet.username, color = textColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(tweet.timestamp.toString(), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(tweet.content, color = textColor)

            Spacer(modifier = Modifier.height(12.dp))

            Row {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(22.dp))
                Icon(Icons.Default.ModeComment, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(22.dp))
                Icon(Icons.Default.Send, contentDescription = null, tint = textColor)
            }
        }

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier
                .padding(start = 12.dp)
                .clickable { onMoreClick() }
        )
    }
}
