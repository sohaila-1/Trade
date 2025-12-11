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
import com.example.tradeconnect.data.datastore.UserPreferences
import com.example.tradeconnect.data.repository.AuthRepository
import com.example.tradeconnect.data.repository.ProfileRepository
import com.example.tradeconnect.nagivation.AppNavHost
import com.example.tradeconnect.ui.theme.TradeConnectTheme
import com.example.tradeconnect.uii.SplashScreen
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase instances
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val preferences = UserPreferences(applicationContext)

        // Create repositories (NO Firebase Storage needed!)
        val authRepo = AuthRepository(auth, firestore)
        val profileRepo = ProfileRepository(firestore, auth)


        // Create factories
        val authFactory = AuthViewModel.Factory(authRepo, preferences)

        setContent {
            TradeConnectTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    //val authViewModel = ViewModelProvider(this, authFactory).get(AuthViewModel::class.java)
                    val authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]


                    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                    when (isLoggedIn) {
                        null -> SplashScreen(navController) // show a loading screen while checking
                        true -> AppNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            authRepo = authRepo,
                            profileRepo = profileRepo,
                            startDestination = "home"
                        )
                        false -> AppNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            authRepo = authRepo,
                            profileRepo = profileRepo,
                            startDestination = "login"
                        )
                    }
                }
            }
        }
    }
}