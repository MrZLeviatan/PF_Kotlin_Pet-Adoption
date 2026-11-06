package co.edu.uniquindio.petadoptionapp.features.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.LinkText
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.auth.TwoFactorChallenge

/**
Diálogo de verificación en dos pasos (A2F) que se abre sobre la pantalla de login.
Aparece cuando LoginUseCase responde que la cuenta requiere código: el código ya se envió al correo y
el usuario debe escribirlo aquí.
Estado: recibe TwoFactorUiState (código, verificando, reenviando, error y segundos para reenviar),
que vive dentro de LoginUiState.
Eventos: onCodeChange, onVerifyClick (VerifyTwoFactorCodeUseCase), onResendClick
(ResendTwoFactorCodeUseCase) y onDismiss (cancela y cierra la sesión pendiente con LogoutUseCase),
todos manejados por LoginViewModel. Si la verificación es correcta, el ViewModel emite LoginSuccess
y LoginScreen navega al dashboard.
**/

/**
Diálogo de la verificación en dos pasos (A2F): una casilla por dígito, "Verificar código" y
"Reenviar código" con cuenta regresiva. Solo dibuja; la lógica vive en [LoginViewModel].
**/
// Marca la función como composable.
@Composable
fun TwoFactorDialog(
    // Estado actual del diálogo.
    state: TwoFactorUiState,
    // Cambio del código escrito.
    onCodeChange: (String) -> Unit,
    // Verificar el código.
    onVerifyClick: () -> Unit,
    // Pedir otro código.
    onResendClick: () -> Unit,
    // Cerrar el diálogo (cancela el inicio de sesión).
    onDismiss: () -> Unit,
) {
    // Ventana de diálogo de Compose.
    Dialog(
        // Atrás equivale a la X: cancela el inicio de sesión.
        onDismissRequest = onDismiss,
        // Tocar fuera del diálogo no lo cierra, para no cancelar el login por accidente.
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        // Columna con el contenido del diálogo.
        Column(
            modifier = Modifier
                // Ocupa todo el ancho permitido por el diálogo.
                .fillMaxWidth()
                // Fondo con esquinas redondeadas de 24 dp.
                .background(AppTheme.colors.background, RoundedCornerShape(24.dp))
                // Borde de 1 dp con la misma forma.
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(24.dp))
                // Márgenes internos.
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            // Centra horizontalmente los elementos.
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Botón X alineado a la derecha.
            CloseButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End))

            // Título del diálogo.
            Text(
                // Texto del título.
                text = stringResource(R.string.two_factor_title),
                // Tipografía de encabezado pequeño en negrita.
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                // Color principal del texto.
                color = AppTheme.colors.textPrimary,
                // Centrado.
                textAlign = TextAlign.Center,
            )
            // Espacio de 8 dp.
            Spacer(Modifier.height(8.dp))
            // Subtítulo con la cantidad de dígitos del código.
            Text(
                // Texto con la longitud del código insertada.
                text = stringResource(R.string.two_factor_subtitle, TwoFactorChallenge.CODE_LENGTH),
                // Tipografía de cuerpo con peso medio.
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                // Color principal del texto.
                color = AppTheme.colors.textPrimary,
                // Centrado.
                textAlign = TextAlign.Center,
            )

            // Espacio de 24 dp.
            Spacer(Modifier.height(24.dp))

            // Casillas del código.
            CodeField(
                // Código actual.
                code = state.code,
                // Notifica cada cambio.
                onCodeChange = onCodeChange,
                // "Listo" en el teclado verifica.
                onDone = onVerifyClick,
                // Pinta las casillas en rojo si hay error.
                isError = state.error != null,
                // Deshabilita la edición mientras se verifica.
                enabled = !state.isVerifying,
            )

            // Si hay un error, lo muestra bajo las casillas.
            state.error?.let { error ->
                // Espacio de 12 dp.
                Spacer(Modifier.height(12.dp))
                // Texto del error traducido.
                ErrorText(text = twoFactorErrorMessage(error))
            }

            // Espacio de 24 dp.
            Spacer(Modifier.height(24.dp))

            // Botón "Verificar código".
            PrimaryButton(
                // Muestra "Verificando..." mientras se procesa.
                text = stringResource(if (state.isVerifying) R.string.two_factor_verifying else R.string.two_factor_verify),
                // Acción de verificar.
                onClick = onVerifyClick,
                // Alto de 52 dp.
                modifier = Modifier.height(52.dp),
                // Solo habilitado con el código completo y sin verificación en curso.
                enabled = state.isCodeComplete && !state.isVerifying,
                // Texto de 16 sp en negrita.
                textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )

            // Espacio de 4 dp.
            Spacer(Modifier.height(4.dp))

            // Fila de reenvío (cuenta regresiva o enlace).
            ResendRow(state = state, onResendClick = onResendClick)
        }
    }
}

