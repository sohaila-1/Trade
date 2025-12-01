package com.example.tradeconnect.nagivation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tradeconnect.ui.feed.CreateTweetScreen
import com.example.tradeconnect.ui.feed.FeedScreen
import com.example.tradeconnect.ui.feed.FeedScreenDark
import com.example.tradeconnect.uii.chat.ChatScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String
) {
    val tweetViewModel: TweetViewModel = viewModel()

    // 🌙 Theme state (light/dark)
    var isDarkMode by remember { mutableStateOf(false) }

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
            HomeScreen(navController)
        }

        composable("chat") {
            ChatScreen(navController)
        }

        // 📌 VERSION AVEC THÈME DYNAMIQUE
        composable("feed") {
            if (isDarkMode) {
                FeedScreenDark(
                    navController = navController,
                    viewModel = tweetViewModel,
                    isDarkMode = isDarkMode,
                    onToggleTheme = { isDarkMode = !isDarkMode }
                )
            } else {
                FeedScreen(
                    navController = navController,
                    viewModel = tweetViewModel,
                    isDarkMode = isDarkMode,
                    onToggleTheme = { isDarkMode = !isDarkMode }
                )
            }
        }

        composable("newTweet") {
            CreateTweetScreen(
                navController = navController,
                viewModel = tweetViewModel
            )
        }
    }
}
