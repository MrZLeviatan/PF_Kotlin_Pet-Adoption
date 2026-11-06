package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Casilla de aceptación de términos y políticas usada en el último paso del
 registro (RegisterScreen). Construye una frase con dos enlaces tocables a partir
 de un recurso de texto con marcadores, de modo que la traducción decide el orden
 de las palabras. Los enlaces usan el token textLink del tema, el estilo SemiBold
 y subrayado.
**/

// Marcadores que se insertan en el string; el orden lo define el recurso, no el código.
private const val TERMS_MARKER = "{terms}"
private const val POLICIES_MARKER = "{policies}"
// Expresión regular que encuentra cualquiera de los dos marcadores; se escapan porque las llaves son caracteres especiales.
private val MARKER_REGEX = Regex(
    "${Regex.escape(TERMS_MARKER)}|${Regex.escape(POLICIES_MARKER)}"
)

/**
Casilla "Acepto los Términos y Condiciones y las Políticas de Adopción de la app".
Los dos enlaces van dentro de la frase y se resuelven con el texto del recurso,
así un traductor puede reordenarlos sin tocar el código.
**/
@Composable
fun TermsCheckbox(
    // Estado actual de la casilla.
    checked: Boolean,
    // Se invoca con el nuevo estado al marcar o desmarcar.
    onCheckedChange: (Boolean) -> Unit,
    // Acción al tocar el enlace de Términos y Condiciones.
    onTermsClick: () -> Unit,
    // Acción al tocar el enlace de Políticas de Adopción.
    onPoliciesClick: () -> Unit,
    // Modificador externo para posicionar la fila.
    modifier: Modifier = Modifier,
) {
    // Frase completa con los marcadores en el lugar donde irán los enlaces.
    val template = stringResource(R.string.register_terms_template, TERMS_MARKER, POLICIES_MARKER)
    // Texto visible del enlace de términos.
    val termsText = stringResource(R.string.register_terms_link)
    // Texto visible del enlace de políticas.
    val policiesText = stringResource(R.string.register_policies_link)

    // Siempre apuntan al callback más reciente.
    // rememberUpdatedState evita que el enlace conserve una lambda vieja si la pantalla recompone con otra.
    val currentOnTermsClick by rememberUpdatedState(onTermsClick)
    val currentOnPoliciesClick by rememberUpdatedState(onPoliciesClick)

    // Estilo común de los dos enlaces.
    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            // Color de enlace del tema (marrón en claro, amarillo en oscuro).
            color = AppTheme.colors.textLink,
            // Peso SemiBold para destacar el enlace.
            fontWeight = FontWeight.SemiBold,
            // El color solo no basta para identificar un link (daltonismo).
            textDecoration = TextDecoration.Underline,
        )
    )

    // Construye la frase reemplazando cada marcador por su enlace tocable.
    val annotatedText = buildAnnotatedString {
        // Posición del template hasta la que ya se copió texto.
        var cursor = 0
        // Recorre cada marcador encontrado en el orden en que aparece en el recurso.
        MARKER_REGEX.findAll(template).forEach { match ->
            // Copia el texto plano que hay antes del marcador.
            append(template.substring(cursor, match.range.first))
            // Determina si el marcador corresponde a términos o a políticas.
            val isTerms = match.value == TERMS_MARKER
            // Envuelve el texto del enlace en una anotación tocable.
            withLink(
                LinkAnnotation.Clickable(
                    // Etiqueta que identifica el enlace.
                    tag = if (isTerms) "terms" else "policies",
                    // Estilo visual del enlace.
                    styles = linkStyle,
                ) {
                    // Al tocar ejecuta el callback correspondiente.
                    if (isTerms) currentOnTermsClick() else currentOnPoliciesClick()
                }
            ) {
                // Agrega el texto visible del enlace.
                append(if (isTerms) termsText else policiesText)
            }
            // Avanza el cursor justo después del marcador procesado.
            cursor = match.range.last + 1
        }
        // Copia el texto que queda después del último marcador.
        append(template.substring(cursor))
    }

    // Fila con la casilla y la frase.
    Row(
        modifier = modifier.fillMaxWidth(),
        // Centra verticalmente la casilla con el texto.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Casilla de verificación.
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            // Colores del tema: marcada en color de texto, sin marcar con el borde y check en superficie.
            colors = CheckboxDefaults.colors(
                checkedColor = AppTheme.colors.textPrimary,
                uncheckedColor = AppTheme.colors.border,
                checkmarkColor = AppTheme.colors.surface,
            ),
        )
        // Frase con los enlaces incrustados.
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Vista previa de la casilla marcada.
@Preview(showBackground = true)
@Composable
private fun TermsCheckboxPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Casilla de ejemplo con callbacks vacíos.
        TermsCheckbox(
            checked = true,
            onCheckedChange = {},
            onTermsClick = {},
            onPoliciesClick = {},
        )
    }
}
