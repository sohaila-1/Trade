package com.example.tradeconnect.ui.follow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.ui.feed.TweetItem
import com.example.tradeconnect.ui.theme.*
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.FollowViewModel
import com.example.tradeconnect.viewmodel.TweetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPublicProfileScreen(
    navController: NavController,
    followViewModel: FollowViewModel,
    userId: String,
    isDarkMode: Boolean = false,
    tweetViewModel: TweetViewModel? = null
) {
    val selectedUser by followViewModel.selectedUser.collectAsState()
    val currentUser by followViewModel.currentUser.collectAsState()
    val followingStatus by followViewModel.followingStatus.collectAsState()
    val blockedStatus by followViewModel.blockedStatus.collectAsState()

    val isFollowing = followingStatus[userId] ?: false
    val isBlocked = blockedStatus[userId] ?: false
    val isOwnProfile = followViewModel.isCurrentUser(userId)
    val isLoading = followViewModel.isLoading
    val isFollowLoading = followViewModel.isFollowLoading == userId

    // Tweets
    var userTweets by remember { mutableStateOf<List<Tweet>>(emptyList()) }
    var likedTweets by remember { mutableStateOf<List<Tweet>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialogs
    var showMenu by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }

    // Couleurs
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider
    val cardColor = if (isDarkMode) DarkSurface else LightSurface

    // Charger le profil
    LaunchedEffect(userId) {
        followViewModel.loadUserProfile(userId)
    }

    // Charger les tweets et likes
    LaunchedEffect(userId, tweetViewModel?.allTweets?.value) {
        if (tweetViewModel != null) {
            val allTweets = tweetViewModel.allTweets.value

            userTweets = allTweets
                .filter { it.userId == userId }
                .sortedByDescending { it.timestamp }

            likedTweets = allTweets
                .filter { it.likes.contains(userId) }
                .sortedByDescending { it.timestamp }
        }
    }

    // Nettoyer à la sortie
    DisposableEffect(Unit) {
        onDispose {
            followViewModel.clearSelectedUser()
        }
    }

    // Dialog de blocage
    if (showBlockDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            containerColor = cardColor,
            title = {
                Text(
                    "Bloquer @${selectedUser!!.email.substringBefore("@")} ?",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Text(
                    "Cette personne ne pourra plus voir votre profil, vos tweets ni vous envoyer de messages. Vous ne verrez plus ses tweets dans votre fil.",
                    color = secondaryColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        followViewModel.blockUser(userId)
                        showBlockDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0245E))
                ) {
                    Text("Bloquer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Annuler", color = TwitterBlue)
                }
            }
        )
    }

    // Dialog de déblocage
    if (showUnblockDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            containerColor = cardColor,
            title = {
                Text(
                    "Débloquer @${selectedUser!!.email.substringBefore("@")} ?",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Text(
                    "Cette personne pourra à nouveau voir votre profil, vos tweets et vous envoyer des messages.",
                    color = secondaryColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        followViewModel.unblockUser(userId)
                        showUnblockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
                ) {
                    Text("Débloquer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockDialog = false }) {
                    Text("Annuler", color = TwitterBlue)
                }
            }
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        IconButton(
                            onClick = { navController.navigate("settings") },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = "Paramètres",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Menu options
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = "Plus",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = cardColor
                            ) {
                                if (isBlocked) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Débloquer",
                                                color = TwitterBlue
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            showUnblockDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.RemoveCircleOutline,
                                                contentDescription = null,
                                                tint = TwitterBlue
                                            )
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Bloquer @${selectedUser?.email?.substringBefore("@") ?: ""}",
                                                color = Color(0xFFE0245E)
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            showBlockDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Block,
                                                contentDescription = null,
                                                tint = Color(0xFFE0245E)
                                            )
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Signaler",
                                            color = textColor
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        // TODO: Signalement
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Flag,
                                            contentDescription = null,
                                            tint = secondaryColor
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        when {
            // Loading
            isLoading && selectedUser == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TwitterBlue)
                }
            }

            // Utilisateur bloqué - Afficher un message
            isBlocked && selectedUser != null -> {
                val user = selectedUser!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor)
                ) {
                    // Bannière
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            secondaryColor.copy(alpha = 0.5f),
                                            secondaryColor.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )
                    }

                    // Section profil bloqué
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-40).dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Avatar grisé
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(82.dp)
                                            .clip(CircleShape)
                                            .background(secondaryColor.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Block,
                                            contentDescription = null,
                                            tint = secondaryColor,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }

                                // Bouton débloquer
                                Button(
                                    onClick = { showUnblockDialog = true },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TwitterBlue
                                    ),
                                    modifier = Modifier
                                        .padding(top = 48.dp)
                                        .height(36.dp)
                                ) {
                                    Text(
                                        "Débloquer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = user.username,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            Text(
                                text = "@${user.email.substringBefore("@")}",
                                fontSize = 15.sp,
                                color = secondaryColor
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Message bloqué
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = cardColor,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Outlined.Block,
                                        contentDescription = null,
                                        tint = Color(0xFFE0245E),
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        "Vous avez bloqué @${user.email.substringBefore("@")}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Vous ne pouvez pas voir les tweets de cette personne et elle ne peut pas voir les vôtres.",
                                        fontSize = 14.sp,
                                        color = secondaryColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Utilisateur non trouvé
            !isLoading && selectedUser == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.PersonOff,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Utilisateur non trouvé",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                        followViewModel.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error,
                                color = Color(0xFFE0245E),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Profil trouvé (non bloqué)
            selectedUser != null && !isBlocked -> {
                val user = selectedUser!!
                val currentUserId = currentUser?.uid ?: ""

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor)
                ) {
                    // Bannière
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            TwitterBlue,
                                            TwitterBlue.copy(alpha = 0.7f),
                                            TwitterBlueDark
                                        )
                                    )
                                )
                        )
                    }

                    // Section profil
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-40).dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        user.profileImageUrl.startsWith("data:image") ||
                                                user.profileImageUrl.length > 200 -> {
                                            Base64ProfileImage(
                                                base64String = user.profileImageUrl,
                                                modifier = Modifier
                                                    .size(82.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                        user.profileImageUrl.isNotEmpty() &&
                                                (user.profileImageUrl.startsWith("http") ||
                                                        user.profileImageUrl.startsWith("https")) -> {
                                            AsyncImage(
                                                model = user.profileImageUrl,
                                                contentDescription = "Profile",
                                                modifier = Modifier
                                                    .size(82.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        else -> {
                                            DefaultAvatar(
                                                letter = user.username.firstOrNull()?.uppercase() ?: "?",
                                                modifier = Modifier.size(82.dp)
                                            )
                                        }
                                    }

                                    if (user.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(bgColor)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(OnlineGreen)
                                                .align(Alignment.BottomEnd)
                                        )
                                    }
                                }

                                // Boutons d'action
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 48.dp)
                                ) {
                                    if (!isOwnProfile) {
                                        OutlinedIconButton(
                                            onClick = {
                                                navController.navigate("chat/${user.uid}/${user.username}")
                                            },
                                            modifier = Modifier.size(36.dp),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(dividerColor, dividerColor)
                                                )
                                            )
                                        ) {
                                            Icon(
                                                Icons.Outlined.Email,
                                                contentDescription = "Message",
                                                tint = textColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Button(
                                            onClick = { followViewModel.toggleFollow(userId) },
                                            enabled = !isFollowLoading,
                                            shape = RoundedCornerShape(20.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isFollowing) Color.Transparent else textColor,
                                                contentColor = if (isFollowing) textColor else bgColor
                                            ),
                                            border = if (isFollowing) ButtonDefaults.outlinedButtonBorder.copy(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(dividerColor, dividerColor)
                                                )
                                            ) else null,
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            if (isFollowLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = if (isFollowing) textColor else bgColor
                                                )
                                            } else {
                                                Text(
                                                    text = if (isFollowing) "Abonné" else "Suivre",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { navController.navigate("edit_profile") },
                                            shape = RoundedCornerShape(20.dp),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(dividerColor, dividerColor)
                                                )
                                            ),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(
                                                "Éditer le profil",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = user.username,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            Text(
                                text = "@${user.email.substringBefore("@")}",
                                fontSize = 15.sp,
                                color = secondaryColor
                            )

                            if (!isOwnProfile && currentUser?.followers?.contains(userId) == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = secondaryColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Vous suit",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 12.sp,
                                        color = secondaryColor
                                    )
                                }
                            }

                            if (user.bio.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = user.bio,
                                    fontSize = 15.sp,
                                    color = textColor,
                                    lineHeight = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (user.isOnline) OnlineGreen else secondaryColor
                                            )
                                    )
                                    Text(
                                        text = if (user.isOnline) "En ligne" else user.getLastSeenText(),
                                        fontSize = 14.sp,
                                        color = secondaryColor
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = secondaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Inscrit ${formatJoinDate(user.createdAt)}",
                                        fontSize = 14.sp,
                                        color = secondaryColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.clickable {
                                        navController.navigate("following/$userId")
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${user.followingCount}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "abonnements",
                                        fontSize = 15.sp,
                                        color = secondaryColor
                                    )
                                }

                                Row(
                                    modifier = Modifier.clickable {
                                        navController.navigate("followers/$userId")
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${user.followersCount}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "followers",
                                        fontSize = 15.sp,
                                        color = secondaryColor
                                    )
                                }
                            }
                        }
                    }

                    // Tabs
                    item {
                        ProfileTabs(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            tweetsCount = userTweets.size,
                            likesCount = likedTweets.size,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            dividerColor = dividerColor
                        )
                    }

                    // Contenu des tabs
                    when (selectedTab) {
                        0 -> {
                            if (userTweets.isEmpty()) {
                                item {
                                    EmptyTabContent(
                                        emoji = "📝",
                                        title = if (isOwnProfile) "Vous n'avez pas encore posté" else "${user.username} n'a pas encore posté",
                                        subtitle = if (isOwnProfile) "Quand vous posterez, vos tweets apparaîtront ici." else "Quand ${user.username} postera, ses tweets apparaîtront ici.",
                                        textColor = textColor,
                                        secondaryColor = secondaryColor
                                    )
                                }
                            } else {
                                items(
                                    items = userTweets,
                                    key = { "post_${it.id}" }
                                ) { tweet ->
                                    TweetItem(
                                        tweet = tweet,
                                        isDarkMode = isDarkMode,
                                        currentUserId = currentUserId,
                                        onMoreClick = { },
                                        onLike = { tweetViewModel?.toggleLike(it) },
                                        onSave = { tweetViewModel?.toggleSave(it) },
                                        onRetweet = { tweetViewModel?.toggleRetweet(it) },
                                        onUserClick = null,
                                        onCommentClick = { tweetId ->
                                            navController.navigate("tweet_detail/$tweetId")
                                        }
                                    )
                                }
                            }
                        }
                        1 -> {
                            item {
                                EmptyTabContent(
                                    emoji = "💬",
                                    title = "Aucune réponse",
                                    subtitle = "Les réponses apparaîtront ici",
                                    textColor = textColor,
                                    secondaryColor = secondaryColor
                                )
                            }
                        }
                        2 -> {
                            item {
                                EmptyTabContent(
                                    emoji = "🖼️",
                                    title = "Aucun média",
                                    subtitle = "Les photos et vidéos apparaîtront ici",
                                    textColor = textColor,
                                    secondaryColor = secondaryColor
                                )
                            }
                        }
                        3 -> {
                            if (likedTweets.isEmpty()) {
                                item {
                                    EmptyTabContent(
                                        emoji = "❤️",
                                        title = if (isOwnProfile) "Vous n'avez aimé aucun tweet" else "${user.username} n'a aimé aucun tweet",
                                        subtitle = if (isOwnProfile) "Les tweets que vous aimez apparaîtront ici." else "Les tweets que ${user.username} aime apparaîtront ici.",
                                        textColor = textColor,
                                        secondaryColor = secondaryColor
                                    )
                                }
                            } else {
                                items(
                                    items = likedTweets,
                                    key = { "like_${it.id}" }
                                ) { tweet ->
                                    TweetItem(
                                        tweet = tweet,
                                        isDarkMode = isDarkMode,
                                        currentUserId = currentUserId,
                                        onMoreClick = { },
                                        onLike = { tweetViewModel?.toggleLike(it) },
                                        onSave = { tweetViewModel?.toggleSave(it) },
                                        onRetweet = { tweetViewModel?.toggleRetweet(it) },
                                        onUserClick = { clickedUserId ->
                                            if (clickedUserId != userId) {
                                                navController.navigate("user_profile/$clickedUserId")
                                            }
                                        },
                                        onCommentClick = { tweetId ->
                                            navController.navigate("tweet_detail/$tweetId")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tweetsCount: Int,
    likesCount: Int,
    textColor: Color,
    secondaryColor: Color,
    dividerColor: Color
) {
    val tabs = listOf(
        TabInfo("Posts", if (tweetsCount > 0) tweetsCount else null),
        TabInfo("Réponses", null),
        TabInfo("Médias", null),
        TabInfo("J'aime", if (likesCount > 0) likesCount else null)
    )

    Column {
        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)

        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = tab.title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) textColor else secondaryColor
                        )

                        if (tab.count != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${tab.count})",
                                fontSize = 12.sp,
                                color = if (selectedTab == index) textColor else secondaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(if (selectedTab == index) 50.dp else 0.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (selectedTab == index) TwitterBlue else Color.Transparent
                            )
                    )
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
    }
}

private data class TabInfo(
    val title: String,
    val count: Int?
)

@Composable
private fun EmptyTabContent(
    emoji: String,
    title: String,
    subtitle: String,
    textColor: Color,
    secondaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = secondaryColor,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatJoinDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.FRANCE)
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "récemment"
    }
}