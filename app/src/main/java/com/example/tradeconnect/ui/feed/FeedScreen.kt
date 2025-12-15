package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.viewmodel.TweetViewModel
import com.example.tradeconnect.ui.feed.components.SidebarMenu
import com.example.tradeconnect.ui.feed.components.TabsHeader
import com.example.tradeconnect.ui.components.UsersToFollowList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: TweetViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {

    // ⭐ Charger données une seule fois
    LaunchedEffect(Unit) {
        viewModel.loadMyTweets()
        viewModel.loadFollowingUsers()
        viewModel.loadAllUsers()
        viewModel.loadAllTweets()
    }

    var selectedTab by remember { mutableStateOf(0) }

    val myTweets = viewModel.myTweets.value
    val followingTweets = viewModel.followingTweets.value
    val tweetsToShow = if (selectedTab == 0) myTweets else followingTweets

    val currentUserId = viewModel.authVM.getCurrentUserId() ?: ""

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMoreDialog by remember { mutableStateOf(false) }
    var selectedTweetId by remember { mutableStateOf<String?>(null) }

    val bgColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

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
                }
            )
        }
    ) {

        Scaffold(
            containerColor = bgColor,
            floatingActionButton = {
                FloatingCreateTweetButton {
                    navController.navigate("createTweet")
                }
            },
            bottomBar = {
                BottomNavBar(navController = navController, isDarkMode = isDarkMode)
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .background(bgColor)
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp)
            ) {

                // ⭐ Header + Tabs
                TabsHeader(
                    isDarkMode = isDarkMode,
                    textColor = textColor,
                    drawerState = drawerState,
                    scope = scope,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ⭐ Onglet "Abonnements" — utilisateurs à suivre
                if (selectedTab == 1) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(tweetsToShow) { tweet ->
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
                            onUserClick = { userId ->
                                navController.navigate("user_profile/$userId")
                            },
                            onCommentClick = { tweetId ->
                                // 🆕 Navigation vers les commentaires
                                navController.navigate("tweet_detail/$tweetId")
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // ⭐ Bottom Sheet (Modifier / Supprimer)
    if (showMoreDialog && selectedTweetId != null) {
        ModalBottomSheet(
            onDismissRequest = { showMoreDialog = false },
            containerColor = bgColor,
            sheetState = sheetState
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {

                Text(
                    "Modifier",
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clickable {
                            showMoreDialog = false
                            navController.navigate("editTweet/$selectedTweetId")
                        }
                )

                Text(
                    "Supprimer",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clickable {
                            viewModel.deleteTweet(selectedTweetId!!)
                            showMoreDialog = false
                        }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Annuler",
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clickable { showMoreDialog = false }
                )
            }
        }
    }
}