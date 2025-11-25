package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun AddTweetScreen(
    viewModel: TweetViewModel,
    onTweetAdded: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Nouveau Tweet",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Quoi de neuf ?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (text.isNotBlank()) {
                    viewModel.addTweet(text)
                    onTweetAdded()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Publier")
        }
    }
}
