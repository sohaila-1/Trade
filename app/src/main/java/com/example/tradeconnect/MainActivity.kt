package com.example.tradeconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.tradeconnect.data.datastore.UserPreferences
import com.example.tradeconnect.data.repository.AuthRepository
import com.example.tradeconnect.nagivation.AppNavHost
import com.example.tradeconnect.ui.theme.TradeConnectTheme
import com.example.tradeconnect.uii.SplashScreen
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = UserPreferences(applicationContext)
        val repo = AuthRepository(FirebaseAuth.getInstance())
        val factory = AuthViewModel.Factory(repo, preferences)

        setContent {
            TradeConnectTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    val authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

                    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                    when (isLoggedIn) {
                        null -> SplashScreen(navController)
                        true -> AppNavHost(navController, authViewModel, startDestination = "feed")
                        false -> AppNavHost(navController, authViewModel, startDestination = "login")
                    }

                }
            }
        }
    }
}

