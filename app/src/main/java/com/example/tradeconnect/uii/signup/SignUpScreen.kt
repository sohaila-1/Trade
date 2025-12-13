package com.example.tradeconnect.uii.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
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
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
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

        Text(
            text = "Create account",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.firstName,
            onValueChange = { viewModel.firstName = it },
            label = { Text("First name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.lastName,
            onValueChange = { viewModel.lastName = it },
            label = { Text("Last name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = { viewModel.phone = it },
            label = { Text("Phone") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
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
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keep me signed in")
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("Create account")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Already have an account?")
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Sign in", color = TBlue)
            }
        }

        viewModel.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    val navController = rememberNavController()
    val fakeRepo = FakeAuthRepository()
    val fakePrefs = FakeUserPreferences()

    val vm: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(fakeRepo, fakePrefs)
    )

    vm.apply {
        firstName = "Jane"
        lastName = "Doe"
        email = "jane@tradeconnect.dev"
        phone = "+33 612345678"
        password = "password123"
        updateRememberMe(true)
    }

    MaterialTheme {
        SignUpScreen(navController, vm)
    }
}
