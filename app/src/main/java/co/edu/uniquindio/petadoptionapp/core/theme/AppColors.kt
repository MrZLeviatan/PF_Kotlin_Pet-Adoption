package co.edu.uniquindio.petadoptionapp.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


/**
 Capa 2: tokens semánticos.
 Traduce la paleta a roles de interfaz (acción, superficie, estado). La UI declara
 qué papel cumple un color, no cuál es, y eso permite que el mismo componente se
 vea correcto en tema claro y oscuro sin condicionales en las pantallas.

 Convenciones:
 - Cada fondo tiene su par `on*` con el color del contenido que va encima,
    así el contraste queda definido en el modelo y no en cada pantalla.
 - Para agregar un rol: nuevo campo aquí y asignación en [LightAppColors] y
    [DarkAppColors]. El compilador exige ambas, por lo que un tema nunca queda
    incompleto.
 - @Immutable: promesa al compilador de Compose de que ninguna propiedad
        cambia después de crear el objeto. Con eso se omite recomposiciones
        de los composables que reciben este objeto cuando no hay cambios reales.
 */
@Immutable
data class AppColors(

    // Marca
    val brand: Color,
    val onBrand: Color,

    // Acciones (botones)
    val actionPrimary: Color,
    val onActionPrimary: Color,
    val actionSecondary: Color,
    val onActionSecondary: Color,

    // Fondos y superficies
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceNotice: Color,
    val surfaceTag: Color,

    // Texto y bordes
    val textPrimary: Color,
    val textOnDark: Color,
    val border: Color,

    // Hipervínculos
    val textLink: Color,

    // Estados
    val stateSelected: Color,
    val stateUnselected: Color,
    val stateDisabled: Color,
    val stateError: Color,
    val onStateError: Color,

    // Chips
    val chipSelected: Color,
    val chipUnselected: Color,

    val isDark: Boolean,
)


/**
 Estático porque el tema cambia pocas veces (claro/oscuro). Evita el costo de
 rastrear cada lectura individual; al cambiar de tema se recompone el árbol completo,
 lo cual es aceptable en ese caso.
 */
internal val LightAppColors = AppColors(
    brand = PetPalette.WarmBrown,
    onBrand = PetPalette.DarkBrown,

    actionPrimary = PetPalette.Yellow,
    onActionPrimary = PetPalette.DarkBrown,
    actionSecondary = PetPalette.Orange,
    onActionSecondary = PetPalette.White,

    background = PetPalette.Cream50,
    surface = PetPalette.White,
    surfaceElevated = PetPalette.Cream100,
    surfaceNotice = PetPalette.Sand100,
    surfaceTag = PetPalette.Sand200,

    textPrimary = PetPalette.DarkBrown,
    textOnDark = PetPalette.White,
    border = PetPalette.BorderBrown,

    stateSelected = PetPalette.Yellow,
    stateUnselected = PetPalette.Ink,
    stateDisabled = PetPalette.Stone,
    stateError = PetPalette.Red,
    onStateError = PetPalette.White,

    chipSelected = PetPalette.Tan,
    chipUnselected = PetPalette.Sand200,

    textLink = PetPalette.LinkBrown,

    isDark = false,
)

internal val DarkAppColors = AppColors(
    brand = PetPalette.WarmBrown,
    onBrand = PetPalette.Night900,

    actionPrimary = PetPalette.Yellow,
    onActionPrimary = PetPalette.Night900,
    actionSecondary = PetPalette.Orange,
    onActionSecondary = PetPalette.Night900,

    background = PetPalette.Night900,
    surface = PetPalette.Night800,
    surfaceElevated = PetPalette.Night700,
    surfaceNotice = PetPalette.Night700,
    surfaceTag = PetPalette.Night600,

    textPrimary = PetPalette.OnNight,
    textOnDark = PetPalette.White,
    border = PetPalette.WarmBrown,

    stateSelected = PetPalette.Yellow,
    stateUnselected = PetPalette.OnNight,
    stateDisabled = PetPalette.Stone,
    stateError = PetPalette.RedLight,
    onStateError = PetPalette.Night900,

    chipSelected = PetPalette.Tan,
    chipUnselected = PetPalette.Night600,

    textLink = PetPalette.Yellow,

    isDark = true,
)


/**
- CompositionLocal: mecanismo de Compose para "inyectar" un valor a todo el
árbol de UI sin pasarlo como parámetro por cada composable.

- static: el valor casi nunca cambia (solo claro <-> oscuro). Compose no
rastrea cada lectura individual, lo que es más eficiente; al cambiar de
tema se recompone todo el árbol, algo aceptable en ese caso.

- El valor entre llaves es el DEFAULT si alguien lee el tema fuera de
`PetAdoptionAppTheme` (por ejemplo, en un preview mal configurado).
*/
internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }