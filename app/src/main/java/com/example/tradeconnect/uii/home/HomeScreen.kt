// app/src/main/java/com/example/tradeconnect/uii/home/HomeScreen.kt
package com.example.tradeconnect.uii.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("chat_list") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TBlue,
                contentColor = Color.White
            )
        ) {
            Text("💬 Messages")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                userViewModel?.updateOnlineStatus(false)
                viewModel.logout {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("Déconnexion")
        }
    }
}