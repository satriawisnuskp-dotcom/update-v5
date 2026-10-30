package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object AppThemeState {
    var currentAccentColor: Color by mutableStateOf(Color(0xFF2F70F2))
    var currentThemeName: String by mutableStateOf("FinTrack Blue")
}

fun getThemeColorByName(name: String): Color {
    return when (name) {
        "Zamrud Emerald" -> IncomeGreen
        "Ungu Royal" -> Color(0xFF8B5CF6)
        "Amber Sunset" -> Color(0xFFF59E0B)
        "Koral Merah" -> ExpenseRed
        "Teal Bahari" -> Color(0xFF14B8A6)
        "Cyan Electric" -> Color(0xFF06B6D4)
        "Rose Gold" -> Color(0xFFEC4899)
        else -> Color(0xFF2F70F2) // FinTrack Blue
    }
}

// Background and Surface Colors (Modern Deep Slate / Obsidian Dark)
val DarkBackground = Color(0xFF0C0F15)
val DarkSurface = Color(0xFF131822)
val DarkSurfaceElevated = Color(0xFF1A212E)
val DarkSurfaceVariant = Color(0xFF222B3C)
val DarkCardBorder = Color(0xFF263246)
val DarkCardSubtle = Color(0xFF171E2B)

// Dynamic Accent Colors reflecting current selected theme
val AccentBlue: Color
    get() = AppThemeState.currentAccentColor

val AccentBlueBright: Color
    get() = AppThemeState.currentAccentColor

val AccentBlueDark: Color
    get() = AppThemeState.currentAccentColor.copy(alpha = 0.6f)

val AccentBlueContainer: Color
    get() = AppThemeState.currentAccentColor.copy(alpha = 0.2f)

// Financial Indicators
val IncomeGreen = Color(0xFF22C55E)
val IncomeGreenDark = Color(0xFF166534)
val IncomeGreenSoft = Color(0x2622C55E)

val ExpenseRed = Color(0xFFF43F5E)
val ExpenseRedDark = Color(0xFF9F1239)
val ExpenseRedSoft = Color(0x26F43F5E)
val ExpenseHeatTile = Color(0xFF2E1A22)
val ExpenseHeatTileActive = Color(0xFF451E28)

// Gamification / Warning Colors
val TrophyYellow = Color(0xFFF59E0B)
val TrophyYellowLight = Color(0xFFFBBF24)
val TrophyYellowSoft = Color(0x33F59E0B)

// Text Colors
val TextWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val TextSubtle = Color(0xFF64748B)

// Net Worth Segment Colors
val SegmentAccountBlue = Color(0xFF38BDF8)
val SegmentAssetGreen = Color(0xFF34D399)
val SegmentDebtRed = Color(0xFFF87171)
