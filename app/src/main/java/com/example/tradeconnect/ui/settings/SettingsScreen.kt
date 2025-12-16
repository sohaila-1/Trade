package com.example.tradeconnect.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.theme.*
import com.example.tradeconnect.viewmodel.AuthViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current

    // Couleurs
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider
    val cardColor = if (isDarkMode) DarkSurface else LightSurface

    // États des dialogs
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // États des notifications
    var notificationsEnabled by remember { mutableStateOf(true) }
    var likesNotifications by remember { mutableStateOf(true) }
    var commentsNotifications by remember { mutableStateOf(true) }
    var followsNotifications by remember { mutableStateOf(true) }
    var messagesNotifications by remember { mutableStateOf(true) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // ==================== DIALOGS ====================

    // Dialog Déconnexion
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = cardColor,
            titleContentColor = textColor,
            textContentColor = secondaryColor,
            title = {
                Text("Se déconnecter", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Êtes-vous sûr de vouloir vous déconnecter ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0245E))
                ) {
                    Text("Déconnexion", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler", color = TwitterBlue)
                }
            }
        )
    }

    // Dialog Mot de passe
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            isDarkMode = isDarkMode
        )
    }

    // Dialog Email
    if (showEmailDialog) {
        ChangeEmailDialog(
            currentEmail = authViewModel.currentUser.value?.email ?: "",
            onDismiss = { showEmailDialog = false },
            isDarkMode = isDarkMode
        )
    }

    // Dialog À propos
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false },
            isDarkMode = isDarkMode
        )
    }

    // Dialog Aide
    if (showHelpDialog) {
        HelpDialog(
            onDismiss = { showHelpDialog = false },
            isDarkMode = isDarkMode,
            onContactSupport = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@tradeconnect.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Support TradeConnect")
                }
                context.startActivity(intent)
            }
        )
    }

    // Dialog Confidentialité
    if (showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { showPrivacyDialog = false },
            isDarkMode = isDarkMode
        )
    }

    // Dialog Notifications
    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false },
            isDarkMode = isDarkMode,
            notificationsEnabled = notificationsEnabled,
            onNotificationsEnabledChange = { notificationsEnabled = it },
            likesNotifications = likesNotifications,
            onLikesChange = { likesNotifications = it },
            commentsNotifications = commentsNotifications,
            onCommentsChange = { commentsNotifications = it },
            followsNotifications = followsNotifications,
            onFollowsChange = { followsNotifications = it },
            messagesNotifications = messagesNotifications,
            onMessagesChange = { messagesNotifications = it }
        )
    }

    // ==================== MAIN CONTENT ====================

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgColor,
                shadowElevation = 2.dp
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = textColor
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Paramètres",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bgColor)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ==================== SECTION COMPTE ====================
            SectionHeader(title = "Compte", textColor = secondaryColor)

            SettingItem(
                icon = Icons.Outlined.Person,
                title = "Modifier le profil",
                subtitle = "Photo, nom, bio",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { navController.navigate("edit_profile") }
            )

            SettingItem(
                icon = Icons.Outlined.Lock,
                title = "Mot de passe",
                subtitle = "Changer votre mot de passe",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { showPasswordDialog = true }
            )

            SettingItem(
                icon = Icons.Outlined.Email,
                title = "Email",
                subtitle = authViewModel.currentUser.value?.email ?: "Non défini",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { showEmailDialog = true }
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = dividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ==================== SECTION APPARENCE ====================
            SectionHeader(title = "Apparence", textColor = secondaryColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleTheme() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) TwitterBlue.copy(alpha = 0.2f)
                            else secondaryColor.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        contentDescription = null,
                        tint = if (isDarkMode) TwitterBlue else secondaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mode sombre",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Text(
                        text = if (isDarkMode) "Activé" else "Désactivé",
                        fontSize = 14.sp,
                        color = secondaryColor
                    )
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TwitterBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = secondaryColor.copy(alpha = 0.5f)
                    )
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = dividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ==================== SECTION NOTIFICATIONS ====================
            SectionHeader(title = "Notifications", textColor = secondaryColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNotificationsDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(secondaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifications push",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Text(
                        text = if (notificationsEnabled) "Activées" else "Désactivées",
                        fontSize = 14.sp,
                        color = secondaryColor
                    )
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TwitterBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = secondaryColor.copy(alpha = 0.5f)
                    )
                )
            }

            // Bouton pour personnaliser les notifications
            if (notificationsEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showNotificationsDialog = true }
                        .padding(start = 72.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personnaliser les notifications",
                        fontSize = 14.sp,
                        color = TwitterBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = TwitterBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = dividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ==================== SECTION CONFIDENTIALITÉ ====================
            SectionHeader(title = "Confidentialité", textColor = secondaryColor)

            SettingItem(
                icon = Icons.Outlined.Security,
                title = "Confidentialité",
                subtitle = "Contrôler vos données",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { showPrivacyDialog = true }
            )

            SettingItem(
                icon = Icons.Outlined.Block,
                title = "Comptes bloqués",
                subtitle = "Gérer les blocages",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { navController.navigate("blocked_users") }
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = dividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ==================== SECTION À PROPOS ====================
            SectionHeader(title = "À propos", textColor = secondaryColor)

            SettingItem(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                title = "Centre d'aide",
                subtitle = "FAQ et support",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { showHelpDialog = true }
            )

            SettingItem(
                icon = Icons.Outlined.Info,
                title = "À propos de TradeConnect",
                subtitle = "Version 1.0.0",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { showAboutDialog = true }
            )

            SettingItem(
                icon = Icons.Outlined.Star,
                title = "Noter l'application",
                subtitle = "Donnez-nous votre avis",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = {
                    // Ouvrir Play Store
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Si Play Store pas installé, ouvrir dans le navigateur
                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                        }
                        context.startActivity(webIntent)
                    }
                }
            )

            SettingItem(
                icon = Icons.Outlined.Share,
                title = "Partager l'application",
                subtitle = "Inviter des amis",
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Rejoins-moi sur TradeConnect ! https://play.google.com/store/apps/details?id=${context.packageName}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
                }
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = dividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ==================== DÉCONNEXION ====================
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0245E).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFE0245E),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Se déconnecter",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE0245E)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "TradeConnect © 2024",
                fontSize = 12.sp,
                color = secondaryColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ==================== COMPONENTS ====================

