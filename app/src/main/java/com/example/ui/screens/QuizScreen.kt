package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.ui.theme.NepalBlue
import com.example.ui.theme.NepalCrimson
import com.example.ui.theme.NepalGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onQuizClosed: () -> Unit,
    onQuizCompleted: () -> Unit
) {
    val context = LocalContext.current
    val questions by viewModel.activeQuizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedOption by viewModel.selectedAnswerOption.collectAsState()
    val isChecked by viewModel.isAnswerChecked.collectAsState()
    val secondsLeft by viewModel.quizSecondsLeft.collectAsState()
    val categoryName by viewModel.activeQuizCategoryName.collectAsState()
    val isFinished by viewModel.isQuizFinished.collectAsState()

    val currentQ = questions.getOrNull(currentIndex)

    // Trigger router completion on finish
    LaunchedEffect(isFinished) {
        if (isFinished) {
            onQuizCompleted()
        }
    }

    // Capture soundCue flows (simulate play visual notifications)
    var transientMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        viewModel.soundCue.collectLatest { cue ->
            when (cue) {
                "correct" -> transientMessage = "🎉 CORRECT! +15 PTS"
                "incorrect" -> transientMessage = "😢 INCORRECT"
            }
        }
    }

    // Auto-clear message
    LaunchedEffect(transientMessage) {
        if (transientMessage != null) {
            kotlinx.coroutines.delay(1200)
            transientMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onQuizClosed,
                        modifier = Modifier.testTag("exit_quiz_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Quiz")
                    }
                },
                actions = {
                    // Question index tracker
                    Text(
                        "Q: ${currentIndex + 1}/${questions.size}",
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padValues ->
        if (currentQ == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Progress Bar
                val progressFraction = (currentIndex + 1).toFloat() / questions.size.toFloat()
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .testTag("quiz_progress"),
                    color = NepalBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Timer Circular Indicator block
                    val timerColor = if (secondsLeft <= 5) NepalCrimson else NepalGold
                    val timerScale = if (secondsLeft <= 5) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scaleAnim by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_scale"
                        )
                        scaleAnim
                    } else {
                        1f
                    }

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .scale(timerScale)
                            .clip(CircleShape)
                            .background(timerColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { secondsLeft / 20f },
                            modifier = Modifier.size(70.dp),
                            color = timerColor,
                            strokeWidth = 5.dp
                        )
                        Text(
                            text = secondsLeft.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (secondsLeft <= 5) NepalCrimson else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // The Question Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Text(
                            text = currentQ.questionText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(24.dp)
                                .testTag("question_text"),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Transient correct/incorrect animation feedback
                    AnimatedVisibility(
                        visible = transientMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (transientMessage != null) {
                            Text(
                                text = transientMessage ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (transientMessage!!.contains("CORRECT")) SuccessGreen else NepalCrimson,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option Buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OptionRow(
                            prefix = "A",
                            optionText = currentQ.optionA,
                            isSelected = selectedOption == "A",
                            isChecked = isChecked,
                            isCorrect = currentQ.correctAnswer == "A",
                            onClick = { viewModel.selectAnswer("A") }
                        )

                        OptionRow(
                            prefix = "B",
                            optionText = currentQ.optionB,
                            isSelected = selectedOption == "B",
                            isChecked = isChecked,
                            isCorrect = currentQ.correctAnswer == "B",
                            onClick = { viewModel.selectAnswer("B") }
                        )

                        OptionRow(
                            prefix = "C",
                            optionText = currentQ.optionC,
                            isSelected = selectedOption == "C",
                            isChecked = isChecked,
                            isCorrect = currentQ.correctAnswer == "C",
                            onClick = { viewModel.selectAnswer("C") }
                        )

                        OptionRow(
                            prefix = "D",
                            optionText = currentQ.optionD,
                            isSelected = selectedOption == "D",
                            isChecked = isChecked,
                            isCorrect = currentQ.correctAnswer == "D",
                            onClick = { viewModel.selectAnswer("D") }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Explanation Screen (if checked)
                    AnimatedVisibility(
                        visible = isChecked && currentQ.explanation.isNotBlank(),
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .testTag("explanation_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = NepalGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Fact Booster",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentQ.explanation,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Lower Action: Check / Next
                    if (!isChecked) {
                        Button(
                            onClick = {
                                viewModel.checkAnswer(selectedOption)
                            },
                            enabled = selectedOption.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_answer_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NepalBlue)
                        ) {
                            Text("Verify Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.moveToNextQuestion()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("next_question_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NepalCrimson)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    if (currentIndex + 1 < questions.size) "Next Question" else "Reveal Final Score",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionRow(
    prefix: String,
    optionText: String,
    isSelected: Boolean,
    isChecked: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    // Dynamic color coding based on selection & correctness states
    val containerColor = when {
        isChecked && isCorrect -> SuccessGreen.copy(alpha = 0.2f)
        isChecked && isSelected && !isCorrect -> NepalCrimson.copy(alpha = 0.2f)
        isSelected -> NepalBlue.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isChecked && isCorrect -> SuccessGreen
        isChecked && isSelected && !isCorrect -> NepalCrimson
        isSelected -> NepalBlue
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val iconTint = when {
        isChecked && isCorrect -> SuccessGreen
        isChecked && isSelected && !isCorrect -> NepalCrimson
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val optionTag = "option_card_$prefix"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isChecked) { onClick() }
            .testTag(optionTag),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle symbol index
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) NepalBlue else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prefix,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = optionText,
                modifier = Modifier.weight(1f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isChecked) {
                if (isCorrect) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Correct",
                        tint = SuccessGreen
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Incorrect",
                        tint = NepalCrimson
                    )
                }
            }
        }
    }
}
