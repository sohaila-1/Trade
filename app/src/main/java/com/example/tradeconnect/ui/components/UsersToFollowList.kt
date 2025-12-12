package com.example.tradeconnect.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.data.model.User

// 🔥 LISTE DES UTILISATEURS À SUIVRE
@Composable
fun UsersToFollowList(
    users: List<User>,
    followingIds: List<String>,
    getLastTweet: (String) -> String,
    onToggleFollow: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    )
 {
     items(users) { user ->
         UserFollowItem(
             user = user,
             isFollowing = followingIds.contains(user.uid),
             lastTweet = getLastTweet(user.uid),
             onToggleFollow = onToggleFollow
         )
     }

    }
}

// 🔥 UN ITEM : UN USER + SON DERNIER TWEET + BOUTON SUIVRE
@Composable
fun UserFollowItem(
    user: User,
    isFollowing: Boolean,
    lastTweet: String,
    onToggleFollow: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- SECTION GAUCHE : AVATAR + NOMS
            Row(verticalAlignment = Alignment.CenterVertically) {

                Avatar(user.username)

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        user.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "@${user.uid.take(7)}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // --- BOUTON SUIVRE
            FollowButton(
                isFollowing = isFollowing,
                onClick = { onToggleFollow(user.uid, isFollowing) }
            )
        }

        // 🔥 AFFICHER LE DERNIER TWEET
        if (lastTweet.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastTweet,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 56.dp) // aligné avec texte
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))
    }
}

//////////////////////////////////////////////
// 🔧 AVATAR COMPOSABLE
//////////////////////////////////////////////
@Composable
fun Avatar(name: String) {
    Box(
        modifier = Modifier
            .size(45.dp)
            .background(Color.LightGray.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

//////////////////////////////////////////////
// 🔧 FOLLOW BUTTON COMPOSABLE
//////////////////////////////////////////////
@Composable
fun FollowButton(
    isFollowing: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing)
                Color.LightGray.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = if (isFollowing) "Abonné(e)" else "Suivre",
            color = if (isFollowing) Color.Black else Color.White
        )
    }
}
