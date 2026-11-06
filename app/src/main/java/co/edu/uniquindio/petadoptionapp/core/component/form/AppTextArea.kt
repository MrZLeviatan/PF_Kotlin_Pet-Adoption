package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Área de texto de varias líneas con contador "0/500 caracteres".
El límite lo aplica quien guarda el valor (el ViewModel); aquí solo se muestra.
Se usa, por ejemplo, para escribir el motivo de rechazo de una publicación, y
comparte con AppTextField la misma paleta de colores del tema.
**/
@Composable
fun AppTextArea(
    // Texto actual del área.
    value: String,
    // Se invoca con el nuevo texto cada vez que el usuario escribe.
    onValueChange: (String) -> Unit,
    // Texto guía que se ve mientras el área está vacía.
    placeholder: String,
    // Máximo de caracteres que se muestra en el contador.
    maxLength: Int,
    // Modificador externo para posicionar el área.
    modifier: Modifier = Modifier,
    // Con error: borde rojo y el mensaje junto al contador ("Mínimo 20 caracteres (5/500 caracteres)").
    errorText: String? = null,
) {
    // Campo con borde de Material 3 usado como área multilínea.
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            // Ocupa todo el ancho disponible.
            .fillMaxWidth()
            // Altura mínima para que se perciba como un área de texto amplia.
            .heightIn(min = 180.dp),
        // Texto guía con estilo de cuerpo.
        placeholder = { Text(text = placeholder, style = MaterialTheme.typography.bodyMedium) },
        // Activa el estado de error cuando hay mensaje.
        isError = errorText != null,
        // Texto inferior: contador y, si aplica, el mensaje de error.
        supportingText = {
            // Construye el texto del contador ("5/500 caracteres") desde los recursos.
            val counter = stringResource(R.string.common_char_counter, value.length, maxLength)
            // Muestra el error con el contador entre paréntesis, o solo el contador, alineado a la derecha.
            Text(
                text = errorText?.let { "$it ($counter)" } ?: counter,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        },
        // Esquinas redondeadas medianas definidas por el tema de Material.
        shape = MaterialTheme.shapes.medium,
        // Colores del área tomados del tema de la app.
        colors = OutlinedTextFieldDefaults.colors(
            // Fondo de superficie con y sin foco.
            focusedContainerColor = AppTheme.colors.surface,
            unfocusedContainerColor = AppTheme.colors.surface,
            // Borde oscuro con foco para que se note, y de borde normal sin foco.
            focusedBorderColor = AppTheme.colors.textPrimary,
            unfocusedBorderColor = AppTheme.colors.border,
            // Color del texto escrito.
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            // Color del cursor.
            cursorColor = AppTheme.colors.textPrimary,
            // Texto guía atenuado.
            focusedPlaceholderColor = AppTheme.colors.stateDisabled,
            unfocusedPlaceholderColor = AppTheme.colors.stateDisabled,
            // Color del contador cuando no hay error.
            focusedSupportingTextColor = AppTheme.colors.textPrimary,
            unfocusedSupportingTextColor = AppTheme.colors.textPrimary,
            // Estado de error: mismo fondo, pero borde, contador y cursor en rojo.
            errorContainerColor = AppTheme.colors.surface,
            errorBorderColor = AppTheme.colors.stateError,
            errorSupportingTextColor = AppTheme.colors.stateError,
            errorCursorColor = AppTheme.colors.stateError,
            errorTextColor = AppTheme.colors.textPrimary,
        ),
    )
}

// Vista previa del área vacía con su texto guía.
@Preview(showBackground = true)
@Composable
private fun AppTextAreaPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Área de ejemplo para el motivo de rechazo.
        AppTextArea(
            value = "",
            onValueChange = {},
            placeholder = "Describe el motivo del rechazo.",
            maxLength = 500,
        )
    }
}