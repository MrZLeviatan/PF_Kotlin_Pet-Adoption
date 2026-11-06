package co.edu.uniquindio.petadoptionapp.features.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.icon.OrDivider
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.LinkText
import co.edu.uniquindio.petadoptionapp.core.component.text.TextWithLink
import co.edu.uniquindio.petadoptionapp.core.component.text.WelcomeTitle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Pantalla de inicio de sesión de la aplicación.
Muestra la imagen principal, el título de bienvenida, los campos de correo y contraseña con sus
errores, el error general de autenticación, el botón de ingresar y los enlaces a "Olvidé mi
contraseña" y "Crear cuenta". Si la cuenta tiene verificación en dos pasos, encima aparece el
TwoFactorDialog para escribir el código recibido por correo.
Estado: LoginUiState del LoginViewModel (campos, errores, carga y estado del diálogo A2F).
Acciones: escribir, cambiar el foco de los campos, ingresar, verificar/reenviar el código o cerrar el diálogo.
Casos de uso (a través del ViewModel): LoginUseCase, VerifyTwoFactorCodeUseCase,
ResendTwoFactorCodeUseCase y LogoutUseCase.
Navegación: el evento LoginSuccess se convierte en onLoginSuccess (AppNavigation abre el dashboard);
onForgotPasswordClick y onRegisterClick llevan a recuperar contraseña y a registro.
**/

// Margen horizontal estándar de la pantalla.
private val ScreenHorizontalPadding = 24.dp
// Radio de redondeo de la imagen principal.
private val HeroCornerRadius = 16.dp
// Proporción ancho:alto de la imagen principal (2:1).
private const val HERO_ASPECT_RATIO = 2f / 1f

