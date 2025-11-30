package com.example.tradeconnect.uii.user

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeconnect.data.repository.FakeAuthRepository
import com.example.tradeconnect.data.repository.FakeProfileRepository
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.ProfileViewModel

@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfileImage(context, it) }
    }

    // Decode base64 image outside composable
    val profileBitmap = remember(viewModel.profileImageUrl) {
        decodeBase64ToBitmap(viewModel.profileImageUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        TopAppBar(
            backgroundColor = Color.White,
            elevation = 0.dp
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "User Profile",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture with Camera Icon
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder icon
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "No Profile Picture",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Loading indicator when uploading
                    if (viewModel.isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = TBlue
                        )
                    }
                }

                // Camera Icon Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // First Name Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "First Name",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = viewModel.firstName,
                    onValueChange = { viewModel.firstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = TBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    enabled = !viewModel.isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last Name Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Last Name",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = viewModel.lastName,
                    onValueChange = { viewModel.lastName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = TBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    enabled = !viewModel.isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // E-Mail Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "E-Mail",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = TBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    enabled = !viewModel.isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mobile Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mobile",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = viewModel.mobile,
                    onValueChange = { viewModel.mobile = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = TBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    enabled = !viewModel.isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message
            viewModel.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Success Message
            viewModel.successMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Save Button
            Button(
                onClick = {
                    viewModel.saveProfile {
                        // Optional: navigate back after successful save
                        // navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = TBlue,
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
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "SAVE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper function to decode base64 string to Bitmap (outside composable)
private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    if (base64String.isEmpty() || !base64String.startsWith("data:image")) {
        return null
    }

    return try {
        val base64Data = base64String.substringAfter("base64,")
        val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    val navController = rememberNavController()

    // Fake repositories for preview
    val fakeAuthRepo = FakeAuthRepository()
    val fakeProfileRepo = FakeProfileRepository()

    val previewViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(fakeProfileRepo, fakeAuthRepo)
    )

    // Pre-fill fields for preview
    previewViewModel.apply {
        firstName = "John"
        lastName = "Doe"
        email = "johndoe@gmail.com"
        mobile = "+91-123456789"
    }

    MaterialTheme {
        UserProfileScreen(navController = navController, viewModel = previewViewModel)
    }
}