package com.santhomach.commercialledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green800,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Teal700,
    onSecondary = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal900,
    tertiary = Amber700,
    onTertiary = Color.White,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Color(0xFF4A2800),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0),
    error = Red600,
    onError = Color.White,
    background = Color(0xFFF6FAF6),
    onBackground = Color(0xFF1C1B1F),
)

@Composable
fun CommercialLedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
