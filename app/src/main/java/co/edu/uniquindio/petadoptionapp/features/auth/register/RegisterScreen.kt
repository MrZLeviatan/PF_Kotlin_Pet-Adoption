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
import co.edu.uniquindio.petadoptionapp.core.component.map.LocationPicker
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
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

/**
Pantalla de registro de usuarios en dos fases.
Fase 1: datos personales (nombre, correo, teléfono, contraseña con barra de seguridad y confirmación).
Fase 2: ubicación elegida en el mapa (LocationPicker) y aceptación de términos y políticas.
Estado: RegisterUiState del RegisterViewModel (paso, valores, errores, cargas y diálogo de éxito).
Acciones agrupadas en RegisterActions: escribir, cambiar el foco, elegir ubicación, aceptar términos,
avanzar/retroceder de fase, crear la cuenta y aceptar el diálogo.
Casos de uso (a través del ViewModel): CheckRegistrationAvailabilityUseCase al pasar a la fase 2 y
RegisterUseCase al crear la cuenta.
Navegación: el evento RegisterSuccess se convierte en onRegisterSuccess; onBackToLogin vuelve al login
y onTermsClick/onPoliciesClick abren los documentos legales. En la fase 2 el botón atrás del sistema
regresa a la fase 1 en lugar de salir.
**/

// Margen horizontal estándar de la pantalla.
private val ScreenHorizontalPadding = 24.dp
// Separación vertical entre los campos del formulario.
private val FieldSpacing = 12.dp

/**
Pantalla de registro en dos fases (datos personales y ubicación).
Conecta el [RegisterViewModel] con la UI; [RegisterContent] solo dibuja.
**/
// Marca la función como composable.
@Composable
fun RegisterScreen(
    // Evento para volver al login.
    onBackToLogin: () -> Unit,
    // Evento cuando la cuenta quedó creada.
    onRegisterSuccess: () -> Unit,
    // Evento para abrir los términos y condiciones.
    onTermsClick: () -> Unit,
    // Evento para abrir las políticas de privacidad.
    onPoliciesClick: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Guarda la versión más reciente del callback sin reiniciar el efecto.
    val currentOnRegisterSuccess by rememberUpdatedState(onRegisterSuccess)

    // Escucha los eventos de un solo uso del ViewModel.
    LaunchedEffect(viewModel) {
        // Recolecta cada evento.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Cuenta creada: navega fuera del registro.
                RegisterEvent.RegisterSuccess -> currentOnRegisterSuccess()
            }
        }
    }

    // En la fase 2, "atrás" vuelve a la fase 1 en vez de salir del registro.
    BackHandler(enabled = uiState.step > 1) { viewModel.onPreviousStep() }

    // Dibuja el contenido con el estado y las acciones conectadas al ViewModel.
    RegisterContent(
        // Estado actual.
        uiState = uiState,
        // Agrupa todas las acciones de la pantalla.
        actions = RegisterActions(
            onFullNameChange = viewModel::onFullNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPhoneChange = viewModel::onPhoneChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onFocusChange = viewModel::onFocusChange,
            onLocationPicked = viewModel::onLocationPicked,
            onTermsChange = viewModel::onTermsChange,
            onNextClick = viewModel::onNextStep,
            onPreviousStep = viewModel::onPreviousStep,
            onCreateAccountClick = viewModel::onCreateAccountClick,
            onSuccessDialogAccept = viewModel::onSuccessDialogAccept,
            // Las acciones de navegación vienen de AppNavigation.
            onLoginClick = onBackToLogin,
            onTermsClick = onTermsClick,
            onPoliciesClick = onPoliciesClick,
        ),
    )
}

