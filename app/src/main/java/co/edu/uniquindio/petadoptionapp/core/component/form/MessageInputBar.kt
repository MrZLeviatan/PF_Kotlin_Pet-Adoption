package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
 Barra inferior para escribir (comentarios, chat): campo en forma de píldora y botón enviar amarillo,
 sobre una franja marrón que llega hasta abajo sin tapar los botones del sistema.
 [leading] va antes del campo (ej. el avatar de quien comenta) y [header] encima (ej. "Respondiendo a...").
 [maxLength] lo define el dominio: no se deja escribir más allá.
 */
@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    sendDescription: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    isSending: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
) {
    val canSend = text.isNotBlank() && !isSending
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.brand)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        header?.invoke()
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            leading?.invoke()
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= maxLength) onTextChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                trailingIcon = {
                    FilledIconButton(
                        onClick = onSend,
                        enabled = canSend,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AppTheme.colors.actionPrimary,
                            contentColor = AppTheme.colors.onActionPrimary,
                            disabledContainerColor = AppTheme.colors.surfaceTag,
                            disabledContentColor = AppTheme.colors.stateDisabled,
                        ),
                    ) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = sendDescription, modifier = Modifier.size(18.dp))
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppTheme.colors.background,
                    unfocusedContainerColor = AppTheme.colors.background,
                    focusedBorderColor = Color.Transparent,     // Sin borde, como en el diseño
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    cursorColor = AppTheme.colors.textPrimary,
                    focusedPlaceholderColor = AppTheme.colors.stateDisabled,
                    unfocusedPlaceholderColor = AppTheme.colors.stateDisabled,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun MessageInputBarPreview() {
    PetAdoptionAppTheme {
        MessageInputBar(text = "", onTextChange = {}, onSend = {}, placeholder = "Escribe un mensaje…", sendDescription = "Enviar", maxLength = 1000)
    }
}
