package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QuizViewModel
import com.example.ui.viewmodel.QuizViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: QuizViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract our globally seeded repository and construct the state ViewModel
        val app = application as SmartQuizApplication
        viewModel = ViewModelProvider(
            this,
            QuizViewModelFactory(app.repository)
        )[QuizViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsState()

                // State-based secure client-side router
                var currentScreen by remember { mutableStateOf("auth") }

                // Auto route logged state
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        currentScreen = "auth"
                    } else if (currentScreen == "auth") {
                        currentScreen = "home"
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "auth" -> {
                            AuthScreen(
                                viewModel = viewModel,
                                onAuthSuccess = {
                                    currentScreen = "home"
                                }
                            )
                        }
                        "home" -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onCategorySelected = { catId, catName ->
                                    viewModel.startQuiz(catId, catName)
                                    currentScreen = "quiz"
                                },
                                onOpenLeaderboard = {
                                    currentScreen = "leaderboard"
                                },
                                onOpenProfile = {
                                    currentScreen = "profile"
                                },
                                onOpenAdmin = {
                                    currentScreen = "admin"
                                }
                            )
                        }
                        "quiz" -> {
                            QuizScreen(
                                viewModel = viewModel,
                                onQuizClosed = {
                                    currentScreen = "home"
                                },
                                onQuizCompleted = {
                                    currentScreen = "result"
                                }
                            )
                        }
                        "result" -> {
                            ResultScreen(
                                viewModel = viewModel,
                                onNavigateHome = {
                                    currentScreen = "home"
                                },
                                onReplayQuiz = {
                                    currentScreen = "quiz"
                                }
                            )
                        }
                        "leaderboard" -> {
                            LeaderboardScreen(
                                viewModel = viewModel,
                                onNavigateHome = {
                                    currentScreen = "home"
                                }
                            )
                        }
                        "profile" -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                onNavigateHome = {
                                    currentScreen = "home"
                                },
                                onLogoutCompleted = {
                                    currentScreen = "auth"
                                }
                            )
                        }
                        "admin" -> {
                            AdminScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentScreen = "home"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
