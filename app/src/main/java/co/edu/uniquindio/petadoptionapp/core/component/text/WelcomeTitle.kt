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

/**
Título de bienvenida de la pantalla de Login ("Bienvenido a Huellas") con un ícono de huella incrustado dentro
del texto. Funciona con el contenido en línea de Compose ([InlineTextContent]): el string de recursos trae un
marcador "{paw}" en el lugar del ícono; el texto se parte en ese marcador y en medio se inserta un espacio reservado
(Placeholder) del tamaño de la fuente, donde se dibuja un [PawCircle]. Para accesibilidad, TalkBack lee una versión
hablada completa de la frase en lugar del texto partido.
**/

// Identificador del contenido en línea y marcador que se inserta en el string.
private const val PAW_ID = "paw"
// Marcador de texto que indica en el string dónde va la huella.
private const val PAW_MARKER = "{paw}"

// Tamaño del círculo relativo a la fuente. Ajustable: 1.0f a 1.3f.
private const val PAW_SCALE = 1.1f

/**
Título "Bienvenido a Huellas" donde la última "o" de "Bienvenido" es un [PawCircle].
La posición del ícono la define el string (`login_welcome`), así un traductor
puede moverla sin tocar el código.
**/
@Composable
fun WelcomeTitle(modifier: Modifier = Modifier) {
    // Obtiene el nombre de la app y string desde los recursos
    val appName = stringResource(R.string.app_name_pantallas)
    // Plantilla del título con el marcador de la huella y el nombre de la app ya insertados.
    val template = stringResource(R.string.login_welcome, PAW_MARKER, appName)
    // Versión de la frase pensada para ser leída en voz alta por TalkBack.
    val spokenText = stringResource(R.string.login_welcome_spoken, appName)
    // Estilo tipográfico del título.
    val textStyle = MaterialTheme.typography.headlineMedium
    // Densidad de pantalla para convertir sp a dp.
    val density = LocalDensity.current

    // Construye el texto anotado de forma reactiva, dividiendo la plantilla según el marcador
    val annotatedText = remember(template) {
        // Separa el texto anterior y posterior al marcador.
        val parts = template.split(PAW_MARKER)
        // Arma el texto con el contenido en línea en medio.
        buildAnnotatedString {
            // Texto antes de la huella.
            append(parts[0])
            // "o" es el texto alternativo: lo que se copia si se selecciona el texto.
            appendInlineContent(PAW_ID, alternateText = "o")
            // Texto después de la huella (vacío si el string no tuviera marcador).
            append(parts.getOrElse(1) { "" })
        }
    }

    // Se mide en sp para que el espacio reservado crezca con la fuente del sistema.
    val pawSize = textStyle.fontSize * PAW_SCALE

    // Define el mapa de contenido en línea que asocia el identificador con el componente visual
    val inlineContent = mapOf(
        PAW_ID to InlineTextContent(
            // Espacio reservado dentro del texto para la huella, centrado verticalmente con las letras.
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
        // Texto anotado con el marcador de contenido en línea.
        text = annotatedText,
        // Contenido que reemplaza al marcador (la huella).
        inlineContent = inlineContent,
        // Estilo del título.
        style = textStyle,
        // Color de texto principal.
        color = AppTheme.colors.textPrimary,
        // Centra el título.
        textAlign = TextAlign.Center,
        // TalkBack lee la frase completa, no "Bienvenid" + un círculo.
        modifier = modifier.clearAndSetSemantics { contentDescription = spokenText },
    )
}
