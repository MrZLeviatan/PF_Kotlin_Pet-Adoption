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

/** Paso 1: título, categoría (del catálogo) y descripción. */
@Composable
fun StepInfoContent(state: CreatePostUiState, actions: CreatePostActions) {
    FormCard(title = stringResource(R.string.create_post_step_info)) {
        FormTextField(
            value = state.title,
            onValueChange = actions.onTitleChange,
            label = stringResource(R.string.create_post_title_label),
            errorText = lengthErrorText(state.titleError, PostValidator.MIN_TITLE_LENGTH, PostValidator.MAX_TITLE_LENGTH),
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormDropdown(
            label = stringResource(R.string.create_post_category_label),
            options = state.categories.map { it.name },
            selectedIndex = state.categories.indexOfFirst { it.id == state.categoryId }.takeIf { it >= 0 },
            onSelect = actions.onCategorySelected,
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormTextArea(
            value = state.description,
            onValueChange = actions.onDescriptionChange,
            label = stringResource(R.string.create_post_description_label),
            placeholder = stringResource(R.string.create_post_description_placeholder),
            maxLength = PostValidator.MAX_DESCRIPTION_LENGTH,
            errorText = lengthErrorText(state.descriptionError, PostValidator.MIN_DESCRIPTION_LENGTH, PostValidator.MAX_DESCRIPTION_LENGTH),
        )
    }
}
