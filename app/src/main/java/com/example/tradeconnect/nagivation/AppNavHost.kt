package com.example.tradeconnect.nagivation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tradeconnect.uii.chat.ChatScreen
import com.example.tradeconnect.uii.home.HomeScreen
import com.example.tradeconnect.uii.login.LoginScreen
import com.example.tradeconnect.uii.signup.SignUpScreen
import com.example.tradeconnect.viewmodel.AuthViewModel
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
        composable("home") {
            HomeScreen(navController)
        }

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

        // PROFILE //this is souhaia name for profilescreen
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
        composable(route = "bookmarks") {
            BookmarkScreen(
                navController = navController,
                viewModel = tweetVM,                // ✅ nom correct
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }   // ✅ fonction locale
            )
        }

