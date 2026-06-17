package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object DesignPresets {
    // Elegant theme definitions containing visual label, color scheme brushes, and UI token variables
    data class GradientTheme(
        val key: String,
        val label: String,
        val brush: Brush,
        val backgroundColor: Color,
        val cardColor: Color,
        val textColor: Color,
        val accentColor: Color,
        val previewBorderColor: Color
    )

    val themes = listOf(
        GradientTheme(
            key = "slate",
            label = "Sophisticated Dark",
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF09090B), Color(0xFF18181B))
            ),
            backgroundColor = Color(0xFF09090B),
            cardColor = Color(0xFF18181B).copy(alpha = 0.9f),
            textColor = Color(0xFFFAFAFA),
            accentColor = Color(0xFF6366F1), // Indigo 500
            previewBorderColor = Color(0xFF27272A) // Zinc 800
        ),
        GradientTheme(
            key = "sunset",
            label = "Sunset Flare",
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFF5F6D), Color(0xFFFFC371))
            ),
            backgroundColor = Color(0xFFFF5F6D),
            cardColor = Color.White.copy(alpha = 0.25f),
            textColor = Color.White,
            accentColor = Color(0xFFFFFbeb),
            previewBorderColor = Color.White.copy(alpha = 0.4f)
        ),
        GradientTheme(
            key = "light",
            label = "Clean White",
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFF4F4F5))
            ),
            backgroundColor = Color(0xFFFFFFFF),
            cardColor = Color(0xFFF4F4F5),
            textColor = Color(0xFF09090B),
            accentColor = Color(0xFF6366F1), // Indigo 500
            previewBorderColor = Color(0xFFE4E4E7) // Zinc 200
        ),
        GradientTheme(
            key = "mint",
            label = "Emerald Moss",
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF064E3B), Color(0xFF047857))
            ),
            backgroundColor = Color(0xFF064E3B),
            cardColor = Color(0xFF065F46).copy(alpha = 0.85f),
            textColor = Color(0xFFECFDF5),
            accentColor = Color(0xFF34D399),
            previewBorderColor = Color(0xFF047857)
        ),
        GradientTheme(
            key = "ocean",
            label = "Sapphire Blue",
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0B192C), Color(0xFF1E3E62))
            ),
            backgroundColor = Color(0xFF0B192C),
            cardColor = Color(0xFF1E3E62).copy(alpha = 0.8f),
            textColor = Color(0xFFF1F5F9),
            accentColor = Color(0xFF60A5FA),
            previewBorderColor = Color(0xFF475569)
        ),
        GradientTheme(
            key = "cosmos",
            label = "Cosmic Nebula",
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1E1B4B), Color(0xFF311042))
            ),
            backgroundColor = Color(0xFF1E1B4B),
            cardColor = Color(0xFF311042).copy(alpha = 0.75f),
            textColor = Color(0xFFFAE8FF),
            accentColor = Color(0xFFE9D5FF),
            previewBorderColor = Color(0xFF581C87)
        )
    )

    fun getTheme(key: String): GradientTheme {
        return themes.find { it.key == key } ?: themes[0]
    }

    data class LayoutPreset(
        val key: String,
        val label: String,
        val description: String
    )

    val layouts = listOf(
        LayoutPreset(
            key = "minimalist",
            label = "Minimalist Card",
            description = "Clean flat cards with subtle transparency and centered details."
        ),
        LayoutPreset(
            key = "glassmorphic",
            label = "Glassmorphic Pro",
            description = "Sleek frosted glass with borders that stand out against the background."
        ),
        LayoutPreset(
            key = "brutalist",
            label = "Neo-Brutalist",
            description = "High-contrast layouts, thick black shadows, and bold borders."
        )
    )
}
