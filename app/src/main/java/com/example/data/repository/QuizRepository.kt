package com.example.data.repository

import com.example.data.local.QuizDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizRepository(private val quizDao: QuizDao) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    // --- User Ops ---
    suspend fun registerUser(email: String, password: String, nickname: String): Boolean {
        // Simple verification that user doesn't already exist
        val existing = quizDao.getUserByEmail(email)
        if (existing != null) {
            return false // Email already registered
        }
        val user = User(
            email = email,
            passwordHash = password, // Using plain comparison for simplicity
            nickname = nickname,
            isAdmin = email.equals("admin@smartquiz.com", ignoreCase = true)
        )
        val id = quizDao.insertUser(user)
        _currentUser.value = user.copy(id = id.toInt())
        return true
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        val user = quizDao.getUserByEmail(email) ?: return false
        if (user.passwordHash == password) {
            _currentUser.value = user
            return true
        }
        return false
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun updateProfile(user: User) {
        quizDao.updateUser(user)
        _currentUser.value = user
    }

    fun getUserByIdFlow(id: Int): Flow<User?> = quizDao.getUserByIdFlow(id)
    fun getAllUsersOrderedByPoints(): Flow<List<User>> = quizDao.getAllUsersOrderedByPoints()
    fun getAllUsersFlow(): Flow<List<User>> = quizDao.getAllUsersFlow()
    suspend fun deleteUser(userId: Int) = quizDao.deleteUserById(userId)

    // --- Categories Ops ---
    val allCategories: Flow<List<Category>> = quizDao.getAllCategoriesFlow()
    suspend fun addCategory(category: Category) = quizDao.insertCategory(category)
    suspend fun deleteCategory(id: String) = quizDao.deleteCategoryById(id)

    // --- Questions Ops ---
    val allQuestions: Flow<List<Question>> = quizDao.getAllQuestionsFlow()
    suspend fun getQuestionsByCategory(categoryId: String): List<Question> = quizDao.getQuestionsByCategory(categoryId)
    fun getQuestionsByCategoryFlow(categoryId: String): Flow<List<Question>> = quizDao.getQuestionsByCategoryFlow(categoryId)
    suspend fun addQuestion(question: Question): Long = quizDao.insertQuestion(question)
    suspend fun updateQuestion(question: Question) = quizDao.updateQuestion(question)
    suspend fun deleteQuestion(question: Question) = quizDao.deleteQuestion(question)

    // --- Results Ops ---
    val allResults: Flow<List<QuizResult>> = quizDao.getAllQuizResultsFlow()
    fun getResultsByUserFlow(userId: Int): Flow<List<QuizResult>> = quizDao.getQuizResultsByUserFlow(userId)
    suspend fun saveQuizResult(result: QuizResult): Long = quizDao.insertQuizResult(result)

    // --- Achievements Ops ---
    fun getAchievementsByUserFlow(userId: Int): Flow<List<UserAchievement>> = quizDao.getAchievementsByUserFlow(userId)
    suspend fun saveAchievement(achievement: UserAchievement) = quizDao.insertAchievement(achievement)


    // --- Seeding Data ---
    suspend fun seedInitialDataIfNecessary() {
        val userCount = quizDao.getUserCount()
        if (userCount == 0) {
            // Seed a Standard Player User
            quizDao.insertUser(
                User(
                    email = "nepal@gmail.com",
                    passwordHash = "nepal123",
                    nickname = "Gorkhali_Hero",
                    level = 3,
                    points = 2450,
                    totalPlayed = 12,
                    totalScore = 480,
                    correctAnswersCount = 48,
                    totalQuestionsAnswered = 60,
                    isAdmin = false,
                    profilePicIdx = 1
                )
            )

            // Seed an Admin User
            quizDao.insertUser(
                User(
                    email = "admin@smartquiz.com",
                    passwordHash = "admin123",
                    nickname = "Quiz_Administrator",
                    level = 10,
                    points = 9999,
                    totalPlayed = 42,
                    totalScore = 1200,
                    correctAnswersCount = 180,
                    totalQuestionsAnswered = 210,
                    isAdmin = true,
                    profilePicIdx = 4
                )
            )

            // Mock other users for active leaderboard
            val dummyNicknames = listOf("Himalayan_Rider", "Annapurna_Trekker", "Kathmandu_Kid", "Lumbini_Peace", "Sherpa_Climber")
            val dummyEmails = listOf("rider@gmail.com", "trekker@gmail.com", "kid@gmail.com", "peace@gmail.com", "sherpa@gmail.com")
            dummyNicknames.forEachIndexed { idx, nick ->
                quizDao.insertUser(
                    User(
                        email = dummyEmails[idx],
                        passwordHash = "pass123",
                        nickname = nick,
                        level = (1..5).random(),
                        points = (500..2000).random(),
                        totalPlayed = (3..8).random(),
                        totalScore = (100..400).random(),
                        correctAnswersCount = (20..50).random(),
                        totalQuestionsAnswered = 60,
                        isAdmin = false,
                        profilePicIdx = (2..5).random()
                    )
                )
            }
        }

        val categoriesCount = quizDao.getAllCategories().size
        if (categoriesCount == 0) {
            val defaultCategories = listOf(
                Category("gk", "General Knowledge", "Public", "General knowledge about Nepal and the wider world."),
                Category("sci", "Science", "Science", "Physics, Chemistry, Biology, and Space exploration."),
                Category("mat", "Mathematics", "Calculate", "Puzzles, arithmetic queries, and basic math."),
                Category("his", "History", "History", "Historical kingdoms, world wars, and integration."),
                Category("geo", "Geography", "Terrain", "Peaks, rivers, lakes, and countries."),
                Category("spo", "Sports", "SportsSoccer", "Football, cricket, traditional games, and Olympics."),
                Category("tec", "Technology", "Computer", "Computers, software, gadgets, and the internet."),
                Category("ent", "Entertainment", "Movie", "Music, movies, theater, and Nepali pop culture."),
                Category("him", "Himalayan Pride", "Himalaya", "Glorious peaks, sherpa heritage, conservation, and mountain wonders.")
            )
            quizDao.insertCategories(defaultCategories)
        }

        // Robust migration: Ensure "Himalayan Pride" category is added if not exists
        if (quizDao.getAllCategories().none { it.id == "him" }) {
            quizDao.insertCategory(
                Category("him", "Himalayan Pride", "Himalaya", "Glorious peaks, sherpa heritage, conservation, and mountain wonders.")
            )
            val extraQuestions = listOf(
                Question(
                    categoryId = "him",
                    questionText = "Who was the first Nepalese climber to scale all 14 peaks above 8,000 meters?",
                    optionA = "Nirmal (Nims) Purja", optionB = "Kami Rita Sherpa", optionC = "Ang Rita Sherpa", optionD = "Babu Chiri Sherpa",
                    correctAnswer = "A", explanation = "Nirmal 'Nims' Purja MBE scaled all 14 of the world's 8,000-meter peaks in a record-breaking 6 months and 6 days under 'Project Possible'."
                ),
                Question(
                    categoryId = "him",
                    questionText = "What legendary creature is deeply rooted in Himalayan folklore as a mystery dweller of the snow peaks?",
                    optionA = "Griffin", optionB = "Yeti (Abominable Snowman)", optionC = "Pegasus", optionD = "Chupacabra",
                    correctAnswer = "B", explanation = "The Yeti, or Abominable Snowman, is a mythical ape-like creature said to inhabit the high-altitude Himalayan region of Nepal and Tibet."
                ),
                Question(
                    categoryId = "him",
                    questionText = "Kami Rita Sherpa famously holds the legendary world record for Everest climbs. How many successful ascents did he reach in May 2024?",
                    optionA = "15 times", optionB = "20 times", optionC = "30 times", optionD = "10 times",
                    correctAnswer = "C", explanation = "Kami Rita Sherpa scaled Mount Everest for a record-shattering 30th time on May 22, 2024, continuing his legacy as a global mountain icon."
                ),
                Question(
                    categoryId = "him",
                    questionText = "Which endangered, majestic big cat acts as the ultimate guardian and symbol of the high-altitude Himalayan ecosystem?",
                    optionA = "Bengal Tiger", optionB = "Snow Leopard", optionC = "Clouded Leopard", optionD = "Asiatic Lion",
                    correctAnswer = "B", explanation = "The Snow Leopard (Uncia uncia) is the crown jewel of alpine ecosystems, roaming high altitudes from 3,000 to 5,000 meters."
                ),
                Question(
                    categoryId = "him",
                    questionText = "In which year did Sir Edmund Hillary and Tenzing Norgay Sherpa accomplish the historic first-ever successful ascent of Mount Everest?",
                    optionA = "1950", optionB = "1953", optionC = "1963", optionD = "1948",
                    correctAnswer = "B", explanation = "On May 29, 1953, Sir Edmund Hillary of New Zealand and Tenzing Norgay Sherpa of Nepal became the first people to stand on top of Mount Everest."
                )
            )
            extraQuestions.forEach { quizDao.insertQuestion(it) }
        }

        val questionCount = quizDao.getQuestionCount()
        if (questionCount == 0) {
            val preseededQuestions = listOf(
                // ---- GK ----
                Question(
                    categoryId = "gk",
                    questionText = "Which city is known as the 'City of Temples' in Nepal?",
                    optionA = "Pokhara", optionB = "Kathmandu", optionC = "Lalitpur", optionD = "Bhaktapur",
                    correctAnswer = "B", explanation = "Kathmandu has a vast density of historic temples such as Pashupatinath, Boudhanath, and Swayambhunath."
                ),
                Question(
                    categoryId = "gk",
                    questionText = "What is the national flower of Nepal?",
                    optionA = "Marigold", optionB = "Jasmine", optionC = "Rhododendron (Lali Gurans)", optionD = "Lotus",
                    correctAnswer = "C", explanation = "Rhododendron Arboreum, locally known as Lali Gurans, is Nepal's beautiful red national flower."
                ),
                Question(
                    categoryId = "gk",
                    questionText = "What is the national animal of Nepal?",
                    optionA = "Cow", optionB = "Tiger", optionC = "One-horned Rhino", optionD = "Snow Leopard",
                    correctAnswer = "A", explanation = "The cow is revered and designated as the official national animal of Nepal."
                ),

                // ---- Science ----
                Question(
                    categoryId = "sci",
                    questionText = "Which chemical compound is represented by the formula H2O?",
                    optionA = "Hydrogen Peroxide", optionB = "Heavy Water", optionC = "Water", optionD = "Hydrochloric Acid",
                    correctAnswer = "C", explanation = "H2O signifies two hydrogen atoms bound covalently to a single oxygen atom (Water)."
                ),
                Question(
                    categoryId = "sci",
                    questionText = "Which planet in our solar system is nicknamed the 'Red Planet'?",
                    optionA = "Venus", optionB = "Mars", optionC = "Jupiter", optionD = "Saturn",
                    correctAnswer = "B", explanation = "Mars appears reddish because of active iron oxide (rust) covering its surface."
                ),
                Question(
                    categoryId = "sci",
                    questionText = "What is the study of plant life called?",
                    optionA = "Zoology", optionB = "Microbiology", optionC = "Botany", optionD = "Geology",
                    correctAnswer = "C", explanation = "Botany is the biological science studying vegetable and plant life forms."
                ),

                // ---- Math ----
                Question(
                    categoryId = "mat",
                    questionText = "What is the approximate value of Pi (π) to two decimal places?",
                    optionA = "3.12", optionB = "3.16", optionC = "3.14", optionD = "3.08",
                    correctAnswer = "C", explanation = "Pi is approximately 22/7, which resolves to 3.14 in decimal notation."
                ),
                Question(
                    categoryId = "mat",
                    questionText = "What is the sum of internal angles inside any triangle?",
                    optionA = "90 degrees", optionB = "180 degrees", optionC = "360 degrees", optionD = "270 degrees",
                    correctAnswer = "B", explanation = "In Euclidean geometry, the internal angles of any triangle always sum exactly to 180 degrees."
                ),

                // ---- History ----
                Question(
                    categoryId = "his",
                    questionText = "Who is revered as the 'Light of Asia'?",
                    optionA = "Gautam Buddha", optionB = "Prithvi Narayan Shah", optionC = "King Janak", optionD = "Araniko",
                    correctAnswer = "A", explanation = "Gautam Buddha founded Buddhism and was born in Lumbini, Nepal, earning the epithet 'Light of Asia'."
                ),
                Question(
                    categoryId = "his",
                    questionText = "Which king spearheaded the historic unification of modern Nepal?",
                    optionA = "Birendra Bir Bikram Shah", optionB = "Mahendra Bir Bikram Shah", optionC = "Prithvi Narayan Shah", optionD = "Tribhuvan Bir Bikram Shah",
                    correctAnswer = "C", explanation = "King Prithvi Narayan Shah of Gorkha initiated the military campaign to unify several small states into one nation in 1768 AD."
                ),

                // ---- Geography ----
                Question(
                    categoryId = "geo",
                    questionText = "What is the official height of Mount Everest as joint-announced by Nepal and China in 2020?",
                    optionA = "8848.00 meters", optionB = "8850.12 meters", optionC = "8848.86 meters", optionD = "8844.43 meters",
                    correctAnswer = "C", explanation = "A comprehensive scientific re-measurement in 2020 updated the height to exactly 8848.86 meters."
                ),
                Question(
                    categoryId = "geo",
                    questionText = "Which lake is situated at the highest altitude in Nepal?",
                    optionA = "Phewa Lake", optionB = "Tilicho Lake", optionC = "Rara Lake", optionD = "Begnas Lake",
                    correctAnswer = "B", explanation = "Tilicho Lake is famously recognized as one of the highest altitude freshwater lakes, located at 4,919 meters."
                ),

                // ---- Sports ----
                Question(
                    categoryId = "spo",
                    questionText = "What is the declared national sport of Nepal?",
                    optionA = "Cricket", optionB = "Football", optionC = "Volleyball", optionD = "Kabaddi",
                    correctAnswer = "C", explanation = "Volleyball was officially declared the national sport of Nepal on May 23, 2017."
                ),
                Question(
                    categoryId = "spo",
                    questionText = "Which nation won the inaugural FIFA Men's Football World Cup in 1930?",
                    optionA = "Argentina", optionB = "Uruguay", optionC = "Brazil", optionD = "Italy",
                    correctAnswer = "B", explanation = "Uruguay hosted and won the first-ever FIFA World Cup in 1930, defeating Argentina in the final."
                ),

                // ---- Technology ----
                Question(
                    categoryId = "tec",
                    questionText = "Who is the key operating system developer of the Android system?",
                    optionA = "Apple", optionB = "Microsoft", optionC = "Google", optionD = "Facebook",
                    correctAnswer = "C", explanation = "Google acquired Android in 2005 and leads the open-source software stack."
                ),
                Question(
                    categoryId = "tec",
                    questionText = "In computer terminology, what does CPU stand for?",
                    optionA = "Computer Processing Unit", optionB = "Central Processing Unit", optionC = "Central Polyphonic Utility", optionD = "Core Programming Unit",
                    correctAnswer = "B", explanation = "CPU stands for Central Processing Unit and executes instructions of computer programs."
                ),

                // ---- Entertainment ----
                Question(
                    categoryId = "ent",
                    questionText = "Who composed the music of Nepal's current national anthem 'Sayaun Thunga Phool Ka'?",
                    optionA = "Amber Gurung", optionB = "Pradeep Kumar Rai (Byakul Maila)", optionC = "Narayan Gopal", optionD = "Aruna Lama",
                    correctAnswer = "A", explanation = "The music was composed by veteran musician Amber Gurung. The lyrics were penned by Byakul Maila."
                ),
                Question(
                    categoryId = "ent",
                    questionText = "Who is globally known as 'Maha Jodi' in Nepali television comedy and cinematic heritage?",
                    optionA = "Deepak and Deepashree", optionB = "Madan Krishna Shrestha and Hari Bansha Acharya", optionC = "Sitaram and Kunjana", optionD = "Dhurmus and Suntali",
                    correctAnswer = "B", explanation = "Madan Krishna Shrestha and Hari Bansha Acharya are highly celebrated comedians/actors known together as Maha Jodi."
                )
            )
            quizDao.insertQuestions(preseededQuestions)
        }
    }
}