// Agrupa las acciones para no arrastrar 17 parámetros por cada composable.
private data class RegisterActions(
    // Cambio del nombre.
    val onFullNameChange: (String) -> Unit = {},
    // Cambio del correo.
    val onEmailChange: (String) -> Unit = {},
    // Cambio del teléfono.
    val onPhoneChange: (String) -> Unit = {},
    // Cambio de la contraseña.
    val onPasswordChange: (String) -> Unit = {},
    // Cambio de la confirmación.
    val onConfirmPasswordChange: (String) -> Unit = {},
    // Avisa al ViewModel cuándo un campo gana o pierde el foco, para mostrar su error.
    val onFocusChange: (RegisterField, Boolean) -> Unit = { _, _ -> },
    // Ubicación elegida en el mapa.
    val onLocationPicked: (Location) -> Unit = {},
    // Cambio de la casilla de términos.
    val onTermsChange: (Boolean) -> Unit = {},
    // Botón "Siguiente" de la fase 1.
    val onNextClick: () -> Unit = {},
    // Volver a la fase anterior.
    val onPreviousStep: () -> Unit = {},
    // Botón "Crear cuenta".
    val onCreateAccountClick: () -> Unit = {},
    // Aceptar el diálogo de éxito.
    val onSuccessDialogAccept: () -> Unit = {},
    // Enlace "Iniciar sesión".
    val onLoginClick: () -> Unit = {},
    // Enlace a términos.
    val onTermsClick: () -> Unit = {},
    // Enlace a políticas.
    val onPoliciesClick: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun RegisterContent(
    // Estado a dibujar.
    uiState: RegisterUiState,
    // Acciones de la pantalla.
    actions: RegisterActions,
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
            // Sube el contenido cuando aparece el teclado.
            .imePadding()
            // Permite desplazarse si no cabe.
            .verticalScroll(rememberScrollState())
            // Márgenes de la pantalla.
            .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        // Centra horizontalmente los elementos.
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Altura reservada también en la fase 1 (sin flecha) para que el encabezado no salte.
        Box(
            modifier = Modifier
                // Ocupa todo el ancho.
                .fillMaxWidth()
                // Alto fijo de 48 dp.
                .height(48.dp),
            // Alinea el contenido a la izquierda y centrado verticalmente.
            contentAlignment = Alignment.CenterStart,
        ) {
            // Solo en la fase 2 se muestra la flecha para volver.
            if (uiState.step > 1) {
                // Botón de volver a la fase 1.
                BackButton(onClick = actions.onPreviousStep)
            }
        }

        // Espacio de 8 dp.
        Spacer(Modifier.height(8.dp))
        // Logo (huella) y título de la pantalla.
        PawTitle(text = stringResource(R.string.register_title))
        // Espacio de 16 dp.
        Spacer(Modifier.height(16.dp))
        // Indicador del paso actual (1 de 2, 2 de 2).
        StepIndicator(currentStep = uiState.step, totalSteps = RegisterUiState.TOTAL_STEPS)
        // Espacio de 24 dp.
        Spacer(Modifier.height(24.dp))

        // Dibuja la fase que corresponda al paso actual.
        when (uiState.step) {
            // Fase 1: datos personales.
            1 -> PersonalDataStep(uiState, actions)
            // Fase 2: ubicación y términos.
            else -> LocationStep(uiState, actions)
        }
    }

    // Muestra el diálogo de éxito cuando el estado lo indica.
    if (uiState.showSuccessDialog) {
        // Diálogo informativo de cuenta creada.
        AppInfoDialog(
            // Título del diálogo.
            title = stringResource(R.string.register_success_title),
            // Mensaje del diálogo.
            message = stringResource(R.string.register_success_message),
            // Texto del botón de confirmar.
            confirmText = stringResource(R.string.register_success_button),
            // Acción al confirmar.
            onConfirm = actions.onSuccessDialogAccept,
        )
    }
}


