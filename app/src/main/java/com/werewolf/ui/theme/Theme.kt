package com.werewolf.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF1A1A2E)
val DarkSurface = Color(0xFF16213E)
val DarkCard = Color(0xFF0F3460)
val WerewolfRed = Color(0xFFE94560)
val VillageGreen = Color(0xFF4CAF50)
val SeerCyan = Color(0xFF00BCD4)
val DoctorBlue = Color(0xFF2196F3)
val HunterAmber = Color(0xFFFFC107)
val NightBlue = Color(0xFF3F51B5)
val DawnGold = Color(0xFFFFD54F)
val DeathGray = Color(0xFF757575)
val TextWhite = Color(0xFFEEEEEE)
val TextDim = Color(0xFF9E9E9E)

private val WerewolfColorScheme = darkColorScheme(
    primary = WerewolfRed,
    secondary = NightBlue,
    tertiary = SeerCyan,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextWhite,
    onSurface = TextWhite,
)

@Composable
fun WerewolfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WerewolfColorScheme,
        content = content
    )
}
