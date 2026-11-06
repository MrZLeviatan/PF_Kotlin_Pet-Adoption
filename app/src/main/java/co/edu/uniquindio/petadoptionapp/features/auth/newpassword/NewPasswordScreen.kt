package co.edu.uniquindio.petadoptionapp.features.auth.newpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.BackButton
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppInfoDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.header.IconHeader
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Pantalla para definir una nueva contraseña.
Muestra un botón de volver, un encabezado con ícono, dos campos (contraseña y confirmación) y el
botón de confirmar; al terminar aparece un diálogo de éxito.
Estado: NewPasswordUiState del NewPasswordViewModel. Acciones: escribir la contraseña, escribir la
confirmación, confirmar y aceptar el diálogo. Actualmente no invoca casos de uso (flujo simulado).
Navegación: onBack vuelve atrás y el evento PasswordUpdated se convierte en onPasswordUpdated, con el
que AppNavigation lleva al login.
**/

// Margen horizontal estándar de la pantalla.
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla para definir la nueva contraseña (se llega desde el enlace del correo).
Conecta el [NewPasswordViewModel] con la UI; [NewPasswordContent] solo dibuja.
**/
// Marca la función como composable.
@Composable
fun NewPasswordScreen(
    // Evento para volver atrás.
    onBack: () -> Unit,
    // Evento cuando la contraseña quedó actualizada.
    onPasswordUpdated: () -> Unit,
    // ViewModel creado con viewModel() (no usa Hilt porque no tiene dependencias).
    viewModel: NewPasswordViewModel = viewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Guarda la versión más reciente del callback sin reiniciar el efecto.
    val currentOnPasswordUpdated by rememberUpdatedState(onPasswordUpdated)

    // Escucha los eventos de un solo uso del ViewModel.
    LaunchedEffect(viewModel) {
        // Recolecta cada evento.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Contraseña actualizada: navega fuera de la pantalla.
                NewPasswordEvent.PasswordUpdated -> currentOnPasswordUpdated()
            }
        }
    }

    // Dibuja el contenido conectando las acciones con el ViewModel.
    NewPasswordContent(
        // Estado actual.
        uiState = uiState,
        // Cambio de la contraseña.
        onPasswordChange = viewModel::onPasswordChange,
        // Cambio de la confirmación.
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        // Botón confirmar.
        onConfirmClick = viewModel::onConfirmClick,
        // Aceptar el diálogo de éxito.
        onDialogAccept = viewModel::onDialogAccept,
        // Volver atrás.
        onBack = onBack,
    )
}

// Solo dibuja: recibe el estado y los callbacks. Es lo que se previsualiza.
@Composable
private fun NewPasswordContent(
    // Estado a dibujar.
    uiState: NewPasswordUiState,
    // Cambio de la contraseña.
    onPasswordChange: (String) -> Unit,
    // Cambio de la confirmación.
    onConfirmPasswordChange: (String) -> Unit,
    // Confirmar.
    onConfirmClick: () -> Unit,
    // Aceptar diálogo.
    onDialogAccept: () -> Unit,
    // Volver atrás.
    onBack: () -> Unit,
) {
    // Columna principal del formulario.
    Column(
        modifier = Modifier
            // Ocupa toda la pantalla.
            .fillMaxSize()
            // Fondo según el tema.
            .background(AppTheme.colors.background)
            // Evita las barras del sistema.
            .systemBarsPadding()
            // Sube el contenido cuando aparece el teclado.
            .imePadding()
            // Permite desplazarse si no cabe.
            .verticalScroll(rememberScrollState())
            // Márgenes de la pantalla.
            .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        // Centra horizontalmente los elementos.
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Botón de volver alineado a la izquierda.
        BackButton(onClick = onBack, modifier = Modifier.align(Alignment.Start))

        // Espacio de 16 dp.
        Spacer(Modifier.height(16.dp))

        // Encabezado con ícono de seguridad, título y subtítulo.
        IconHeader(
            icon = Icons.Outlined.Security,
            title = stringResource(R.string.new_password_title),
            subtitle = stringResource(R.string.new_password_subtitle),
        )

        // Espacio de 32 dp.
        Spacer(Modifier.height(32.dp))

        // Columna con los dos campos separados 12 dp.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Campo de la nueva contraseña.
            AppTextField(
                // Valor actual.
                value = uiState.password,
                // Notifica cada cambio.
                onValueChange = onPasswordChange,
                // Etiqueta del campo.
                label = stringResource(R.string.new_password_label),
                // Ícono de candado.
                leadingIcon = Icons.Outlined.Lock,
                // Oculta los caracteres.
                isPassword = true,
            )
            // Campo de confirmación de la contraseña.
            AppTextField(
                // Valor actual.
                value = uiState.confirmPassword,
                // Notifica cada cambio.
                onValueChange = onConfirmPasswordChange,
                // Etiqueta del campo.
                label = stringResource(R.string.new_password_confirm_label),
                // Ícono de candado.
                leadingIcon = Icons.Outlined.Lock,
                // Oculta los caracteres.
                isPassword = true,
                // La tecla de acción del teclado es "Listo".
                imeAction = ImeAction.Done,
                // Al pulsar "Listo" confirma si el formulario está lleno.
                keyboardActions = KeyboardActions(
                    onDone = { if (uiState.isFormFilled) onConfirmClick() }
                ),
            )
        }

        // Espacio de 32 dp.
        Spacer(Modifier.height(32.dp))

        // Botón principal de confirmar.
        PrimaryButton(
            // Texto del botón.
            text = stringResource(R.string.new_password_button),
            // Acción de confirmar.
            onClick = onConfirmClick,
            // Solo habilitado si ambos campos tienen contenido.
            enabled = uiState.isFormFilled,
        )
    }

    // Muestra el diálogo de éxito cuando el estado lo indica.
    if (uiState.showSuccessDialog) {
        // Diálogo informativo de contraseña actualizada.
        AppInfoDialog(
            // Título del diálogo.
            title = stringResource(R.string.new_password_success_title),
            // Mensaje del diálogo.
            message = stringResource(R.string.new_password_success_message),
            // Texto del botón de confirmar.
            confirmText = stringResource(R.string.new_password_success_button),
            // Acción al confirmar.
            onConfirm = onDialogAccept,
            // Ícono circular de seguridad en la parte superior.
            icon = {
                IconCircle(
                    icon = Icons.Outlined.Security,
                    size = 56.dp,
                    containerColor = AppTheme.colors.surfaceTag,
                )
            },
        )
    }
}

// Vista previa con el formulario vacío.
@Preview(name = "Vacía", showBackground = true, heightDp = 800)
@Composable
private fun NewPasswordEmptyPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con el estado inicial.
        NewPasswordContent(
            uiState = NewPasswordUiState(),
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onConfirmClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

// Vista previa con el formulario lleno.
@Preview(name = "Llena", showBackground = true, heightDp = 800)
@Composable
private fun NewPasswordFilledPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con contraseñas de ejemplo.
        NewPasswordContent(
            uiState = NewPasswordUiState(password = "12345678", confirmPassword = "12345678"),
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onConfirmClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

// Vista previa con el diálogo de éxito abierto.
@Preview(name = "Contraseña actualizada", showBackground = true, heightDp = 800)
@Composable
private fun NewPasswordSuccessPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con el diálogo visible.
        NewPasswordContent(
            uiState = NewPasswordUiState(
                password = "12345678",
                confirmPassword = "12345678",
                showSuccessDialog = true,
            ),
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onConfirmClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}
