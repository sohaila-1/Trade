package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.R
import com.example.tradeconnect.viewmodel.TweetViewModel

@Composable
fun CreateTweetScreen(
    navController: NavController,
    viewModel: TweetViewModel
) {
    var content by remember { mutableStateOf("") }

    // État du menu de visibilité
    var visibilityExpanded by remember { mutableStateOf(false) }
    var selectedVisibility by remember { mutableStateOf("Tout le monde") }

    val visibilityOptions = listOf("Tout le monde", "Amis", "Privé")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                backgroundColor = Color.White,
                elevation = 0.dp,
                navigationIcon = {
                    Text(
                        "Annuler",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable { navController.popBackStack() },
                        color = Color(0xFF1DA1F2),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.createTweet(
                                content = content,
                                username = "Sohaila",
                                userId = "12345"
                            )
                            navController.popBackStack()
                        },
                        enabled = content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (content.isNotBlank()) Color(0xFF1DA1F2) else Color(
                                0xFFB3DFFC
                            ),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("Poster")
                    }
                }
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {

            // 🔵 Avatar + Visibilité
            Row(verticalAlignment = Alignment.Top) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1DA1F2).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color(0xFF1DA1F2),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {

                    // Menu déroulant VISIBILITÉ
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF1DA1F2).copy(alpha = 0.15f))
                                .clickable { visibilityExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                selectedVisibility,
                                color = Color(0xFF1DA1F2),
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "",
                                tint = Color(0xFF1DA1F2)
                            )
                        }

                        DropdownMenu(
                            expanded = visibilityExpanded,
                            onDismissRequest = { visibilityExpanded = false }
                        ) {
                            visibilityOptions.forEach { option ->
                                DropdownMenuItem(onClick = {
                                    selectedVisibility = option
                                    visibilityExpanded = false
                                }) {
                                    Text(option)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Champ texte
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("Quoi de neuf ?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🌍 Qui peut répondre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Default.Public, contentDescription = "", tint = Color(0xFF1DA1F2))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Tout le monde peut répondre",
                    color = Color(0xFF1DA1F2),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 🔵 Barre d’actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Default.Image, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Default.Gif, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Default.Poll, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Default.EmojiEmotions, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(28.dp))
            }
        }
    }
}
