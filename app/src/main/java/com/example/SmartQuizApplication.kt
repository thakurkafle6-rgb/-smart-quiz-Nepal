package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartQuizApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { QuizRepository(database.quizDao()) }

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        
        // Seed initial Nepal-centric quiz content and default/admin accounts
        applicationScope.launch {
            repository.seedInitialDataIfNecessary()
        }
    }
}
