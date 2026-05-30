package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String, // Stored securely
    val nickname: String,
    val level: Int = 1,
    val points: Int = 100, // Starts with some initial points
    val totalPlayed: Int = 0,
    val totalScore: Int = 0,
    val correctAnswersCount: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val isAdmin: Boolean = false,
    val profilePicIdx: Int = 0 // Avatar index
) : Serializable

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val description: String
) : Serializable

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String = ""
) : Serializable

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userNickname: String,
    val categoryId: String,
    val categoryName: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val pointsEarned: Int,
    val accuracy: Double,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "user_achievements")
data class UserAchievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val achievementId: String, // e.g. "gk_master", "perfect_score"
    val title: String,
    val description: String,
    val unlockedAt: Long = System.currentTimeMillis()
) : Serializable
