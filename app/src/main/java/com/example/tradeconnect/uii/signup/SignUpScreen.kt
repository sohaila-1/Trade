package com.example.tradeconnect.uii.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeconnect.R
import com.example.tradeconnect.data.datastore.FakeUserPreferences
import com.example.tradeconnect.repository.FakeAuthRepository
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(navController: NavController, viewModel: AuthViewModel) {
    // set default rememberMe = true on sign up screen
    LaunchedEffect(Unit) {
        viewModel.updateRememberMe(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Sign in to App", style = MaterialTheme.typography.h5, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.firstName,
            onValueChange = { viewModel.firstName = it },
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.lastName,
            onValueChange = { viewModel.lastName = it },
            label = { Text("Last name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = { viewModel.phone = it },
            label = { Text("Phone") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.rememberMe,
                onCheckedChange = { viewModel.updateRememberMe(it) }
            )
            Text("Keep me signed in")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.signUp {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = TBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("Sign in")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("You have an account?")
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Sign In", color = TBlue)
            }
        }

        viewModel.errorMessage?.let {
            Text(it, color = MaterialTheme.colors.error)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    val navController = rememberNavController()

    // Fake repo and prefs for preview
    val fakeRepo = FakeAuthRepository()
    val fakePrefs = FakeUserPreferences()

    // Use ViewModelProvider with your Factory
    val previewViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(fakeRepo, fakePrefs)
    )

    // Pre-fill fields for preview
    previewViewModel.apply {
        firstName = "Jane"
        lastName = "Doe"
        email = "jane.doe@example.com"
        phone = "+33 612345678"
        password = "password123"
        updateRememberMe(true)
    }

    MaterialTheme {
        SignUpScreen(navController = navController, viewModel = previewViewModel)
    }
}
