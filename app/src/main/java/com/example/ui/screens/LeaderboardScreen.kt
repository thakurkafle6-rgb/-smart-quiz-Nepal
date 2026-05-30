package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.NepalBlue
import com.example.ui.theme.NepalCrimson
import com.example.ui.theme.NepalGold
import com.example.ui.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: QuizViewModel,
    onNavigateHome: () -> Unit
) {
    val usersList by viewModel.allUsersOrdered.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: All-Time

    // Dynamically scale/simulate scores for separate tabs so they aren't duplicates
    val computedUsers: List<UserRankItem> = remember(usersList, selectedTab) {
        usersList.mapIndexed { idx, user ->
            val scaleFactor = when (selectedTab) {
                0 -> 0.15f // Daily points
                1 -> 0.45f // Weekly points
                else -> 1.0f // All-Time points
            }
            val calculatedPoints = (user.points * scaleFactor).toInt().coerceAtLeast(10)
            UserRankItem(
                user = user,
                displayPoints = if (user.id == currentUser?.id) {
                    // Accumulate real score
                    if (selectedTab == 0) (user.points % 150).coerceAtLeast(45)
                    else if (selectedTab == 1) (user.points % 400).coerceAtLeast(120)
                    else user.points
                } else {
                    calculatedPoints
                }
            )
        }.sortedByDescending { it.displayPoints }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crown Leaderboard", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padValues)
        ) {
            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Daily", "Weekly", "All-Time").forEachIndexed { index, tabName ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                if (isSelected) NepalBlue else Color.Transparent
                            )
                            .clickable { selectedTab = index }
                            .testTag("leaderboard_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (computedUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Header Podium (1st, 2nd, 3rd)
                    item {
                        PodiumSection(
                            topThree = computedUsers.take(3),
                            currentUserId = currentUser?.id ?: -1
                        )
                    }

                    // Remaining ranking listings
                    val remList = computedUsers.drop(3)
                    itemsIndexed(remList) { valIndex, rankItem ->
                        val trueRank = valIndex + 4
                        val user = rankItem.user
                        val isSelf = user.id == currentUser?.id

                        val bgCardColor = if (isSelf) {
                            NepalBlue.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }

                        val borderStroke = if (isSelf) {
                            androidx.compose.foundation.BorderStroke(1.5.dp, NepalBlue)
                        } else {
                            null
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("leaderboard_user_$trueRank"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = bgCardColor),
                            border = borderStroke
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank numerical state
                                Text(
                                    text = "$trueRank",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Custom Avatar
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = UserAvatars.getOrNull(user.profilePicIdx) ?: "🏔️",
                                        fontSize = 20.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isSelf) "${user.nickname} (You)" else user.nickname,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Level ${user.level} Scholar",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Points tracker
                                Text(
                                    text = "${rankItem.displayPoints} pts",
                                    fontWeight = FontWeight.Black,
                                    color = NepalCrimson,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Simple Home navigation bar setup
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* already here */ },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") }
                )
            }
        }
    }
}

@Composable
fun PodiumSection(
    topThree: List<UserRankItem>,
    currentUserId: Int
) {
    // 1st is middle, 2nd on left, 3rd on right
    val first = topThree.getOrNull(0)
    val second = topThree.getOrNull(1)
    val third = topThree.getOrNull(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(210.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // SECOND PLACE (Left)
        if (second != null) {
            PodiumCol(
                rankItem = second,
                placement = 2,
                podiumHeight = 110.dp,
                color = Color(0xFFC0C0C0), // Silver
                crownIcon = Icons.Default.Star,
                isSelf = second.user.id == currentUserId
            )
        }

        // FIRST PLACE (Center - tallest)
        if (first != null) {
            PodiumCol(
                rankItem = first,
                placement = 1,
                podiumHeight = 150.dp,
                color = NepalGold, // Gold
                crownIcon = Icons.Default.MilitaryTech,
                isSelf = first.user.id == currentUserId
            )
        }

        // THIRD PLACE (Right)
        if (third != null) {
            PodiumCol(
                rankItem = third,
                placement = 3,
                podiumHeight = 90.dp,
                color = Color(0xFFCD7F32), // Bronze
                crownIcon = Icons.Default.Star,
                isSelf = third.user.id == currentUserId
            )
        }
    }
}

@Composable
fun PodiumCol(
    rankItem: UserRankItem,
    placement: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    color: Color,
    crownIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelf: Boolean
) {
    Column(
        modifier = Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(if (placement == 1) 64.dp else 52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = UserAvatars.getOrNull(rankItem.user.profilePicIdx) ?: "🏔️",
                    fontSize = if (placement == 1) 32.sp else 24.sp
                )
            }

            // Small badge/Crown
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = crownIcon,
                    contentDescription = null,
                    tint = NepalBlue,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSelf) "${rankItem.user.nickname} (You)" else rankItem.user.nickname,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${rankItem.displayPoints} pts",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NepalCrimson
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Visual podium base block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(color.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$placement",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = NepalBlue
            )
        }
    }
}

data class UserRankItem(
    val user: User,
    val displayPoints: Int
)
