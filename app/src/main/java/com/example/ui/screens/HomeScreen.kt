package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.ui.theme.NepalBlue
import com.example.ui.theme.NepalCrimson
import com.example.ui.theme.NepalGold
import com.example.ui.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: QuizViewModel,
    onCategorySelected: (String, String) -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDailyChallenge by remember { mutableStateOf(false) }

    val filteredCategories = categories.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar View
                        val avatarStr = UserAvatars.getOrNull(user?.profilePicIdx ?: 0) ?: "🏔️"
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(NepalBlue)
                                .clickable { onOpenProfile() }
                                .testTag("home_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatarStr, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Namaste, ${user?.nickname ?: "Guest"}!",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Level ${user?.level ?: 1} Scholar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NepalCrimson
                            )
                        }

                        // Points Badge
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = NepalGold),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = "Points",
                                    tint = NepalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${user?.points ?: 0} pts",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = NepalBlue
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Elegant bottom bar setup
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already Home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onOpenLeaderboard() },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onOpenProfile() },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            // If admin, show Admin command panel FAB
            if (user?.isAdmin == true) {
                FloatingActionButton(
                    onClick = { onOpenAdmin() },
                    containerColor = NepalCrimson,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_fab")
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Panel")
                }
            }
        }
    ) { padValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search categories (e.g. Science)") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("category_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            // Animated view of grid selection
            AnimatedVisibility(
                visible = searchQuery.isEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // Daily Challenge Hero Panel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                // Trigger playing Himalayan Pride Challenge
                                onCategorySelected("him", "Himalayan Pride Challenge")
                            }
                            .testTag("daily_challenge_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(NepalBlue, NepalCrimson)
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NepalGold)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "HIMALAYAN PRIDE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = NepalBlue
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        "Everest & Sherpa Legends",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )

                                    Text(
                                        "Play now for double points + extra level boost celebrating peak icons!",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 16.sp
                                    )
                                }

                                SystemIconCircle(
                                    icon = Icons.Default.Landscape,
                                    tint = NepalGold,
                                    size = 48.dp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Quiz Categories",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (filteredCategories.isEmpty()) {
                // Empty state search search
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No categories fit your search criteria.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Categories Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredCategories) { cat ->
                        CategoryCard(
                            category = cat,
                            onPlay = { onCategorySelected(cat.id, cat.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onPlay() }
            .testTag("category_card_${category.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.description,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SystemIconCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
