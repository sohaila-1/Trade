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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeconnect.R
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(navController: NavController, viewModel: AuthViewModel) {

    LaunchedEffect(Unit) {
        viewModel.updateRememberMe(true)
        viewModel.clearError()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Sign up for App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        // FIRST NAME
        OutlinedTextField(
            value = viewModel.firstName,
            onValueChange = {
                viewModel.firstName = it
                viewModel.clearError()
            },
            label = { Text("First Name *") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(Modifier.height(12.dp))

        // LAST NAME
        OutlinedTextField(
            value = viewModel.lastName,
            onValueChange = {
                viewModel.lastName = it
                viewModel.clearError()
            },
            label = { Text("Last Name *") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(Modifier.height(12.dp))

        // EMAIL
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = {
                viewModel.email = it
                viewModel.clearError()
            },
            label = { Text("Email *") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(Modifier.height(12.dp))

        // PHONE OPTIONAL
        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = {
                viewModel.phone = it
                viewModel.clearError()
            },
            label = { Text("Phone (optional)") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(Modifier.height(12.dp))

        // PASSWORD
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = {
                viewModel.password = it
                viewModel.clearError()
            },
            label = { Text("Password *") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.rememberMe,
                onCheckedChange = { viewModel.updateRememberMe(it) }
            )
            Text("Keep me signed in")
        }

        // ERROR
        viewModel.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.signUp {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(TBlue),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("Sign Up")
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Already have an account?")
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Log In", color = TBlue)
            }
        }
    }
}

private fun AuthViewModel.clearError() {
    TODO("Not yet implemented")
}
