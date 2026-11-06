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

/**
Pantalla "Editar perfil", a la que se llega desde el menú de Mi Perfil.
Permite cambiar la foto (cámara o galería), el nombre, el teléfono, la ciudad y la dirección;
el nombre de usuario y el correo se muestran pero no se editan.
El estado (EditProfileUiState) y la validación viven en EditProfileViewModel, que usa
ObserveCurrentUserUseCase para cargar los datos y UpdateProfileUseCase para guardarlos.
La pantalla escucha el evento EditProfileEvent.Saved y llama a onSaved para que
AppNavigation vuelva al perfil; la flecha atrás llama a onBack.
**/

// Margen horizontal común de la pantalla (contenido y botón inferior).
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla para editar los datos del perfil: foto, nombre, teléfono y ubicación.
El nombre de usuario y el correo se muestran pero no se editan aquí.
Conecta el [EditProfileViewModel] con la UI; [EditProfileContent] solo dibuja.
**/
@Composable
fun EditProfileScreen(
    // Callback para volver atrás sin guardar.
    onBack: () -> Unit,
    // Callback que se invoca cuando los cambios se guardaron y la persona aceptó el aviso.
    onSaved: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    // Recolecta el estado del formulario respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Mantiene siempre la última versión de onSaved sin reiniciar el LaunchedEffect.
    val currentOnSaved by rememberUpdatedState(onSaved)

    // Escucha los eventos de una sola vez mientras el ViewModel sea el mismo.
    LaunchedEffect(viewModel) {
        // Recolecta cada evento emitido por el ViewModel.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Guardado confirmado: delega la navegación de regreso.
                EditProfileEvent.Saved -> currentOnSaved()
            }
        }
    }

    // Dibuja el contenido sin estado con el estado actual y las acciones del ViewModel.
    EditProfileContent(
        // Estado actual del formulario.
        uiState = uiState,
        // Agrupa los callbacks enlazados a las funciones del ViewModel.
        actions = EditProfileActions(
            // Volver atrás.
            onBack = onBack,
            // Cambios en el nombre.
            onFullNameChange = viewModel::onFullNameChange,
            // Nueva foto seleccionada.
            onPhotoPicked = viewModel::onPhotoPicked,
            // Cambios en el teléfono.
            onPhoneChange = viewModel::onPhoneChange,
            // Cambios en la ciudad.
            onCityChange = viewModel::onCityChange,
            // Cambios en la dirección.
            onAddressChange = viewModel::onAddressChange,
            // Toque en "Fijar ubicación".
            onSetLocationClick = viewModel::onSetLocationClick,
            // Toque en "Guardar Cambios".
            onSaveClick = viewModel::onSaveClick,
            // Aceptar el diálogo de éxito.
            onSuccessDialogAccept = viewModel::onSuccessDialogAccept,
        ),
    )
}

