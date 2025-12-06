package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.R
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.viewmodel.TweetViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTweetScreen(
    navController: NavController,
    viewModel: TweetViewModel
) {
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ANNULER
                Text(
                    text = "Annuler",
                    color = Color(0xFF1DA1F2),
                    modifier = Modifier.clickable { navController.popBackStack() }
                )

                // POSTER
                Text(
                    text = "Poster",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF1DA1F2), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            viewModel.createTweet(content)
                            navController.popBackStack()
                        }
                )
            }
        },

        // ⭐ FOOTER AVEC FIX isDarkMode ⭐
        bottomBar = {
            BottomNavBar(
                navController = navController,
                isDarkMode = false   // <<< FIX IMPORTANT
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // CHAMP DE SAISIE DU TWEET
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
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ICÔNES TWITTER-LIKE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
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
