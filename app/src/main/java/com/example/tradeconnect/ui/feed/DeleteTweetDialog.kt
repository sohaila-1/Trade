package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun DeleteTweetDialog(
    show: Boolean,
    tweetId: String,
    viewModel: TweetViewModel,
    onDismiss: () -> Unit
) {
    if (!show) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        Column(Modifier.padding(24.dp)) {

            Text(
                text = "Supprimer",
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.deleteTweet(tweetId)
                        onDismiss()
                    }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Annuler",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
            )
        }
    }
}