/**
Pantalla de inicio de sesión.

Conecta el [LoginViewModel] con la UI: observa el estado, reenvía las acciones
del usuario y reacciona a los eventos de un solo uso. [LoginContent] solo dibuja.
**/
// Marca la función como composable.
@Composable
fun LoginScreen(
    // Evento cuando la sesión queda abierta.
    onLoginSuccess: () -> Unit,
    // Evento para ir a recuperar la contraseña.
    onForgotPasswordClick: () -> Unit,
    // Evento para ir a la pantalla de registro.
    onRegisterClick: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: LoginViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Guarda la versión más reciente del callback sin reiniciar el efecto.
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    // Escucha los eventos de un solo uso del ViewModel.
    LaunchedEffect(viewModel) {
        // Recolecta cada evento.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Sesión iniciada: navega al dashboard.
                LoginEvent.LoginSuccess -> currentOnLoginSuccess()
            }
        }
    }

    // Dibuja el formulario conectando cada acción con el ViewModel o con la navegación.
    LoginContent(
        // Estado actual.
        uiState = uiState,
        // Cambio del correo.
        onEmailChange = viewModel::onEmailChange,
        // Cambio de la contraseña.
        onPasswordChange = viewModel::onPasswordChange,
        // Cambio de foco del correo.
        onEmailFocusChange = viewModel::onEmailFocusChange,
        // Cambio de foco de la contraseña.
        onPasswordFocusChange = viewModel::onPasswordFocusChange,
        // Botón ingresar.
        onLoginClick = viewModel::onLoginClick,
        // Enlace "Olvidé mi contraseña".
        onForgotPasswordClick = onForgotPasswordClick,
        // Enlace "Crear cuenta".
        onRegisterClick = onRegisterClick,
    )

    // Segundo paso: el código A2F que llegó al correo.
    uiState.twoFactor?.let { twoFactor ->
        // Diálogo de verificación en dos pasos.
        TwoFactorDialog(
            // Estado del diálogo.
            state = twoFactor,
            // Cambio del código escrito.
            onCodeChange = viewModel::onTwoFactorCodeChange,
            // Botón verificar.
            onVerifyClick = viewModel::onVerifyTwoFactorClick,
            // Botón reenviar código.
            onResendClick = viewModel::onResendTwoFactorClick,
            // Cerrar el diálogo (cancela el inicio de sesión).
            onDismiss = viewModel::onDismissTwoFactor,
        )
    }
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza y prueba.
@Composable
private fun LoginContent(
    // Estado a dibujar.
    uiState: LoginUiState,
    // Cambio del correo.
    onEmailChange: (String) -> Unit,
    // Cambio de la contraseña.
    onPasswordChange: (String) -> Unit,
    // Cambio de foco del correo.
    onEmailFocusChange: (Boolean) -> Unit,
    // Cambio de foco de la contraseña.
    onPasswordFocusChange: (Boolean) -> Unit,
    // Botón ingresar.
    onLoginClick: () -> Unit,
    // Enlace "Olvidé mi contraseña".
    onForgotPasswordClick: () -> Unit,
    // Enlace "Crear cuenta".
    onRegisterClick: () -> Unit,
) {
    // Columna principal de la pantalla.
    Column(
        modifier = Modifier
            // Ocupa toda la pantalla.
            .fillMaxSize()
            // Fondo según el tema.
            .background(AppTheme.colors.background)
            // Evita las barras del sistema.
            .systemBarsPadding()
            // Sube el contenido cuando aparece el teclado, para no tapar el botón.
            .imePadding()
            // Con teclado abierto el contenido no cabe: debe poder desplazarse.
            .verticalScroll(rememberScrollState())
            // Márgenes de la pantalla.
            .padding(horizontal = ScreenHorizontalPadding, vertical = 50.dp),
        // Centra horizontalmente los elementos.
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Mismo archivo que el splash: un solo recurso para ambas pantallas.
        Image(
            // Imagen principal desde los recursos locales.
            painter = painterResource(R.drawable.hero_image),
            // Descripción accesible de la imagen.
            contentDescription = stringResource(R.string.login_hero_description),
            // Recorta la imagen para llenar su espacio sin deformarse.
            contentScale = ContentScale.Crop,
            modifier = Modifier
                // Ocupa todo el ancho.
                .fillMaxWidth()
                // Mantiene la proporción 2:1.
                .aspectRatio(HERO_ASPECT_RATIO)
                // Redondea las esquinas.
                .clip(RoundedCornerShape(HeroCornerRadius)),
        )

        // Espacio de 24 dp.
        Spacer(Modifier.height(24.dp))

        // Título de bienvenida con la marca.
        WelcomeTitle()

        // Espacio de 32 dp.
        Spacer(Modifier.height(32.dp))

        // Título "Iniciar sesión".
        Text(
            // Texto del título.
            text = stringResource(R.string.login_title),
            // Tipografía de título grande.
            style = MaterialTheme.typography.titleLarge,
            // Color principal del texto.
            color = AppTheme.colors.textPrimary,
        )

        // Espacio de 24 dp.
        Spacer(Modifier.height(24.dp))

        // Campo del correo electrónico.
        AppTextField(
            // Valor actual.
            value = uiState.email,
            // Notifica cada cambio.
            onValueChange = onEmailChange,
            // onFocusChanged avisa al ViewModel cuando el usuario entra o sale del campo,
            // para mostrar el error si lo deja vacío.
            modifier = Modifier.onFocusChanged { onEmailFocusChange(it.isFocused) },
            // Etiqueta del campo.
            label = stringResource(R.string.login_email_placeholder),
            // Ícono de sobre.
            leadingIcon = Icons.Outlined.Email,
            // Mensaje de error traducido (null si no hay).
            errorMessage = emailErrorMessage(uiState.emailError),
            // Teclado para correos.
            keyboardType = KeyboardType.Email,
            // "Siguiente" salta a la contraseña.
            imeAction = ImeAction.Next,
        )

        // Espacio de 16 dp.
        Spacer(Modifier.height(16.dp))

        // Campo de la contraseña.
        AppTextField(
            // Valor actual.
            value = uiState.password,
            // Notifica cada cambio.
            onValueChange = onPasswordChange,
            // Avisa al ViewModel los cambios de foco.
            modifier = Modifier.onFocusChanged { onPasswordFocusChange(it.isFocused) },
            // Etiqueta del campo.
            label = stringResource(R.string.login_password_placeholder),
            // Ícono de candado.
            leadingIcon = Icons.Outlined.Lock,
            // Mensaje de error traducido (null si no hay).
            errorMessage = passwordErrorMessage(uiState.passwordError),
            // Oculta los caracteres.
            isPassword = true,
            // "Listo" en el último campo.
            imeAction = ImeAction.Done,
            // El ViewModel ignora la acción si el formulario no es válido.
            keyboardActions = KeyboardActions(onDone = { onLoginClick() }),
        )

        // Espacio de 30 dp.
        Spacer(Modifier.height(30.dp))

        // Muestra el error general de autenticación si existe (ErrorText usa liveRegion: TalkBack lo anuncia al aparecer).
        loginErrorMessage(uiState.loginError)?.let { message ->
            // Espacio de 12 dp.
            Spacer(Modifier.height(12.dp))
            // Texto del error.
            ErrorText(text = message)
        }

        // Botón principal "Ingresar".
        PrimaryButton(
            // Texto del botón.
            text = stringResource(R.string.login_button),
            // Acción de iniciar sesión.
            onClick = onLoginClick,
            // Alto y texto propios del diseño de esta pantalla (52 dp, 16 sp en negrita).
            modifier = Modifier.height(52.dp),
            // Habilitado solo con ambos campos válidos y sin una petición en curso.
            enabled = uiState.isFormValid && !uiState.isLoading,
            // Estilo del texto: labelLarge a 16 sp en negrita.
            textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        )

        // Espacio de 15 dp.
        Spacer(Modifier.height(15.dp))


        // Enlace "¿Olvidaste tu contraseña?".
        LinkText(
            // Texto del enlace.
            text = stringResource(R.string.login_forgot_password),

            // Navega a recuperar contraseña.
            onClick = onForgotPasswordClick,
        )

        // Espacio de 15 dp.
        Spacer(Modifier.height(15.dp))

        // Separador con el texto "o".
        OrDivider(
            // Texto del separador.
            text = stringResource(R.string.login_divider_or)
        )

        // Texto "¿No tienes cuenta?" con enlace para crearla.
        TextWithLink(
            // Texto fijo.
            text = stringResource(R.string.login_no_account),
            // Texto del enlace.
            linkText = stringResource(R.string.login_create_account),
            // Navega a la pantalla de registro.
            onLinkClick = onRegisterClick,
        )
    }
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
// Viven aquí y no en el ViewModel porque stringResource necesita el contexto de Compose.
@Composable
private fun emailErrorMessage(error: EmailError?): String? = when (error) {
    // Sin error: no se muestra texto.
    null -> null
    // Correo vacío.
    EmailError.EMPTY -> stringResource(R.string.login_error_email_empty)
    // Correo con formato inválido.
    EmailError.INVALID_FORMAT -> stringResource(R.string.login_error_email_invalid)
}

// Traduce el error general de autenticación a su texto.
@Composable
private fun loginErrorMessage(error: LoginError?): String? = when (error) {
    // Sin error.
    null -> null
    // Correo inválido.
    LoginError.INVALID_EMAIL -> stringResource(R.string.login_error_email_invalid)
    // Credenciales incorrectas.
    LoginError.INVALID_CREDENTIALS -> stringResource(R.string.login_error_invalid_credentials)
    // Moderador inactivo.
    LoginError.INACTIVE_MODERATOR -> stringResource(R.string.login_error_inactive_moderator)
    // Cuenta eliminada.
    LoginError.ACCOUNT_DELETED -> stringResource(R.string.login_error_account_deleted)
    // No se pudo enviar el código A2F.
    LoginError.CODE_NOT_SENT -> stringResource(R.string.login_error_code_not_sent)
    // Error genérico.
    LoginError.GENERIC -> stringResource(R.string.login_error_generic)
}

// Traduce el error del campo de contraseña a su texto.
@Composable
private fun passwordErrorMessage(error: PasswordError?): String? = when (error) {
    // Sin error.
    null -> null
    // Contraseña vacía.
    PasswordError.EMPTY -> stringResource(R.string.login_error_password_empty)
}

// Vista previa con el formulario vacío y sin errores.
@Preview(name = "Sin errores", showBackground = true, heightDp = 1200)
@Composable
private fun LoginContentPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con el estado inicial y callbacks vacíos.
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onEmailFocusChange = {},
            onPasswordFocusChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
        )
    }
}

// Vista previa con errores en ambos campos.
@Preview(name = "Con errores", showBackground = true, heightDp = 1200)
@Composable
private fun LoginContentErrorPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con los errores de campo vacío.
        LoginContent(
            uiState = LoginUiState(
                emailError = EmailError.EMPTY,
                passwordError = PasswordError.EMPTY,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onEmailFocusChange = {},
            onPasswordFocusChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
        )
    }
}

// Vista previa con un formulario válido (botón habilitado).
@Preview(name = "Formulario válido", showBackground = true, heightDp = 1200)
@Composable
private fun LoginContentValidPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con datos de ejemplo válidos.
        LoginContent(
            uiState = LoginUiState(email = "ana@correo.com", password = "123456"),
            onEmailChange = {},
            onPasswordChange = {},
            onEmailFocusChange = {},
            onPasswordFocusChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
        )
    }
}
