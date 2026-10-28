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
import co.edu.uniquindio.petadoptionapp.core.component.photo.PhotoSourceSheet
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostActions
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState

/** Paso 3: fotos de la mascota (mínimo una; el máximo lo define el validador). */
@Composable
fun StepPhotosContent(state: CreatePostUiState, actions: CreatePostActions) {
    var showSheet by rememberSaveable { mutableStateOf(false) }

    FormCard(title = stringResource(R.string.create_post_step_photos)) {
        FormLabel(stringResource(R.string.create_post_photos_label, PostValidator.MAX_PHOTOS))
        Spacer(modifier = Modifier.height(12.dp))
        PhotoPickerGrid(
            photos = state.photos,
            maxPhotos = PostValidator.MAX_PHOTOS,
            onAddClick = { showSheet = true },
            onRemove = actions.onRemovePhoto,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    InfoTip(icon = Icons.Outlined.Image, text = stringResource(R.string.create_post_photos_tip))

    if (showSheet) {
        // TODO (Cloudinary): cámara y galería reales; por ahora ambas agregan una foto de prueba.
        PhotoSourceSheet(
            onDismiss = { showSheet = false },
            onCameraClick = { actions.onAddPhoto(); showSheet = false },
            onGalleryClick = { actions.onAddPhoto(); showSheet = false },
        )
    }
}
