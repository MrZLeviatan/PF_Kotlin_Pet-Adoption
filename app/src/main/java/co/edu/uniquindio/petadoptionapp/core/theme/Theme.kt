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
sin recibir colores manualmente. Envuelve toda la UI desde MainActivity y desde
los previews, y expone `AppTheme.colors` como acceso único a los tokens.
**/

/**
Puente hacia Material 3. Solo se mapean los roles que la app define; el resto
conserva los valores por defecto del esquema base, que siguen siendo válidos.
**/
private fun AppColors.toMaterialColorScheme() =
    // Si la paleta actual es oscura usa darkColorScheme(), de lo contrario usa lightColorScheme(), y luego crea una copia
    (if (isDark) darkColorScheme() else lightColorScheme()).copy(
        // Botones y elementos principales de Material toman la acción primaria de la marca.
        primary = actionPrimary,
        onPrimary = onActionPrimary,
        // Elementos secundarios de Material toman la acción secundaria.
        secondary = actionSecondary,
        onSecondary = onActionSecondary,
        // Fondo de pantalla y su contenido.
        background = background,
        onBackground = textPrimary,
        // Superficies (tarjetas, diálogos, menús) y su contenido.
        surface = surface,
        onSurface = textPrimary,
        // Variante de superficie usada por Material en campos y contenedores secundarios.
        surfaceVariant = surfaceNotice,
        // Bordes de OutlinedTextField y otros componentes delineados.
        outline = border,
        // Colores de error para validaciones y mensajes.
        error = stateError,
        onError = onStateError,
    )


/**
Tema raíz de la aplicación. Recibe si se debe usar el modo oscuro, publica la
paleta correspondiente en LocalAppColors y configura MaterialTheme con el
ColorScheme derivado y la tipografía de marca, de modo que todo el contenido
hijo comparta la misma identidad visual.
**/
@Composable
fun PetAdoptionAppTheme(
    // Indica si se usa el tema oscuro; por defecto es claro y quien lo invoca decide (por ejemplo, según la preferencia del usuario).
    darkTheme: Boolean = false,
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
**/
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
