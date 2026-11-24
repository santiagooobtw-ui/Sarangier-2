package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BotanicalLightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerSoft,
    onPrimaryContainer = EmeraldDark,
    secondary = SecondaryEmerald,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerMint,
    onSecondaryContainer = OnSecondaryContainerMint,
    tertiary = AmberBadgeText,
    onTertiary = Color.White,
    tertiaryContainer = AmberBadgeBg,
    onTertiaryContainer = Color(0xFF653E00),
    error = ErrorDeep,
    onError = Color.White,
    errorContainer = CoralBadgeBg,
    onErrorContainer = CoralBadgeText,
    background = CanvasSandLinen,
    onBackground = CharcoalText,
    surface = CardWhite,
    onSurface = CharcoalText,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = SlateTechnical,
    surfaceContainerLowest = CardWhite,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = OutlineMuted,
    outlineVariant = OutlineSubtle
)

val SarangierShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun SarangierTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BotanicalLightColorScheme,
        typography = SarangierTypography,
        shapes = SarangierShapes,
        content = content
    )
}
