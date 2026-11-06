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
 Se usa en ChatScreen y en las vistas de comentarios; los colores salen de los
 roles brand, background y actionPrimary del tema.
**/
@Composable
fun MessageInputBar(
    // Texto que el usuario está escribiendo.
    text: String,
    // Se invoca con el nuevo texto (solo si no supera el máximo).
    onTextChange: (String) -> Unit,
    // Acción de enviar el mensaje.
    onSend: () -> Unit,
    // Texto guía del campo vacío.
    placeholder: String,
    // Descripción accesible del botón de enviar.
    sendDescription: String,
    // Longitud máxima permitida del mensaje.
    maxLength: Int,
    // Modificador externo para posicionar la barra.
    modifier: Modifier = Modifier,
    // Si es true hay un envío en curso y se bloquea el botón para evitar duplicados.
    isSending: Boolean = false,
    // Contenido opcional a la izquierda del campo.
    leading: (@Composable () -> Unit)? = null,
    // Contenido opcional encima de la fila del campo.
    header: (@Composable () -> Unit)? = null,
) {
    // Solo se puede enviar si hay texto real (no solo espacios) y no hay otro envío en curso.
    val canSend = text.isNotBlank() && !isSending
    // Columna que forma la franja inferior con el encabezado opcional y la fila del campo.
    Column(
        modifier = modifier
            // Ocupa todo el ancho de la pantalla.
            .fillMaxWidth()
            // Fondo de la franja con el color de marca.
            .background(AppTheme.colors.brand)
            // Agrega espacio para no quedar bajo la barra de navegación del sistema (el fondo sí llega hasta abajo).
            .navigationBarsPadding()
            // Margen interno de la franja.
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Dibuja el encabezado solo si se proporcionó.
        header?.invoke()
        // Fila con el contenido inicial y el campo, centrados verticalmente.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dibuja el contenido inicial (por ejemplo, un avatar) si existe.
            leading?.invoke()
            // Campo de texto con forma de píldora.
            OutlinedTextField(
                value = text,
                // Ignora los cambios que superen el máximo, así no se puede escribir más allá del límite.
                onValueChange = { if (it.length <= maxLength) onTextChange(it) },
                modifier = Modifier
                    // Ocupa todo el espacio horizontal restante.
                    .weight(1f)
                    // Altura táctil mínima.
                    .heightIn(min = 48.dp),
                // Texto guía con estilo de cuerpo.
                placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
                // Crece hasta 4 líneas y luego hace scroll interno.
                maxLines = 4,
                // Bordes totalmente redondeados (forma de píldora).
                shape = RoundedCornerShape(24.dp),
                // El botón del teclado muestra la acción "Enviar".
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                // Al pulsar "Enviar" en el teclado, envía solo si está permitido.
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                // Botón de enviar dentro del campo, a la derecha.
                trailingIcon = {
                    // Botón circular relleno.
                    FilledIconButton(
                        onClick = onSend,
                        // Deshabilitado cuando no hay texto o hay un envío en curso.
                        enabled = canSend,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        // Amarillo de acción cuando está habilitado y atenuado cuando no.
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AppTheme.colors.actionPrimary,
                            contentColor = AppTheme.colors.onActionPrimary,
                            disabledContainerColor = AppTheme.colors.surfaceTag,
                            disabledContentColor = AppTheme.colors.stateDisabled,
                        ),
                    ) {
                        // Flecha hacia arriba con descripción accesible.
                        Icon(Icons.Filled.ArrowUpward, contentDescription = sendDescription, modifier = Modifier.size(18.dp))
                    }
                },
                // Colores del campo tomados del tema.
                colors = OutlinedTextFieldDefaults.colors(
                    // Fondo claro del campo para que contraste con la franja marrón.
                    focusedContainerColor = AppTheme.colors.background,
                    unfocusedContainerColor = AppTheme.colors.background,
                    // Sin borde, como en el diseño
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    // Color del texto escrito.
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    // Color del cursor.
                    cursorColor = AppTheme.colors.textPrimary,
                    // Texto guía atenuado.
                    focusedPlaceholderColor = AppTheme.colors.stateDisabled,
                    unfocusedPlaceholderColor = AppTheme.colors.stateDisabled,
                ),
            )
        }
    }
}

// Vista previa de la barra vacía (botón de enviar deshabilitado).
@Preview
@Composable
private fun MessageInputBarPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Barra de ejemplo para el chat.
        MessageInputBar(text = "", onTextChange = {}, onSend = {}, placeholder = "Escribe un mensaje…", sendDescription = "Enviar", maxLength = 1000)
    }
}
