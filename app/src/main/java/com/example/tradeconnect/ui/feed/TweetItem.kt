package com.example.tradeconnect.ui.feed

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.ui.components.rememberFormattedTime
import com.example.tradeconnect.ui.theme.*

@Composable
fun TweetItem(
    tweet: Tweet,
    isDarkMode: Boolean,
    currentUserId: String,
    onMoreClick: () -> Unit,
    onLike: (String) -> Unit,
    onSave: (String) -> Unit,
    onRetweet: (String) -> Unit = {},  // 🆕 Callback retweet
    onUserClick: ((String) -> Unit)? = null,
    onCommentClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider

    val formattedTime = rememberFormattedTime(tweet.timestamp)

    val isLiked = tweet.likes.contains(currentUserId)
    val isSaved = tweet.saves.contains(currentUserId)
    val isRetweeted = tweet.retweets.contains(currentUserId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TwitterBlue)
                    .clickable(enabled = onUserClick != null) {
                        onUserClick?.invoke(tweet.userId)
                    },
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
                // Header: Username + Time + More
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Username
                    Text(
                        text = tweet.username,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable(enabled = onUserClick != null) {
                            onUserClick?.invoke(tweet.userId)
                        }
                    )

                    Text(
                        text = " · ",
                        color = secondaryColor,
                        fontSize = 14.sp
                    )

                    // Time
                    Text(
                        text = formattedTime,
                        color = secondaryColor,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // More button (seulement pour mes tweets)
                    if (tweet.userId == currentUserId) {
                        IconButton(
                            onClick = onMoreClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreHoriz,
                                contentDescription = "More",
                                tint = secondaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Content - Cliquable pour ouvrir les commentaires
                Text(
                    text = tweet.content,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.clickable(enabled = onCommentClick != null) {
                        onCommentClick?.invoke(tweet.id)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Comments
                    ActionButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        count = tweet.commentsCount,
                        isActive = false,
                        activeColor = TwitterBlue,
                        inactiveColor = secondaryColor,
                        onClick = { onCommentClick?.invoke(tweet.id) }
                    )

                    // 🆕 Retweet
                    ActionButton(
                        icon = if (isRetweeted) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                        count = tweet.retweets.size,
                        isActive = isRetweeted,
                        activeColor = RetweetGreen,
                        inactiveColor = secondaryColor,
                        onClick = { onRetweet(tweet.id) }
                    )

                    // Like
                    ActionButton(
                        icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        count = tweet.likes.size,
                        isActive = isLiked,
                        activeColor = LikeRed,
                        inactiveColor = secondaryColor,
                        onClick = { onLike(tweet.id) }
                    )

                    // Bookmark
                    ActionButton(
                        icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        count = null,
                        isActive = isSaved,
                        activeColor = TwitterBlue,
                        inactiveColor = secondaryColor,
                        onClick = { onSave(tweet.id) }
                    )

                    // 🆕 Share
                    ActionButton(
                        icon = Icons.Outlined.Share,
                        count = null,
                        isActive = false,
                        activeColor = TwitterBlue,
                        inactiveColor = secondaryColor,
                        onClick = {
                            val shareText = "${tweet.username} sur TradeConnect:\n\n${tweet.content}"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Partager via")
                            context.startActivity(shareIntent)
                        }
                    )
                }
            }
        }

        // Divider
        HorizontalDivider(
            thickness = 0.5.dp,
            color = dividerColor
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    count: Int?,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val color = if (isActive) activeColor else inactiveColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatCount(count),
                color = color,
                fontSize = 13.sp
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}