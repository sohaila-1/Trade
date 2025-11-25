package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeconnect.R
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.viewmodel.TweetViewModel
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.filled.Person
import androidx.navigation.NavHostController


// -----------------------------------------------------------
// 🔥 TOP BAR (like Twitter)
// -----------------------------------------------------------
@Composable
fun FeedTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 🟦 Left: Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center: App Logo or Title (assuming a drawable resource for logo)
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "TradeConnect Logo",
            modifier = Modifier.size(32.dp)
        )

        // Right: Placeholder for search or menu (can be expanded)
        Spacer(modifier = Modifier.size(40.dp)) // Balances the left avatar
    }
}

// -----------------------------------------------------------
// 🔥 TABS For you / Subscriptions
// -----------------------------------------------------------
@Composable
fun FeedTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val tabs = listOf("Pour vous", "Abonnements")

        tabs.forEachIndexed { index, title ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(index) }
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == index) Color.Black else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(60.dp)
                        .background(
                            if (selectedTab == index) Color(0xFF1DA1F2)
                            else Color.Transparent
                        )
                )
            }
        }
    }
}

// -----------------------------------------------------------
// 🔥 COMPLETE FEED PAGE
// -----------------------------------------------------------
@Composable
fun FeedScreen(
    navController: NavHostController,
    viewModel: TweetViewModel = TweetViewModel()
)

{
    val tweets by viewModel.tweets.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 🔥 TOP BAR
            FeedTopBar()

            // 🔥 TABS
            FeedTabs(selectedTab, onTabSelected = { selectedTab = it })

            // 🔥 CONTENT BASED ON TAB
            if (selectedTab == 0) {
                // For you
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(tweets) { tweet ->
                        TweetCard(tweet)
                    }
                }
            } else {
                // Subscriptions (empty)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun abonnement pour le moment",
                        color = Color.Gray
                    )
                }
            }
        }

        // 🔥 ADD BUTTON
        FloatingActionButton(
            onClick = { navController.navigate("addTweet") },
            containerColor = Color(0xFF1DA1F2),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Tweet",
                tint = Color.White
            )
        }



    }
}

// -----------------------------------------------------------
// 🔥 TWEET CARD
// -----------------------------------------------------------
@Composable
fun TweetCard(tweet: Tweet) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar with icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1DA1F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = tweet.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "@${tweet.username.lowercase()}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = tweet.content,
            fontSize = 16.sp
        )

        // Assuming Tweet has an optional imageRes for attached images
        tweet.imageRes?.let { resId ->
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Tweet Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            )
        }

        TweetActionsRow(tweet)

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE1E8ED))
        )
    }
}

@Composable
fun TweetActionsRow(tweet: Tweet) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // 💬 Reply
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* TODO: Handle reply */ }
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Reply",
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${tweet.replies}", color = Color.Gray, fontSize = 14.sp)
        }

        // 🔁 Retweet
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* TODO: Handle retweet */ }
        ) {
            Icon(
                imageVector = Icons.Outlined.Repeat,
                contentDescription = "Retweet",
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${tweet.retweets}", color = Color.Gray, fontSize = 14.sp)
        }

        // ❤️ Like
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* TODO: Handle like */ }
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${tweet.likes}", color = Color.Gray, fontSize = 14.sp)
        }

        // 📤 Share
        Icon(
            imageVector = Icons.Outlined.Upload,
            contentDescription = "Share",
            tint = Color.Gray,
            modifier = Modifier
                .size(22.dp)
                .clickable { /* TODO: Handle share */ }
        )
    }
}
