package co.edu.uniquindio.petadoptionapp.features.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.icon.OrDivider
import co.edu.uniquindio.petadoptionapp.core.component.text.LinkText
import co.edu.uniquindio.petadoptionapp.core.component.text.WelcomeTitle
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 24.dp
private val HeroCornerRadius = 24.dp
private const val HERO_ASPECT_RATIO = 3f / 2f

/**
 * Pantalla de inicio de sesión.
 *
 * Conecta el [LoginViewModel] con la UI: observa el estado, reenvía las acciones
 * del usuario y reacciona a los eventos de un solo uso. [LoginContent] solo dibuja.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    // Se detiene la observación cuando la app pasa a segundo plano.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Siempre apunta al callback más reciente sin reiniciar la colección.
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.LoginSuccess -> currentOnLoginSuccess()
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onEmailFocusChange = viewModel::onEmailFocusChange,
        onPasswordFocusChange = viewModel::onPasswordFocusChange,
        onLoginClick = viewModel::onLoginClick,
        onForgotPasswordClick = onForgotPasswordClick,
        onRegisterClick = onRegisterClick,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza y prueba.
@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailFocusChange: (Boolean) -> Unit,
    onPasswordFocusChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
            // Sube el contenido cuando aparece el teclado, para no tapar el botón.
            .imePadding()
            // Con teclado abierto el contenido no cabe: debe poder desplazarse.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Mismo archivo que el splash: un solo recurso para ambas pantallas.
        Image(
            painter = painterResource(R.drawable.hero_image),
            contentDescription = stringResource(R.string.login_hero_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(HERO_ASPECT_RATIO)
                .clip(RoundedCornerShape(HeroCornerRadius)),
        )

        Spacer(Modifier.height(24.dp))

        WelcomeTitle()

        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
        )

        Spacer(Modifier.height(24.dp))

        AppTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            // onFocusChanged avisa al ViewModel cuando el usuario entra o sale del campo,
            // para mostrar el error si lo deja vacío.
            modifier = Modifier.onFocusChanged { onEmailFocusChange(it.isFocused) },
            placeholder = stringResource(R.string.login_email_placeholder),
            leadingIcon = Icons.Outlined.Email,
            errorMessage = emailErrorMessage(uiState.emailError),
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,    // "Siguiente" salta a la contraseña
        )

        Spacer(Modifier.height(16.dp))

        AppTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.onFocusChanged { onPasswordFocusChange(it.isFocused) },
            placeholder = stringResource(R.string.login_password_placeholder),
            leadingIcon = Icons.Outlined.Lock,
            errorMessage = passwordErrorMessage(uiState.passwordError),
            isPassword = true,
            imeAction = ImeAction.Done,    // "Listo" en el último campo
            // El ViewModel ignora la acción si el formulario no es válido.
            keyboardActions = KeyboardActions(onDone = { onLoginClick() }),
        )

        Spacer(Modifier.height(30.dp))

        PrimaryButton(
            text = stringResource(R.string.login_button),
            onClick = onLoginClick,
            // Habilitado solo con ambos campos válidos y sin una petición en curso.
            enabled = uiState.isFormValid && !uiState.isLoading,
        )

        Spacer(Modifier.height(15.dp))


        LinkText(
            text = stringResource(R.string.login_forgot_password),

            onClick = onForgotPasswordClick,
        )

        Spacer(Modifier.height(15.dp))

        OrDivider(
            text = stringResource(R.string.login_divider_or)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Text(
                text = stringResource(R.string.login_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
            )
            LinkText(
                text = stringResource(R.string.login_create_account),
                onClick = onRegisterClick,
            )
        }
    }
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
// Viven aquí y no en el ViewModel porque stringResource necesita el contexto de Compose.
@Composable
private fun emailErrorMessage(error: EmailError?): String? = when (error) {
    null -> null
    EmailError.EMPTY -> stringResource(R.string.login_error_email_empty)
    EmailError.INVALID_FORMAT -> stringResource(R.string.login_error_email_invalid)
}

@Composable
private fun passwordErrorMessage(error: PasswordError?): String? = when (error) {
    null -> null
    PasswordError.EMPTY -> stringResource(R.string.login_error_password_empty)
}

@Preview(name = "Sin errores", showBackground = true, heightDp = 1200)
@Composable
private fun LoginContentPreview() {
    PetAdoptionAppTheme {
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

@Preview(name = "Con errores", showBackground = true, heightDp = 1200)
@Composable
private fun LoginContentErrorPreview() {
    PetAdoptionAppTheme {
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

@Preview(name = "Formulario válido", showBackground = true, heightDp = 1200)
@Composable
private fun LoginContentValidPreview() {
    PetAdoptionAppTheme {
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