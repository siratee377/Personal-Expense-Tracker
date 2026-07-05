package com.example.personal_expense_tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF93D7B2),
    onPrimary = Color(0xFF082F21),
    secondary = Color(0xFFFFB49E),
    tertiary = Color(0xFFFFB49E),
    background = Color(0xFF101814),
    surface = Color(0xFF19231E),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF174D3A),
    onPrimary = Color.White,
    secondary = Color(0xFFEF765A),
    tertiary = Color(0xFFB04C36),
    background = Color(0xFFF3F0E8),
    surface = Color(0xFFFFFCF5),
    surfaceVariant = Color(0xFFE4EBE3),
    onBackground = Color(0xFF18201C),
    onSurface = Color(0xFF18201C),
)

@Composable
fun PersonalExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
