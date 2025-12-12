package com.example.tradeconnect.nagivation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.remote.FirebaseMessagingService
import com.example.tradeconnect.data.repository.IAuthRepository
import com.example.tradeconnect.data.repository.IProfileRepository
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.ui.chat.ChatListScreen
import com.example.tradeconnect.ui.chat.ChatScreen
import com.example.tradeconnect.ui.chat.UserSearchScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.uii.user.UserProfileScreen
import com.example.tradeconnect.util.NetworkObserver
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.example.tradeconnect.viewmodel.ChatListViewModel
import com.example.tradeconnect.viewmodel.ProfileViewModel
import com.example.tradeconnect.viewmodel.UserSearchViewModel

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
            HomeScreen(navController, authViewModel)
        }

        // Chat list screen (main messages screen)
        composable("chat_list") {
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

        composable("userprofile") {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepo, authRepo)
            )
            UserProfileScreen(navController, profileViewModel)
        }
    }
}