package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.FormCard
import co.edu.uniquindio.petadoptionapp.core.component.form.FormDropdown
import co.edu.uniquindio.petadoptionapp.core.component.form.FormTextArea
import co.edu.uniquindio.petadoptionapp.core.component.form.FormTextField
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostActions
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState

/**
Paso 1 del asistente de crear/editar publicación (CreatePostScreen): título, categoría
(del catálogo cargado por CreatePostViewModel) y descripción.
No tiene estado propio: lee los valores y errores de CreatePostUiState y avisa cada cambio
mediante las lambdas de CreatePostActions, que terminan en el ViewModel.
**/
@Composable
fun StepInfoContent(state: CreatePostUiState, actions: CreatePostActions) {
    // Tarjeta del formulario con el título del paso.
    FormCard(title = stringResource(R.string.create_post_step_info)) {
        // Campo de texto para el título de la publicación.
        FormTextField(
            // Valor actual del título.
            value = state.title,
            // Avisa al ViewModel cada cambio del título.
            onValueChange = actions.onTitleChange,
            // Etiqueta del campo.
            label = stringResource(R.string.create_post_title_label),
            // Mensaje de error según los límites de longitud del validador.
            errorText = lengthErrorText(state.titleError, PostValidator.MIN_TITLE_LENGTH, PostValidator.MAX_TITLE_LENGTH),
        )

        // Espacio vertical entre campos.
        Spacer(modifier = Modifier.height(16.dp))

        // Lista desplegable para elegir la categoría.
        FormDropdown(
            // Etiqueta del desplegable.
            label = stringResource(R.string.create_post_category_label),
            // Nombres de las categorías del catálogo.
            options = state.categories.map { it.name },
            // Posición de la categoría seleccionada, o null si no hay ninguna.
            selectedIndex = state.categories.indexOfFirst { it.id == state.categoryId }.takeIf { it >= 0 },
            // Avisa al ViewModel la posición elegida.
            onSelect = actions.onCategorySelected,
        )

        // Espacio vertical entre campos.
        Spacer(modifier = Modifier.height(16.dp))

        // Área de texto para la descripción.
        FormTextArea(
            // Valor actual de la descripción.
            value = state.description,
            // Avisa al ViewModel cada cambio de la descripción.
            onValueChange = actions.onDescriptionChange,
            // Etiqueta del campo.
            label = stringResource(R.string.create_post_description_label),
            // Texto de ayuda cuando está vacío.
            placeholder = stringResource(R.string.create_post_description_placeholder),
            // Longitud máxima permitida (muestra contador).
            maxLength = PostValidator.MAX_DESCRIPTION_LENGTH,
            // Mensaje de error según los límites de longitud del validador.
            errorText = lengthErrorText(state.descriptionError, PostValidator.MIN_DESCRIPTION_LENGTH, PostValidator.MAX_DESCRIPTION_LENGTH),
        )
    }
}
