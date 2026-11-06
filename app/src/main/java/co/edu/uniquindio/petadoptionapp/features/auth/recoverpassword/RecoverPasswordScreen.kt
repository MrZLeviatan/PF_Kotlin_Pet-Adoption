package co.edu.uniquindio.petadoptionapp.features.auth.recoverpassword

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.BackButton
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppInfoDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.header.IconHeader
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Pantalla "Olvidé mi contraseña".
Muestra un botón de volver, un encabezado con ícono, un campo para el correo, el error (si lo hay)
y el botón de enviar; al terminar muestra un diálogo de confirmación.
Estado: RecoverPasswordUiState del RecoverPasswordViewModel. Acciones: escribir el correo, enviar y
aceptar el diálogo. El ViewModel usa SendPasswordResetUseCase para pedir el enlace a Firebase.
Navegación: onBack vuelve atrás y el evento EmailSent se convierte en onEmailSent, con el que
AppNavigation regresa al login.
**/

// Margen horizontal estándar de la pantalla.
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla para pedir el enlace de recuperación de contraseña.
Conecta el [RecoverPasswordViewModel] con la UI; [RecoverPasswordContent] solo dibuja.
**/
// Marca la función como composable.
@Composable
fun RecoverPasswordScreen(
    // Evento para volver a la pantalla anterior.
    onBack: () -> Unit,
    // Evento que se dispara cuando el correo ya se envió y el usuario aceptó.
    onEmailSent: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: RecoverPasswordViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Guarda la versión más reciente del callback sin reiniciar el efecto.
    val currentOnEmailSent by rememberUpdatedState(onEmailSent)

    // Escucha los eventos de un solo uso del ViewModel.
    LaunchedEffect(viewModel) {
        // Recolecta cada evento.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Correo enviado: navega fuera de la pantalla.
                RecoverPasswordEvent.EmailSent -> currentOnEmailSent()
            }
        }
    }

    // Dibuja el contenido conectando las acciones con el ViewModel.
    RecoverPasswordContent(
        // Estado actual.
        uiState = uiState,
        // Cambio del texto del correo.
        onEmailChange = viewModel::onEmailChange,
        // Botón enviar.
        onSendClick = viewModel::onSendClick,
        // Aceptar el diálogo de éxito.
        onDialogAccept = viewModel::onDialogAccept,
        // Volver atrás.
        onBack = onBack,
    )
}

// Solo dibuja: recibe el estado y los callbacks. Es lo que se previsualiza.
@Composable
private fun RecoverPasswordContent(
    // Estado a dibujar.
    uiState: RecoverPasswordUiState,
    // Cambio del correo.
    onEmailChange: (String) -> Unit,
    // Enviar solicitud.
    onSendClick: () -> Unit,
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
            title = stringResource(R.string.recover_title),
            subtitle = stringResource(R.string.recover_subtitle),
        )

        // Espacio de 32 dp.
        Spacer(Modifier.height(32.dp))

        // Campo de texto para el correo.
        AppTextField(
            // Valor actual del correo.
            value = uiState.email,
            // Notifica cada cambio.
            onValueChange = onEmailChange,
            // Etiqueta del campo.
            label = stringResource(R.string.recover_email_label),
            // Ícono de sobre al inicio.
            leadingIcon = Icons.Outlined.Email,
            // Teclado optimizado para correos.
            keyboardType = KeyboardType.Email,
            // La tecla de acción del teclado es "Listo".
            imeAction = ImeAction.Done,
            // Al pulsar "Listo" envía si el campo tiene contenido.
            keyboardActions = KeyboardActions(
                onDone = { if (uiState.isEmailFilled) onSendClick() }
            ),
        )

        // Si hay un error, lo muestra bajo el campo.
        uiState.error?.let { error ->
            // Espacio de 12 dp.
            Spacer(Modifier.height(12.dp))
            // Texto de error.
            ErrorText(
                // Traduce el error de UI al texto correspondiente.
                text = stringResource(
                    when (error) {
                        // Correo con formato inválido.
                        RecoverPasswordError.INVALID_EMAIL -> R.string.login_error_email_invalid
                        // Error genérico.
                        RecoverPasswordError.GENERIC -> R.string.recover_error_generic
                    }
                )
            )
        }

        // Espacio de 32 dp.
        Spacer(Modifier.height(32.dp))

        // Botón principal de envío.
        PrimaryButton(
            // Muestra "Enviando..." mientras se procesa.
            text = stringResource(if (uiState.isSending) R.string.recover_sending else R.string.recover_send_button),
            // Acción de enviar.
            onClick = onSendClick,
            // Solo habilitado con correo escrito y sin envío en curso.
            enabled = uiState.isEmailFilled && !uiState.isSending,
        )
    }

    // Muestra el diálogo de éxito cuando el estado lo indica.
    if (uiState.showSuccessDialog) {
        // Diálogo informativo de correo enviado.
        AppInfoDialog(
            // Título del diálogo.
            title = stringResource(R.string.recover_success_title),
            // Mensaje del diálogo.
            message = stringResource(R.string.recover_success_message),
            // Texto del botón de confirmar.
            confirmText = stringResource(R.string.recover_success_button),
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
private fun RecoverPasswordEmptyPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con el estado inicial.
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(),
            onEmailChange = {}, onSendClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

// Vista previa con un correo escrito.
@Preview(name = "Con correo", showBackground = true, heightDp = 800)
@Composable
private fun RecoverPasswordFilledPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con un correo de ejemplo.
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(email = "nicolas.cabreras@uqvirtual.edu.co"),
            onEmailChange = {}, onSendClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}

// Vista previa con el diálogo de éxito abierto.
@Preview(name = "Correo enviado", showBackground = true, heightDp = 800)
@Composable
private fun RecoverPasswordSuccessPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con el diálogo visible.
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(
                email = "nicolas.cabreras@uqvirtual.edu.co",
                showSuccessDialog = true,
            ),
            onEmailChange = {}, onSendClick = {}, onDialogAccept = {}, onBack = {},
        )
    }
}
