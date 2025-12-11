package com.example.tradeconnect.uii.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.AuthViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Feed")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("userprofile") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TBlue, contentColor = androidx.compose.ui.graphics.Color.White)
        ) {
            Text("User Profile")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("chat") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TBlue, contentColor = androidx.compose.ui.graphics.Color.White)
        ) {
            Text("Chat")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
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
            Text("Logout")
        }
    }
}