// Fase 1: datos personales.
@Composable
private fun PersonalDataStep(uiState: RegisterUiState, actions: RegisterActions) {
    // Columna con los campos separados por FieldSpacing.
    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        // Campo del nombre completo.
        AppTextField(
            // Valor actual.
            value = uiState.fullName,
            // Notifica cada cambio.
            onValueChange = actions.onFullNameChange,
            // onFocusChanged avisa al ViewModel cuando el usuario entra o sale del campo,
            // para mostrar el error si lo deja vacío.
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.NAME, it.isFocused)
            },
            // Etiqueta del campo.
            label = stringResource(R.string.register_name_label),
            // Ícono de persona.
            leadingIcon = Icons.Outlined.Person,
            // Mensaje de error traducido.
            errorMessage = nameErrorMessage(uiState.nameError),
        )
        // Campo del correo.
        AppTextField(
            // Valor actual.
            value = uiState.email,
            // Notifica cada cambio.
            onValueChange = actions.onEmailChange,
            // Avisa los cambios de foco del correo.
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.EMAIL, it.isFocused)
            },
            // Etiqueta del campo.
            label = stringResource(R.string.register_email_label),
            // Ícono de sobre.
            leadingIcon = Icons.Outlined.Email,
            // Mensaje de error traducido.
            errorMessage = emailErrorMessage(uiState.emailError),
            // Teclado para correos.
            keyboardType = KeyboardType.Email,
        )
        // Campo del teléfono.
        AppTextField(
            // Valor actual.
            value = uiState.phone,
            // Notifica cada cambio.
            onValueChange = actions.onPhoneChange,
            // Avisa los cambios de foco del teléfono.
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.PHONE, it.isFocused)
            },
            // Etiqueta del campo.
            label = stringResource(R.string.register_phone_label),
            // Ícono de teléfono.
            leadingIcon = Icons.Outlined.Phone,
            // Mensaje de error traducido.
            errorMessage = phoneErrorMessage(uiState.phoneError),
            // Teclado telefónico.
            keyboardType = KeyboardType.Phone,
        )
        // Campo de la contraseña.
        AppTextField(
            // Valor actual.
            value = uiState.password,
            // Notifica cada cambio.
            onValueChange = actions.onPasswordChange,
            // Avisa los cambios de foco de la contraseña.
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.PASSWORD, it.isFocused)
            },
            // Etiqueta del campo.
            label = stringResource(R.string.register_password_label),
            // Ícono de candado.
            leadingIcon = Icons.Outlined.Lock,
            // Mensaje de error traducido.
            errorMessage = passwordErrorMessage(uiState.passwordError),
            // Oculta los caracteres.
            isPassword = true,
        )
        // Se calcula en el estado: aparece apenas hay algo escrito en la contraseña.
        uiState.passwordStrength?.let { PasswordStrengthBar(strength = it) }
        // Campo de confirmación de la contraseña.
        AppTextField(
            // Valor actual.
            value = uiState.confirmPassword,
            // Notifica cada cambio.
            onValueChange = actions.onConfirmPasswordChange,
            // Avisa los cambios de foco de la confirmación.
            modifier = Modifier.onFocusChanged {
                actions.onFocusChange(RegisterField.CONFIRM_PASSWORD, it.isFocused)
            },
            // Etiqueta del campo.
            label = stringResource(R.string.register_confirm_password_label),
            // Ícono de candado.
            leadingIcon = Icons.Outlined.Lock,
            // Mensaje de error traducido.
            errorMessage = confirmPasswordErrorMessage(uiState.confirmPasswordError),
            // Oculta los caracteres.
            isPassword = true,
            // La tecla de acción del teclado es "Listo".
            imeAction = ImeAction.Done,
            // El ViewModel vuelve a validar: la acción se ignora si el formulario no es válido.
            keyboardActions = KeyboardActions(onDone = { actions.onNextClick() }),
        )
    }

    // Espacio de 32 dp.
    Spacer(Modifier.height(32.dp))

    // Correo o teléfono ya usados por otra cuenta (se revisa al tocar "Siguiente").
    if (uiState.step == 1) {
        // Muestra el error general si existe.
        registerErrorMessage(uiState.registerError)?.let { message ->
            // Texto del error a todo el ancho.
            ErrorText(text = message, modifier = Modifier.fillMaxWidth())
            // Espacio de 12 dp.
            Spacer(Modifier.height(12.dp))
        }
    }

    // Botón "Siguiente".
    PrimaryButton(
        // Texto del botón.
        text = stringResource(R.string.register_next_button),
        // Valida y avanza a la fase 2.
        onClick = actions.onNextClick,
        // Habilitado solo con todos los campos válidos (y mientras no se esté revisando).
        enabled = uiState.isPersonalDataValid && !uiState.isCheckingAvailability,
    )

    // Espacio de 16 dp.
    Spacer(Modifier.height(16.dp))

    // Texto "¿Ya tienes cuenta?" con enlace para iniciar sesión.
    TextWithLink(
        text = stringResource(R.string.register_have_account),
        linkText = stringResource(R.string.register_login_link),
        onLinkClick = actions.onLoginClick,
    )
}

