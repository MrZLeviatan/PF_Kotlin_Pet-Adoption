package co.edu.uniquindio.petadoptionapp.core.component.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 */
@Composable
fun AppTextField(
    value: String,                              // Texto actual ingresado en el campo
    onValueChange: (String) -> Unit,            // Callback que se ejecuta cuando el usuario escribe o borra texto
    placeholder: String,                        // Texto guía o marcador que aparece cuando el campo está vacío
    leadingIcon: ImageVector,                   // Icono decorativo ubicado al inicio del campo de texto
    modifier: Modifier = Modifier,              // Modificador externo para personalizar posición o márgenes
    errorMessage: String? = null,
    isPassword: Boolean = false,                // Bandera para indicar si el campo es de tipo contraseña
    keyboardType: KeyboardType = KeyboardType.Text, // Tipo de teclado virtual que se mostrará (texto, número, email, etc.)
    imeAction: ImeAction = ImeAction.Next,      // Acción del botón inferior del teclado (Siguiente, Hecho, Buscar, etc.)
    keyboardActions: KeyboardActions = KeyboardActions.Default, // Acciones personalizadas al presionar los botones del teclado
) {
    // Visibilidad como estado interno: ninguna pantalla debe preocuparse por esto.
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Dibuja el campo de texto con borde oficial de Material Design 3
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,          // Limita el campo a una sola línea de texto
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(text = it) } },
        shape = MaterialTheme.shapes.medium,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingIcon = {
            // Decorativo: el placeholder ya describe el campo.
            Icon(imageVector = leadingIcon, contentDescription = null)
        },
        // Si es contraseña, muestra un icono interactivo al final para alternar la visibilidad del texto
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
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
        } else null,
        // Oculta los caracteres con asteriscos/puntos si es contraseña y la visibilidad está desactivada
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        // Configura el tipo de teclado y la acción del botón de envío según los parámetros
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        // Todos los colores salen del tema: el campo responde a claro/oscuro solo.
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.surface,
            unfocusedContainerColor = AppTheme.colors.surface,
            focusedBorderColor = AppTheme.colors.textPrimary,   // el foco se nota
            unfocusedBorderColor = AppTheme.colors.border,
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            cursorColor = AppTheme.colors.textPrimary,
            focusedLeadingIconColor = AppTheme.colors.textPrimary,
            unfocusedLeadingIconColor = AppTheme.colors.border,
            focusedTrailingIconColor = AppTheme.colors.textPrimary,
            unfocusedTrailingIconColor = AppTheme.colors.border,
            focusedPlaceholderColor = AppTheme.colors.stateDisabled,
            unfocusedPlaceholderColor = AppTheme.colors.stateDisabled,
            // Colores del estado de error (sin esto, Material usa los suyos por defecto)
            errorContainerColor = AppTheme.colors.surface,
            errorBorderColor = AppTheme.colors.stateError,
            errorTextColor = AppTheme.colors.textPrimary,
            errorCursorColor = AppTheme.colors.stateError,
            errorLeadingIconColor = AppTheme.colors.stateError,
            errorTrailingIconColor = AppTheme.colors.stateError,
            errorSupportingTextColor = AppTheme.colors.stateError,
            errorPlaceholderColor = AppTheme.colors.stateDisabled,
        ),
    )
}