// Agrupa las acciones de la pantalla (mismo patrón que Registro y Configuración).
private data class EditProfileActions(
    // Volver a la pantalla anterior.
    val onBack: () -> Unit = {},
    // Se llama al escribir en el campo nombre.
    val onFullNameChange: (String) -> Unit = {},
    // Se llama con la URL de la foto elegida.
    val onPhotoPicked: (String) -> Unit = {},
    // Se llama al escribir en el campo teléfono.
    val onPhoneChange: (String) -> Unit = {},
    // Se llama al escribir en el campo ciudad.
    val onCityChange: (String) -> Unit = {},
    // Se llama al escribir en el campo dirección.
    val onAddressChange: (String) -> Unit = {},
    // Se llama al tocar el botón del recuadro de mapa.
    val onSetLocationClick: () -> Unit = {},
    // Se llama al tocar "Guardar Cambios".
    val onSaveClick: () -> Unit = {},
    // Se llama al aceptar el diálogo de éxito.
    val onSuccessDialogAccept: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun EditProfileContent(
    // Estado del formulario a pintar.
    uiState: EditProfileUiState,
    // Callbacks de la pantalla.
    actions: EditProfileActions,
) {
    // Columna raíz: barra superior, contenido desplazable y botón fijo abajo.
    Column(
        // Ocupa toda la pantalla, pinta el fondo y respeta barras del sistema y teclado.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
            .imePadding(),
    ) {
        // Barra superior con el título y la flecha de regreso.
        AppTopBar(
            // Título "Editar perfil".
            title = stringResource(R.string.edit_profile_title),
            // Acción de la flecha atrás.
            onBack = actions.onBack,
            // Pequeño margen horizontal para la barra.
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // Zona central con scroll que contiene la tarjeta del formulario.
        Column(
            // Toma el espacio sobrante (deja sitio al botón), permite scroll y añade márgenes.
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        ) {
            // Tarjeta con la foto y los campos, centrados horizontalmente.
            AppCard(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tocar la foto, su botón de cámara o "Cambiar foto" abre la cámara o la galería.
                var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
                // Foto de perfil editable; al tocarla abre el selector de origen.
                ProfilePhotoEditor(
                    userName = uiState.userName,
                    photoUrl = uiState.photoUrl,
                    onClick = { showPhotoSheet = true },
                )
                // Hoja inferior para elegir entre cámara y galería.
                ImageSourcePicker(
                    // Visible solo cuando se pidió cambiar la foto.
                    visible = showPhotoSheet,
                    // Al cerrarse, oculta la hoja.
                    onDismiss = { showPhotoSheet = false },
                    // Toma solo la primera imagen elegida y la envía al ViewModel.
                    onPicked = { urls -> urls.firstOrNull()?.let(actions.onPhotoPicked) },
                )

                // Separación entre la foto y los campos.
                Spacer(Modifier.height(20.dp))

                // Columna con los campos del formulario, separados 12 dp.
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Campo nombre completo (obligatorio).
                    AppTextField(
                        value = uiState.fullName,
                        onValueChange = actions.onFullNameChange,
                        label = stringResource(R.string.edit_profile_name),
                        leadingIcon = Icons.Outlined.Person,
                        // Mensaje de error traducido o null si es válido.
                        errorMessage = nameErrorMessage(uiState.nameError),
                        required = true,
                    )
                    // El correo es la identidad de la cuenta: se ve, pero no se edita.
                    AppTextField(
                        value = uiState.email,
                        // No hace nada: el campo es de solo lectura.
                        onValueChange = {},
                        label = stringResource(R.string.edit_profile_email),
                        leadingIcon = Icons.Outlined.Email,
                        readOnly = true,
                    )
                    // Campo teléfono (obligatorio) con teclado numérico.
                    AppTextField(
                        value = uiState.phone,
                        onValueChange = actions.onPhoneChange,
                        label = stringResource(R.string.edit_profile_phone),
                        leadingIcon = Icons.Outlined.Phone,
                        errorMessage = phoneErrorMessage(uiState.phoneError),
                        // Muestra el teclado de teléfono.
                        keyboardType = KeyboardType.Phone,
                        required = true,
                    )
                    // Campo ciudad (obligatorio).
                    AppTextField(
                        value = uiState.city,
                        onValueChange = actions.onCityChange,
                        label = stringResource(R.string.edit_profile_city),
                        leadingIcon = Icons.Outlined.Public,
                        errorMessage = requiredErrorMessage(uiState.cityError, R.string.edit_profile_error_city),
                        required = true,
                    )
                    // Campo dirección (obligatorio); es el último, por eso el teclado muestra "Listo".
                    AppTextField(
                        value = uiState.address,
                        onValueChange = actions.onAddressChange,
                        label = stringResource(R.string.edit_profile_address),
                        leadingIcon = Icons.Outlined.LocationOn,
                        errorMessage = requiredErrorMessage(uiState.addressError, R.string.edit_profile_error_address),
                        // Acción "Listo" del teclado.
                        imeAction = ImeAction.Done,
                        required = true,
                    )
                    // Recuadro provisional: aquí irá el mapa.
                    LocationMapPlaceholder(onSetLocationClick = actions.onSetLocationClick)
                }
            }

            // Si hay un error general de guardado, lo muestra debajo de la tarjeta.
            saveErrorMessage(uiState.saveError)?.let { message ->
                // Separación entre la tarjeta y el mensaje.
                Spacer(Modifier.height(12.dp))
                // Texto de error a todo el ancho.
                ErrorText(text = message, modifier = Modifier.fillMaxWidth())
            }
        }

        // Botón fijo inferior "Guardar Cambios".
        PrimaryButton(
            text = stringResource(R.string.edit_profile_save),
            onClick = actions.onSaveClick,
            // Bloqueado hasta que se cambie algún dato (y mientras haya errores).
            enabled = uiState.canSave,
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        )
    }

    // Muestra el diálogo de confirmación cuando el guardado terminó con éxito.
    if (uiState.showSuccessDialog) {
        // Diálogo informativo con un único botón de confirmación.
        AppInfoDialog(
            title = stringResource(R.string.edit_profile_success_title),
            message = stringResource(R.string.edit_profile_success_message),
            confirmText = stringResource(R.string.edit_profile_success_button),
            // Al aceptar, el ViewModel cierra el diálogo y emite el evento de navegación.
            onConfirm = actions.onSuccessDialogAccept,
        )
    }
}

// Traducen el tipo de error (lógica) al texto del recurso (presentación).
// Reutilizan los mensajes del registro: son las mismas reglas.
// Mensaje para el campo nombre: vacío o inválido.
@Composable
private fun nameErrorMessage(error: ProfileFieldError?): String? = when (error) {
    // Sin error: no se muestra mensaje.
    null -> null
    // Campo vacío.
    ProfileFieldError.EMPTY -> stringResource(R.string.register_error_name_empty)
    // Cualquier otro caso se considera nombre inválido.
    else -> stringResource(R.string.register_error_name_invalid)
}

// Mensaje para el campo teléfono: vacío, ya registrado o inválido.
@Composable
private fun phoneErrorMessage(error: ProfileFieldError?): String? = when (error) {
    // Sin error.
    null -> null
    // Campo vacío.
    ProfileFieldError.EMPTY -> stringResource(R.string.register_error_phone_empty)
    // Teléfono usado por otra cuenta.
    ProfileFieldError.TAKEN -> stringResource(R.string.register_error_phone_taken)
    // Formato inválido.
    ProfileFieldError.INVALID -> stringResource(R.string.register_error_phone_invalid)
}

// Mensaje para campos obligatorios: si hay error, muestra el texto de "vacío" recibido.
@Composable
private fun requiredErrorMessage(error: ProfileFieldError?, emptyRes: Int): String? =
    error?.let { stringResource(emptyRes) }

// Mensaje del error general de guardado.
@Composable
private fun saveErrorMessage(error: EditProfileError?): String? = when (error) {
    // Sin error.
    null -> null
    // Problema con la ubicación.
    EditProfileError.LOCATION -> stringResource(R.string.register_error_location)
    // Error inesperado.
    EditProfileError.GENERIC -> stringResource(R.string.settings_error_generic)
}

// Estado de ejemplo compartido por las vistas previas.
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
Los tres elementos ejecutan el mismo onClick, que abre el selector de cámara o galería.
**/
@Composable
private fun ProfilePhotoEditor(userName: String, photoUrl: String?, onClick: () -> Unit) {
    // Texto "Cambiar foto", usado como etiqueta visible y de accesibilidad.
    val changeLabel = stringResource(R.string.edit_profile_change_photo)
    // Columna que apila la foto y la píldora "Cambiar foto", centradas.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // El Box no recorta: así el botón de cámara se ve completo aunque sobresalga de la foto.
        Box(modifier = Modifier.size(116.dp)) {
            // Avatar del usuario (foto o iniciales).
            UserAvatar(
                name = userName,
                photoUrl = photoUrl,
                size = 104.dp,
                // Centrado, con anillo del color primario, recorte circular y clic accesible como botón.
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(3.dp, AppTheme.colors.actionPrimary, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .clickable(onClickLabel = changeLabel, role = Role.Button, onClick = onClick),
            )
            // Botón de cámara: el TalkBack lo anuncia una vez (la foto ya tiene la misma acción).
            Box(
                // Esquina inferior derecha, círculo de color primario con borde del color de la tarjeta.
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.actionPrimary, CircleShape)
                    .border(3.dp, AppTheme.colors.surface, CircleShape)
                    .clickable(onClickLabel = changeLabel, role = Role.Button, onClick = onClick),
                // Centra el icono dentro del círculo.
                contentAlignment = Alignment.Center,
            ) {
                // Icono de cámara decorativo (sin descripción: la acción ya está etiquetada).
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = AppTheme.colors.onActionPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        // Separación entre la foto y la píldora.
        Spacer(Modifier.height(10.dp))
        // "Cambiar foto" como píldora delineada, con su icono.
        Row(
            // Forma de píldora con borde, clic como botón y relleno interno.
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            // Alinea verticalmente el icono y el texto.
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icono de lápiz decorativo.
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
            // Espacio entre el icono y el texto.
            Spacer(Modifier.width(6.dp))
            // Texto "Cambiar foto".
            Text(changeLabel, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
        }
    }
}

// Vista previa del formulario con datos válidos.
@Preview(name = "Editar perfil", showBackground = true, heightDp = 1000)
@Composable
private fun EditProfileContentPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido con el estado de ejemplo y acciones vacías.
        EditProfileContent(uiState = previewState, actions = EditProfileActions())
    }
}

// Vista previa del formulario mostrando errores de validación.
@Preview(name = "Con errores", showBackground = true, heightDp = 1100)
@Composable
private fun EditProfileErrorsPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido con un teléfono inválido y la ciudad vacía.
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