@Composable
private fun SectionHeader(title: String, textColor: Color) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(secondaryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = secondaryColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = secondaryColor
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = secondaryColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==================== DIALOGS ====================

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = cardColor,
        title = {
            Text(
                "Changer le mot de passe",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Mot de passe actuel
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; errorMessage = null },
                    label = { Text("Mot de passe actuel") },
                    singleLine = true,
                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                imageVector = if (showCurrentPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = secondaryColor
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = TwitterBlue,
                        cursorColor = TwitterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Nouveau mot de passe
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = null },
                    label = { Text("Nouveau mot de passe") },
                    singleLine = true,
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = secondaryColor
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = TwitterBlue,
                        cursorColor = TwitterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirmer mot de passe
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirmer le mot de passe") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = TwitterBlue,
                        cursorColor = TwitterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Messages d'erreur/succès
                errorMessage?.let {
                    Text(it, color = Color(0xFFE0245E), fontSize = 14.sp)
                }
                successMessage?.let {
                    Text(it, color = OnlineGreen, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPassword.isBlank() -> errorMessage = "Entrez votre mot de passe actuel"
                        newPassword.length < 6 -> errorMessage = "Le mot de passe doit contenir au moins 6 caractères"
                        newPassword != confirmPassword -> errorMessage = "Les mots de passe ne correspondent pas"
                        else -> {
                            isLoading = true
                            errorMessage = null

                            val user = FirebaseAuth.getInstance().currentUser
                            val email = user?.email

                            if (user != null && email != null) {
                                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                                user.reauthenticate(credential)
                                    .addOnSuccessListener {
                                        user.updatePassword(newPassword)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                successMessage = "Mot de passe modifié avec succès !"
                                                currentPassword = ""
                                                newPassword = ""
                                                confirmPassword = ""
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = e.message ?: "Erreur lors de la modification"
                                            }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        errorMessage = "Mot de passe actuel incorrect"
                                    }
                            }
                        }
                    }
                },
                enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Modifier", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Annuler", color = TwitterBlue)
            }
        }
    )
}

@Composable
private fun ChangeEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    var newEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = cardColor,
        title = {
            Text(
                "Changer l'email",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Email actuel: $currentEmail",
                    fontSize = 14.sp,
                    color = secondaryColor
                )

                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it; errorMessage = null },
                    label = { Text("Nouvel email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = TwitterBlue,
                        cursorColor = TwitterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Mot de passe actuel") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = TwitterBlue,
                        cursorColor = TwitterBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(it, color = Color(0xFFE0245E), fontSize = 14.sp)
                }
                successMessage?.let {
                    Text(it, color = OnlineGreen, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() -> {
                            errorMessage = "Email invalide"
                        }
                        password.isBlank() -> {
                            errorMessage = "Entrez votre mot de passe"
                        }
                        else -> {
                            isLoading = true
                            errorMessage = null

                            val user = FirebaseAuth.getInstance().currentUser
                            val email = user?.email

                            if (user != null && email != null) {
                                val credential = EmailAuthProvider.getCredential(email, password)
                                user.reauthenticate(credential)
                                    .addOnSuccessListener {
                                        user.verifyBeforeUpdateEmail(newEmail)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                successMessage = "Un email de vérification a été envoyé à $newEmail"
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = e.message ?: "Erreur"
                                            }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        errorMessage = "Mot de passe incorrect"
                                    }
                            }
                        }
                    }
                },
                enabled = !isLoading && newEmail.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Modifier", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Annuler", color = TwitterBlue)
            }
        }
    )
}

