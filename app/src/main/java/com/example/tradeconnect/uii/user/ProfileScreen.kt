package com.example.tradeconnect.uii.user

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.viewmodel.ProfileViewModel
import com.example.tradeconnect.repository.FakeAuthRepository
import com.example.tradeconnect.repository.FakeProfileRepository
import com.yalantis.ucrop.UCrop
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    var showChangePasswordSheet by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var passwordSuccessMessage by remember { mutableStateOf<String?>(null) }

    val tempCroppedFile = remember {
        File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg")
    }

    val uCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            UCrop.getOutput(result.data!!)?.let {
                viewModel.uploadProfileImage(context, it)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setToolbarTitle("Crop Profile Picture")
            setToolbarColor("#1976D2".toColorInt())
            setStatusBarColor("#1976D2".toColorInt())
            setToolbarWidgetColor("#FFFFFF".toColorInt())
            setRootViewBackgroundColor("#000000".toColorInt()) // ✅ FIX
            setCircleDimmedLayer(true)
        }

        val intent = UCrop.of(uri, Uri.fromFile(tempCroppedFile))
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .getIntent(context)

        uCropLauncher.launch(intent)
    }

    val profileBitmap = remember(viewModel.profileImageUrl) {
        decodeBase64ToBitmap(viewModel.profileImageUrl)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    profileBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            ProfileTextField("First Name", viewModel.firstName) { viewModel.firstName = it }
            ProfileTextField("Last Name", viewModel.lastName) { viewModel.lastName = it }
            ProfileTextField("E-Mail", viewModel.email) { viewModel.email = it }
            ProfileTextField("Mobile", viewModel.mobile) { viewModel.mobile = it }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveProfile {} },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TBlue)
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showChangePasswordSheet = true },
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Icon(Icons.Default.Lock, null)
                Spacer(Modifier.width(8.dp))
                Text("Change Password")
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp)
    )
}

private fun decodeBase64ToBitmap(base64: String): Bitmap? =
    try {
        val bytes = Base64.decode(base64.substringAfter("base64,"), Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    val navController = rememberNavController()

    val vm = remember {
        ProfileViewModel(
            profileRepo = FakeProfileRepository(),
            authRepo = FakeAuthRepository()
        )
    }

    vm.apply {
        firstName = "John"
        lastName = "Doe"
        email = "john.doe@tradeconnect.dev"
        mobile = "+33 612345678"
    }

    MaterialTheme {
        UserProfileScreen(
            navController = navController,
            viewModel = vm
        )
    }
}

