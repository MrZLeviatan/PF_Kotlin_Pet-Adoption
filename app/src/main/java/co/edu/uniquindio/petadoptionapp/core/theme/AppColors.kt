package co.edu.uniquindio.petadoptionapp.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


/**
 Capa 2: tokens semánticos.
 Traduce la paleta a roles de interfaz (acción, superficie, estado). La UI declara
 qué papel cumple un color, no cuál es, y eso permite que el mismo componente se
 vea correcto en tema claro y oscuro sin condicionales en las pantallas.
 Este archivo define el modelo de roles (AppColors), sus dos implementaciones
 (LightAppColors y DarkAppColors) y el CompositionLocal por el que PetAdoptionAppTheme
 los publica; las pantallas los leen con `AppTheme.colors`.

 Convenciones:
 - Cada fondo tiene su par `on*` con el color del contenido que va encima,
    así el contraste queda definido en el modelo y no en cada pantalla.
 - Para agregar un rol: nuevo campo aquí y asignación en [LightAppColors] y
    [DarkAppColors]. El compilador exige ambas, por lo que un tema nunca queda
    incompleto.
 - @Immutable: promesa al compilador de Compose de que ninguna propiedad
        cambia después de crear el objeto. Con eso se omite recomposiciones
        de los composables que reciben este objeto cuando no hay cambios reales.
**/
@Immutable
data class AppColors(

    // Marca
    // Color de identidad de la marca (encabezados, fondos destacados).
    val brand: Color,
    // Color del contenido (texto, íconos) que se dibuja sobre el color de marca.
    val onBrand: Color,

    // Acciones (botones)
    // Fondo de los botones de acción principal.
    val actionPrimary: Color,
    // Contenido sobre los botones de acción principal.
    val onActionPrimary: Color,
    // Fondo de los botones de acción secundaria.
    val actionSecondary: Color,
    // Contenido sobre los botones de acción secundaria.
    val onActionSecondary: Color,

    // Fondos y superficies
    // Fondo general de las pantallas.
    val background: Color,
    // Superficie base de tarjetas, campos y hojas.
    val surface: Color,
    // Superficie que debe resaltar sobre el fondo (tarjetas destacadas).
    val surfaceElevated: Color,
    // Superficie para avisos e información contextual.
    val surfaceNotice: Color,
    // Superficie de etiquetas y chips informativos.
    val surfaceTag: Color,

    // Texto y bordes
    // Color principal del texto y de los íconos.
    val textPrimary: Color,
    // Texto que se pinta sobre fondos oscuros o fotografías.
    val textOnDark: Color,
    // Color de bordes de campos, tarjetas y separadores.
    val border: Color,

    // Hipervínculos
    // Color de los textos tocables que funcionan como enlace.
    val textLink: Color,

    // Estados
    // Elemento seleccionado o activo (pestaña, paso actual, opción marcada).
    val stateSelected: Color,
    // Elemento no seleccionado o inactivo.
    val stateUnselected: Color,
    // Elemento deshabilitado que no admite interacción.
    val stateDisabled: Color,
    // Color de error para mensajes, bordes y validaciones fallidas.
    val stateError: Color,
    // Contenido que se dibuja sobre un fondo de error.
    val onStateError: Color,
    // Elemento completado (por ejemplo, pasos ya superados de un asistente).
    val stateComplete: Color,

    // Chips
    // Fondo de un chip seleccionado.
    val chipSelected: Color,
    // Fondo de un chip no seleccionado.
    val chipUnselected: Color,

    // Indica si esta paleta corresponde al tema oscuro; lo usa Theme.kt para elegir el esquema base de Material.
    val isDark: Boolean,

    // En data class AppColors, junto a los demás estados:
    // Color de advertencia (estados intermedios, contraseñas de fuerza media).
    val stateWarning: Color,
    // Color de éxito (operaciones completadas, contraseñas fuertes).
    val stateSuccess: Color,

)


