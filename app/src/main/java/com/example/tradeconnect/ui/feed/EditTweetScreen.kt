package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.viewmodel.TweetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTweetScreen(
    navController: NavController,
    tweetId: String,
    viewModel: TweetViewModel
) {
    val tweet = viewModel.getTweetById(tweetId)
    var content by remember { mutableStateOf(tweet?.content ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le tweet") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenu du tweet") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // ANNULER
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Annuler")
                }

                // ENREGISTRER
                Button(
                    onClick = {
                        viewModel.editTweet(tweetId, content)
                        navController.popBackStack()
                    }
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }
}
