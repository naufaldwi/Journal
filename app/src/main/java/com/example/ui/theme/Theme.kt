package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BotanicalSage,
    onPrimary = CrispIvory,
    primaryContainer = BotanicalSageLight,
    onPrimaryContainer = BotanicalSageDark,
    secondary = EucalyptusMuted,
    onSecondary = CrispIvory,
    secondaryContainer = EucalyptusWash,
    onSecondaryContainer = BotanicalSageDark,
    tertiary = DustyTerracotta,
    onTertiary = CrispIvory,
    tertiaryContainer = TerracottaWash,
    onTertiaryContainer = TerracottaDeep,
    background = OatPaper,
    onBackground = InkCharcoal,
    surface = CrispIvory,
    onSurface = InkCharcoal,
    surfaceVariant = SoftLinen,
    onSurfaceVariant = SlateSage,
    surfaceContainerLowest = CrispIvory,
    surfaceContainerLow = SoftLinen,
    surfaceContainer = WarmSand,
    outline = HairlineStone,
    outlineVariant = WarmSand,
    error = RoseClay,
    onError = CrispIvory,
    errorContainer = RoseClayWash,
    onErrorContainer = InkCharcoal
)

private val DarkColorScheme = darkColorScheme(
    primary = SageMist,
    onPrimary = ForestNight,
    primaryContainer = SageDeepContainer,
    onPrimaryContainer = ParchmentInk,
    secondary = EucalyptusMuted,
    onSecondary = ForestNight,
    secondaryContainer = MossElevated,
    onSecondaryContainer = ParchmentInk,
    tertiary = ClayWarm,
    onTertiary = ForestNight,
    tertiaryContainer = ClayDeepContainer,
    onTertiaryContainer = ParchmentInk,
    background = ForestNight,
    onBackground = ParchmentInk,
    surface = MossSurface,
    onSurface = ParchmentInk,
    surfaceVariant = MossElevated,
    onSurfaceVariant = MutedSageText,
    surfaceContainerLowest = ForestNight,
    surfaceContainerLow = MossSurface,
    surfaceContainer = MossElevated,
    outline = DarkHairline,
    outlineVariant = MossElevated,
    error = ClayWarm,
    onError = ForestNight,
    errorContainer = ClayDeepContainer,
    onErrorContainer = ParchmentInk
)

val JournalShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun JournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JournalTypography,
        shapes = JournalShapes,
        content = content
    )
}
