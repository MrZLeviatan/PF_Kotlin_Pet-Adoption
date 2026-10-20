package co.edu.uniquindio.petadoptionapp.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.R


/**
 Tipografía de la app (Material 3).

 Dos familias con responsabilidades distintas:
 - Gabarito: títulos. Personalidad de marca.
 - Rubik: botones, formularios e información. Legibilidad en tamaños pequeños.

 Las reglas de peso de la marca se asignan a roles que Material ya usa
 internamente, por lo que sus componentes las heredan sin configuración.

 Cada Font declara su peso: Compose elige el archivo exacto en lugar de
 simular negritas, que se ven deformadas.
 */

// Define la familia tipográfica privada para los títulos, utilizando archivos físicos para negrita y negrita extra
private val TitleFontFamily = FontFamily(
    // Asocia el archivo de fuente Gabarito Bold al peso Bold
    Font(R.font.gabarito_bold, FontWeight.Bold),
    // Asocia el archivo Gabarito ExtraBold al peso ExtraBold
    Font(R.font.gabarito_extrabold, FontWeight.ExtraBold),
)


// Define la familia tipográfica privada para el cuerpo de texto y elementos de lectura/interacción
private val BodyFontFamily = FontFamily(
    // Asocia el archivo Rubik Regular al peso Normal
    Font(R.font.rubik_regular, FontWeight.Normal),
    // Asocia el archivo Rubik Medium al peso Medium
    Font(R.font.rubik_medium, FontWeight.Medium),
    // Asocia el archivo Rubik SemiBold al peso SemiBold
    Font(R.font.rubik_semibold, FontWeight.SemiBold),
)


// Crea una instancia base de la tipografía predeterminada de Material 3 para heredar sus tamaños e interlineados
private val Base = Typography()


// Define y exporta la configuración tipográfica global personalizada de la aplicación
val PetTypography = Typography(

    // Títulos generales: Gabarito ExtraBold
    displayLarge = Base.displayLarge.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),
    displayMedium = Base.displayMedium.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),
    displaySmall = Base.displaySmall.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),

    headlineLarge = Base.headlineLarge.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),
    headlineMedium = Base.headlineMedium.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),

    titleLarge = Base.titleLarge.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),
    titleMedium = Base.titleMedium.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),
    titleSmall = Base.titleSmall.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.ExtraBold),

    // Título de diálogos (A2F, confirmaciones): Gabarito Bold
    // AlertDialog usa headlineSmall para su título, por eso se asigna aquí.
    headlineSmall = Base.headlineSmall.copy(fontFamily = TitleFontFamily, fontWeight = FontWeight.Bold),

    // Información (publicaciones, descripciones, campos): Rubik Regular
    bodyLarge = Base.bodyLarge.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal),
    bodySmall = Base.bodySmall.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal),

    // Mensaje de diálogos y snackbars: Rubik Medium
    // AlertDialog y Snackbar usan bodyMedium para su texto.
    bodyMedium = Base.bodyMedium.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium),

    // Botones y chips: Rubik Medium
    // Button usa labelLarge; los campos de texto, labelSmall en su etiqueta flotante.
    labelLarge = Base.labelLarge.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium),
    labelSmall = Base.labelSmall.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium),

    // Links: Rubik SemiBold
    // Se eligió labelMedium porque es el rol de etiqueta con menos componentes
    // de Material asociados. Se sube a 14 sp porque los 12 sp por defecto son
    // pequeños para un elemento tocable.
    labelMedium = Base.labelMedium.copy(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)