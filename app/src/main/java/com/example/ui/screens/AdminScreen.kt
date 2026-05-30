package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Question
import com.example.data.model.User
import com.example.ui.theme.NepalBlue
import com.example.ui.theme.NepalCrimson
import com.example.ui.theme.NepalGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit
) {
    val usersList by viewModel.allUsersOrdered.collectAsState()
    val questionsList by viewModel.filteredQuestions.collectAsState()
    val resultsList by viewModel.allResults.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Questions, 1: Users, 2: Statistics

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedQuestionForEdit by remember { mutableStateOf<Question?>(null) }

    // Search query binds directly to ViewModel state
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatIdFilter by viewModel.selectedCategoryIdFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Admin Dashboard", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("admin_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = NepalBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_question_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Question")
                }
            }
        }
    ) { padValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padValues)
        ) {
            // Admin Sub navigation row
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Questions") },
                    modifier = Modifier.testTag("admin_tab_questions")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Scholars") },
                    modifier = Modifier.testTag("admin_tab_users")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Live Stats") },
                    modifier = Modifier.testTag("admin_tab_stats")
                )
            }

            when (activeTab) {
                0 -> {
                    // QUESTION MANAGER TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Filters row
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search question text...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .testTag("admin_question_search"),
                            singleLine = true
                        )

                        // Categories drop list filters
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            FilterChip(
                                selected = selectedCatIdFilter == "all",
                                onClick = { viewModel.setCategoryFilter("all") },
                                label = { Text("All") }
                            )

                            // Show categories
                            categories.take(4).forEach { cat ->
                                FilterChip(
                                    selected = selectedCatIdFilter == cat.id,
                                    onClick = { viewModel.setCategoryFilter(cat.id) },
                                    label = { Text(cat.name) }
                                )
                            }
                        }

                        if (questionsList.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No questions match query.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 70.dp)
                            ) {
                                items(questionsList) { q ->
                                    QuestionAdminRow(
                                        question = q,
                                        onEdit = {
                                            selectedQuestionForEdit = q
                                            showEditDialog = true
                                        },
                                        onDelete = { viewModel.adminDeleteQuestion(q) }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // USER MANAGER TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Registered Scholars (${usersList.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(usersList) { user ->
                                UserAdminRow(
                                    user = user,
                                    onDeleteUser = { viewModel.adminDeleteUser(user.id) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // APP LIVE STATISTICS TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Application Metadata Statistics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = NepalBlue
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                StatValueRow(label = "Total Questions Seseeded", valString = questionsList.size.toString())
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                StatValueRow(label = "Total Scholars Enrolled", valString = usersList.size.toString())
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                StatValueRow(label = "Total Quizzes Logged", valString = resultsList.size.toString())
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                StatValueRow(label = "Admin accounts active", valString = usersList.count { it.isAdmin }.toString())
                            }
                        }

                        // Recent Logged Games List
                        Text(
                            "Recent Activity Logs",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (resultsList.isEmpty()) {
                            Text("No quiz logs generated yet. Play some matches first!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        } else {
                            resultsList.take(6).forEach { res ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${res.userNickname} played ${res.categoryName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                "Answers: ${res.correctAnswers}/${res.totalQuestions} [${res.accuracy.toInt()}% accuracy]",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            "+${res.pointsEarned} pts",
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Question Add Dialogue Creator
    if (showAddDialog) {
        QuestionFormDialog(
            categories = categories,
            title = "Add Question",
            onDismiss = { showAddDialog = false },
            onSave = { catId, text, a, b, c, d, correct, expl ->
                viewModel.adminAddQuestion(catId, text, a, b, c, d, correct, expl)
                showAddDialog = false
            }
        )
    }

    // Question Edit Dialogue Creator
    if (showEditDialog && selectedQuestionForEdit != null) {
        val q = selectedQuestionForEdit!!
        QuestionFormDialog(
            categories = categories,
            title = "Edit Question",
            initialQuestion = q,
            onDismiss = {
                showEditDialog = false
                selectedQuestionForEdit = null
            },
            onSave = { catId, text, a, b, c, d, correct, expl ->
                val updatedQ = q.copy(
                    categoryId = catId,
                    questionText = text,
                    optionA = a,
                    optionB = b,
                    optionC = c,
                    optionD = d,
                    correctAnswer = correct,
                    explanation = expl
                )
                viewModel.adminUpdateQuestion(updatedQ)
                showEditDialog = false
                selectedQuestionForEdit = null
            }
        )
    }
}

@Composable
fun QuestionAdminRow(
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("q_row_${question.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NepalCrimson.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        question.categoryId.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NepalCrimson
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.questionText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "A: ${question.optionA} | B: ${question.optionB} | Correct: ${question.correctAnswer}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_q_${question.id}")) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Question", tint = NepalBlue)
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_q_${question.id}")) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = NepalCrimson)
            }
        }
    }
}

@Composable
fun UserAdminRow(
    user: User,
    onDeleteUser: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("user_row_${user.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NepalBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(UserAvatars.getOrNull(user.profilePicIdx) ?: "🏔️")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.nickname, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${user.email} | Level ${user.level} | ${user.points} pts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (user.isAdmin) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NepalGold.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ADMIN", fontWeight = FontWeight.Black, fontSize = 10.sp, color = NepalGold)
                }
            } else {
                IconButton(onClick = onDeleteUser, modifier = Modifier.testTag("delete_user_${user.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Scholar", tint = NepalCrimson)
                }
            }
        }
    }
}

