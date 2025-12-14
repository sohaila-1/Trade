package com.example.tradeconnect.nagivation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.repository.IAuthRepository
import com.example.tradeconnect.data.repository.IProfileRepository
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.repository.FollowRepository
import com.example.tradeconnect.repository.TweetRepository
import com.example.tradeconnect.repository.UserRepository
import com.example.tradeconnect.ui.chat.ChatListScreen
import com.example.tradeconnect.ui.chat.ChatScreen
import com.example.tradeconnect.ui.chat.UserSearchScreen
import com.example.tradeconnect.ui.feed.BookmarkScreen
import com.example.tradeconnect.ui.feed.CreateTweetScreen
import com.example.tradeconnect.ui.feed.EditTweetScreen
import com.example.tradeconnect.ui.feed.FeedScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.uii.user.UserProfileScreen
import com.example.tradeconnect.util.NetworkObserver
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.ChatListViewModel
import com.example.tradeconnect.viewmodel.ProfileViewModel
import com.example.tradeconnect.viewmodel.TweetViewModel
import com.example.tradeconnect.viewmodel.UserSearchViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    authRepo: IAuthRepository,
    profileRepo: IProfileRepository,
    startDestination: String,
    appDatabase: AppDatabase,
    networkObserver: NetworkObserver,
    messageRepository: MessageRepository
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
//        composable("home") {
//            HomeScreen(navController)
//        }

        composable("chat") {
            val viewModel: ChatListViewModel = viewModel(
                factory = ChatListViewModel.Factory(messageRepository)
            )
            ChatListScreen(navController, viewModel)
        }

        //User search screen (accessed from chat list)
        composable("user_search") {
            val viewModel: UserSearchViewModel = viewModel(
                factory = UserSearchViewModel.Factory(messageRepository)
            )
            UserSearchScreen(navController, viewModel)
        }

        composable(
            route = "chat/{partnerId}/{partnerName}",
            arguments = listOf(
                navArgument("partnerId") { type = NavType.StringType },
                navArgument("partnerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId") ?: return@composable

            ChatScreen(
                navController = navController,
                partnerId = partnerId,
                messageRepository = messageRepository,
                networkObserver = networkObserver
            )
        }

        composable("profile") {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepo, authRepo)
            )
            UserProfileScreen(navController, profileViewModel)
        }

//         CHAT
//        composable("chat") {
//            ChatScreen(navController)
//        }

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

        // PROFILE //this is souhaia name for profilescreen
//        composable("profile") {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) { Text("Profile screen") }
//        }

        // SETTINGS
        composable("settings") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Settings screen") }
        }

        // BOOKMARKS
        composable(route = "bookmarks") {
            BookmarkScreen(
                navController = navController,
                viewModel = tweetVM,                // ✅ nom correct
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }   // ✅ fonction locale
            )
        }
    }
}
