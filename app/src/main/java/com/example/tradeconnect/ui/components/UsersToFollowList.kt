package com.example.tradeconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.data.model.User
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UsersToFollowList(
    users: List<User>,
    followingIds: List<String>,
    onToggleFollow: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp) // évite le crash avec LazyColumn dans LazyColumn
    ) {
        items(users) { user ->
            UserFollowItem(
                user = user,
                isFollowing = followingIds.contains(user.uid),
                onToggleFollow = onToggleFollow
            )
            Divider()
        }
    }
}

@Composable
fun UserFollowItem(
    user: User,
    isFollowing: Boolean,
    onToggleFollow: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            // AVATAR THREADS
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.firstOrNull()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "@${user.uid.take(7)}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // BUTTON THREADS STYLE
        Button(
            onClick = { onToggleFollow(user.uid, isFollowing) },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowing)
                    Color.Gray.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .height(36.dp)
                .defaultMinSize(minWidth = 90.dp)
        ) {
            Text(
                text = if (isFollowing) "Abonné(e)" else "Suivre",
                color = if (isFollowing) Color.Black else Color.White
            )
        }
    }

    Divider(thickness = 0.6.dp, color = Color.LightGray.copy(alpha = 0.4f))
}
@Composable
fun rememberFormattedTime(timestamp: Long): String {
    return remember(timestamp) {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
