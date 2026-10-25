package co.edu.uniquindio.petadoptionapp.core.component.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.icon.PawCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

// Identificador del contenido en línea y marcador que se inserta en el string.
private const val PAW_ID = "paw"
private const val PAW_MARKER = "{paw}"

// Tamaño del círculo relativo a la fuente. Ajustable: 1.0f a 1.3f.
private const val PAW_SCALE = 1.1f

/**
 Título "Bienvenido a Huellas" donde la última "o" de "Bienvenido" es un [PawCircle].
 La posición del ícono la define el string (`login_welcome`), así un traductor
 puede moverla sin tocar el código.
 */
@Composable
fun WelcomeTitle(modifier: Modifier = Modifier) {
    // Obtiene el nombre de la app y string desde los recursos
    val appName = stringResource(R.string.app_name_pantallas)
    val template = stringResource(R.string.login_welcome, PAW_MARKER, appName)
    val spokenText = stringResource(R.string.login_welcome_spoken, appName)
    val textStyle = MaterialTheme.typography.headlineMedium
    val density = LocalDensity.current

    // Construye el texto anotado de forma reactiva, dividiendo la plantilla según el marcador
    val annotatedText = remember(template) {
        val parts = template.split(PAW_MARKER)
        buildAnnotatedString {
            append(parts[0])
            // "o" es el texto alternativo: lo que se copia si se selecciona el texto.
            appendInlineContent(PAW_ID, alternateText = "o")
            append(parts.getOrElse(1) { "" })
        }
    }

    // Se mide en sp para que el espacio reservado crezca con la fuente del sistema.
    val pawSize = textStyle.fontSize * PAW_SCALE

    // Define el mapa de contenido en línea que asocia el identificador con el componente visual
    val inlineContent = mapOf(
        PAW_ID to InlineTextContent(
            placeholder = Placeholder(
                width = pawSize,
                height = pawSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
            ),
        ) {
            // Convierte el tamaño en unidades 'sp' a 'dp' considerando la escala de fuente actual
            PawCircle(
                size = with(density) { pawSize.toDp() },
                containerColor = AppTheme.colors.surfaceTag,
            )
        },
    )

    // Renderiza el componente de texto enriquecido con el icono integrado
    Text(
        text = annotatedText,
        inlineContent = inlineContent,
        style = textStyle,
        color = AppTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
        // TalkBack lee la frase completa, no "Bienvenid" + un círculo.
        modifier = modifier.clearAndSetSemantics { contentDescription = spokenText },
    )
}