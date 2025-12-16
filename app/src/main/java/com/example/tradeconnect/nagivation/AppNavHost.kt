// app/src/main/java/com/example/tradeconnect/nagivation/AppNavHost.kt
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
import com.example.tradeconnect.ui.notifications.NotificationsScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tradeconnect.ui.feed.TweetDetailScreen

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
import com.example.tradeconnect.ui.follow.DiscoverUsersScreen
import com.example.tradeconnect.ui.follow.FollowListScreen
import com.example.tradeconnect.ui.follow.FollowListType
import com.example.tradeconnect.ui.follow.UserPublicProfileScreen
import com.example.tradeconnect.ui.settings.SettingsScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.uii.user.UserProfileScreen
import com.example.tradeconnect.util.NetworkObserver
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.ChatListViewModel
import com.example.tradeconnect.viewmodel.FollowViewModel
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
        firestore = FirebaseFirestore.getInstance()
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

    // ------------------------------
    // INIT FOLLOW VIEWMODEL
    // ------------------------------
    val followVM: FollowViewModel = viewModel(
        factory = FollowViewModel.Factory(followRepo)
    )

    var isDarkMode by remember { mutableStateOf(false) }

    // ------------------------------
    // NAVIGATION
    // ------------------------------
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ==================== AUTH ====================

        // LOGIN
        composable("login") {
            LoginScreen(navController, authVM)
        }

        // SIGNUP
        composable("signup") {
            SignUpScreen(navController, authVM)
        }

        // ==================== CHAT ====================

        composable("chat") {
            val viewModel: ChatListViewModel = viewModel(
                factory = ChatListViewModel.Factory(messageRepository)
            )
            ChatListScreen(
                navController = navController,
                viewModel = viewModel,
                isDarkMode = isDarkMode
            )
        }

        // User search screen (accessed from chat list)
        composable("user_search") {
            val viewModel: UserSearchViewModel = viewModel(
                factory = UserSearchViewModel.Factory(messageRepository)
            )
            UserSearchScreen(
                navController = navController,
                viewModel = viewModel,
                isDarkMode = isDarkMode
            )
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
                networkObserver = networkObserver,
                isDarkMode = isDarkMode
            )
        }

        // ==================== PROFILE ====================

        // Mon profil (consultation)
        composable("profile") {
            val userId = authVM.getCurrentUserId() ?: return@composable
            UserPublicProfileScreen(
                navController = navController,
                followViewModel = followVM,
                userId = userId,
                isDarkMode = isDarkMode,
                tweetViewModel = tweetVM
            )
        }

        // Mon profil (édition)
        composable("edit_profile") {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepo, authRepo)
            )
            UserProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                isDarkMode = isDarkMode
            )
        }

        // ==================== FEED ====================

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
            CreateTweetScreen(navController, tweetVM,
                isDarkMode = isDarkMode )
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

        // BOOKMARKS
        composable(route = "bookmarks") {
            BookmarkScreen(
                navController = navController,
                viewModel = tweetVM,
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }
            )
        }

        // ==================== FOLLOW SYSTEM ====================

        // Profil public d'un utilisateur
        composable(
            route = "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserPublicProfileScreen(
                navController = navController,
                followViewModel = followVM,
                userId = userId,
                tweetViewModel = tweetVM
            )
        }
        composable(
            route = "tweet_detail/{tweetId}",
            arguments = listOf(navArgument("tweetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tweetId = backStackEntry.arguments?.getString("tweetId") ?: return@composable
            TweetDetailScreen(
                navController = navController,
                tweetId = tweetId,
                viewModel = tweetVM,
                isDarkMode = isDarkMode
            )
        }
        // Liste des followers
        composable(
            route = "followers/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            FollowListScreen(
                navController = navController,
                followViewModel = followVM,
                userId = userId,
                listType = FollowListType.FOLLOWERS,
            )
        }

        // Liste des abonnements (following)
        composable(
            route = "following/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            FollowListScreen(
                navController = navController,
                followViewModel = followVM,
                userId = userId,
                listType = FollowListType.FOLLOWING,
            )
        }
// Découvrir des utilisateurs
        composable("discover") {
            DiscoverUsersScreen(
                navController = navController,
                followViewModel = followVM,
            )
        }
        // ==================== SETTINGS ====================
// Dans le NavHost { ... }, ajoute :
        composable("notifications") {
            NotificationsScreen(
                navController = navController,
                isDarkMode = isDarkMode
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                authViewModel = authVM,
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }
            )
        }
    }
}