/**
 Paleta del tema claro: asigna a cada rol semántico un tono de PetPalette.
 Estático porque el tema cambia pocas veces (claro/oscuro). Evita el costo de
 rastrear cada lectura individual; al cambiar de tema se recompone el árbol completo,
 lo cual es aceptable en ese caso.
**/
internal val LightAppColors = AppColors(
    // Marca: marrón cálido con contenido en marrón oscuro.
    brand = PetPalette.WarmBrown,
    onBrand = PetPalette.DarkBrown,

    // Acción principal amarilla con texto oscuro para garantizar contraste.
    actionPrimary = PetPalette.Yellow,
    onActionPrimary = PetPalette.DarkBrown,
    // Acción secundaria naranja con texto blanco.
    actionSecondary = PetPalette.Orange,
    onActionSecondary = PetPalette.White,

    // Fondo crema claro y superficies en blanco, crema y arena según su jerarquía.
    background = PetPalette.Cream50,
    surface = PetPalette.White,
    surfaceElevated = PetPalette.Cream100,
    surfaceNotice = PetPalette.Sand100,
    surfaceTag = PetPalette.Sand200,

    // Texto marrón oscuro, texto blanco sobre fondos oscuros y bordes marrón medio.
    textPrimary = PetPalette.DarkBrown,
    textOnDark = PetPalette.White,
    border = PetPalette.BorderBrown,

    // Estados de selección, deshabilitado y completado.
    stateSelected = PetPalette.Yellow,
    stateUnselected = PetPalette.Ink,
    stateDisabled = PetPalette.Stone,
    stateComplete = PetPalette.DarkBrown,

    // Error en rojo estándar con contenido blanco encima.
    stateError = PetPalette.Red,
    onStateError = PetPalette.White,


    // Chips: canela cuando están seleccionados y arena cuando no.
    chipSelected = PetPalette.Tan,
    chipUnselected = PetPalette.Sand200,

    // Enlaces en marrón intenso para diferenciarlos del texto normal.
    textLink = PetPalette.LinkBrown,

    // Advertencia y éxito definidos con valores directos (aún no existen en PetPalette).
    stateWarning = Color(0xFFE0A100),
    stateSuccess = Color(0xFF3E9B4F),



    // Marca la paleta como clara.
    isDark = false,
)

/**
 Paleta del tema oscuro: mantiene los acentos de marca (amarillo, naranja, canela)
 y sustituye fondos y textos por la escala Night para conservar el contraste.
**/
internal val DarkAppColors = AppColors(
    // Marca: se conserva el marrón cálido; el contenido pasa al fondo nocturno.
    brand = PetPalette.WarmBrown,
    onBrand = PetPalette.Night900,

    // Acciones con los mismos acentos y contenido oscuro encima.
    actionPrimary = PetPalette.Yellow,
    onActionPrimary = PetPalette.Night900,
    actionSecondary = PetPalette.Orange,
    onActionSecondary = PetPalette.Night900,

    // Fondos y superficies escalonados de más oscuro a más claro según la elevación.
    background = PetPalette.Night900,
    surface = PetPalette.Night800,
    surfaceElevated = PetPalette.Night700,
    surfaceNotice = PetPalette.Night700,
    surfaceTag = PetPalette.Night600,

    // Texto claro sobre fondos nocturnos y bordes en marrón cálido.
    textPrimary = PetPalette.OnNight,
    textOnDark = PetPalette.White,
    border = PetPalette.WarmBrown,

    // Estados adaptados: el no seleccionado usa texto claro y el error un rojo suavizado.
    stateSelected = PetPalette.Yellow,
    stateUnselected = PetPalette.OnNight,
    stateDisabled = PetPalette.Stone,
    stateError = PetPalette.RedLight,
    onStateError = PetPalette.Night900,
    stateComplete = PetPalette.DarkBrown,


    // Chips: canela seleccionado y superficie nocturna clara cuando no lo está.
    chipSelected = PetPalette.Tan,
    chipUnselected = PetPalette.Night600,

    // En oscuro los enlaces usan amarillo, que resalta más que el marrón sobre el fondo nocturno.
    textLink = PetPalette.Yellow,

    // Advertencia y éxito en tonos más claros para que se lean sobre fondo oscuro.
    stateWarning = Color(0xFFFFCA28),
    stateSuccess = Color(0xFF81C784),

    // Marca la paleta como oscura.
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
**/
internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }
