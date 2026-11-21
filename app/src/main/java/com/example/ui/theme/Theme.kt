package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class HamshiraStatusColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val urgent: Color,
    val urgentContainer: Color
)

val LocalHamshiraStatusColors = staticCompositionLocalOf {
    HamshiraStatusColors(
        success = StatusSuccess,
        successContainer = StatusSuccessContainer,
        warning = StatusWarning,
        warningContainer = StatusWarningContainer,
        urgent = StatusUrgent,
        urgentContainer = StatusUrgentContainer
    )
}

private val LightColorScheme = lightColorScheme(
    primary = MedicalTealPrimary,
    onPrimary = Color.White,
    primaryContainer = MedicalMintContainer,
    onPrimaryContainer = OnMedicalMintContainer,
    secondary = CalmBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = CalmBlueContainer,
    onSecondaryContainer = OnCalmBlueContainer,
    tertiary = MedicalTealDark,
    onTertiary = Color.White,
    background = ClinicalBackground,
    onBackground = ClinicalTextPrimary,
    surface = ClinicalSurface,
    onSurface = ClinicalTextPrimary,
    surfaceVariant = ClinicalSurfaceVariant,
    onSurfaceVariant = ClinicalTextSecondary,
    surfaceContainerLowest = ClinicalSurface,
    surfaceContainerLow = ClinicalBackground,
    surfaceContainer = ClinicalSurfaceContainer,
    surfaceContainerHigh = ClinicalSurfaceVariant,
    outline = ClinicalOutline,
    outlineVariant = ClinicalOutlineVariant,
    error = StatusUrgent,
    onError = Color.White,
    errorContainer = StatusUrgentContainer,
    onErrorContainer = StatusUrgent
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTealPrimary,
    onPrimary = DarkOnTealPrimary,
    primaryContainer = DarkMintContainer,
    onPrimaryContainer = DarkOnMintContainer,
    secondary = DarkBlueSecondary,
    onSecondary = DarkOnBlueContainer,
    secondaryContainer = DarkBlueContainer,
    onSecondaryContainer = DarkOnBlueContainer,
    tertiary = DarkTealPrimary,
    onTertiary = DarkOnTealPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = DarkSurface,
    surfaceContainerLow = DarkBackground,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkStatusUrgent,
    onError = Color(0xFF450A0A),
    errorContainer = DarkStatusUrgentContainer,
    onErrorContainer = DarkStatusUrgent
)

val HamshiraShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HamshiraAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val statusColors = if (darkTheme) {
        HamshiraStatusColors(
            success = DarkStatusSuccess,
            successContainer = DarkStatusSuccessContainer,
            warning = DarkStatusWarning,
            warningContainer = DarkStatusWarningContainer,
            urgent = DarkStatusUrgent,
            urgentContainer = DarkStatusUrgentContainer
        )
    } else {
        HamshiraStatusColors(
            success = StatusSuccess,
            successContainer = StatusSuccessContainer,
            warning = StatusWarning,
            warningContainer = StatusWarningContainer,
            urgent = StatusUrgent,
            urgentContainer = StatusUrgentContainer
        )
    }

    CompositionLocalProvider(LocalHamshiraStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = HamshiraShapes,
            content = content
        )
    }
}
