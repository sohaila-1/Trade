package com.example.tradeconnect.ui.feed

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.tradeconnect.ui.components.UsersToFollowList
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.ui.feed.components.SidebarMenu
import com.example.tradeconnect.ui.feed.components.TabsHeader
import com.example.tradeconnect.ui.theme.*
import com.example.tradeconnect.viewmodel.TweetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: TweetViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    // Charger données une seule fois
    LaunchedEffect(Unit) {
        viewModel.loadMyTweets()
        viewModel.loadFollowingUsers()
        viewModel.loadAllUsers()
        viewModel.loadAllTweets()
        viewModel.loadCurrentUserProfile()
    }

    var selectedTab by remember { mutableStateOf(0) }

    val currentUserId = viewModel.authVM.getCurrentUserId() ?: ""

    // 🆕 CORRECTION : Récupérer les tweets
    val myTweets = viewModel.myTweets.value
    val followingTweets = viewModel.followingTweets.value

    // "Pour vous" = Mes tweets + Tweets des personnes que je suis
    val tweetsToShow = (myTweets + followingTweets)
        .distinctBy { it.id }
        .sortedByDescending { it.timestamp }

    // Récupérer le profil complet
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMoreDialog by remember { mutableStateOf(false) }
    var selectedTweetId by remember { mutableStateOf<String?>(null) }

    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val surfaceColor = if (isDarkMode) DarkSurface else LightSurface

    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarMenu(
                navController = navController,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("feed") { inclusive = true }
                    }
                },
                currentUser = currentUserProfile
            )
        }
    ) {
        Scaffold(
            containerColor = bgColor,
            floatingActionButton = {
                if (selectedTab == 0) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Scroll to top button
                        AnimatedVisibility(
                            visible = showScrollToTop,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                },
                                containerColor = surfaceColor,
                                contentColor = textColor
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Scroll to top"
                                )
                            }
                        }

                        // Create tweet button
                        FloatingCreateTweetButton {
                            navController.navigate("createTweet")
                        }
                    }
                }
            },
            bottomBar = {
                BottomNavBar(navController = navController, isDarkMode = isDarkMode)
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(paddingValues)
            ) {
                // Header + Tabs
                TabsHeader(
                    isDarkMode = isDarkMode,
                    textColor = textColor,
                    drawerState = drawerState,
                    scope = scope,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                // Contenu selon l'onglet
                when (selectedTab) {
                    // Onglet "Pour vous" - Mes tweets + Tweets des abonnements
                    0 -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Message si pas de tweets
                            if (tweetsToShow.isEmpty()) {
                                item {
                                    EmptyState(
                                        isDarkMode = isDarkMode,
                                        emoji = "🐦",
                                        title = "Aucun tweet pour le moment",
                                        subtitle = "Suivez des personnes pour voir leurs tweets ici !"
                                    )
                                }
                            }

                            // Liste des tweets
                            items(
                                items = tweetsToShow,
                                key = { it.id }
                            ) { tweet ->
                                TweetItem(
                                    tweet = tweet,
                                    isDarkMode = isDarkMode,
                                    currentUserId = currentUserId,
                                    onMoreClick = {
                                        selectedTweetId = tweet.id
                                        showMoreDialog = true
                                    },
                                    onLike = { id -> viewModel.toggleLike(id) },
                                    onSave = { id -> viewModel.toggleSave(id) },
                                    onRetweet = { id -> viewModel.toggleRetweet(id) },
                                    onUserClick = { userId ->
                                        navController.navigate("user_profile/$userId")
                                    },
                                    onCommentClick = { tweetId ->
                                        navController.navigate("tweet_detail/$tweetId")
                                    }
                                )
                            }

                            // Espace en bas
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }

                    // Onglet "Découvrir" - Utilisateurs à suivre
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Titre de section
                            Text(
                                text = "Personnes à suivre",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(16.dp)
                            )

                            if (viewModel.allUsers.value.isEmpty()) {
                                EmptyState(
                                    isDarkMode = isDarkMode,
                                    emoji = "👥",
                                    title = "Aucun utilisateur trouvé",
                                    subtitle = "Revenez plus tard pour découvrir de nouvelles personnes"
                                )
                            } else {
                                UsersToFollowList(
                                    users = viewModel.allUsers.value,
                                    followingIds = viewModel.followingList.value,
                                    getLastTweet = { uid -> viewModel.getLastTweetOfUser(uid) },
                                    onToggleFollow = { uid, isFollowing ->
                                        if (isFollowing) viewModel.unfollowUser(uid)
                                        else viewModel.followUser(uid)
                                    },
                                    onUserClick = { userId ->
                                        navController.navigate("user_profile/$userId")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet (Modifier / Supprimer)
    if (showMoreDialog && selectedTweetId != null) {
        ModalBottomSheet(
            onDismissRequest = { showMoreDialog = false },
            containerColor = surfaceColor,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Handle indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Modifier
                BottomSheetItem(
                    text = "Modifier",
                    textColor = textColor,
                    onClick = {
                        showMoreDialog = false
                        navController.navigate("editTweet/$selectedTweetId")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Supprimer
                BottomSheetItem(
                    text = "Supprimer",
                    textColor = Color.Red,
                    onClick = {
                        viewModel.deleteTweet(selectedTweetId!!)
                        showMoreDialog = false
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Annuler
                BottomSheetItem(
                    text = "Annuler",
                    textColor = Color.Gray,
                    onClick = { showMoreDialog = false }
                )
            }
        }
    }
}

@Composable
private fun BottomSheetItem(
    text: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = textColor,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp)
    )
}

@Composable
private fun EmptyState(
    isDarkMode: Boolean,
    emoji: String,
    title: String,
    subtitle: String
) {
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = secondaryColor
        )
    }
}