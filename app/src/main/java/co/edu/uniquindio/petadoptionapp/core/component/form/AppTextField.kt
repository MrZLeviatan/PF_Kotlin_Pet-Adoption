package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme

/**
Campo de texto de la app (correo, contraseña, nombre...).

Centraliza el estilo del formulario para que todas las pantallas se vean igual.
Con `isPassword = true` gestiona solo el botón de mostrar/ocultar.
El texto escrito usa `bodyLarge` (Rubik Regular) por defecto en Material.
Se usa en login, registro, recuperar/cambiar contraseña, editar perfil, ajustes
y diálogos de formulario; todos sus colores salen de AppTheme.

La descripción del campo es una etiqueta flotante: se ve dentro del campo mientras
está vacío y sin foco, y sube al borde superior al enfocarlo o escribir.

Opcionales:
- `readOnly = true`: muestra el valor con un candado y no deja editarlo (ej. el correo).
- `required = true`: añade un asterisco rojo a la etiqueta.
- `textStyle`: cambia la letra del texto escrito y de la etiqueta (ej. más pequeña en un diálogo).
**/
@Composable
fun AppTextField(
    // Texto actual ingresado en el campo
    value: String,
    // Callback que se ejecuta cuando el usuario escribe o borra texto
    onValueChange: (String) -> Unit,
    // Descripción del campo: flota arriba al enfocar o escribir
    label: String,
    // Icono decorativo ubicado al inicio del campo de texto
    leadingIcon: ImageVector,
    // Modificador externo para personalizar posición o márgenes
    modifier: Modifier = Modifier,
    // Mensaje de error bajo el campo; null significa que el valor es válido.
    errorMessage: String? = null,
    // Bandera para indicar si el campo es de tipo contraseña
    isPassword: Boolean = false,
    // Tipo de teclado virtual que se mostrará (texto, número, email, etc.)
    keyboardType: KeyboardType = KeyboardType.Text,
    // Acción del botón inferior del teclado (Siguiente, Hecho, Buscar, etc.)
    imeAction: ImeAction = ImeAction.Next,
    // Acciones personalizadas al presionar los botones del teclado
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    // Si es true el valor se muestra pero no se puede editar.
    readOnly: Boolean = false,
    // Si es true se agrega un asterisco rojo a la etiqueta para marcar el campo como obligatorio.
    required: Boolean = false,
    // null: la letra por defecto de Material (bodyLarge)
    textStyle: TextStyle? = null,
) {
    // Visibilidad como estado interno: ninguna pantalla debe preocuparse por esto.
    // rememberSaveable conserva el valor si la pantalla rota o el proceso se recrea.
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    // Se lee el color de error fuera del builder del texto, porque allí no se puede llamar a funciones @Composable.
    val errorColor = AppTheme.colors.stateError

    // Dibuja el campo de texto con borde oficial de Material Design 3
    OutlinedTextField(
        // Valor mostrado y callback de cambio (estado elevado: lo controla quien usa el campo).
        value = value,
        onValueChange = onValueChange,
        // El campo siempre ocupa todo el ancho disponible.
        modifier = modifier.fillMaxWidth(),
        // Bloquea la edición cuando el campo es de solo lectura.
        readOnly = readOnly,
        // Usa el estilo recibido o, si no hay, el estilo de texto actual de Material.
        textStyle = textStyle ?: LocalTextStyle.current,
        // Limita el campo a una sola línea de texto
        singleLine = true,
        // Activa el estado de error cuando existe un mensaje.
        isError = errorMessage != null,
        // Muestra el mensaje de error bajo el campo; si no hay, no reserva espacio.
        supportingText = errorMessage?.let { { Text(text = it) } },
        // Esquinas redondeadas medianas definidas por el tema de Material.
        shape = MaterialTheme.shapes.medium,
        // `label` en lugar de `placeholder`: Material anima la subida al borde superior
        // y además TalkBack sigue leyendo la descripción cuando el campo ya tiene texto.
        label = {
            Text(
                // Texto enriquecido: la etiqueta y, si es obligatorio, un asterisco en rojo.
                text = buildAnnotatedString {
                    // Agrega el texto de la etiqueta.
                    append(label)
                    // Si el campo es obligatorio añade "*" con el color de error.
                    if (required) withStyle(SpanStyle(color = errorColor)) { append("*") }
                },
                // Misma letra que el texto escrito para que la etiqueta y el valor se vean coherentes.
                style = textStyle ?: MaterialTheme.typography.bodyLarge,
            )
        },
        leadingIcon = {
            // Decorativo: la etiqueta ya describe el campo.
            Icon(imageVector = leadingIcon, contentDescription = null)
        },
        // Contraseña: botón de mostrar/ocultar. Solo lectura: candado que avisa que no se edita.
        trailingIcon = when {
            // Caso contraseña: botón que alterna la visibilidad del texto.
            isPassword -> {
                {
                    // Al pulsarlo invierte el estado de visibilidad.
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            // Ojo tachado si se ve la contraseña, ojo normal si está oculta.
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            // Este sí es interactivo: TalkBack debe anunciar su acción.
                            contentDescription = stringResource(
                                if (passwordVisible) R.string.common_hide_password
                                else R.string.common_show_password
                            ),
                        )
                    }
                }
            }
            // Caso solo lectura: candado con descripción que explica que no es editable.
            readOnly -> {
                { Icon(Icons.Outlined.Lock, contentDescription = stringResource(R.string.common_not_editable)) }
            }
            // En cualquier otro caso no hay icono final.
            else -> null
        },
        // Oculta los caracteres con asteriscos/puntos si es contraseña y la visibilidad está desactivada
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        // Configura el tipo de teclado y la acción del botón de envío según los parámetros
        keyboardOptions = KeyboardOptions(
            // Las contraseñas fuerzan el teclado de contraseña (sin sugerencias ni autocorrección).
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            // Acción del botón principal del teclado.
            imeAction = imeAction,
        ),
        // Acciones a ejecutar al pulsar el botón del teclado (por ejemplo, enviar el formulario).
        keyboardActions = keyboardActions,
        // Todos los colores salen del tema: el campo responde a claro/oscuro solo.
        colors = OutlinedTextFieldDefaults.colors(
            // Fondo de superficie con y sin foco.
            focusedContainerColor = AppTheme.colors.surface,
            unfocusedContainerColor = AppTheme.colors.surface,
            // el foco se nota
            focusedBorderColor = AppTheme.colors.textPrimary,
            // Borde normal cuando el campo no tiene foco.
            unfocusedBorderColor = AppTheme.colors.border,
            // Color del texto escrito.
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            // Color del cursor.
            cursorColor = AppTheme.colors.textPrimary,
            // Icono inicial oscuro con foco y atenuado sin foco.
            focusedLeadingIconColor = AppTheme.colors.textPrimary,
            unfocusedLeadingIconColor = AppTheme.colors.border,
            // Icono final con el mismo criterio que el inicial.
            focusedTrailingIconColor = AppTheme.colors.textPrimary,
            unfocusedTrailingIconColor = AppTheme.colors.border,
            // Etiqueta: gris dentro del campo (hace de placeholder), oscura al enfocar
            focusedLabelColor = AppTheme.colors.textPrimary,
            unfocusedLabelColor = AppTheme.colors.stateDisabled,
            // Colores del estado de error (sin esto, Material usa los suyos por defecto)
            errorContainerColor = AppTheme.colors.surface,
            errorBorderColor = AppTheme.colors.stateError,
            errorTextColor = AppTheme.colors.textPrimary,
            errorCursorColor = AppTheme.colors.stateError,
            errorLeadingIconColor = AppTheme.colors.stateError,
            errorTrailingIconColor = AppTheme.colors.stateError,
            errorSupportingTextColor = AppTheme.colors.stateError,
            errorLabelColor = AppTheme.colors.stateError,
        ),
    )
}