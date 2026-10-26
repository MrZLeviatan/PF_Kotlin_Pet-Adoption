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
 */
@Composable
fun AppTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        placeholder = { Text(text = placeholder, style = MaterialTheme.typography.bodyMedium) },
        supportingText = {
            Text(
                text = stringResource(R.string.common_char_counter, value.length, maxLength),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.surface,
            unfocusedContainerColor = AppTheme.colors.surface,
            focusedBorderColor = AppTheme.colors.textPrimary,
            unfocusedBorderColor = AppTheme.colors.border,
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            cursorColor = AppTheme.colors.textPrimary,
            focusedPlaceholderColor = AppTheme.colors.stateDisabled,
            unfocusedPlaceholderColor = AppTheme.colors.stateDisabled,
            focusedSupportingTextColor = AppTheme.colors.textPrimary,
            unfocusedSupportingTextColor = AppTheme.colors.textPrimary,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTextAreaPreview() {
    PetAdoptionAppTheme {
        AppTextArea(
            value = "",
            onValueChange = {},
            placeholder = "Describe el motivo del rechazo.",
            maxLength = 500,
        )
    }
}