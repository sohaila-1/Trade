package com.example.tradeconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import com.example.tradeconnect.data.datastore.UserPreferences
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.remote.FirebaseMessagingService
import com.example.tradeconnect.data.repository.AuthRepository
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.data.repository.ProfileRepository
import com.example.tradeconnect.nagivation.AppNavHost
import com.example.tradeconnect.ui.theme.TradeConnectTheme
import com.example.tradeconnect.uii.SplashScreen
import com.example.tradeconnect.util.NetworkObserver
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {

    // Initialize these as class properties so they persist
    private lateinit var appDatabase: AppDatabase
    private lateinit var networkObserver: NetworkObserver
    private lateinit var messageRepository: MessageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase instances
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val realtimeDatabase = FirebaseDatabase.getInstance("https://tradeconnect-670e8-default-rtdb.europe-west1.firebasedatabase.app/")

        val preferences = UserPreferences(applicationContext)

        // Initialize Room Database
        appDatabase = AppDatabase.getDatabase(applicationContext)

        // Initialize Network Observer
        networkObserver = NetworkObserver(applicationContext)

        // Initialize Firebase Messaging Service
        val firebaseMessagingService = FirebaseMessagingService(realtimeDatabase, auth)

        // Create Message Repository
        messageRepository = MessageRepository(appDatabase, firebaseMessagingService)

        // Create repositories
        val authRepo = AuthRepository(auth, firestore)
        val profileRepo = ProfileRepository(firestore, auth)

        // FIXED: Create factory with messageRepository for data clearing on logout
        val authFactory = AuthViewModel.Factory(authRepo, preferences, messageRepository)

        setContent {
            TradeConnectTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Ensure ViewModelStoreOwner is available
                    CompositionLocalProvider(LocalViewModelStoreOwner provides this) {
                        val navController = rememberNavController()
                        val authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

                        val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                        when (isLoggedIn) {
                            null -> SplashScreen(navController) // show a loading screen while checking
                            true -> AppNavHost(
                                navController = navController,
                                authViewModel = authViewModel,
                                authRepo = authRepo,
                                profileRepo = profileRepo,
                                startDestination = "feed",
                                appDatabase = appDatabase,
                                networkObserver = networkObserver,
                                messageRepository = messageRepository
                            )
                            false -> AppNavHost(
                                navController = navController,
                                authViewModel = authViewModel,
                                authRepo = authRepo,
                                profileRepo = profileRepo,
                                startDestination = "login",
                                appDatabase = appDatabase,
                                networkObserver = networkObserver,
                                messageRepository = messageRepository
                            )
                        }
                    }
                }
            }
        }
    }
}

