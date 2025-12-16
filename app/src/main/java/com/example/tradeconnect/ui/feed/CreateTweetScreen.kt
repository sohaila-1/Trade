package com.example.tradeconnect.ui.feed

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.theme.*
import com.example.tradeconnect.viewmodel.TweetViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTweetScreen(
    navController: NavController,
    viewModel: TweetViewModel,
    isDarkMode: Boolean = false
) {
    var content by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    val user = viewModel.authVM.currentUser.value
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Couleurs
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider

    // Limite de caractères
    val maxCharacters = 280
    val remainingCharacters = maxCharacters - content.length
    val isOverLimit = remainingCharacters < 0
    val isNearLimit = remainingCharacters in 0..20
    val progress = (content.length.toFloat() / maxCharacters).coerceIn(0f, 1f)

    // Couleur du compteur
    val progressColor = when {
        isOverLimit -> Color.Red
        isNearLimit -> Color(0xFFFFAD1F)
        else -> TwitterBlue
    }

    // Bouton actif
    val canPost = content.isNotBlank() && !isOverLimit && !isPosting

    // Focus automatique
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    // Animation de rotation pour le loading
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgColor
            ) {
                Column {
                    // 🆕 Espace en haut pour la status bar
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bouton Fermer
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = textColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Logo au centre
                        Text(
                            text = "TradeConnect",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TwitterBlue
                        )

                        // Bouton Poster
                        Button(
                            onClick = {
                                if (canPost) {
                                    isPosting = true
                                    keyboardController?.hide()
                                    viewModel.createTweet(content.trim())
                                    navController.popBackStack()
                                }
                            },
                            enabled = canPost,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TwitterBlue,
                                contentColor = Color.White,
                                disabledContainerColor = TwitterBlue.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (isPosting) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(rotation)
                                )
                            } else {
                                Text(
                                    text = "Poster",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = dividerColor
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 🆕 Espace supplémentaire en haut du contenu
            Spacer(modifier = Modifier.height(8.dp))

            // Zone de composition scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // 🆕 Espace avant l'avatar
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        TwitterBlue,
                                        TwitterBlue.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.username?.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Info utilisateur
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user?.username ?: "Utilisateur",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textColor
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Badge "Public"
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = TwitterBlue.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Public,
                                        contentDescription = null,
                                        tint = TwitterBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Public",
                                        fontSize = 11.sp,
                                        color = TwitterBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Zone de texte
                        BasicTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = textColor,
                                fontSize = 20.sp,
                                lineHeight = 28.sp
                            ),
                            cursorBrush = SolidColor(TwitterBlue),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (content.isEmpty()) {
                                        Text(
                                            text = "Quoi de neuf ?",
                                            color = secondaryColor,
                                            fontSize = 20.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // Barre d'outils du bas
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgColor,
                shadowElevation = 8.dp
            ) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = dividerColor
                    )

                    // 🆕 Plus d'espace dans la barre d'outils
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icônes d'action
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ToolbarIcon(
                                icon = Icons.Outlined.Image,
                                tint = TwitterBlue.copy(alpha = 0.6f),
                                onClick = { /* TODO */ }
                            )
                            ToolbarIcon(
                                icon = Icons.Outlined.Gif,
                                tint = TwitterBlue.copy(alpha = 0.6f),
                                onClick = { /* TODO */ }
                            )
                            ToolbarIcon(
                                icon = Icons.Outlined.Poll,
                                tint = TwitterBlue.copy(alpha = 0.6f),
                                onClick = { /* TODO */ }
                            )
                            ToolbarIcon(
                                icon = Icons.Outlined.LocationOn,
                                tint = TwitterBlue.copy(alpha = 0.6f),
                                onClick = { /* TODO */ }
                            )
                        }

                        // Compteur de caractères
                        if (content.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Cercle de progression
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    // Cercle de fond
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.size(26.dp),
                                        color = dividerColor,
                                        strokeWidth = 2.5.dp
                                    )

                                    // Cercle de progression
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.size(26.dp),
                                        color = progressColor,
                                        strokeWidth = 2.5.dp
                                    )

                                    // Nombre au centre si près de la limite
                                    if (remainingCharacters <= 20) {
                                        Text(
                                            text = remainingCharacters.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = progressColor
                                        )
                                    }
                                }

                                // Séparateur et compteur textuel
                                if (content.length > maxCharacters - 100) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(24.dp)
                                            .background(dividerColor)
                                    )

                                    Text(
                                        text = remainingCharacters.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isOverLimit) FontWeight.Bold else FontWeight.Normal,
                                        color = progressColor
                                    )
                                }
                            }
                        }
                    }

                    // 🆕 Espace en bas pour la navigation système
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ToolbarIcon(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp)  // 🆕 Plus grand pour être plus cliquable
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)  // 🆕 Icônes plus grandes
        )
    }

}