package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.model.Tweet

@Composable
fun TweetItem(tweet: Tweet) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF1DA1F2).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tweet.username.first().uppercase(),
                color = Color(0xFF1DA1F2),
                style = MaterialTheme.typography.subtitle1
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = tweet.username,
                style = MaterialTheme.typography.subtitle1,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tweet.content,
                style = MaterialTheme.typography.body2,
                color = Color.DarkGray
            )
        }
    }
}
