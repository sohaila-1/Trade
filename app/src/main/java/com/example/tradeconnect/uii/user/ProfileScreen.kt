package com.example.tradeconnect.uii.user

import android.app.Activity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.viewmodel.ProfileViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Bottom sheet state for change password
    val sheetState = rememberModalBottomSheetState()
    var showChangePasswordSheet by remember { mutableStateOf(false) }

    // Password fields
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var passwordSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Create a temporary file for cropped image
    val tempCroppedFile = remember {
        File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg")
    }

    // UCrop launcher
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                viewModel.uploadProfileImage(context, uri)
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            val destinationUri = Uri.fromFile(tempCroppedFile)

            val options = UCrop.Options().apply {
                setCompressionQuality(80)
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false)
                setToolbarTitle("Crop Profile Picture")
                setShowCropGrid(true)
                setShowCropFrame(true)
                setCircleDimmedLayer(true)
            }

            val uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .getIntent(context)

            uCropLauncher.launch(uCropIntent)
        }
    }

    // Decode base64 image
    val profileBitmap = remember(viewModel.profileImageUrl) {
        decodeBase64ToBitmap(viewModel.profileImageUrl)
    }

    // Change Password Bottom Sheet
    if (showChangePasswordSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showChangePasswordSheet = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordError = null
                passwordSuccessMessage = null
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Current Password Field
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        passwordError = null
                        passwordSuccessMessage = null
                    },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    enabled = !isChangingPassword,
                    singleLine = true,
                    isError = passwordError?.contains("current", ignoreCase = true) == true ||
                            passwordError?.contains("incorrect", ignoreCase = true) == true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = null
                        passwordSuccessMessage = null
                    },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    enabled = !isChangingPassword,
                    singleLine = true,
                    isError = passwordError?.contains(
                        "new password",
                        ignoreCase = true
                    ) == true ||
                            passwordError?.contains("weak", ignoreCase = true) == true ||
                            passwordError?.contains("6 characters", ignoreCase = true) == true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordError = null
                        passwordSuccessMessage = null
                    },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    enabled = !isChangingPassword,
                    singleLine = true,
                    isError = passwordError?.contains("match", ignoreCase = true) == true ||
                            passwordError?.contains("confirm", ignoreCase = true) == true
                )

                // Error message
                passwordError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Success message
                passwordSuccessMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        // Validate passwords
                        when {
                            currentPassword.isBlank() -> {
                                passwordError = "Please enter your current password"
                            }

                            newPassword.isBlank() -> {
                                passwordError = "Please enter a new password"
                            }

                            newPassword.length < 6 -> {
                                passwordError = "New password must be at least 6 characters"
                            }

                            confirmPassword.isBlank() -> {
                                passwordError = "Please confirm your new password"
                            }

                            newPassword != confirmPassword -> {
                                passwordError = "Passwords do not match"
                            }

                            currentPassword == newPassword -> {
                                passwordError =
                                    "New password must be different from current password"
                            }

                            else -> {
                                isChangingPassword = true
                                viewModel.changePassword(
                                    currentPassword,
                                    newPassword
                                ) { success, error ->
                                    isChangingPassword = false
                                    if (success) {
                                        passwordSuccessMessage = "Password changed successfully"
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        // Auto-close after success
                                        scope.launch {
                                            kotlinx.coroutines.delay(1500)
                                            showChangePasswordSheet = false
                                            passwordSuccessMessage = null
                                        }
                                    } else {
                                        passwordError = error ?: "Failed to change password"
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    enabled = !isChangingPassword
                ) {
                    if (isChangingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
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

    // Main Screen Content
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "User Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
    ) { paddingValues ->

        if (viewModel.isLoading && viewModel.firstName.isEmpty()) {
            // Initial Loading State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading profile...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Profile Picture with Camera Icon
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "No Profile Picture",
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        if (viewModel.isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }

                    // Camera Icon Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // First Name Field
                ProfileTextField(
                    label = "First Name",
                    value = viewModel.firstName,
                    onValueChange = { viewModel.firstName = it },
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Last Name Field
                ProfileTextField(
                    label = "Last Name",
                    value = viewModel.lastName,
                    onValueChange = { viewModel.lastName = it },
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // E-Mail Field
                ProfileTextField(
                    label = "E-Mail",
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mobile Field
                ProfileTextField(
                    label = "Mobile",
                    value = viewModel.mobile,
                    onValueChange = { viewModel.mobile = it.filter { it.isDigit() } },
                    enabled = !viewModel.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error Message
                viewModel.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Success Message
                viewModel.successMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Save Button
                Button(
                    onClick = {
                        viewModel.saveProfile {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "SAVE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Change Password Button
                OutlinedButton(
                    onClick = { showChangePasswordSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Change Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}

// Helper function to decode base64 string to Bitmap
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
