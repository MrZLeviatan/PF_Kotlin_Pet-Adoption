package co.edu.uniquindio.petadoptionapp.features.user.profile.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import co.edu.uniquindio.petadoptionapp.core.component.photo.ImageSourcePicker
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.card.AppCard
import co.edu.uniquindio.petadoptionapp.core.component.card.LocationMapPlaceholder
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppInfoDialog
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextField
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla para editar los datos del perfil: nombre, teléfono y ubicación.
El correo y la foto se muestran pero no se editan aquí.
Conecta el [EditProfileViewModel] con la UI; [EditProfileContent] solo dibuja.
 */
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSaved by rememberUpdatedState(onSaved)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.Saved -> currentOnSaved()
            }
        }
    }

    EditProfileContent(
        uiState = uiState,
        actions = EditProfileActions(
            onBack = onBack,
            onFullNameChange = viewModel::onFullNameChange,
            onPhotoPicked = viewModel::onPhotoPicked,
            onPhoneChange = viewModel::onPhoneChange,
            onCityChange = viewModel::onCityChange,
            onAddressChange = viewModel::onAddressChange,
            onSetLocationClick = viewModel::onSetLocationClick,
            onSaveClick = viewModel::onSaveClick,
            onSuccessDialogAccept = viewModel::onSuccessDialogAccept,
        ),
    )
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class EditProfileActions(
    val onBack: () -> Unit = {},
    val onFullNameChange: (String) -> Unit = {},
    val onPhotoPicked: (String) -> Unit = {},
    val onPhoneChange: (String) -> Unit = {},
    val onCityChange: (String) -> Unit = {},
    val onAddressChange: (String) -> Unit = {},
    val onSetLocationClick: () -> Unit = {},
    val onSaveClick: () -> Unit = {},
    val onSuccessDialogAccept: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun EditProfileContent(
    uiState: EditProfileUiState,
    actions: EditProfileActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
            .imePadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.edit_profile_title),
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        ) {
            AppCard(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tocar la foto, su botón de cámara o "Cambiar foto" abre la cámara o la galería.
                var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
                ProfilePhotoEditor(
                    userName = uiState.userName,
                    photoUrl = uiState.photoUrl,
                    onClick = { showPhotoSheet = true },
                )
                ImageSourcePicker(
                    visible = showPhotoSheet,
                    onDismiss = { showPhotoSheet = false },
                    onPicked = { urls -> urls.firstOrNull()?.let(actions.onPhotoPicked) },
                )

                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = uiState.fullName,
                        onValueChange = actions.onFullNameChange,
                        label = stringResource(R.string.edit_profile_name),
                        leadingIcon = Icons.Outlined.Person,
                        errorMessage = nameErrorMessage(uiState.nameError),
                        required = true,
                    )
                    // El correo es la identidad de la cuenta: se ve, pero no se edita.
                    AppTextField(
                        value = uiState.email,
                        onValueChange = {},
                        label = stringResource(R.string.edit_profile_email),
                        leadingIcon = Icons.Outlined.Email,
                        readOnly = true,
                    )
                    AppTextField(
                        value = uiState.phone,
                        onValueChange = actions.onPhoneChange,
                        label = stringResource(R.string.edit_profile_phone),
                        leadingIcon = Icons.Outlined.Phone,
                        errorMessage = phoneErrorMessage(uiState.phoneError),
                        keyboardType = KeyboardType.Phone,
                        required = true,
                    )
                    AppTextField(
                        value = uiState.city,
                        onValueChange = actions.onCityChange,
                        label = stringResource(R.string.edit_profile_city),
                        leadingIcon = Icons.Outlined.Public,
                        errorMessage = requiredErrorMessage(uiState.cityError, R.string.edit_profile_error_city),
                        required = true,
                    )
                    AppTextField(
                        value = uiState.address,
                        onValueChange = actions.onAddressChange,
                        label = stringResource(R.string.edit_profile_address),
                        leadingIcon = Icons.Outlined.LocationOn,
                        errorMessage = requiredErrorMessage(uiState.addressError, R.string.edit_profile_error_address),
                        imeAction = ImeAction.Done,
                        required = true,
                    )
                    // Recuadro provisional: aquí irá el mapa.
                    LocationMapPlaceholder(onSetLocationClick = actions.onSetLocationClick)
                }
            }

            saveErrorMessage(uiState.saveError)?.let { message ->
                Spacer(Modifier.height(12.dp))
                ErrorText(text = message, modifier = Modifier.fillMaxWidth())
            }
        }

        PrimaryButton(
            text = stringResource(R.string.edit_profile_save),
            onClick = actions.onSaveClick,
            // Bloqueado hasta que se cambie algún dato (y mientras haya errores).
            enabled = uiState.canSave,
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        )
    }

    if (uiState.showSuccessDialog) {
        AppInfoDialog(
            title = stringResource(R.string.edit_profile_success_title),
            message = stringResource(R.string.edit_profile_success_message),
            confirmText = stringResource(R.string.edit_profile_success_button),
            onConfirm = actions.onSuccessDialogAccept,
        )
    }
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
// Reutilizan los mensajes del registro: son las mismas reglas.
@Composable
private fun nameErrorMessage(error: ProfileFieldError?): String? = when (error) {
    null -> null
    ProfileFieldError.EMPTY -> stringResource(R.string.register_error_name_empty)
    else -> stringResource(R.string.register_error_name_invalid)
}

