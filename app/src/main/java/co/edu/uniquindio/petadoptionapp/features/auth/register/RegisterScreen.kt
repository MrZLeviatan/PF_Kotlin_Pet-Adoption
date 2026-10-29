package co.edu.uniquindio.petadoptionapp.features.auth.register

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.BackButton
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.card.LocationMapPlaceholder
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppInfoDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.form.PasswordStrengthBar
import co.edu.uniquindio.petadoptionapp.core.component.form.TermsCheckbox
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StepIndicator
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.PawTitle
import co.edu.uniquindio.petadoptionapp.core.component.text.TextWithLink
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 24.dp
private val FieldSpacing = 12.dp

/**
Pantalla de registro en dos fases (datos personales y ubicación).
Conecta el [RegisterViewModel] con la UI; [RegisterContent] solo dibuja.
 */
@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onTermsClick: () -> Unit,
    onPoliciesClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnRegisterSuccess by rememberUpdatedState(onRegisterSuccess)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.RegisterSuccess -> currentOnRegisterSuccess()
            }
        }
    }

    // En la fase 2, "atrás" vuelve a la fase 1 en vez de salir del registro.
    BackHandler(enabled = uiState.step > 1) { viewModel.onPreviousStep() }

    RegisterContent(
        uiState = uiState,
        actions = RegisterActions(
            onFullNameChange = viewModel::onFullNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPhoneChange = viewModel::onPhoneChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onFocusChange = viewModel::onFocusChange,
            onCityChange = viewModel::onCityChange,
            onAddressChange = viewModel::onAddressChange,
            onTermsChange = viewModel::onTermsChange,
            onNextClick = viewModel::onNextStep,
            onPreviousStep = viewModel::onPreviousStep,
            onSetLocationClick = viewModel::onSetLocationClick,
            onCreateAccountClick = viewModel::onCreateAccountClick,
            onSuccessDialogAccept = viewModel::onSuccessDialogAccept,
            onLoginClick = onBackToLogin,
            onTermsClick = onTermsClick,
            onPoliciesClick = onPoliciesClick,
        ),
    )
}

// Agrupa las acciones para no arrastrar 17 parámetros por cada composable.
private data class RegisterActions(
    val onFullNameChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onPhoneChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onConfirmPasswordChange: (String) -> Unit = {},
    // Avisa al ViewModel cuándo un campo gana o pierde el foco, para mostrar su error.
    val onFocusChange: (RegisterField, Boolean) -> Unit = { _, _ -> },
    val onCityChange: (String) -> Unit = {},
    val onAddressChange: (String) -> Unit = {},
    val onTermsChange: (Boolean) -> Unit = {},
    val onNextClick: () -> Unit = {},
    val onPreviousStep: () -> Unit = {},
    val onSetLocationClick: () -> Unit = {},
    val onCreateAccountClick: () -> Unit = {},
    val onSuccessDialogAccept: () -> Unit = {},
    val onLoginClick: () -> Unit = {},
    val onTermsClick: () -> Unit = {},
    val onPoliciesClick: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    actions: RegisterActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Altura reservada también en la fase 1 (sin flecha) para que el encabezado no salte.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (uiState.step > 1) {
                BackButton(onClick = actions.onPreviousStep)
            }
        }

        Spacer(Modifier.height(8.dp))
        // Logo (huella) y título de la pantalla.
        PawTitle(text = stringResource(R.string.register_title))
        Spacer(Modifier.height(16.dp))
        StepIndicator(currentStep = uiState.step, totalSteps = RegisterUiState.TOTAL_STEPS)
        Spacer(Modifier.height(24.dp))

        when (uiState.step) {
            1 -> PersonalDataStep(uiState, actions)
            else -> LocationStep(uiState, actions)
        }
    }

    if (uiState.showSuccessDialog) {
        AppInfoDialog(
            title = stringResource(R.string.register_success_title),
            message = stringResource(R.string.register_success_message),
            confirmText = stringResource(R.string.register_success_button),
            onConfirm = actions.onSuccessDialogAccept,
        )
    }
}


