package com.example.tradeconnect.uii.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.AuthViewModel

@Composable
fun HomeScreen(navController: NavHostController) {

    Column {

        Text("Home")

        Button(onClick = { navController.navigate("feed") }) {
            Text("Go to Feed")
        }

        Button(onClick = { navController.navigate("newTweet") }) {
            Text("Create Tweet")
        }

        Button(onClick = { navController.navigate("chat") }) {
            Text("Chat")
        }
    }
}
