package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.ui.components.TwitterBlue
import com.example.tradeconnect.ui.components.rememberFormattedTime
import com.example.tradeconnect.ui.theme.DarkGrayText
import com.example.tradeconnect.ui.theme.DarkText
import com.example.tradeconnect.ui.theme.LightGrayText
import com.example.tradeconnect.ui.theme.LightText

@Composable
fun TweetItem(
    tweet: Tweet,
    isDarkMode: Boolean,
    currentUserId: String,
    onMoreClick: () -> Unit,
    onLike: (String) -> Unit,
    onSave: (String) -> Unit
) {
    val bg = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) DarkText else LightText
    val secondary = if (isDarkMode) DarkGrayText else LightGrayText

    val formattedTime = rememberFormattedTime(tweet.timestamp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        Row(modifier = Modifier.fillMaxWidth()) {

            // --- Avatar ---
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF2F3336) else Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tweet.username.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        tweet.username,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        formattedTime,
                        color = secondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    tweet.content,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // LIKE ❤️
                    IconButton(onClick = { onLike(tweet.id) }) {
                        Icon(
                            imageVector = if (tweet.likes.contains(currentUserId))
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (tweet.likes.contains(currentUserId)) Color.Red else textColor
                        )
                    }

                    // Compteur de likes
                    Text(
                        text = tweet.likes.size.toString(),
                        color = secondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )


                    // SAVE 🔖
                    IconButton(onClick = { onSave(tweet.id) }) {
                        Icon(
                            imageVector = if (tweet.saves.contains(currentUserId))
                                Icons.Filled.Bookmark
                            else
                                Icons.Filled.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (tweet.saves.contains(currentUserId)) TwitterBlue else textColor
                        )
                    }
                }
            }

            if (tweet.userId == currentUserId) {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = secondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = secondary.copy(alpha = 0.2f), thickness = 0.5.dp)
    }
}