@Composable
fun StatValueRow(label: String, valString: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = valString, fontSize = 16.sp, fontWeight = FontWeight.Black, color = NepalBlue)
    }
}

@Composable
fun QuestionFormDialog(
    categories: List<Category>,
    title: String,
    initialQuestion: Question? = null,
    onDismiss: () -> Unit,
    onSave: (catId: String, text: String, a: String, b: String, c: String, d: String, correct: String, expl: String) -> Unit
) {
    var categoryId by remember { mutableStateOf(initialQuestion?.categoryId ?: categories.firstOrNull()?.id ?: "gk") }
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var optionA by remember { mutableStateOf(initialQuestion?.optionA ?: "") }
    var optionB by remember { mutableStateOf(initialQuestion?.optionB ?: "") }
    var optionC by remember { mutableStateOf(initialQuestion?.optionC ?: "") }
    var optionD by remember { mutableStateOf(initialQuestion?.optionD ?: "") }
    var correctAnswer by remember { mutableStateOf(initialQuestion?.correctAnswer ?: "A") }
    var explanation by remember { mutableStateOf(initialQuestion?.explanation ?: "") }

    var expandedSelect by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category click selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedSelect = true },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_cat_picker")
                    ) {
                        Text("Category: ${categoryId.uppercase()}")
                    }

                    DropdownMenu(
                        expanded = expandedSelect,
                        onDismissRequest = { expandedSelect = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    categoryId = cat.id
                                    expandedSelect = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_q_text")
                )

                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("Option A") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_opt_a")
                )

                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("Option B") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_opt_b")
                )

                OutlinedTextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("Option C") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_opt_c")
                )

                OutlinedTextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("Option D") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_opt_d")
                )

                // Correct Answer selector A, B, C, D
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Correct choice:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    listOf("A", "B", "C", "D").forEach { choice ->
                        val isSel = correctAnswer == choice
                        ElevatedFilterChip(
                            selected = isSel,
                            onClick = { correctAnswer = choice },
                            label = { Text(choice) },
                            modifier = Modifier.testTag("dialog_correct_option_$choice")
                        )
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Fact / Explanation (Booster)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_explanation")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()) {
                        onSave(categoryId, questionText, optionA, optionB, optionC, optionD, correctAnswer, explanation)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                modifier = Modifier.testTag("dialog_save_q_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
