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

/*
 Campos del estilo "formulario por pasos" (Crear publicación): fondo crema, borde marrón
 y etiqueta con asterisco rojo. Para los formularios de cuenta (login, registro) se usa AppTextField.
 */

/** Texto con un asterisco rojo al final, para marcar un campo obligatorio. */
fun requiredText(text: String): AnnotatedString = buildAnnotatedString {
    append(text)
    withStyle(SpanStyle(color = Color.Red)) { append("*") }
}

/** Colores compartidos por los campos de este formulario. */
@Composable
fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = AppTheme.colors.background,
    unfocusedContainerColor = AppTheme.colors.background,
    errorContainerColor = AppTheme.colors.background,
    focusedBorderColor = AppTheme.colors.border,
    unfocusedBorderColor = AppTheme.colors.border,
    focusedTextColor = AppTheme.colors.textPrimary,
    unfocusedTextColor = AppTheme.colors.textPrimary,
    focusedLabelColor = AppTheme.colors.textPrimary,
    errorTextColor = AppTheme.colors.textPrimary,
    unfocusedLabelColor = AppTheme.colors.textPrimary,
    errorBorderColor = AppTheme.colors.stateError,
    errorLabelColor = AppTheme.colors.stateError,
    errorSupportingTextColor = AppTheme.colors.stateError,
    errorCursorColor = AppTheme.colors.stateError,
)

/** Etiqueta de un campo obligatorio que va encima de él ("Descripción*"). */
@Composable
fun FormLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = requiredText(text),
        modifier = modifier,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.textPrimary,
    )
}

/** Campo de una línea, obligatorio, con su error debajo cuando lo hay. */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    // Tamaño de la etiqueta flotante; útil en campos angostos ("Edad aproximada" junto a sus unidades).
    labelFontSize: TextUnit = TextUnit.Unspecified,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(requiredText(label), fontSize = labelFontSize, maxLines = 1) },
        singleLine = true,
        isError = errorText != null,
        supportingText = if (errorText != null) { { Text(errorText) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = formFieldColors(),
    )
}

/**
 Área de texto con la etiqueta arriba y el contador "0/1000 caracteres" abajo.
 Si hay error, el contador va junto al mensaje. El límite lo aplica quien guarda el valor.
 */
@Composable
fun FormTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    errorText: String? = null,
) {
    Column(modifier = modifier) {
        FormLabel(label)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = AppTheme.colors.stateDisabled) },
            isError = errorText != null,
            supportingText = {
                val counter = stringResource(R.string.common_char_counter, value.length, maxLength)
                Text(
                    text = errorText?.let { "$it ($counter)" } ?: counter,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = formFieldColors(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormFieldsPreview() {
    PetAdoptionAppTheme {
        Column {
            FormTextField(value = "", onValueChange = {}, label = "Título de la publicación", errorText = "Mínimo 5 caracteres")
            FormTextArea(value = "", onValueChange = {}, label = "Descripción", placeholder = "Describe…", maxLength = 1000)
        }
    }
}
