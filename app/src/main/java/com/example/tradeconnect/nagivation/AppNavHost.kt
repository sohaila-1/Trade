package com.example.tradeconnect.nagivation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tradeconnect.ui.feed.AddTweetScreen
import com.example.tradeconnect.uii.chat.ChatScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.ui.feed.FeedScreen

@Composable
fun AppNavHost(
navController: NavHostController,
authViewModel: AuthViewModel,
startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        composable("signup") {
            SignUpScreen(navController, authViewModel)
        }

        composable("home") {
            FeedScreen(navController = navController)
        }

        composable("chat") {
            ChatScreen(navController)
        }

        // ⚠️ SUPPRIMÉ "feed" car doublon avec "home"
        // composable("feed") { FeedScreen() } → inutile / erreur

        composable("addTweet") {
            AddTweetScreen(
                viewModel = viewModel(),
                onTweetAdded = { navController.popBackStack() }
            )
        }
    }
}