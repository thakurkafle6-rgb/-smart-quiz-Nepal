package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NepalBlue
import com.example.ui.theme.NepalCrimson
import com.example.ui.theme.NepalGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: QuizViewModel,
    onNavigateHome: () -> Unit,
    onLogoutCompleted: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val achievements by viewModel.currentAchievements.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var editedNickname by remember { mutableStateOf(user?.nickname ?: "") }

    val totalPlayed = user?.totalPlayed ?: 0
    val totalScore = user?.totalScore ?: 0
    val accuracy = if ((user?.totalQuestionsAnswered ?: 0) > 0) {
        ((user?.correctAnswersCount ?: 0).toFloat() / (user?.totalQuestionsAnswered ?: 0).toFloat()) * 100f
    } else {
        0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scholar Profile", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* navigate to leaderboard handled in parent router */ },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* already here */ },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Identity Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_identity_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar clickable
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(NepalBlue)
                                .clickable { showAvatarDialog = true }
                                .testTag("avatar_picker_trigger"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                UserAvatars.getOrNull(user?.profilePicIdx ?: 0) ?: "🏔️",
                                fontSize = 54.sp
                            )
                            // edit indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(NepalCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = user?.nickname ?: "Guest Scholar",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    editedNickname = user?.nickname ?: ""
                                    showEditDialog = true
                                },
                                modifier = Modifier.size(24.dp).testTag("edit_nickname_button")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = user?.email ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Level slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Level ${user?.level ?: 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NepalCrimson
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Simple loading progression mapping next level info
                            val progression = (user?.points ?: 0) % 500
                            LinearProgressIndicator(
                                progress = { progression / 500f },
                                modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                                color = SuccessGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "$progression/500 exp",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Stats row (Quizzes | Score | Accuracy)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatBadge(
                        modifier = Modifier.weight(1f).testTag("total_quizzes_cell"),
                        title = "Played",
                        value = "$totalPlayed",
                        subtext = "Quizzes",
                        icon = Icons.Default.Quiz,
                        color = NepalBlue
                    )

                    MiniStatBadge(
                        modifier = Modifier.weight(1f).testTag("total_score_cell"),
                        title = "Aggregate",
                        value = "$totalScore",
                        subtext = "Points",
                        icon = Icons.Default.EmojiEvents,
                        color = NepalGold
                    )

                    MiniStatBadge(
                        modifier = Modifier.weight(1f).testTag("accuracy_cell"),
                        title = "Accuracy",
                        value = "${accuracy.toInt()}%",
                        subtext = "Tracked",
                        icon = Icons.Default.Adjust,
                        color = SuccessGreen
                    )
                }
            }

            // Badge/Achievements cabinet
            item {
                Text(
                    "Achievement Cabinet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Standard checklist of possible achievements
            val potentialAchievements = listOf(
                AchievementDef("first_quiz", "Maiden Ascent", "Played first quiz session", Icons.Default.Star),
                AchievementDef("five_quizzes", "Trivia Regular", "Completed 5 distinct quizzes", Icons.Default.Public),
                AchievementDef("perfect_score", "Everest Cleared", "Gained 100% accurate results", Icons.Default.Landscape),
                AchievementDef("point_milestone", "Golden Pagoda", "Accumulated over 1500 loyalty points", Icons.Default.AccountBalance),
                AchievementDef("admin_creation", "App Architect", "Enriched Nepalese data pools", Icons.Default.Settings)
            )

            potentialAchievements.forEach { def ->
                val matchingEarned = achievements.find { it.achievementId == def.id }
                val isUnlocked = matchingEarned != null

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("badge_card_${def.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isUnlocked) NepalGold.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = def.icon,
                                    contentDescription = null,
                                    tint = if (isUnlocked) NepalGold else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = def.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = def.description,
                                    fontSize = 11.sp,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            if (isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "UNLOCKED",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = SuccessGreen
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.Gray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Log Out Button Action
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogoutCompleted()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("logout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NepalCrimson),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout Scholar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Edit Name Dialogue
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Nickname", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editedNickname,
                    onValueChange = { editedNickname = it },
                    placeholder = { Text("Enter nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("name_edit_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfileNickname(editedNickname)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                    modifier = Modifier.testTag("name_edit_confirm")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Avatar Choice Dialogue
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Choose Scholar Avatar", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(UserAvatars) { index, avatar ->
                            val isSelected = user?.profilePicIdx == index
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) NepalBlue else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        viewModel.changeProfileAvatar(index)
                                        showAvatarDialog = false
                                    }
                                    .testTag("avatar_option_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatar, fontSize = 28.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun MiniStatBadge(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(subtext, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