// Fase 2: ubicación y términos.
@Composable
private fun LocationStep(uiState: RegisterUiState, actions: RegisterActions) {
    // Subtítulo explicativo de la fase.
    Text(
        // Texto del subtítulo.
        text = stringResource(R.string.register_location_subtitle),
        // Tipografía de cuerpo mediano.
        style = MaterialTheme.typography.bodyMedium,
        // Color principal del texto.
        color = AppTheme.colors.textPrimary,
        // Centrado.
        textAlign = TextAlign.Center,
    )

    // Espacio de 20 dp.
    Spacer(Modifier.height(20.dp))

    // Ciudad (lista desplegable) y dirección (buscada o tocando el mapa) filtran y fijan el punto.
    LocationPicker(
        // Ubicación previa (si el usuario vuelve a esta fase).
        initial = uiState.pickedLocation,
        // Notifica la ubicación elegida.
        onLocationChange = actions.onLocationPicked,
        // Clave para conservar el estado interno del selector.
        key = "register-location",
    )

    // Espacio de 24 dp.
    Spacer(Modifier.height(24.dp))

    // Casilla de aceptación de términos y políticas con sus enlaces.
    TermsCheckbox(
        // Estado de la casilla.
        checked = uiState.termsAccepted,
        // Notifica el cambio.
        onCheckedChange = actions.onTermsChange,
        // Abre los términos.
        onTermsClick = actions.onTermsClick,
        // Abre las políticas.
        onPoliciesClick = actions.onPoliciesClick,
    )

    // Espacio de 16 dp.
    Spacer(Modifier.height(16.dp))

    // Error del intento de registro. liveRegion: TalkBack lo anuncia solo al aparecer.
    registerErrorMessage(uiState.registerError)?.let { message ->
        // Texto del error a todo el ancho.
        ErrorText(text = message, modifier = Modifier.fillMaxWidth())
        // Espacio de 12 dp.
        Spacer(Modifier.height(12.dp))
    }

    // Botón "Crear cuenta".
    PrimaryButton(
        // Texto del botón.
        text = stringResource(R.string.register_create_button),
        // Crea la cuenta.
        onClick = actions.onCreateAccountClick,
        // Habilitado con ubicación y términos completos; deshabilitado mientras hay una petición en curso.
        enabled = uiState.isLocationDataFilled && !uiState.isLoading,
    )
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
// Viven aquí y no en el ViewModel porque stringResource necesita el contexto de Compose.
// Texto del error del nombre.
@Composable
private fun nameErrorMessage(error: FieldError?): String? = when (error) {
    // Sin error.
    null -> null
    // Vacío.
    FieldError.EMPTY -> stringResource(R.string.register_error_name_empty)
    // Cualquier otro: formato inválido.
    else -> stringResource(R.string.register_error_name_invalid)
}

// Texto del error del correo.
@Composable
private fun emailErrorMessage(error: FieldError?): String? = when (error) {
    // Sin error.
    null -> null
    // Vacío.
    FieldError.EMPTY -> stringResource(R.string.register_error_email_empty)
    // Ya usado por otra cuenta.
    FieldError.TAKEN -> stringResource(R.string.register_error_email_taken)
    // Formato inválido.
    else -> stringResource(R.string.register_error_email_invalid)
}

// Texto del error del teléfono.
@Composable
private fun phoneErrorMessage(error: FieldError?): String? = when (error) {
    // Sin error.
    null -> null
    // Vacío.
    FieldError.EMPTY -> stringResource(R.string.register_error_phone_empty)
    // Ya usado por otra cuenta.
    FieldError.TAKEN -> stringResource(R.string.register_error_phone_taken)
    // Formato inválido.
    else -> stringResource(R.string.register_error_phone_invalid)
}

// Texto del error de la contraseña.
@Composable
private fun passwordErrorMessage(error: FieldError?): String? = when (error) {
    // Sin error.
    null -> null
    // Vacía.
    FieldError.EMPTY -> stringResource(R.string.register_error_password_empty)
    // Débil.
    else -> stringResource(R.string.register_error_password_weak)
}

// Texto del error de la confirmación.
@Composable
private fun confirmPasswordErrorMessage(error: FieldError?): String? = when (error) {
    // Sin error.
    null -> null
    // Vacía.
    FieldError.EMPTY -> stringResource(R.string.register_error_confirm_empty)
    // No coincide.
    else -> stringResource(R.string.register_error_password_mismatch)
}

// Texto del error general del registro.
@Composable
private fun registerErrorMessage(error: RegisterError?): String? = when (error) {
    // Sin error.
    null -> null
    // Ubicación inválida.
    RegisterError.LOCATION -> stringResource(R.string.register_error_location)
    // Correo o teléfono ya usados.
    RegisterError.TAKEN -> stringResource(R.string.register_error_taken)
    // Error genérico.
    RegisterError.GENERIC -> stringResource(R.string.register_error_generic)
}

// Vista previa de la fase 1 vacía.
@Preview(name = "Fase 1 vacía", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep1Preview() {
    // Aplica el tema y dibuja con el estado inicial.
    PetAdoptionAppTheme {
        RegisterContent(uiState = RegisterUiState(), actions = RegisterActions())
    }
}

// Vista previa de la fase 1 con datos válidos.
@Preview(name = "Fase 1 llena", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep1FilledPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja con datos de ejemplo.
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

// Vista previa de la fase 1 con todos los errores de campo vacío.
@Preview(name = "Fase 1 campos vacíos", showBackground = true, heightDp = 1100)
@Composable
private fun RegisterStep1EmptyErrorsPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja con los errores EMPTY en cada campo.
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

// Vista previa de la fase 1 con errores de formato.
@Preview(name = "Fase 1 formato inválido", showBackground = true, heightDp = 1100)
@Composable
private fun RegisterStep1InvalidErrorsPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja con valores inválidos y sus errores.
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

// Vista previa de la fase 2 completa.
@Preview(name = "Fase 2", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep2Preview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja la fase 2 con ciudad, dirección y términos aceptados.
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

// Vista previa de la fase 2 con un error general.
@Preview(name = "Fase 2 con error", showBackground = true, heightDp = 900)
@Composable
private fun RegisterStep2ErrorPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja la fase 2 con el error genérico visible.
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

// Vista previa con el diálogo de cuenta creada.
@Preview(name = "Confirmación", showBackground = true, heightDp = 900)
@Composable
private fun RegisterSuccessPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja la fase 2 con el diálogo de éxito abierto.
        RegisterContent(
            uiState = RegisterUiState(step = 2, showSuccessDialog = true),
            actions = RegisterActions(),
        )
    }
}