@Composable
private fun phoneErrorMessage(error: ProfileFieldError?): String? = when (error) {
    null -> null
    ProfileFieldError.EMPTY -> stringResource(R.string.register_error_phone_empty)
    ProfileFieldError.TAKEN -> stringResource(R.string.register_error_phone_taken)
    ProfileFieldError.INVALID -> stringResource(R.string.register_error_phone_invalid)
}

@Composable
private fun requiredErrorMessage(error: ProfileFieldError?, emptyRes: Int): String? =
    error?.let { stringResource(emptyRes) }

@Composable
private fun saveErrorMessage(error: EditProfileError?): String? = when (error) {
    null -> null
    EditProfileError.LOCATION -> stringResource(R.string.register_error_location)
    EditProfileError.GENERIC -> stringResource(R.string.settings_error_generic)
}

private val previewState = EditProfileUiState(
    userName = "nicolas_cs",
    email = "nicolas.cabreras@uqvirtual.edu.co",
    fullName = "Nicolas Cabrera Serrano",
    phone = "3054819209",
    city = "Armenia",
    address = "Oro negro, Calle 20 Norte",
    isLoaded = true,
)

/**
 Foto de perfil editable: la foto con un anillo, el botón de cámara montado sobre el borde
 (fuera del recorte circular, con un borde del color de la tarjeta para separarlo) y "Cambiar foto" debajo.
 */
@Composable
private fun ProfilePhotoEditor(userName: String, photoUrl: String?, onClick: () -> Unit) {
    val changeLabel = stringResource(R.string.edit_profile_change_photo)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // El Box no recorta: así el botón de cámara se ve completo aunque sobresalga de la foto.
        Box(modifier = Modifier.size(116.dp)) {
            UserAvatar(
                name = userName,
                photoUrl = photoUrl,
                size = 104.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(3.dp, AppTheme.colors.actionPrimary, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .clickable(onClickLabel = changeLabel, role = Role.Button, onClick = onClick),
            )
            // Botón de cámara: el TalkBack lo anuncia una vez (la foto ya tiene la misma acción).
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.actionPrimary, CircleShape)
                    .border(3.dp, AppTheme.colors.surface, CircleShape)
                    .clickable(onClickLabel = changeLabel, role = Role.Button, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = AppTheme.colors.onActionPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        // "Cambiar foto" como píldora delineada, con su icono.
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(changeLabel, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
        }
    }
}

@Preview(name = "Editar perfil", showBackground = true, heightDp = 1000)
@Composable
private fun EditProfileContentPreview() {
    PetAdoptionAppTheme {
        EditProfileContent(uiState = previewState, actions = EditProfileActions())
    }
}

@Preview(name = "Con errores", showBackground = true, heightDp = 1100)
@Composable
private fun EditProfileErrorsPreview() {
    PetAdoptionAppTheme {
        EditProfileContent(
            uiState = previewState.copy(
                phone = "305481",
                phoneError = ProfileFieldError.INVALID,
                city = "",
                cityError = ProfileFieldError.EMPTY,
            ),
            actions = EditProfileActions(),
        )
    }
}
