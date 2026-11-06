package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.FormCard
import co.edu.uniquindio.petadoptionapp.core.component.card.InfoTip
import co.edu.uniquindio.petadoptionapp.core.component.form.FormLabel
import co.edu.uniquindio.petadoptionapp.core.component.photo.PhotoPickerGrid
import co.edu.uniquindio.petadoptionapp.core.component.photo.ImageSourcePicker
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostActions
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState

/**
Paso 3 del asistente de crear/editar publicación: fotos de la mascota (mínimo una; el
máximo lo define PostValidator.MAX_PHOTOS).
Muestra la cuadrícula de fotos elegidas (state.photos) y una hoja inferior para elegir entre
cámara o galería. Las fotos agregadas o quitadas se avisan a CreatePostViewModel mediante
CreatePostActions (onAddPhotos y onRemovePhoto); solo la visibilidad de la hoja es estado local.
**/
@Composable
fun StepPhotosContent(state: CreatePostUiState, actions: CreatePostActions) {
    // Controla si la hoja de elegir origen de imagen está visible (sobrevive a rotaciones).
    var showSheet by rememberSaveable { mutableStateOf(false) }

    // Tarjeta del formulario con el título del paso.
    FormCard(title = stringResource(R.string.create_post_step_photos)) {
        // Etiqueta que indica cuántas fotos se pueden subir como máximo.
        FormLabel(stringResource(R.string.create_post_photos_label, PostValidator.MAX_PHOTOS))
        // Espacio vertical.
        Spacer(modifier = Modifier.height(12.dp))
        // Cuadrícula con las fotos elegidas y el botón de agregar.
        PhotoPickerGrid(
            // Fotos actuales.
            photos = state.photos,
            // Máximo de fotos permitido.
            maxPhotos = PostValidator.MAX_PHOTOS,
            // Al tocar agregar, abre la hoja de origen.
            onAddClick = { showSheet = true },
            // Avisa al ViewModel la foto a quitar.
            onRemove = actions.onRemovePhoto,
        )
    }

    // Espacio vertical.
    Spacer(modifier = Modifier.height(16.dp))

    // Consejo sobre cómo tomar buenas fotos.
    InfoTip(icon = Icons.Outlined.Image, text = stringResource(R.string.create_post_photos_tip))

    // Cámara (pide el permiso) o galería (varias a la vez, hasta completar el máximo).
    ImageSourcePicker(
        // Visible según el estado local.
        visible = showSheet,
        // Al cerrar, oculta la hoja.
        onDismiss = { showSheet = false },
        // Envía al ViewModel las fotos elegidas.
        onPicked = actions.onAddPhotos,
        // Cantidad máxima que aún se puede elegir (al menos una).
        maxSelection = (PostValidator.MAX_PHOTOS - state.photos.size).coerceAtLeast(1),
    )
}