// Fase 1: datos personales.
@Composable
private fun PersonalDataStep(uiState: RegisterUiState, actions: RegisterActions) {
    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        AppTextField(
            value = uiState.fullName,
            onValueChange = actions.onFullNameChange,
            // onFocusChanged avisa al ViewModel cuando el usuario entra o sale del campo,
            // para mostrar el error si lo deja vacío.
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.NAME, it.isFocused)
            },
            label = stringResource(R.string.register_name_label),
            leadingIcon = Icons.Outlined.Person,
            errorMessage = nameErrorMessage(uiState.nameError),
        )
        AppTextField(
            value = uiState.email,
            onValueChange = actions.onEmailChange,
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.EMAIL, it.isFocused)
            },
            label = stringResource(R.string.register_email_label),
            leadingIcon = Icons.Outlined.Email,
            errorMessage = emailErrorMessage(uiState.emailError),
            keyboardType = KeyboardType.Email,
        )
        AppTextField(
            value = uiState.phone,
            onValueChange = actions.onPhoneChange,
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.PHONE, it.isFocused)
            },
            label = stringResource(R.string.register_phone_label),
            leadingIcon = Icons.Outlined.Phone,
            errorMessage = phoneErrorMessage(uiState.phoneError),
            keyboardType = KeyboardType.Phone,
        )
        AppTextField(
            value = uiState.password,
            onValueChange = actions.onPasswordChange,
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.PASSWORD, it.isFocused)
            },
            label = stringResource(R.string.register_password_label),
            leadingIcon = Icons.Outlined.Lock,
            errorMessage = passwordErrorMessage(uiState.passwordError),
            isPassword = true,
        )
        // Se calcula en el estado: aparece apenas hay algo escrito en la contraseña.
        uiState.passwordStrength?.let { PasswordStrengthBar(strength = it) }
        AppTextField(
            value = uiState.confirmPassword,
            onValueChange = actions.onConfirmPasswordChange,
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.CONFIRM_PASSWORD, it.isFocused)
            },
            label = stringResource(R.string.register_confirm_password_label),
            leadingIcon = Icons.Outlined.Lock,
            errorMessage = confirmPasswordErrorMessage(uiState.confirmPasswordError),
            isPassword = true,
            imeAction = ImeAction.Done,
            // El ViewModel vuelve a validar: la acción se ignora si el formulario no es válido.
            keyboardActions = KeyboardActions(onDone = { actions.onNextClick() }),
        )
    }

    Spacer(Modifier.height(32.dp))

    // Correo o teléfono ya usados por otra cuenta (se revisa al tocar "Siguiente").
    if (uiState.step == 1) {
        registerErrorMessage(uiState.registerError)?.let { message ->
            ErrorText(text = message, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
    }

    PrimaryButton(
        text = stringResource(R.string.register_next_button),
        onClick = actions.onNextClick,
        // Habilitado solo con todos los campos válidos (y mientras no se esté revisando).
        enabled = uiState.isPersonalDataValid && !uiState.isCheckingAvailability,
    )

    Spacer(Modifier.height(16.dp))

    TextWithLink(
        text = stringResource(R.string.register_have_account),
        linkText = stringResource(R.string.register_login_link),
        onLinkClick = actions.onLoginClick,
    )
}

// Fase 2: ubicación y términos.
@Composable
private fun LocationStep(uiState: RegisterUiState, actions: RegisterActions) {
    Text(
        text = stringResource(R.string.register_location_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(20.dp))

    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        AppTextField(
            value = uiState.city,
            onValueChange = actions.onCityChange,
            label = stringResource(R.string.register_city_label),
            leadingIcon = Icons.Outlined.Public,
        )
        AppTextField(
            value = uiState.address,
            onValueChange = actions.onAddressChange,
            label = stringResource(R.string.register_address_label),
            leadingIcon = Icons.Outlined.LocationOn,
            imeAction = ImeAction.Done,
        )
        LocationMapPlaceholder(onSetLocationClick = actions.onSetLocationClick)
    }

    Spacer(Modifier.height(24.dp))

    TermsCheckbox(
        checked = uiState.termsAccepted,
        onCheckedChange = actions.onTermsChange,
        onTermsClick = actions.onTermsClick,
        onPoliciesClick = actions.onPoliciesClick,
    )

    Spacer(Modifier.height(16.dp))

    // Error del intento de registro. liveRegion: TalkBack lo anuncia solo al aparecer.
    registerErrorMessage(uiState.registerError)?.let { message ->
        ErrorText(text = message, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
    }

    PrimaryButton(
        text = stringResource(R.string.register_create_button),
        onClick = actions.onCreateAccountClick,
        // Deshabilitado mientras hay una petición en curso.
        enabled = uiState.isLocationDataFilled && !uiState.isLoading,
    )
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
// Viven aquí y no en el ViewModel porque stringResource necesita el contexto de Compose.
@Composable
private fun nameErrorMessage(error: FieldError?): String? = when (error) {
    null -> null
    FieldError.EMPTY -> stringResource(R.string.register_error_name_empty)
    else -> stringResource(R.string.register_error_name_invalid)
}

@Composable
private fun emailErrorMessage(error: FieldError?): String? = when (error) {
    null -> null
    FieldError.EMPTY -> stringResource(R.string.register_error_email_empty)
    FieldError.TAKEN -> stringResource(R.string.register_error_email_taken)
    else -> stringResource(R.string.register_error_email_invalid)
}

@Composable
private fun phoneErrorMessage(error: FieldError?): String? = when (error) {
    null -> null
    FieldError.EMPTY -> stringResource(R.string.register_error_phone_empty)
    FieldError.TAKEN -> stringResource(R.string.register_error_phone_taken)
    else -> stringResource(R.string.register_error_phone_invalid)
}

@Composable
private fun passwordErrorMessage(error: FieldError?): String? = when (error) {
    null -> null
    FieldError.EMPTY -> stringResource(R.string.register_error_password_empty)
    else -> stringResource(R.string.register_error_password_weak)
}

@Composable
private fun confirmPasswordErrorMessage(error: FieldError?): String? = when (error) {
    null -> null
    FieldError.EMPTY -> stringResource(R.string.register_error_confirm_empty)
    else -> stringResource(R.string.register_error_password_mismatch)
}

@Composable
private fun registerErrorMessage(error: RegisterError?): String? = when (error) {
    null -> null
    RegisterError.LOCATION -> stringResource(R.string.register_error_location)
    RegisterError.TAKEN -> stringResource(R.string.register_error_taken)
    RegisterError.GENERIC -> stringResource(R.string.register_error_generic)
}

@Preview(name = "Fase 1 vacía", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep1Preview() {
    PetAdoptionAppTheme {
        RegisterContent(uiState = RegisterUiState(), actions = RegisterActions())
    }
}

@Preview(name = "Fase 1 llena", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep1FilledPreview() {
    PetAdoptionAppTheme {
        RegisterContent(
            uiState = RegisterUiState(
                fullName = "Nicolás Cabrera Serrano",
                email = "nicolas@correo.com",
                phone = "3064819209",
                // Con esta contraseña la barra de seguridad sale en "Media".
                password = "Secreta123",
                confirmPassword = "Secreta123",
            ),
            actions = RegisterActions(),
        )
    }
}

@Preview(name = "Fase 1 campos vacíos", showBackground = true, heightDp = 1100)
@Composable
private fun RegisterStep1EmptyErrorsPreview() {
    PetAdoptionAppTheme {
        RegisterContent(
            uiState = RegisterUiState(
                nameError = FieldError.EMPTY,
                emailError = FieldError.EMPTY,
                phoneError = FieldError.EMPTY,
                passwordError = FieldError.EMPTY,
                confirmPasswordError = FieldError.EMPTY,
            ),
            actions = RegisterActions(),
        )
    }
}

@Preview(name = "Fase 1 formato inválido", showBackground = true, heightDp = 1100)
@Composable
private fun RegisterStep1InvalidErrorsPreview() {
    PetAdoptionAppTheme {
        RegisterContent(
            uiState = RegisterUiState(
                fullName = "A",
                email = "ana@",
                phone = "123",
                password = "abc",
                confirmPassword = "xyz",
                nameError = FieldError.INVALID,
                emailError = FieldError.INVALID,
                phoneError = FieldError.INVALID,
                passwordError = FieldError.WEAK,
                confirmPasswordError = FieldError.MISMATCH,
            ),
            actions = RegisterActions(),
        )
    }
}

@Preview(name = "Fase 2", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep2Preview() {
    PetAdoptionAppTheme {
        RegisterContent(
            uiState = RegisterUiState(
                step = 2,
                city = "Bogotá D.C",
                address = "Calle 20 Norte",
                termsAccepted = true,
            ),
            actions = RegisterActions(),
        )
    }
}

@Preview(name = "Fase 2 con error", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep2ErrorPreview() {
    PetAdoptionAppTheme {
        RegisterContent(
            uiState = RegisterUiState(
                step = 2,
                city = "Bogotá D.C",
                address = "Calle 20 Norte",
                termsAccepted = true,
                registerError = RegisterError.GENERIC,
            ),
            actions = RegisterActions(),
        )
    }
}

@Preview(name = "Confirmación", showBackground = true, heightDp = 900)
@Composable
private fun RegisterSuccessPreview() {
    PetAdoptionAppTheme {
        RegisterContent(
            uiState = RegisterUiState(step = 2, showSuccessDialog = true),
            actions = RegisterActions(),
        )
    }
}