package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Campos del estilo "formulario por pasos" (Crear publicación): fondo crema, borde marrón
 y etiqueta con asterisco rojo. Para los formularios de cuenta (login, registro) se usa AppTextField.
 Además de los campos, expone utilidades compartidas (requiredText, formFieldColors
 y FormLabel) que reutilizan FormDropdown y OptionSelector para que todo el
 formulario se vea uniforme.
**/

/** Texto con un asterisco rojo al final, para marcar un campo obligatorio. */
fun requiredText(text: String): AnnotatedString = buildAnnotatedString {
    // Agrega el texto original de la etiqueta.
    append(text)
    // Agrega el asterisco en rojo fijo (no usa el token de error del tema, así que no cambia en modo oscuro).
    withStyle(SpanStyle(color = Color.Red)) { append("*") }
}

/**
 Colores compartidos por los campos de este formulario. Fondo igual al de la
 pantalla, borde marrón fijo (con y sin foco) y rojo del tema en estado de error.
**/
@Composable
fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    // Fondo del campo igual al de la pantalla en todos los estados.
    focusedContainerColor = AppTheme.colors.background,
    unfocusedContainerColor = AppTheme.colors.background,
    errorContainerColor = AppTheme.colors.background,
    // El borde no cambia con el foco para mantener el estilo del diseño.
    focusedBorderColor = AppTheme.colors.border,
    unfocusedBorderColor = AppTheme.colors.border,
    // Color del texto escrito.
    focusedTextColor = AppTheme.colors.textPrimary,
    unfocusedTextColor = AppTheme.colors.textPrimary,
    // Color de la etiqueta flotante con foco.
    focusedLabelColor = AppTheme.colors.textPrimary,
    // Texto escrito sigue legible aunque el campo tenga error.
    errorTextColor = AppTheme.colors.textPrimary,
    // Color de la etiqueta sin foco.
    unfocusedLabelColor = AppTheme.colors.textPrimary,
    // En error, borde, etiqueta, mensaje y cursor pasan al rojo del tema.
    errorBorderColor = AppTheme.colors.stateError,
    errorLabelColor = AppTheme.colors.stateError,
    errorSupportingTextColor = AppTheme.colors.stateError,
    errorCursorColor = AppTheme.colors.stateError,
)

/** Etiqueta de un campo obligatorio que va encima de él ("Descripción*"). */
@Composable
fun FormLabel(text: String, modifier: Modifier = Modifier) {
    // Texto de la etiqueta con su asterisco de obligatorio.
    Text(
        text = requiredText(text),
        modifier = modifier,
        // Tamaño pequeño para que la etiqueta no compita con el contenido del campo.
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.textPrimary,
    )
}

/** Campo de una línea, obligatorio, con su error debajo cuando lo hay. */
@Composable
fun FormTextField(
    // Texto actual del campo.
    value: String,
    // Se invoca con el nuevo texto cada vez que el usuario escribe.
    onValueChange: (String) -> Unit,
    // Etiqueta flotante del campo.
    label: String,
    // Modificador externo para posicionar el campo.
    modifier: Modifier = Modifier,
    // Mensaje de error bajo el campo; null si no hay error.
    errorText: String? = null,
    // Tipo de teclado (texto, número, etc.).
    keyboardType: KeyboardType = KeyboardType.Text,
    // Tamaño de la etiqueta flotante; útil en campos angostos ("Edad aproximada" junto a sus unidades).
    labelFontSize: TextUnit = TextUnit.Unspecified,
) {
    // Campo con borde de Material 3 configurado con el estilo del formulario.
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        // Ocupa todo el ancho disponible.
        modifier = modifier.fillMaxWidth(),
        // Etiqueta con asterisco, en una sola línea y con el tamaño indicado.
        label = { Text(requiredText(label), fontSize = labelFontSize, maxLines = 1) },
        // Restringe la entrada a una sola línea.
        singleLine = true,
        // Activa el estado de error cuando hay mensaje.
        isError = errorText != null,
        // Muestra el mensaje de error solo cuando existe.
        supportingText = if (errorText != null) { { Text(errorText) } } else null,
        // Teclado adecuado al tipo de dato.
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        // Esquinas redondeadas de 12 dp, comunes a todo el formulario.
        shape = RoundedCornerShape(12.dp),
        // Paleta compartida del formulario.
        colors = formFieldColors(),
    )
}

/**
 Área de texto con la etiqueta arriba y el contador "0/1000 caracteres" abajo.
 Si hay error, el contador va junto al mensaje. El límite lo aplica quien guarda el valor.
**/
@Composable
fun FormTextArea(
    // Texto actual del área.
    value: String,
    // Se invoca con el nuevo texto cada vez que el usuario escribe.
    onValueChange: (String) -> Unit,
    // Etiqueta que se muestra encima del área.
    label: String,
    // Texto guía dentro del área vacía.
    placeholder: String,
    // Máximo de caracteres mostrado en el contador.
    maxLength: Int,
    // Modificador externo para posicionar el bloque.
    modifier: Modifier = Modifier,
    // Mensaje de error; null si no hay error.
    errorText: String? = null,
) {
    // Columna que apila la etiqueta y el área.
    Column(modifier = modifier) {
        // Etiqueta obligatoria encima del área.
        FormLabel(label)
        // Separación entre la etiqueta y el área.
        Spacer(modifier = Modifier.height(6.dp))
        // Área de texto multilínea.
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                // Ocupa todo el ancho.
                .fillMaxWidth()
                // Altura fija para dar espacio a descripciones largas.
                .height(180.dp),
            // Texto guía pequeño y atenuado.
            placeholder = { Text(placeholder, fontSize = 13.sp, color = AppTheme.colors.stateDisabled) },
            // Activa el estado de error cuando hay mensaje.
            isError = errorText != null,
            // Texto inferior con el contador y, si aplica, el error.
            supportingText = {
                // Construye el contador ("5/1000 caracteres") desde los recursos.
                val counter = stringResource(R.string.common_char_counter, value.length, maxLength)
                // Muestra el error con el contador entre paréntesis, o solo el contador, alineado a la derecha.
                Text(
                    text = errorText?.let { "$it ($counter)" } ?: counter,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
            // Esquinas redondeadas comunes al formulario.
            shape = RoundedCornerShape(12.dp),
            // Paleta compartida del formulario.
            colors = formFieldColors(),
        )
    }
}

// Vista previa de un campo con error y un área de texto vacía.
@Preview(showBackground = true)
@Composable
private fun FormFieldsPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Apila los ejemplos.
        Column {
            // Campo de título con mensaje de error.
            FormTextField(value = "", onValueChange = {}, label = "Título de la publicación", errorText = "Mínimo 5 caracteres")
            // Área de descripción vacía.
            FormTextArea(value = "", onValueChange = {}, label = "Descripción", placeholder = "Describe…", maxLength = 1000)
        }
    }
}
