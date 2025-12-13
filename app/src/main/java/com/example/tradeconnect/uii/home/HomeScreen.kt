package com.example.tradeconnect.uii.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.AuthViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val isDarkMode = false

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "feed"
            )

        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            Spacer(modifier = Modifier.height(32.dp))

            // 🔵 HEADER
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🧩 CARD PRINCIPALE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Welcome to TradeConnect",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔹 FEED BUTTON
                    Button(
                        onClick = { navController.navigate("feed") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TBlue
                        )
                    ) {
                        Text("Go to Feed")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔹 CHAT BUTTON
                    OutlinedButton(
                        onClick = { navController.navigate("chatList") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Messages")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { viewModel.logout() }
            ) {
                Text(
                    text = "Logout",
                    color = Color.Gray
                )
            }
        }
    }
}
}
