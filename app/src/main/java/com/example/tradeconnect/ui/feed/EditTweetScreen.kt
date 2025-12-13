package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.viewmodel.TweetViewModel
import com.example.tradeconnect.ui.feed.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTweetScreen(
    navController: NavController,
    tweetId: String,
    viewModel: TweetViewModel
) {
    val tweet = viewModel.getTweetById(tweetId)
    var content by remember { mutableStateOf(tweet?.content ?: "") }

    val TwitterBlue = Color(0xFF1DA1F2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le tweet") }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TwitterBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(TwitterBlue)
                    )
                ) {
                    Text("Annuler")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        viewModel.editTweet(tweetId, content)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TwitterBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }
}
