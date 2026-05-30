package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: QuizRepository) : ViewModel() {

    // --- Authentication ---
    val currentUser: StateFlow<User?> = repository.currentUser

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _forgotPasswordMessage = MutableStateFlow<String?>(null)
    val forgotPasswordMessage: StateFlow<String?> = _forgotPasswordMessage.asStateFlow()

    // --- DB Flow Listings ---
    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<Question>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsersOrdered: StateFlow<List<User>> = repository.getAllUsersOrderedByPoints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allResults: StateFlow<List<QuizResult>> = repository.allResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search & Filter Questions ---
    private val _selectedCategoryIdFilter = MutableStateFlow<String>("all")
    val selectedCategoryIdFilter: StateFlow<String> = _selectedCategoryIdFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredQuestions: StateFlow<List<Question>> = combine(
        allQuestions,
        _selectedCategoryIdFilter,
        _searchQuery
    ) { questions, catFilter, query ->
        questions.filter { q ->
            val matchCat = catFilter == "all" || q.categoryId == catFilter
            val matchQuery = query.isEmpty() || q.questionText.contains(query, ignoreCase = true) ||
                    q.optionA.contains(query, ignoreCase = true) || q.optionB.contains(query, ignoreCase = true) ||
                    q.optionC.contains(query, ignoreCase = true) || q.optionD.contains(query, ignoreCase = true)
            matchCat && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategoryFilter(catId: String) {
        _selectedCategoryIdFilter.value = catId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Achievements ---
    val currentAchievements: StateFlow<List<UserAchievement>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getAchievementsByUserFlow(user.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Quiz playing State ---
    private val _activeQuizQuestions = MutableStateFlow<List<Question>>(emptyList())
    val activeQuizQuestions = _activeQuizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswerOption = MutableStateFlow<String>("") // "A", "B", "C", "D"
    val selectedAnswerOption = _selectedAnswerOption.asStateFlow()

    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked = _isAnswerChecked.asStateFlow()

    private val _quizSecondsLeft = MutableStateFlow(20)
    val quizSecondsLeft = _quizSecondsLeft.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    private val _correctAnswersCount = MutableStateFlow(0)
    val correctAnswersCount = _correctAnswersCount.asStateFlow()

    private val _pointsEarned = MutableStateFlow(0)
    val pointsEarned = _pointsEarned.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished = _isQuizFinished.asStateFlow()

    private val _activeQuizCategoryName = MutableStateFlow("General Quiz")
    val activeQuizCategoryName = _activeQuizCategoryName.asStateFlow()

    private val _activeQuizCategoryId = MutableStateFlow("gk")
    val activeQuizCategoryId = _activeQuizCategoryId.asStateFlow()

    private var timerJob: Job? = null

    // --- Sound & Feedback Simulation ---
    private val _soundCue = MutableSharedFlow<String>(replay = 0) // "correct", "incorrect", "completed", "badge"
    val soundCue = _soundCue.asSharedFlow()

    // --- User Actions ---
    fun register(email: String, pwhash: String, nickname: String, onCompleted: (Boolean) -> Unit) {
        _authError.value = null
        if (email.isBlank() || pwhash.isBlank() || nickname.isBlank()) {
            _authError.value = "All fields are required."
            onCompleted(false)
            return
        }
        viewModelScope.launch {
            val success = repository.registerUser(email, pwhash, nickname)
            if (success) {
                onCompleted(true)
            } else {
                _authError.value = "Email address already registered."
                onCompleted(false)
            }
        }
    }

    fun login(email: String, pwhash: String, onCompleted: (Boolean) -> Unit) {
        _authError.value = null
        if (email.isBlank() || pwhash.isBlank()) {
            _authError.value = "Email and password are required."
            onCompleted(false)
            return
        }
        viewModelScope.launch {
            val success = repository.loginUser(email, pwhash)
            if (success) {
                onCompleted(true)
            } else {
                _authError.value = "Invalid email or matching password."
                onCompleted(false)
            }
        }
    }

    fun recoverPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordMessage.value = "Please input your email address first."
            return
        }
        viewModelScope.launch {
            // Simulated retrieval
            _forgotPasswordMessage.value = "Reset link dispatched! For locally seeded accounts, passwords are: 'nepal123' for Smart player and 'admin123' for Administrators."
        }
    }

    fun clearAuthMessages() {
        _authError.value = null
        _forgotPasswordMessage.value = null
    }

    fun logout() {
        timerJob?.cancel()
        _isQuizFinished.value = false
        _activeQuizQuestions.value = emptyList()
        repository.logout()
    }

    fun changeProfileAvatar(avatarIdx: Int) {
        val u = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateProfile(u.copy(profilePicIdx = avatarIdx))
        }
    }

    fun updateProfileNickname(newNick: String) {
        val u = currentUser.value ?: return
        if (newNick.isNotBlank()) {
            viewModelScope.launch {
                repository.updateProfile(u.copy(nickname = newNick))
            }
        }
    }

    // --- Quiz engine ---
    fun startQuiz(categoryId: String, categoryName: String) {
        viewModelScope.launch {
            _activeQuizCategoryId.value = categoryId
            _activeQuizCategoryName.value = categoryName

            // Normal lists of questions grouped by category
            var activeSet = repository.getQuestionsByCategory(categoryId)
            if (activeSet.isEmpty()) {
                // Return fallback general questions
                activeSet = repository.getQuestionsByCategory("gk")
            }

            // Shuffle the set to satisfy "Randomized questions"
            val randomizedList = activeSet.shuffled().take(10) // Limit to 10 questions per quiz
            _activeQuizQuestions.value = randomizedList
            _currentQuestionIndex.value = 0
            _selectedAnswerOption.value = ""
            _isAnswerChecked.value = false
            _quizScore.value = 0
            _correctAnswersCount.value = 0
            _pointsEarned.value = 0
            _isQuizFinished.value = false
            _quizSecondsLeft.value = 20

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _quizSecondsLeft.value = 20
        timerJob = viewModelScope.launch {
            while (_quizSecondsLeft.value > 0) {
                delay(1000)
                _quizSecondsLeft.value -= 1
            }
            // If timer runs out of time
            if (!_isAnswerChecked.value) {
                checkAnswer("") // Auto incorrect on timeout
            }
        }
    }

    fun selectAnswer(option: String) {
        if (_isAnswerChecked.value) return // Block change after verification
        _selectedAnswerOption.value = option
    }

    fun checkAnswer(option: String) {
        if (_isAnswerChecked.value) return
        timerJob?.cancel()
        _isAnswerChecked.value = true
        _selectedAnswerOption.value = option

        val currentQ = _activeQuizQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val isCorrect = currentQ.correctAnswer.equals(option, ignoreCase = true)

        viewModelScope.launch {
            if (isCorrect) {
                _soundCue.emit("correct")
                _quizScore.value += 10
                _correctAnswersCount.value += 1
                _pointsEarned.value += 15
            } else {
                _soundCue.emit("incorrect")
            }
        }
    }

    fun moveToNextQuestion() {
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _activeQuizQuestions.value.size) {
            _currentQuestionIndex.value = nextIdx
            _selectedAnswerOption.value = ""
            _isAnswerChecked.value = false
            startTimer()
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        timerJob?.cancel()
        _isQuizFinished.value = true
        viewModelScope.launch {
            _soundCue.emit("completed")
        }

        val user = currentUser.value ?: return
        val score = _quizScore.value
        val correctCount = _correctAnswersCount.value
        val questionsCount = _activeQuizQuestions.value.size
        val points = _pointsEarned.value
        val accuracyPct = if (questionsCount > 0) (correctCount.toDouble() / questionsCount.toDouble()) * 100.0 else 0.0

        val result = QuizResult(
            userId = user.id,
            userNickname = user.nickname,
            categoryId = _activeQuizCategoryId.value,
            categoryName = _activeQuizCategoryName.value,
            score = score,
            totalQuestions = questionsCount,
            correctAnswers = correctCount,
            pointsEarned = points,
            accuracy = accuracyPct
        )

        viewModelScope.launch {
            // Save local game result
            repository.saveQuizResult(result)

            // Update user stats
            val updatedPoints = user.points + points
            val updatedTotalPlayed = user.totalPlayed + 1
            val updatedScore = user.totalScore + score
            val updatedCorrect = user.correctAnswersCount + correctCount
            val updatedTotalAns = user.totalQuestionsAnswered + questionsCount

            // Level progression formula: 1 + totalPoints / 500
            val updatedLevel = 1 + (updatedPoints / 500)

            val updatedUser = user.copy(
                points = updatedPoints,
                totalPlayed = updatedTotalPlayed,
                totalScore = updatedScore,
                correctAnswersCount = updatedCorrect,
                totalQuestionsAnswered = updatedTotalAns,
                level = updatedLevel
            )
            repository.updateProfile(updatedUser)

            // Check achievements
            checkAndAwardAchievements(updatedUser, result)
        }
    }

    private suspend fun checkAndAwardAchievements(user: User, lastResult: QuizResult) {
        val unlockedList = mutableListOf<UserAchievement>()

        // 1. "GK Beginner" / Played first quiz
        if (user.totalPlayed == 1) {
            unlockedList.add(
                UserAchievement(
                    userId = user.id,
                    achievementId = "first_quiz",
                    title = "Maiden Ascent",
                    description = "Unlocked by playing your very first quiz in Smart Quiz Nepal!"
                )
            )
        }

        // 2. Played 5 quizzes
        if (user.totalPlayed == 5) {
            unlockedList.add(
                UserAchievement(
                    userId = user.id,
                    achievementId = "five_quizzes",
                    title = "Trivia Regular",
                    description = "Unlocked by completing 5 customized quiz sessions."
                )
            )
        }

        // 3. Perfect Score in any quiz
        if (lastResult.correctAnswers == lastResult.totalQuestions && lastResult.totalQuestions >= 5) {
            unlockedList.add(
                UserAchievement(
                    userId = user.id,
                    achievementId = "perfect_score",
                    title = "Everest Cleared",
                    description = "Answered 100% of the questions correctly in an active quiz!"
                )
            )
        }

        // 4. Over 1000 points earned
        if (user.points >= 1500) {
            unlockedList.add(
                UserAchievement(
                    userId = user.id,
                    achievementId = "point_milestone",
                    title = "Golden Pagoda",
                    description = "Accumulated over 1500 loyalty points in the rewards system."
                )
            )
        }

        unlockedList.forEach { ach ->
            repository.saveAchievement(ach)
            _soundCue.emit("badge")
        }
    }

    // --- Admin Dashboard Panel Operations ---
    fun adminDeleteQuestion(question: Question) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }

    fun adminAddQuestion(
        categoryId: String,
        questionText: String,
        optA: String, optB: String, optC: String, optD: String,
        correct: String, explanation: String
    ) {
        viewModelScope.launch {
            val q = Question(
                categoryId = categoryId,
                questionText = questionText,
                optionA = optA,
                optionB = optB,
                optionC = optC,
                optionD = optD,
                correctAnswer = correct,
                explanation = explanation
            )
            repository.addQuestion(q)

            // Trigger admin developer achievement check
            val u = currentUser.value
            if (u != null) {
                repository.saveAchievement(
                    UserAchievement(
                        userId = u.id,
                        achievementId = "admin_creation",
                        title = "App Architect",
                        description = "Enriched the Nepal knowledge pool by inserting a custom question."
                    )
                )
            }
        }
    }

    fun adminUpdateQuestion(question: Question) {
        viewModelScope.launch {
            repository.updateQuestion(question)
        }
    }

    fun adminDeleteUser(userId: Int) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }
}

// Factory companion
class QuizViewModelFactory(private val repository: QuizRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class modelClass")
    }
}
