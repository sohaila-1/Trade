package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: TweetViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val tweets = viewModel.tweets.value
    var selectedTab by remember { mutableStateOf(0) } // 0 = Pour vous, 1 = Abonnements

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Home") },
                    backgroundColor = Color.White,
                    elevation = 0.dp,
                    actions = {
                        TextButton(onClick = onToggleTheme) {
                            Text(
                                if (isDarkMode) "Light" else "Dark",
                                color = Color(0xFF1DA1F2)
                            )
                        }
                    }
                )

                // ---- TABS ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabItem(
                        title = "Pour vous",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    TabItem(
                        title = "Abonnements",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("newTweet") },
                backgroundColor = Color(0xFF1DA1F2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tweet")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {

            val filteredTweets = if (selectedTab == 0) {
                tweets  // Pour vous : tous les tweets
            } else {
                tweets.filter { it.username == "Sohaila" }
            }

            items(filteredTweets) { tweet ->
                TweetItem(tweet = tweet, viewModel = viewModel) // <--- CORRECT
                Divider(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
fun TabItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (selected) Color.Black else Color.Gray,
            style = MaterialTheme.typography.subtitle1
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (selected) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(40.dp)
                    .background(Color.Black)
            )
        }
    }
}
