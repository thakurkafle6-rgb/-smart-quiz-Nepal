package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NepalBlue
import com.example.ui.theme.NepalCrimson
import com.example.ui.theme.NepalGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: QuizViewModel,
    onNavigateHome: () -> Unit,
    onReplayQuiz: () -> Unit
) {
    val context = LocalContext.current
    val score by viewModel.quizScore.collectAsState()
    val correctCount by viewModel.correctAnswersCount.collectAsState()
    val questions by viewModel.activeQuizQuestions.collectAsState()
    val pointsEarned by viewModel.pointsEarned.collectAsState()
    val categoryName by viewModel.activeQuizCategoryName.collectAsState()
    val categoryId by viewModel.activeQuizCategoryId.collectAsState()

    val totalQuestions = questions.size
    val accuracyPct = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions.toFloat()) * 100 else 0f

    val brushBg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    // Achievement text check
    val performanceTier = when {
        accuracyPct >= 100f -> "🏆 Legendary! (Perfect Everest Run)"
        accuracyPct >= 80f -> "⭐ Excellent Smart Quizzer"
        accuracyPct >= 50f -> "👍 Good effort"
        else -> "📚 Keep learning! Nepal is beautiful."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Finished", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brushBg)
                .padding(padValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = categoryName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Accuracy Ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { accuracyPct / 100f },
                        modifier = Modifier.size(130.dp),
                        strokeWidth = 10.dp,
                        color = if (accuracyPct >= 80) SuccessGreen else if (accuracyPct >= 50) NepalGold else NepalCrimson,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${accuracyPct.toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Accuracy",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = performanceTier,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Numeric stats rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCell(
                        modifier = Modifier.weight(1f).testTag("score_stat_cell"),
                        title = "Final Score",
                        value = "$score / ${totalQuestions * 10}",
                        subValue = "$correctCount Correct Ans",
                        icon = Icons.Default.EmojiEvents,
                        color = NepalGold
                    )

                    StatCell(
                        modifier = Modifier.weight(1f).testTag("points_stat_cell"),
                        title = "Rewards Points",
                        value = "+$pointsEarned PTS",
                        subValue = "Double Exp",
                        icon = Icons.Default.MilitaryTech,
                        color = NepalCrimson
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mock Social Share Button
                Button(
                    onClick = {
                        val shareText = "I scored $score points with ${accuracyPct.toInt()}% accuracy in '$categoryName' on Smart Quiz Nepal! Can you defeat my high score? 🏔️🇳🇵"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Smart Quiz Nepal Score", shareText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Score summary copied to Clipboard! Ready to paste and share.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_score_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Share Score on Social Media",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Lower Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.startQuiz(categoryId, categoryName)
                        onReplayQuiz()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("replay_quiz_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NepalBlue)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Play Again", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                OutlinedButton(
                    onClick = onNavigateHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("back_home_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Return to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCell(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subValue,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
