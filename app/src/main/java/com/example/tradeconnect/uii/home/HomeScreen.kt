// app/src/main/java/com/example/tradeconnect/uii/home/HomeScreen.kt
package com.example.tradeconnect.uii.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    userViewModel: UserViewModel? = null
) {
    val currentUser by userViewModel?.currentUser?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Home Feed",
            fontSize = 24.sp
        )
fun HomeScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val isDarkMode = false

        // Afficher le nom de l'utilisateur si disponible
        currentUser?.let { user ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bienvenue, ${user.username}",
                color = Color.Gray
            )
            Text(
                text = "${user.followersCount} followers · ${user.followingCount} abonnements",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "feed"
            )

        // Bouton Découvrir des utilisateurs
        Button(
            onClick = { navController.navigate("discover_users") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TBlue,
                contentColor = Color.White
            )
        ) {
            Text("🔍 Découvrir des utilisateurs")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bouton Mon profil
        Button(
            onClick = {
                val userId = userViewModel?.getCurrentUserId()
                if (userId != null) {
                    navController.navigate("other_profile/$userId")
                } else {
                    navController.navigate("userprofile")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TBlue,
                contentColor = Color.White
            )
        ) {
            Text("👤 Mon Profil")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("userprofile") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TBlue,
                contentColor = Color.White
            )
        ) {
            Text("⚙️ Modifier mon profil")
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TBlue,
                contentColor = Color.White
            )
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("💬 Messages")
        }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                userViewModel?.updateOnlineStatus(false)
                viewModel.logout {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
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
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("Déconnexion")
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
