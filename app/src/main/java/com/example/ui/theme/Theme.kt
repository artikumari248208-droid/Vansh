package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonochromeColorScheme = darkColorScheme(
    primary = PureWhite,
    secondary = LightGray,
    tertiary = TextGray,
    background = PureBlack,
    surface = NearBlack,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onTertiary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    outline = BorderDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = PureWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonochromeColorScheme,
        typography = Typography,
        content = content
    )
}
