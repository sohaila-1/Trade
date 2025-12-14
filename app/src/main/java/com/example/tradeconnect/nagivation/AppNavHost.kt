// app/src/main/java/com/example/tradeconnect/nagivation/AppNavHost.kt
package com.example.tradeconnect.nagivation
package com.example.tradeconnect.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.repository.IAuthRepository
import com.example.tradeconnect.data.repository.IProfileRepository
import com.example.tradeconnect.data.repository.IUserRepository
import com.google.firebase.firestore.FirebaseFirestore

// Repositories
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.data.repository.ProfileRepository
import com.example.tradeconnect.repository.AuthRepository
import com.example.tradeconnect.repository.TweetRepository
import com.example.tradeconnect.repository.UserRepository
import com.example.tradeconnect.repository.FollowRepository
import com.example.tradeconnect.repository.IProfileRepository

// UI
import com.example.tradeconnect.ui.chat.ChatListScreen
import com.example.tradeconnect.ui.chat.ChatScreen
import com.example.tradeconnect.ui.chat.UserSearchScreen
import com.example.tradeconnect.ui.user.DiscoverUsersScreen
import com.example.tradeconnect.ui.user.FollowListScreen
import com.example.tradeconnect.ui.user.FollowListType
import com.example.tradeconnect.ui.user.OtherUserProfileScreen
import com.example.tradeconnect.ui.feed.*
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.uii.user.UserProfileScreen
import com.example.tradeconnect.util.NetworkObserver

// ViewModels
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.TweetViewModel
import com.example.tradeconnect.viewmodel.ChatListViewModel
import com.example.tradeconnect.viewmodel.ProfileViewModel
import com.example.tradeconnect.viewmodel.UserSearchViewModel
import com.example.tradeconnect.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    authRepo: IAuthRepository,
    profileRepo: IProfileRepository,
    userRepo: IUserRepository,  // NOUVEAU paramètre
    startDestination: String,
    messageRepository: MessageRepository,
    networkObserver: NetworkObserver
) {
    // Créer le UserViewModel une seule fois pour tout le NavHost
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.Factory(userRepo)
    )


    // ------------------------------
    // REPOSITORIES
    // ------------------------------
    val tweetRepo = remember {
        TweetRepository(FirebaseFirestore.getInstance())
    }
    val followRepo = remember { FollowRepository() }
    val userRepo = remember { UserRepository() }


    val profileRepo: IProfileRepository = remember {
            ProfileRepository(
            firestore = FirebaseFirestore.getInstance()
        )
    }

    val authRepo = remember {
        AuthRepository(
            auth = FirebaseAuth.getInstance(),
            firestore = FirebaseFirestore.getInstance()
        )
    }



    // ------------------------------
// PROFILE REPOSITORIES
// ------------------------------


    // ------------------------------
    // VIEWMODELS
    // ------------------------------
    val tweetVM: TweetViewModel = viewModel(
        factory = TweetViewModel.Factory(
            tweetRepo = tweetRepo,
            followRepo = followRepo,
            authVM = authViewModel,
            userRepo = userRepo
        )
    )

    val chatListVM: ChatListViewModel = viewModel(
        factory = ChatListViewModel.Factory(messageRepository)
    )


    var isDarkMode by remember { mutableStateOf(false) }

    // ------------------------------
    // NAVIGATION
    // ------------------------------
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ---------- AUTH ----------
        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        composable("signup") {
            SignUpScreen(navController, authViewModel)
        }

        // ---------- HOME ----------
        composable("home") {
            HomeScreen(navController, authViewModel, userViewModel)
        }

        // Chat list screen
        composable("chat_list") {
            val viewModel: ChatListViewModel = viewModel(
                factory = ChatListViewModel.Factory(messageRepository)
        // ---------- FEED ----------
        composable("feed") {
            FeedScreen(
                navController = navController,
                viewModel = tweetVM,
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }
            )
        }

        // User search screen (pour nouveau message)
        composable("profile") {

            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(
                    profileRepo = profileRepo,
                    authRepo = authRepo
                )
            )

            UserProfileScreen(
                navController = navController,
                viewModel = profileViewModel
            )
        }


            // ---------- NOTIFICATIONS ----------

            // ---------- CHAT LIST ----------
        composable("chatList") {
            ChatListScreen(
                navController = navController,
                viewModel = chatListVM
            )
        }

        // ---------- USER SEARCH ----------
        composable("user_search") {
            val searchViewModel: UserSearchViewModel = viewModel(
                factory = UserSearchViewModel.Factory(messageRepository)
            val userSearchVM: UserSearchViewModel = viewModel(
                factory = UserSearchViewModel.Factory(UserRepository())
            )
            UserSearchScreen(navController, searchViewModel, userViewModel)

            UserSearchScreen(
                navController = navController,
                viewModel = userSearchVM
            )
        }

        // Chat screen


        // ---------- CHAT SCREEN ----------
        composable(
            route = "chat/{partnerId}/{username}",
            arguments = listOf(
                navArgument("partnerId") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val partnerId = backStackEntry.arguments!!.getString("partnerId")!!
            val username = backStackEntry.arguments!!.getString("username")!!

            ChatScreen(
                navController = navController,
                partnerId = partnerId,
                username = username,
                messageRepository = messageRepository,
                networkObserver = networkObserver
            )
        }

        // Mon profil (édition)
        composable("userprofile") {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepo, authRepo)
        // ---------- TWEETS ----------
        composable("createTweet") {
            CreateTweetScreen(navController, tweetVM)
        }

        composable("editTweet/{tweetId}") { backStackEntry ->
            val tweetId = backStackEntry.arguments?.getString("tweetId") ?: ""
            EditTweetScreen(navController, tweetId, tweetVM)
        }

        composable("bookmarks") {
            BookmarkScreen(
                navController = navController,
                viewModel = tweetVM,
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }
            )
        }

        // ========== NOUVELLES ROUTES FOLLOW/UNFOLLOW ==========

        // Découvrir des utilisateurs
        composable("discover_users") {
            DiscoverUsersScreen(navController, userViewModel)
        }

        // Profil d'un autre utilisateur
        composable(
            route = "other_profile/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            OtherUserProfileScreen(navController, userViewModel, userId)
        }

        // Liste des followers
        composable(
            route = "followers/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            FollowListScreen(navController, userViewModel, userId, FollowListType.FOLLOWERS)
        }

        // Liste des abonnements (following)
        composable(
            route = "following/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            FollowListScreen(navController, userViewModel, userId, FollowListType.FOLLOWING)
        }
    }
}


