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
import com.example.tradeconnect.ui.components.UserFollowItem
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
    // Charger les données
    LaunchedEffect(Unit) {
        viewModel.loadMyTweets()
        viewModel.loadFollowingUsers()
        viewModel.loadAllUsers()
    }

    var selectedTab by remember { mutableStateOf(0) }

    val myTweets = viewModel.myTweets.value
    val followingTweets = viewModel.followingTweets.value
    val tweetsToShow = if (selectedTab == 0) myTweets else followingTweets

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

                // Header : Tabs + Menu
                TabsHeader(
                    isDarkMode = isDarkMode,
                    textColor = textColor,
                    drawerState = drawerState,
                    scope = scope,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ⭐ FIX CRASH : LazyColumn doit avoir weight(1f)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)   // OBLIGATOIRE !!!!!
                ) {

                    if (selectedTab == 1) {

                        // Titre section
                        item {
                            Text(
                                text = "Suggestions",
                                color = textColor,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Liste utilisateurs → chaque user = item séparé
                        items(viewModel.allUsers.value) { user ->
                            UserFollowItem(
                                user = user,
                                isFollowing = viewModel.followingList.value.contains(user.uid),
                                onToggleFollow = { uid, isFollowing ->
                                    if (isFollowing) viewModel.unfollowUser(uid)
                                    else viewModel.followUser(uid)
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }


                    // Tweets
                    items(tweetsToShow) { tweet ->
                        TweetItem(
                            tweet = tweet,
                            isDarkMode = isDarkMode,
                            onMoreClick = {
                                selectedTweetId = tweet.id
                                showMoreDialog = true
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // ---- BOTTOM SHEET ----
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
                    text = "Modifier",
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
                    text = "Supprimer",
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
                    text = "Annuler",
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
