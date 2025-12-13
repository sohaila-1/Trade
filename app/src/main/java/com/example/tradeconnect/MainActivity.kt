package com.example.tradeconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController

import com.example.tradeconnect.navigation.AppNavHost
import com.example.tradeconnect.ui.theme.TradeConnectTheme
import com.example.tradeconnect.uii.SplashScreen
import com.example.tradeconnect.viewmodel.AuthViewModel

import com.example.tradeconnect.data.datastore.UserPreferences
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.remote.ChatFirebaseService
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.repository.AuthRepository
import com.example.tradeconnect.util.NetworkObserver

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Firebase
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // 🔹 Local DB
        val database = AppDatabase.getDatabase(applicationContext)

        // 🔹 Firebase service MÉTIER (PAS Android Service)
        val realtimeDb = FirebaseDatabase.getInstance()

        val chatFirebaseService = ChatFirebaseService(
            database = realtimeDb,
            auth = auth
        )

        // 🔹 Preferences
        val preferences = UserPreferences(applicationContext)

        // 🔹 Network
        val networkObserver = NetworkObserver(applicationContext)

        // 🔹 Repositories
        val authRepository = AuthRepository(auth, firestore)
        val messageRepository = MessageRepository(
            database = database,
            firebaseService = chatFirebaseService
        )

        // 🔹 ViewModel
        val authFactory = AuthViewModel.Factory(authRepository, preferences)

        setContent {
            TradeConnectTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()
                    val authViewModel =
                        ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

                    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                    when (isLoggedIn) {
                        null -> SplashScreen(navController)

                        true -> AppNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            startDestination = "feed",
                            messageRepository = messageRepository,
                            networkObserver = networkObserver
                        )

                        false -> AppNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            startDestination = "login",
                            messageRepository = messageRepository,
                            networkObserver = networkObserver
                        )
                    }
                }
            }
        }
    }
}
