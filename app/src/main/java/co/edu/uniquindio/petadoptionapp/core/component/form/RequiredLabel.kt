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

/**
 Título de un campo o sección. Con [required] añade un asterisco en rojo.
 Se usa en la pantalla de rechazo de publicaciones para titular las secciones
 del formulario; a diferencia de FormLabel usa la tipografía titleSmall del tema
 y el rojo del token stateError, por lo que se adapta al modo oscuro.
**/
@Composable
fun RequiredLabel(
    // Texto del título.
    text: String,
    // Modificador externo para posicionar el título.
    modifier: Modifier = Modifier,
    // Si es true agrega el asterisco de obligatorio; por defecto lo es.
    required: Boolean = true,
) {
    // Se lee el color de error fuera del builder, donde no se pueden invocar funciones @Composable.
    val errorColor = AppTheme.colors.stateError

    // Texto del título con el asterisco opcional.
    Text(
        text = buildAnnotatedString {
            // Agrega el texto del título.
            append(text)
            // Si es obligatorio añade "*" en el color de error.
            if (required) withStyle(SpanStyle(color = errorColor)) { append("*") }
        },
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colors.textPrimary,
        modifier = modifier,
    )
}

// Vista previa de un título obligatorio.
@Preview(showBackground = true)
@Composable
private fun RequiredLabelPreview() {
    // Aplica el tema y muestra "Descripción*".
    PetAdoptionAppTheme { RequiredLabel(text = "Descripción") }
}