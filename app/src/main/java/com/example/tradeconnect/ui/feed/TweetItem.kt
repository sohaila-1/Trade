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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.ui.components.rememberFormattedTime
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TweetItem(
    tweet: Tweet,
    isDarkMode: Boolean,
    onMoreClick: () -> Unit
) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondary = if (isDarkMode) Color.LightGray else Color.Gray

    // Format timestamp in "hh:mm" or "yesterday"
    val formattedTime = rememberFormattedTime(tweet.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        // AVATAR
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Color.DarkGray.copy(alpha = 0.8f)),
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

            // USERNAME + TIMESTAMP + MORE OPTIONS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = tweet.username,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = formattedTime,
                        color = secondary,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = secondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onMoreClick() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TWEET CONTENT
            Text(
                text = tweet.content,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ACTIONS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Icon(Icons.Default.FavoriteBorder, null, tint = textColor)
                Icon(Icons.Default.ModeComment, null, tint = textColor)
                Icon(Icons.Default.Send, null, tint = textColor)
            }
        }
    }
}
