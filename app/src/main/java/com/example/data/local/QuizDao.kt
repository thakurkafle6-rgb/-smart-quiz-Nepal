package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    // --- USER QUERIES ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: Int): Flow<User?>

    @Query("SELECT * FROM users ORDER BY points DESC")
    fun getAllUsersOrderedByPoints(): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)


    // --- CATEGORY QUERIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("SELECT * FROM categories")
    fun getAllCategoriesFlow(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)


    // --- QUESTION QUERIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestionsFlow(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE categoryId = :categoryId")
    suspend fun getQuestionsByCategory(categoryId: String): List<Question>

    @Query("SELECT * FROM questions WHERE categoryId = :categoryId")
    fun getQuestionsByCategoryFlow(categoryId: String): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int


    // --- QUIZ RESULTS QUERIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResult): Long

    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResultsFlow(): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getQuizResultsByUserFlow(userId: Int): Flow<List<QuizResult>>


    // --- ACHIEVEMENTS QUERIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: UserAchievement)

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    fun getAchievementsByUserFlow(userId: Int): Flow<List<UserAchievement>>
}
