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

    val isFollowing = followingStatus[userId] ?: false
    val isOwnProfile = followViewModel.isCurrentUser(userId)
    val isLoading = followViewModel.isLoading
    val isFollowLoading = followViewModel.isFollowLoading == userId

    // 🆕 Tweets
    var userTweets by remember { mutableStateOf<List<Tweet>>(emptyList()) }
    var likedTweets by remember { mutableStateOf<List<Tweet>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Couleurs
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider

    // Charger le profil
    LaunchedEffect(userId) {
        followViewModel.loadUserProfile(userId)
    }

    // 🆕 Charger les tweets et likes
    LaunchedEffect(userId, tweetViewModel?.allTweets?.value) {
        if (tweetViewModel != null) {
            val allTweets = tweetViewModel.allTweets.value

            // Tweets de l'utilisateur
            userTweets = allTweets
                .filter { it.userId == userId }
                .sortedByDescending { it.timestamp }

            // Tweets aimés par l'utilisateur
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
                        IconButton(
                            onClick = { /* TODO */ },
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
                    }
                }
            }

            // Profil trouvé
            selectedUser != null -> {
                val user = selectedUser!!
                val currentUserId = currentUser?.uid ?: ""

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor)
                ) {
                    // ==================== BANNIÈRE ====================
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

                    // ==================== SECTION PROFIL ====================
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-40).dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            // Row avec Avatar et Boutons
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

                                    // Indicateur en ligne
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
                                        // Bouton Message
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

                                        // Bouton Follow/Unfollow
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
                                        // Bouton Modifier le profil
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

                            // Nom d'utilisateur
                            Text(
                                text = user.username,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            // Email / Handle
                            Text(
                                text = "@${user.email.substringBefore("@")}",
                                fontSize = 15.sp,
                                color = secondaryColor
                            )

                            // Badge "Vous suit"
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

                            // Bio
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

                            // Infos supplémentaires
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Statut en ligne
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

                                // Date d'inscription
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

                            // Stats Followers / Following
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Following
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

                                // Followers
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

                    // ==================== TABS ====================
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

                    // ==================== CONTENU DES TABS ====================
                    when (selectedTab) {
                        // Tab "Posts"
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
                                        onMoreClick = { /* TODO */ },
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

                        // Tab "Réponses"
                        1 -> {
                            item {
                                EmptyTabContent(
                                    emoji = "💬",
                                    title = "Aucune réponse",
                                    subtitle = "Les réponses aux tweets apparaîtront ici",
                                    textColor = textColor,
                                    secondaryColor = secondaryColor
                                )
                            }
                        }

                        // Tab "Médias"
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

                        // Tab "J'aime" - 🆕 FONCTIONNEL
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
                                        onMoreClick = { /* TODO */ },
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

                    // Espace en bas
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

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
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

                        // Badge avec le nombre
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

                    // Indicateur
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
        Text(
            text = emoji,
            fontSize = 48.sp
        )

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