package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun CreateTweetScreen(
    navController: NavController,
    viewModel: TweetViewModel
) {
    var content by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("What's happening?") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.createTweet(
                    content = content,
                    username = "Sohaila",
                    userId = "12345"
                )
                navController.popBackStack()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Tweet")
        }
    }
}
