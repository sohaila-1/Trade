package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.model.Tweet

@Composable
fun TweetItemDark(tweet: Tweet) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {

        Row {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tweet.username.first().uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.subtitle1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = tweet.username,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tweet.content,
                    style = MaterialTheme.typography.body2,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ACTION BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 58.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.LightGray)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Repeat, null, tint = Color.LightGray)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.FavoriteBorder, null, tint = Color.LightGray)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Share, null, tint = Color.LightGray)
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
