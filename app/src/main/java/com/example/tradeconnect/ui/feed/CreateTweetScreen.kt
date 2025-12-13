package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.viewmodel.TweetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTweetScreen(
    navController: NavController,
    viewModel: TweetViewModel
) {
    var content by remember { mutableStateOf("") }
    val user = viewModel.authVM.currentUser.value

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CANCEL
                Text(
                    text = "Annuler",
                    color = Color(0xFF1DA1F2),
                    modifier = Modifier.clickable { navController.popBackStack() }
                )

                // POST BUTTON
                Text(
                    text = "Poster",
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            if (content.isNotBlank()) Color(0xFF1DA1F2) else Color(0xFF9ECFEF),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(enabled = content.isNotBlank()) {
                            viewModel.createTweet(content)
                            navController.popBackStack()
                        }
                )
            }
        },

        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "feed"
            )

        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // PROFILE + TEXT INPUT
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // AVATAR
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Quoi de neuf ?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ICON BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF1DA1F2))
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF1DA1F2))
                Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF1DA1F2))
                Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF1DA1F2))
            }
        }
    }
}
