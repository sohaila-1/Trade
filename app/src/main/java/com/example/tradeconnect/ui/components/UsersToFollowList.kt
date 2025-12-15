package com.example.tradeconnect.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.ui.theme.LightText

// 🌟 Couleurs Twitter
val TwitterBlue = Color(0xFF1DA1F2)
val TwitterBlueDark = Color(0xFF1A8CD8)
val LightGrayDark = Color(0xFF9CA3AF)

// 🔥 LISTE DES UTILISATEURS
@Composable
fun UsersToFollowList(
    users: List<User>,
    followingIds: List<String>,
    getLastTweet: (String) -> String,
    onToggleFollow: (String, Boolean) -> Unit,
    onUserClick: ((String) -> Unit)? = null  // 🆕 Callback pour clic sur utilisateur
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        items(users) { user ->
            UserFollowItem(
                user = user,
                isFollowing = followingIds.contains(user.uid),
                lastTweet = getLastTweet(user.uid),
                onToggleFollow = onToggleFollow,
                onUserClick = onUserClick  // 🆕
            )
        }
    }
}

@Composable
fun UserFollowItem(
    user: User,
    isFollowing: Boolean,
    lastTweet: String,
    onToggleFollow: (String, Boolean) -> Unit,
    onUserClick: ((String) -> Unit)? = null  // 🆕
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onUserClick != null) {
                onUserClick?.invoke(user.uid)
            }
            .padding(vertical = 10.dp, horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {

                Avatar(username = user.username)

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        user.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "@${user.uid.take(7)}",
                        color = if (isDark) LightGrayDark else Color.Gray
                    )
                    // 🆕 Statut en ligne
                    if (user.isOnline) {
                        Text(
                            text = "🟢 En ligne",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            FollowButton(
                isFollowing = isFollowing,
                onClick = { onToggleFollow(user.uid, isFollowing) }
            )
        }

        if (lastTweet.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = lastTweet,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDark) Color.White else Color.Black,
                modifier = Modifier.padding(start = 56.dp),
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            thickness = 0.6.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun Avatar(username: String) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .size(45.dp)
            .background(
                color = if (isDark) Color.DarkGray else Color.LightGray.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.firstOrNull()?.uppercase() ?: "?",
            color = if (isDark) Color.White else Color.Black
        )
    }
}

@Composable
fun FollowButton(
    isFollowing: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isFollowing) Color.LightGray.copy(alpha = 0.25f) else TwitterBlue
    val textColor = if (isFollowing) LightText else Color.White

    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        modifier = Modifier.height(34.dp)
    ) {
        Text(
            text = if (isFollowing) "Abonné(e)" else "Suivre",
            fontWeight = FontWeight.SemiBold
        )
    }
}