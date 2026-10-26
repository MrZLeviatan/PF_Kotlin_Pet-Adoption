package co.edu.uniquindio.petadoptionapp.features.register

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.card.LocationMapPlaceholder
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppInfoDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.form.PasswordStrength
import co.edu.uniquindio.petadoptionapp.core.component.form.PasswordStrengthBar
import co.edu.uniquindio.petadoptionapp.core.component.form.TermsCheckbox
import co.edu.uniquindio.petadoptionapp.core.component.icon.PawCircle
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StepIndicator
import co.edu.uniquindio.petadoptionapp.core.component.text.LinkText
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
    viewModel: RegisterViewModel = viewModel(),
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

// Agrupa las acciones para no arrastrar 16 parámetros por cada composable.
private data class RegisterActions(
    val onFullNameChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onPhoneChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onConfirmPasswordChange: (String) -> Unit = {},
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
                IconButton(onClick = actions.onPreviousStep) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = AppTheme.colors.textPrimary,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        RegisterHeader()
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

// Logo (huella) y título de la pantalla.
@Composable
private fun RegisterHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PawCircle(size = 40.dp, containerColor = AppTheme.colors.surfaceTag)
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.colors.textPrimary,
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
            label = stringResource(R.string.register_name_label),
            leadingIcon = Icons.Outlined.Person,
        )
        AppTextField(
            value = uiState.email,
            onValueChange = actions.onEmailChange,
            label = stringResource(R.string.register_email_label),
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
        )
        AppTextField(
            value = uiState.phone,
            onValueChange = actions.onPhoneChange,
            label = stringResource(R.string.register_phone_label),
            leadingIcon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone,
        )
        AppTextField(
            value = uiState.password,
            onValueChange = actions.onPasswordChange,
            label = stringResource(R.string.register_password_label),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
        )
        // Solo aparece cuando hay un nivel calculado.
        uiState.passwordStrength?.let { PasswordStrengthBar(strength = it) }
        AppTextField(
            value = uiState.confirmPassword,
            onValueChange = actions.onConfirmPasswordChange,
            label = stringResource(R.string.register_confirm_password_label),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { if (uiState.isPersonalDataFilled) actions.onNextClick() }
            ),
        )
    }

    Spacer(Modifier.height(32.dp))

    PrimaryButton(
        text = stringResource(R.string.register_next_button),
        onClick = actions.onNextClick,
        enabled = uiState.isPersonalDataFilled,
    )

    Spacer(Modifier.height(16.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Text(
            text = stringResource(R.string.register_have_account),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
        )
        LinkText(
            text = stringResource(R.string.register_login_link),
            onClick = actions.onLoginClick,
        )
    }
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

    PrimaryButton(
        text = stringResource(R.string.register_create_button),
        onClick = actions.onCreateAccountClick,
        enabled = uiState.isLocationDataFilled,
    )
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
                password = "12345678",
                confirmPassword = "12345678",
                passwordStrength = PasswordStrength.MEDIUM,
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