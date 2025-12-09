package com.example.tradeconnect.uii.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.example.tradeconnect.data.repository.FakeAuthRepository
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(navController: NavController, viewModel: AuthViewModel) {
    // Set default rememberMe = true on sign up screen and clear errors
    LaunchedEffect(Unit) {
        viewModel.updateRememberMe(true)
        viewModel.clearError()
    }

    // Make the screen scrollable for smaller devices
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
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

        Text(
            "Sign up for App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.firstName,
            onValueChange = {
                viewModel.firstName = it
                viewModel.clearError()
            },
            label = { Text("First Name *") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading,
            singleLine = true,
            isError = viewModel.errorMessage?.contains("first name", ignoreCase = true) == true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.lastName,
            onValueChange = {
                viewModel.lastName = it
                viewModel.clearError()
            },
            label = { Text("Last Name *") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading,
            singleLine = true,
            isError = viewModel.errorMessage?.contains("last name", ignoreCase = true) == true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = {
                viewModel.email = it
                viewModel.clearError()
            },
            label = { Text("Email *") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading,
            singleLine = true,
            isError = viewModel.errorMessage?.contains("email", ignoreCase = true) == true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = {
                viewModel.phone = it
                viewModel.clearError()
            },
            label = { Text("Phone (optional)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = {
                viewModel.password = it
                viewModel.clearError()
            },
            label = { Text("Password * (min 6 characters)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading,
            singleLine = true,
            isError = viewModel.errorMessage?.contains("password", ignoreCase = true) == true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.rememberMe,
                onCheckedChange = { viewModel.updateRememberMe(it) },
                enabled = !viewModel.isLoading
            )
            Text("Keep me signed in")
        }

        // Error message
        viewModel.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                viewModel.signUp {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = TBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Already have an account?")
            TextButton(
                onClick = {
                    viewModel.clearError()
                    navController.navigate("login")
                },
                enabled = !viewModel.isLoading
            ) {
                Text("Log In", color = TBlue)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    val navController = rememberNavController()

    val fakeRepo = FakeAuthRepository()
    val fakePrefs = FakeUserPreferences()

    val previewViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(fakeRepo, fakePrefs)
    )

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