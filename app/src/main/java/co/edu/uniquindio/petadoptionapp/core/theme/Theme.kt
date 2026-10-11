package co.edu.uniquindio.petadoptionapp.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CafeLeche,
    secondary = NaranjaCalido,
    tertiary = CafeOscuro,

    background = Cream,
    surface = Cream,

    onPrimary = Blanco,
    onSecondary = CafeOscuro,
    onTertiary = Blanco,

    onBackground = CafeOscuro,
    onSurface = CafeOscuro
)

private val DarkColorScheme = darkColorScheme(
    primary = CafeLeche,
    secondary = NaranjaCalido,
    tertiary = Cream,

    background = CafeOscuro,
    surface = CafeOscuro,

    onPrimary = Blanco,
    onSecondary = CafeOscuro,
    onTertiary = CafeOscuro,

    onBackground = Cream,
    onSurface = Cream
)

@Composable
fun PetAdoptionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}