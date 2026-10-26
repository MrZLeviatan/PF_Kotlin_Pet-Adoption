package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/** Título de un campo o sección. Con [required] añade un asterisco en rojo. */
@Composable
fun RequiredLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = true,
) {
    val errorColor = AppTheme.colors.stateError

    Text(
        text = buildAnnotatedString {
            append(text)
            if (required) withStyle(SpanStyle(color = errorColor)) { append("*") }
        },
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colors.textPrimary,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun RequiredLabelPreview() {
    PetAdoptionAppTheme { RequiredLabel(text = "Descripción") }
}