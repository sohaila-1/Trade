package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tradeconnect.model.Comment
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.ui.components.TwitterBlue
import com.example.tradeconnect.ui.components.rememberFormattedTime
import com.example.tradeconnect.viewmodel.TweetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweetDetailScreen(
    navController: NavController,
    tweetId: String,
    viewModel: TweetViewModel,
    isDarkMode: Boolean
) {
    val tweet by viewModel.selectedTweet.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isCommentsLoading.collectAsState()
    val isPostingComment by viewModel.isPostingComment.collectAsState()
    val errorMessage by viewModel.commentError.collectAsState()

    val currentUserId = viewModel.authVM.getCurrentUserId() ?: ""

    var commentText by remember { mutableStateOf("") }

    val bgColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryColor = if (isDarkMode) Color.Gray else Color.DarkGray

    // Charger le tweet et les commentaires
    LaunchedEffect(tweetId) {
        viewModel.loadTweetWithComments(tweetId)
        viewModel.observeTweet(tweetId)
    }

    // Nettoyer à la sortie
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearComments()
        }
    }

    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tweet") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bgColor
                    )
                )
            },
            bottomBar = {
                // Barre d'envoi de commentaire
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = bgColor,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Champ de texte
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Ajouter un commentaire...") },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp, max = 120.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TwitterBlue,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Bouton envoyer
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(tweetId, commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank() && !isPostingComment
                        ) {
                            if (isPostingComment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = TwitterBlue
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Envoyer",
                                    tint = if (commentText.isNotBlank()) TwitterBlue else Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            containerColor = bgColor
        ) { padding ->
            if (isLoading && tweet == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TwitterBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Le tweet
                    tweet?.let { t ->
                        item {
                            TweetDetailHeader(
                                tweet = t,
                                isDarkMode = isDarkMode,
                                currentUserId = currentUserId,
                                onLike = { viewModel.toggleLike(it) },
                                onSave = { viewModel.toggleSave(it) },
                                onUserClick = { userId ->
                                    navController.navigate("user_profile/$userId")
                                }
                            )
                        }
                    }

                    // Séparateur
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                    }

                    // Titre commentaires
                    item {
                        Text(
                            text = "Commentaires (${comments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Erreur
                    errorMessage?.let { error ->
                        item {
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Liste des commentaires
                    if (comments.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💬", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Aucun commentaire",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Soyez le premier à commenter !",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                isDarkMode = isDarkMode,
                                currentUserId = currentUserId,
                                onLike = { viewModel.toggleCommentLike(tweetId, comment.id) },
                                onDelete = { viewModel.deleteComment(tweetId, comment.id) },
                                onUserClick = { userId ->
                                    navController.navigate("user_profile/$userId")
                                }
                            )
                        }
                    }

                    // Espace en bas
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TweetDetailHeader(
    tweet: Tweet,
    isDarkMode: Boolean,
    currentUserId: String,
    onLike: (String) -> Unit,
    onSave: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryColor = if (isDarkMode) Color.Gray else Color.DarkGray

    val formattedTime = rememberFormattedTime(tweet.timestamp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header avec avatar et nom
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onUserClick(tweet.userId) }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF2F3336) else Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tweet.username.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = tweet.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    text = formattedTime,
                    fontSize = 14.sp,
                    color = secondaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenu du tweet
        Text(
            text = tweet.content,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Commentaires
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${tweet.commentsCount} commentaires",
                    color = secondaryColor,
                    fontSize = 14.sp
                )
            }

            // Likes
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${tweet.likes.size} j'aime",
                    color = secondaryColor,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

        Spacer(modifier = Modifier.height(12.dp))

        // Boutons d'action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLike(tweet.id) }
            ) {
                Icon(
                    imageVector = if (tweet.likes.contains(currentUserId))
                        Icons.Filled.Favorite
                    else
                        Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (tweet.likes.contains(currentUserId)) Color.Red else secondaryColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (tweet.likes.contains(currentUserId)) "Aimé" else "J'aime",
                    color = if (tweet.likes.contains(currentUserId)) Color.Red else secondaryColor
                )
            }

            // Save
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSave(tweet.id) }
            ) {
                Icon(
                    imageVector = if (tweet.saves.contains(currentUserId))
                        Icons.Filled.Bookmark
                    else
                        Icons.Filled.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (tweet.saves.contains(currentUserId)) TwitterBlue else secondaryColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (tweet.saves.contains(currentUserId)) "Sauvegardé" else "Sauvegarder",
                    color = if (tweet.saves.contains(currentUserId)) TwitterBlue else secondaryColor
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isDarkMode: Boolean,
    currentUserId: String,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryColor = if (isDarkMode) Color.Gray else Color.DarkGray
    val bgColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)

    val formattedTime = rememberFormattedTime(comment.timestamp)
    val isMyComment = comment.userId == currentUserId

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le commentaire") },
            text = { Text("Voulez-vous vraiment supprimer ce commentaire ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TwitterBlue)
                        .clickable { onUserClick(comment.userId) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comment.username.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textColor,
                        modifier = Modifier.clickable { onUserClick(comment.userId) }
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        color = secondaryColor
                    )
                }

                // Menu pour supprimer (seulement pour mes commentaires)
                if (isMyComment) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenu du commentaire
            Text(
                text = comment.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Like du commentaire
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLike() }
            ) {
                Icon(
                    imageVector = if (comment.likes.contains(currentUserId))
                        Icons.Filled.Favorite
                    else
                        Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (comment.likes.contains(currentUserId)) Color.Red else secondaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = comment.likes.size.toString(),
                    fontSize = 12.sp,
                    color = secondaryColor
                )
            }
        }
    }
}