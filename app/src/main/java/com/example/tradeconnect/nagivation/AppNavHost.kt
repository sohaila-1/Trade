// app/src/main/java/com/example/tradeconnect/nagivation/AppNavHost.kt
package com.example.tradeconnect.nagivation

import androidx.compose.runtime.Composable
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
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.ui.chat.ChatListScreen
import com.example.tradeconnect.ui.chat.ChatScreen
import com.example.tradeconnect.ui.chat.UserSearchScreen
import com.example.tradeconnect.ui.user.DiscoverUsersScreen
import com.example.tradeconnect.ui.user.FollowListScreen
import com.example.tradeconnect.ui.user.FollowListType
import com.example.tradeconnect.ui.user.OtherUserProfileScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.uii.user.UserProfileScreen
import com.example.tradeconnect.util.NetworkObserver
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.ChatListViewModel
import com.example.tradeconnect.viewmodel.ProfileViewModel
import com.example.tradeconnect.viewmodel.UserSearchViewModel
import com.example.tradeconnect.viewmodel.UserViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    authRepo: IAuthRepository,
    profileRepo: IProfileRepository,
    userRepo: IUserRepository,  // NOUVEAU paramètre
    startDestination: String,
    appDatabase: AppDatabase,
    networkObserver: NetworkObserver,
    messageRepository: MessageRepository
) {
    // Créer le UserViewModel une seule fois pour tout le NavHost
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.Factory(userRepo)
    )

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
            HomeScreen(navController, authViewModel, userViewModel)
        }

        // Chat list screen
        composable("chat_list") {
            val viewModel: ChatListViewModel = viewModel(
                factory = ChatListViewModel.Factory(messageRepository)
            )
            ChatListScreen(navController, viewModel)
        }

        // User search screen (pour nouveau message)
        composable("user_search") {
            val searchViewModel: UserSearchViewModel = viewModel(
                factory = UserSearchViewModel.Factory(messageRepository)
            )
            UserSearchScreen(navController, searchViewModel, userViewModel)
        }

        // Chat screen
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

        // Mon profil (édition)
        composable("userprofile") {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepo, authRepo)
            )
            UserProfileScreen(navController, profileViewModel)
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