package com.example.tradeconnect.navigation


import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// UI Screens
import com.example.tradeconnect.ui.feed.FeedScreen
import com.example.tradeconnect.ui.feed.CreateTweetScreen
import com.example.tradeconnect.ui.feed.EditTweetScreen

import com.example.tradeconnect.uii.chat.ChatScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen

// ViewModels
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String
) {
    val tweetViewModel: TweetViewModel = viewModel()

    var isDarkMode by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // LOGIN
        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        // SIGNUP
        composable("signup") {
            SignUpScreen(navController, authViewModel)
        }

        // HOME
        composable("home") {
            HomeScreen(navController)
        }

        // CHAT
        composable("chat") {
            ChatScreen(navController)
        }

        // FEED (Accueil Tweets)
        composable("feed") {
            FeedScreen(
                navController = navController,
                viewModel = tweetViewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }
            )
        }

        // CREATE NEW TWEET
        composable("createTweet") {
            CreateTweetScreen(navController, tweetViewModel)
        }


        // EDIT TWEET — Route dynamique
        composable(
            route = "editTweet/{tweetId}",
            arguments = listOf(navArgument("tweetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tweetId = backStackEntry.arguments?.getString("tweetId") ?: ""

            EditTweetScreen(
                navController = navController,
                tweetId = tweetId,
                viewModel = tweetViewModel
            )
        }



        // PROFILE
        composable("profile") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Profile screen") }
        }

        // SETTINGS
        composable("settings") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Settings screen") }
        }

        // BOOKMARKS
        composable("bookmarks") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Bookmarks screen") }
        }
    }
}
