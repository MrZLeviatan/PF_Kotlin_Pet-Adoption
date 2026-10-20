package co.edu.uniquindio.petadoptionapp.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable


/**
Capa 3: punto de entrada del sistema de diseño.

Es el único lugar que conoce a la vez los tokens de la app y Material 3.
Selecciona la paleta según el tema, la publica en el árbol de composición y
deriva el `ColorScheme`, de modo que los componentes de Material heredan la marca
sin recibir colores manualmente.
*/
private fun AppColors.toMaterialColorScheme() =
    // Si la paleta actual es oscura usa darkColorScheme(), de lo contrario usa lightColorScheme(), y luego crea una copia
    (if (isDark) darkColorScheme() else lightColorScheme()).copy(
        primary = actionPrimary,
        onPrimary = onActionPrimary,
        secondary = actionSecondary,
        onSecondary = onActionSecondary,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceNotice,
        outline = border,
        error = stateError,
        onError = onStateError,
    )


/**
Puente hacia Material 3. Solo se mapean los roles que la app define; el resto
conserva los valores por defecto del esquema base, que siguen siendo válidos.
 */
@Composable
fun PetAdoptionAppTheme(
    // Detecta por defecto si el móvil está en modo oscuro.
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Pasa las pantallas o vistas hijas que irán dentro del tema
    content: @Composable () -> Unit,
) {
    // Selecciona la paleta de colores personalizada de la app según si está activo el modo oscuro o claro
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    // Provee 'appColors' a través de LocalAppColors para que cualquier vista hija pueda leer estos colores de forma implícita
    CompositionLocalProvider(LocalAppColors provides appColors) {
        // Aplica el tema de Material 3 a toda la jerarquía de vistas
        MaterialTheme(
            // Mapea y aplica los colores a Material 3
            colorScheme = appColors.toMaterialColorScheme(),
            // Mapea y aplica la tipografía establecida y combinada con Material 3
            typography = PetTypography,
            // Renderiza los componentes de la interfaz pasados como parámetro
            content = content,
        )
    }
}


/**
 Acceso único a los tokens: `AppTheme.colors.actionPrimary`. Centralizarlo permite
 que el autocompletado muestre solo roles semánticos y que sea posible cambiar
 la implementación interna sin tocar las pantallas.
 */
object AppTheme {
    // Variable de lectura que devuelve los colores activos de la app
    val colors: AppColors
        // Permite usar la propiedad dentro de funciones Composables
        @Composable
        // Indica que no genera estados ni vuelve a dibujar componentes por sí misma
        @ReadOnlyComposable
        // Obtiene la paleta de colores activa proporcionada por CompositionLocalProvider
        get() = LocalAppColors.current
}