// X redonda arriba a la derecha, como en el diseño.
@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Botón de ícono con área táctil accesible.
    IconButton(onClick = onClick, modifier = modifier) {
        // Círculo de fondo con borde.
        Box(
            modifier = Modifier
                // Tamaño de 32 dp.
                .size(32.dp)
                // Fondo circular.
                .background(AppTheme.colors.surface, CircleShape)
                // Borde circular de 1 dp.
                .border(1.dp, AppTheme.colors.border, CircleShape),
            // Centra el ícono.
            contentAlignment = Alignment.Center,
        ) {
            // Ícono X.
            Icon(
                // Vector de cerrar.
                imageVector = Icons.Filled.Close,
                // Descripción accesible.
                contentDescription = stringResource(R.string.two_factor_close),
                // Color del ícono.
                tint = AppTheme.colors.textPrimary,
                // Tamaño de 18 dp.
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
Un solo campo de texto invisible que dibuja una casilla por dígito.
Así funcionan pegar el código, borrar y el autocompletado del teclado sin manejar el foco casilla por casilla.
**/
@Composable
private fun CodeField(
    // Código actual.
    code: String,
    // Cambio del código.
    onCodeChange: (String) -> Unit,
    // Acción "Listo" del teclado.
    onDone: () -> Unit,
    // true para pintar las casillas en estado de error.
    isError: Boolean,
    // false para bloquear la edición.
    enabled: Boolean,
) {
    // Permite pedir el foco del campo desde código.
    val focusRequester = remember { FocusRequester() }
    // Indica si el campo tiene el foco (para resaltar la casilla activa).
    var isFocused by remember { mutableStateOf(false) }
    // Descripción accesible del campo completo.
    val description = stringResource(R.string.two_factor_code_description)

    // Abre el teclado numérico al mostrar el diálogo.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Campo de texto básico sin decoración propia; las casillas se dibujan en decorationBox.
    BasicTextField(
        // Valor actual.
        value = code,
        // Notifica cada cambio (el ViewModel filtra dígitos y longitud).
        onValueChange = onCodeChange,
        // Habilita o bloquea la edición.
        enabled = enabled,
        // Una sola línea.
        singleLine = true,
        // Teclado numérico de tipo contraseña y acción "Listo".
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        // Al pulsar "Listo" ejecuta onDone.
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier
            // Asocia el solicitador de foco.
            .focusRequester(focusRequester)
            // Guarda si el campo tiene el foco.
            .onFocusChanged { isFocused = it.isFocused }
            // Expone la descripción a los lectores de pantalla.
            .semantics { contentDescription = description },
        // Dibuja las casillas en lugar del campo de texto normal.
        decorationBox = {
            // Fila con las casillas separadas 8 dp.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Crea una casilla por cada dígito del código.
                repeat(TwoFactorChallenge.CODE_LENGTH) { index ->
                    // La casilla activa es la siguiente por llenar.
                    val isCurrent = isFocused && index == code.length.coerceAtMost(TwoFactorChallenge.CODE_LENGTH - 1)
                    // Color del borde según el estado.
                    val borderColor = when {
                        // Rojo si hay error.
                        isError -> AppTheme.colors.stateError
                        // Color de enlace para la casilla activa.
                        isCurrent -> AppTheme.colors.textLink
                        // Borde normal en las demás.
                        else -> AppTheme.colors.border
                    }
                    // Casilla individual.
                    Box(
                        modifier = Modifier
                            // Reparte el ancho por igual entre las casillas.
                            .weight(1f)
                            // Ancho máximo de 48 dp.
                            .widthIn(max = 48.dp)
                            // Alto de 52 dp.
                            .height(52.dp)
                            // Borde más grueso en la casilla activa.
                            .border(if (isCurrent) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
                        // Centra el dígito.
                        contentAlignment = Alignment.Center,
                    ) {
                        // Dígito de esta posición (vacío si aún no se escribe).
                        Text(
                            text = code.getOrNull(index)?.toString().orEmpty(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
            }
        },
    )
}

// "Reenviar código en 00:30" mientras corre la espera; después, el enlace para pedir otro.
@Composable
private fun ResendRow(state: TwoFactorUiState, onResendClick: () -> Unit) {
    // Elige qué mostrar según el estado.
    when {
        // Reenvío en curso: "Reenviando...".
        state.isResending -> ResendLabel(stringResource(R.string.two_factor_resending))
        // Cuenta regresiva activa: muestra el tiempo restante en formato 00:SS.
        state.resendSeconds > 0 -> ResendLabel(
            stringResource(R.string.two_factor_resend_in, "00:%02d".format(state.resendSeconds))
        )
        // Tiempo cumplido: enlace para pedir otro código.
        else -> LinkText(text = stringResource(R.string.two_factor_resend), onClick = onResendClick)
    }
}

// Texto informativo de reenvío, con la misma altura que el enlace para que no salte el diseño.
@Composable
private fun ResendLabel(text: String) {
    // Caja de 48 dp de alto que centra el texto.
    Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.Center) {
        // Texto con color de enlace.
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textLink,
        )
    }
}

// Traduce el error del diálogo A2F a su texto.
@Composable
private fun twoFactorErrorMessage(error: TwoFactorError): String = when (error) {
    // Código incorrecto.
    TwoFactorError.WRONG_CODE -> stringResource(R.string.two_factor_error_wrong)
    // Código vencido.
    TwoFactorError.EXPIRED -> stringResource(R.string.two_factor_error_expired)
    // Demasiados intentos.
    TwoFactorError.TOO_MANY_ATTEMPTS -> stringResource(R.string.two_factor_error_too_many)
    // No se pudo enviar el código.
    TwoFactorError.NOT_SENT -> stringResource(R.string.two_factor_error_not_sent)
    // Error genérico.
    TwoFactorError.GENERIC -> stringResource(R.string.two_factor_error_generic)
}

// Vista previa con un código a medio escribir y la cuenta regresiva activa.
@Preview(name = "Esperando el código")
@Composable
private fun TwoFactorDialogPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el diálogo con datos de ejemplo.
        TwoFactorDialog(
            state = TwoFactorUiState(accountId = "u1", code = "123", resendSeconds = 30),
            onCodeChange = {}, onVerifyClick = {}, onResendClick = {}, onDismiss = {},
        )
    }
}

// Vista previa con el error de código incorrecto.
@Preview(name = "Código incorrecto")
@Composable
private fun TwoFactorDialogErrorPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el diálogo con el error visible.
        TwoFactorDialog(
            state = TwoFactorUiState(accountId = "u1", error = TwoFactorError.WRONG_CODE),
            onCodeChange = {}, onVerifyClick = {}, onResendClick = {}, onDismiss = {},
        )
    }
}
