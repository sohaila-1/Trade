package com.example.tradeconnect.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Firestore
import com.google.firebase.firestore.FirebaseFirestore

// Repositories
import com.example.tradeconnect.repository.TweetRepository
import com.example.tradeconnect.repository.UserRepository
import com.example.tradeconnect.repository.FollowRepository

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
    // ------------------------------
    // INIT REPOSITORIES
    // ------------------------------
    val tweetRepo = TweetRepository(
        firestore = FirebaseFirestore.getInstance()   // ✅ correction
    )
    val followRepo = FollowRepository()
    val userRepo = UserRepository()

    // Auth ViewModel déjà fourni
    val authVM = authViewModel

    // ------------------------------
    // INIT TWEET VIEWMODEL
    // ------------------------------
    val tweetVM = viewModel<TweetViewModel>(
        factory = TweetViewModel.Factory(
            tweetRepo = tweetRepo,
            followRepo = followRepo,
            authVM = authVM,
            userRepo = userRepo
        )
    )

    var isDarkMode by remember { mutableStateOf(false) }

    // ------------------------------
    // NAVIGATION
    // ------------------------------
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // LOGIN
        composable("login") {
            LoginScreen(navController, authVM)
        }

        // SIGNUP
        composable("signup") {
            SignUpScreen(navController, authVM)
        }

        // HOME
        composable("home") {
            HomeScreen(navController)
        }

        // CHAT
        composable("chat") {
            ChatScreen(navController)
        }

        // FEED
        composable("feed") {
            FeedScreen(
                navController = navController,
                viewModel = tweetVM,
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }
            )
        }

        // CREATE TWEET
        composable("createTweet") {
            CreateTweetScreen(navController, tweetVM)
        }

        // EDIT TWEET
        composable(
            route = "editTweet/{tweetId}",
            arguments = listOf(navArgument("tweetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tweetId = backStackEntry.arguments?.getString("tweetId") ?: ""

            EditTweetScreen(
                navController = navController,
                tweetId = tweetId,
                viewModel = tweetVM
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