@Composable
private fun NotificationsDialog(
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
    notificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    likesNotifications: Boolean,
    onLikesChange: (Boolean) -> Unit,
    commentsNotifications: Boolean,
    onCommentsChange: (Boolean) -> Unit,
    followsNotifications: Boolean,
    onFollowsChange: (Boolean) -> Unit,
    messagesNotifications: Boolean,
    onMessagesChange: (Boolean) -> Unit
) {
    val cardColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                "Notifications",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationToggleItem(
                    title = "Toutes les notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsEnabledChange,
                    textColor = textColor,
                    enabled = true
                )

                HorizontalDivider(color = if (isDarkMode) DarkDivider else LightDivider)

                NotificationToggleItem(
                    title = "J'aime",
                    subtitle = "Quand quelqu'un aime votre tweet",
                    checked = likesNotifications,
                    onCheckedChange = onLikesChange,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    enabled = notificationsEnabled
                )

                NotificationToggleItem(
                    title = "Commentaires",
                    subtitle = "Quand quelqu'un commente",
                    checked = commentsNotifications,
                    onCheckedChange = onCommentsChange,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    enabled = notificationsEnabled
                )

                NotificationToggleItem(
                    title = "Nouveaux abonnés",
                    subtitle = "Quand quelqu'un vous suit",
                    checked = followsNotifications,
                    onCheckedChange = onFollowsChange,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    enabled = notificationsEnabled
                )

                NotificationToggleItem(
                    title = "Messages",
                    subtitle = "Nouveaux messages privés",
                    checked = messagesNotifications,
                    onCheckedChange = onMessagesChange,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    enabled = notificationsEnabled
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
            ) {
                Text("OK", color = Color.White)
            }
        }
    )
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    secondaryColor: Color? = null,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = if (enabled) secondaryColor ?: textColor else (secondaryColor ?: textColor).copy(alpha = 0.5f)
                )
            }
        }

        Switch(
            checked = checked && enabled,
            onCheckedChange = { if (enabled) onCheckedChange(it) },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TwitterBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = (secondaryColor ?: textColor).copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun PrivacyDialog(
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    var privateAccount by remember { mutableStateOf(false) }
    var showActivityStatus by remember { mutableStateOf(true) }
    var allowTagging by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                "Confidentialité",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Compte privé", fontWeight = FontWeight.Medium, color = textColor)
                        Text("Seuls vos abonnés voient vos tweets", fontSize = 13.sp, color = secondaryColor)
                    }
                    Switch(
                        checked = privateAccount,
                        onCheckedChange = { privateAccount = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TwitterBlue
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Afficher le statut d'activité", fontWeight = FontWeight.Medium, color = textColor)
                        Text("Montrer quand vous êtes en ligne", fontSize = 13.sp, color = secondaryColor)
                    }
                    Switch(
                        checked = showActivityStatus,
                        onCheckedChange = { showActivityStatus = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TwitterBlue
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Autoriser les mentions", fontWeight = FontWeight.Medium, color = textColor)
                        Text("Permettre aux autres de vous mentionner", fontSize = 13.sp, color = secondaryColor)
                    }
                    Switch(
                        checked = allowTagging,
                        onCheckedChange = { allowTagging = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TwitterBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
            ) {
                Text("Enregistrer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TwitterBlue)
            }
        }
    )
}

@Composable
private fun HelpDialog(
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
    onContactSupport: () -> Unit
) {
    val cardColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                "Centre d'aide",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // FAQ Items
                HelpItem(
                    question = "Comment modifier mon profil ?",
                    answer = "Allez dans Paramètres > Modifier le profil pour changer votre photo, nom et bio.",
                    textColor = textColor,
                    secondaryColor = secondaryColor
                )

                HelpItem(
                    question = "Comment supprimer un tweet ?",
                    answer = "Appuyez sur les 3 points en haut à droite de votre tweet, puis sélectionnez Supprimer.",
                    textColor = textColor,
                    secondaryColor = secondaryColor
                )

                HelpItem(
                    question = "Comment bloquer un utilisateur ?",
                    answer = "Visitez le profil de l'utilisateur et appuyez sur les 3 points, puis Bloquer.",
                    textColor = textColor,
                    secondaryColor = secondaryColor
                )

                HorizontalDivider(color = if (isDarkMode) DarkDivider else LightDivider)

                Text(
                    "Vous ne trouvez pas la réponse ?",
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onContactSupport()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
            ) {
                Icon(
                    Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contacter le support", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = TwitterBlue)
            }
        }
    )
}

@Composable
private fun HelpItem(
    question: String,
    answer: String,
    textColor: Color,
    secondaryColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = secondaryColor
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                fontSize = 14.sp,
                color = secondaryColor
            )
        }
    }
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkSurface else LightSurface
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TwitterBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TC",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "TradeConnect",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = textColor
                )

                Text(
                    "Version 1.0.0",
                    fontSize = 14.sp,
                    color = secondaryColor
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TradeConnect est une plateforme sociale moderne pour partager vos idées, suivre les tendances et connecter avec d'autres utilisateurs.",
                    textAlign = TextAlign.Center,
                    color = secondaryColor,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Développé avec ❤️ en Kotlin",
                    fontSize = 13.sp,
                    color = secondaryColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "© 2024 TradeConnect",
                    fontSize = 12.sp,
                    color = secondaryColor.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
            ) {
                Text("OK", color = Color.White)
            }
        }
    )
}