package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun FeedScreenDark(
    navController: NavController,
    viewModel: TweetViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
){
    val tweets = viewModel.tweets.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home", color = Color.White) },
                backgroundColor = Color.Black,
                elevation = 0.dp,
                actions = {
                    TextButton(onClick = { onToggleTheme() }) {
                        Text(
                            text = if (isDarkMode) "Light" else "Dark",
                            color = Color.White
                        )
                    }
                }
            )
        },
        backgroundColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("newTweet") },
                backgroundColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Tweet", tint = Color.Black)
            }
        }
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 12.dp)
        ) {
            items(tweets) { tweet ->
                TweetItemDark(tweet)
                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
            }
        }
    }
}
