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

// Marcadores que se insertan en el string; el orden lo define el recurso, no el código.
private const val TERMS_MARKER = "{terms}"
private const val POLICIES_MARKER = "{policies}"
private val MARKER_REGEX = Regex(
    "${Regex.escape(TERMS_MARKER)}|${Regex.escape(POLICIES_MARKER)}"
)

/**
Casilla "Acepto los Términos y Condiciones y las Políticas de Adopción de la app".
Los dos enlaces van dentro de la frase y se resuelven con el texto del recurso,
así un traductor puede reordenarlos sin tocar el código.
 */
@Composable
fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPoliciesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val template = stringResource(R.string.register_terms_template, TERMS_MARKER, POLICIES_MARKER)
    val termsText = stringResource(R.string.register_terms_link)
    val policiesText = stringResource(R.string.register_policies_link)

    // Siempre apuntan al callback más reciente.
    val currentOnTermsClick by rememberUpdatedState(onTermsClick)
    val currentOnPoliciesClick by rememberUpdatedState(onPoliciesClick)

    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = AppTheme.colors.textLink,
            fontWeight = FontWeight.SemiBold,
            // El color solo no basta para identificar un link (daltonismo).
            textDecoration = TextDecoration.Underline,
        )
    )

    val annotatedText = buildAnnotatedString {
        var cursor = 0
        MARKER_REGEX.findAll(template).forEach { match ->
            append(template.substring(cursor, match.range.first))
            val isTerms = match.value == TERMS_MARKER
            withLink(
                LinkAnnotation.Clickable(
                    tag = if (isTerms) "terms" else "policies",
                    styles = linkStyle,
                ) {
                    if (isTerms) currentOnTermsClick() else currentOnPoliciesClick()
                }
            ) {
                append(if (isTerms) termsText else policiesText)
            }
            cursor = match.range.last + 1
        }
        append(template.substring(cursor))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AppTheme.colors.textPrimary,
                uncheckedColor = AppTheme.colors.border,
                checkmarkColor = AppTheme.colors.surface,
            ),
        )
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TermsCheckboxPreview() {
    PetAdoptionAppTheme {
        TermsCheckbox(
            checked = true,
            onCheckedChange = {},
            onTermsClick = {},
            onPoliciesClick = {},
        )
    }
}