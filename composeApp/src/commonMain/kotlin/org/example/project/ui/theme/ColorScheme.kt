package org.example.project.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Greenish color palette
val GreenPrimary = Color(0xFF4CAF50)      // Material Green 500
val GreenOnPrimary = Color(0xFFFFFFFF)    // White
val GreenPrimaryContainer = Color(0xFFC8E6C9)  // Green 100
val GreenOnPrimaryContainer = Color(0xFF1B5E20) // Green 900

val GreenSecondary = Color(0xFF66BB6A)    // Green 400
val GreenOnSecondary = Color(0xFFFFFFFF)   // White
val GreenSecondaryContainer = Color(0xFFE8F5E9) // Green 50
val GreenOnSecondaryContainer = Color(0xFF2E7D32) // Green 800

val GreenTertiary = Color(0xFF81C784)     // Green 300
val GreenOnTertiary = Color(0xFF000000)   // Black
val GreenTertiaryContainer = Color(0xFFF1F8E9) // Light Green 50
val GreenOnTertiaryContainer = Color(0xFF33691E) // Light Green 900

val GreenError = Color(0xFFBA1A1A)        // Red for errors
val GreenOnError = Color(0xFFFFFFFF)      // White
val GreenErrorContainer = Color(0xFFFFDAD6) // Light red
val GreenOnErrorContainer = Color(0xFF410002) // Dark red

val GreenBackground = Color(0xFFF5FBF5)   // Very light green tint
val GreenOnBackground = Color(0xFF1A1C1A) // Dark gray-green

val GreenSurface = Color(0xFFF5FBF5)      // Very light green tint
val GreenOnSurface = Color(0xFF1A1C1A)    // Dark gray-green
val GreenSurfaceVariant = Color(0xFFE0E5E0) // Light gray-green
val GreenOnSurfaceVariant = Color(0xFF424942) // Medium gray-green

val GreenOutline = Color(0xFF72796F)      // Medium gray-green
val GreenOutlineVariant = Color(0xFFC1C9BE) // Light gray-green

val GreenInverseSurface = Color(0xFF2F312F) // Dark gray-green
val GreenInverseOnSurface = Color(0xFFF0F4F0) // Very light green
val GreenInversePrimary = Color(0xFF81C784) // Green 300

// Light greenish color scheme
val GreenishLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenOnSecondaryContainer,
    tertiary = GreenTertiary,
    onTertiary = GreenOnTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    onTertiaryContainer = GreenOnTertiaryContainer,
    error = GreenError,
    onError = GreenOnError,
    errorContainer = GreenErrorContainer,
    onErrorContainer = GreenOnErrorContainer,
    background = GreenBackground,
    onBackground = GreenOnBackground,
    surface = GreenSurface,
    onSurface = GreenOnSurface,
    surfaceVariant = GreenSurfaceVariant,
    onSurfaceVariant = GreenOnSurfaceVariant,
    outline = GreenOutline,
    outlineVariant = GreenOutlineVariant,
    inverseSurface = GreenInverseSurface,
    inverseOnSurface = GreenInverseOnSurface,
    inversePrimary = GreenInversePrimary
)

// Dark greenish color scheme
val GreenishDarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),          // Green 300 (lighter for dark theme)
    onPrimary = Color(0xFF1B5E20),        // Green 900
    primaryContainer = Color(0xFF2E7D32),  // Green 800
    onPrimaryContainer = Color(0xFFC8E6C9), // Green 100
    secondary = Color(0xFFA5D6A7),         // Green 200
    onSecondary = Color(0xFF1B5E20),       // Green 900
    secondaryContainer = Color(0xFF388E3C), // Green 700
    onSecondaryContainer = Color(0xFFE8F5E9), // Green 50
    tertiary = Color(0xFFB2DFDB),         // Teal 200
    onTertiary = Color(0xFF004D40),        // Teal 900
    tertiaryContainer = Color(0xFF00695C), // Teal 800
    onTertiaryContainer = Color(0xFFB2DFDB), // Teal 200
    error = Color(0xFFFFB4AB),             // Light red
    onError = Color(0xFF690005),           // Dark red
    errorContainer = Color(0xFF93000A),    // Darker red
    onErrorContainer = Color(0xFFFFDAD6),  // Light red
    background = Color(0xFF121412),       // Very dark green-gray
    onBackground = Color(0xFFE0E5E0),      // Light gray-green
    surface = Color(0xFF121412),          // Very dark green-gray
    onSurface = Color(0xFFE0E5E0),        // Light gray-green
    surfaceVariant = Color(0xFF424942),   // Medium gray-green
    onSurfaceVariant = Color(0xFFC1C9BE), // Light gray-green
    outline = Color(0xFF8B9389),          // Medium gray-green
    outlineVariant = Color(0xFF424942),   // Medium gray-green
    inverseSurface = Color(0xFFE0E5E0),   // Light gray-green
    inverseOnSurface = Color(0xFF2F312F), // Dark gray-green
    inversePrimary = Color(0xFF4CAF50)    // Green 500
)


