package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Public" -> Icons.Default.Public
        "Science" -> Icons.Default.Science
        "Calculate" -> Icons.Default.Calculate
        "History" -> Icons.Default.History
        "Terrain" -> Icons.Default.Terrain
        "SportsSoccer" -> Icons.Default.SportsSoccer
        "Computer" -> Icons.Default.Computer
        "Movie" -> Icons.Default.Movie
        "Himalaya" -> Icons.Default.Landscape
        else -> Icons.Default.HelpCenter
    }
}

// Fixed set of avatars for user profile choice
val UserAvatars = listOf(
    "🏔️", // Nepal snow peak
    "🌸", // Rhododendron
    "🕉️", // Nepal temple
    "🎓", // Academic Guru
    "🦁", // Snow leopard / lion
    "🐅", // Bengal tiger
    "🚲", // Himalayan rider
    "🦅"  // Falcon
)
