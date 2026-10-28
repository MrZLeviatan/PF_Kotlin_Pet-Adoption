package co.edu.uniquindio.petadoptionapp.features.user.post.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.dialog.AppActionDialog
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StepIndicator
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepInfoContent
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepLocationContent
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepPetContent
import co.edu.uniquindio.petadoptionapp.features.user.post.create.step.StepPhotosContent

/**
Crear publicación en 4 pasos: publicación, mascota, fotos y ubicación.
Al publicar llama a CreatePostUseCase; la publicación nace sin verificar y queda en revisión.
Desde la ruta EditPost es "Editar publicación": los mismos pasos llenos con sus datos; al guardar
llama a UpdatePostUseCase y la publicación vuelve a revisión. "Guardar" solo se habilita si hubo cambios.
Conecta el [CreatePostViewModel] con la UI; [CreatePostContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
 */
@Composable
fun CreatePostScreen(
    onBack: () -> Unit,
    onViewPost: (postId: String) -> Unit,
    onGoHome: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // El "atrás" del sistema retrocede de paso en vez de salir (en el paso 1 sí sale).
    BackHandler(enabled = state.step > 1) { viewModel.previousStep() }

    CreatePostContent(
        state = state,
        actions = CreatePostActions(
            onBack = { if (state.step > 1) viewModel.previousStep() else onBack() },
            onNext = viewModel::onNextClick,
            onTitleChange = viewModel::onTitleChange,
            onCategorySelected = viewModel::onCategorySelected,
            onDescriptionChange = viewModel::onDescriptionChange,
            onAnimalTypeSelected = viewModel::onAnimalTypeSelected,
            onBreedChange = viewModel::onBreedChange,
            onSizeSelected = viewModel::onSizeSelected,
            onAgeChange = viewModel::onAgeChange,
            onAgeUnitSelected = viewModel::onAgeUnitSelected,
            onVaccinatedChange = viewModel::onVaccinatedChange,
            onNeuteredChange = viewModel::onNeuteredChange,
            onAddPhoto = viewModel::onAddPhoto,
            onRemovePhoto = viewModel::onRemovePhoto,
            onUseCurrentLocationChange = viewModel::onUseCurrentLocationChange,
            onViewPost = { state.savedPostId?.let(onViewPost) },
            onGoHome = onGoHome,
        ),
    )
}

/** Acciones del formulario; los pasos las reciben en lugar del ViewModel, así se pueden previsualizar. */
data class CreatePostActions(
    val onBack: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onTitleChange: (String) -> Unit = {},
    val onCategorySelected: (Int) -> Unit = {},
    val onDescriptionChange: (String) -> Unit = {},
    val onAnimalTypeSelected: (Int) -> Unit = {},
    val onBreedChange: (String) -> Unit = {},
    val onSizeSelected: (Int) -> Unit = {},
    val onAgeChange: (String) -> Unit = {},
    val onAgeUnitSelected: (Int) -> Unit = {},
    val onVaccinatedChange: (Boolean) -> Unit = {},
    val onNeuteredChange: (Boolean) -> Unit = {},
    val onAddPhoto: () -> Unit = {},
    val onRemovePhoto: (Int) -> Unit = {},
    val onUseCurrentLocationChange: (Boolean) -> Unit = {},
    val onViewPost: () -> Unit = {},
    val onGoHome: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun CreatePostContent(
    state: CreatePostUiState,
    actions: CreatePostActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceElevated)
            .systemBarsPadding()
            .imePadding(),
    ) {
        // El padding vertical de 4 dp más los 56 dp de la barra dan 64 dp de alto.
        AppTopBar(
            title = stringResource(if (state.isEditing) R.string.edit_post_title else R.string.create_post_title),
            onBack = actions.onBack,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            titleStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        )

        // Editar: mientras se cargan los datos, o si ya no se puede editar, solo un aviso.
        if (state.isLoading || state.loadError) {
            StatusMessage(
                text = stringResource(if (state.isLoading) R.string.edit_post_loading else R.string.edit_post_load_error),
                isError = state.loadError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepIndicator(
                currentStep = state.step,
                totalSteps = CREATE_POST_STEPS,
                completedColor = AppTheme.colors.brand,
                segmentWidth = 32.dp,
                segmentHeight = 5.dp,
                segmentSpacing = 6.dp,
                labelSpacing = 4.dp,
                labelStyle = LocalTextStyle.current.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(subtitleFor(state.step)),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (state.step) {
                1 -> StepInfoContent(state, actions)
                2 -> StepPetContent(state, actions)
                3 -> StepPhotosContent(state, actions)
                else -> StepLocationContent(state, actions)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        publishErrorText(state.publishError)?.let { message ->
            ErrorText(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
        }

        PrimaryButton(
            text = stringResource(
                when {
                    state.step < CREATE_POST_STEPS -> R.string.create_post_next
                    state.isEditing -> R.string.edit_post_save
                    else -> R.string.create_post_publish
                }
            ),
            enabled = state.isStepValid,
            onClick = actions.onNext,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(52.dp),
            textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        )
    }

    // Publicada o editada: queda en revisión. Se puede ir a verla o volver al inicio.
    if (state.savedPostId != null) {
        AppActionDialog(
            title = stringResource(if (state.isEditing) R.string.edit_post_saved_title else R.string.create_post_sent_title),
            message = stringResource(if (state.isEditing) R.string.edit_post_saved_message else R.string.create_post_sent_message),
            highlight = stringResource(R.string.create_post_sent_time),
            primaryText = stringResource(R.string.create_post_view_post),
            onPrimary = actions.onViewPost,
            secondaryText = stringResource(R.string.create_post_go_home),
            onSecondary = actions.onGoHome,
        )
    }
}

private fun subtitleFor(step: Int) = when (step) {
    1 -> R.string.create_post_subtitle_info
    2, 3 -> R.string.create_post_subtitle_pet
    else -> R.string.create_post_subtitle_location
}

@Composable
private fun publishErrorText(error: PublishError?): String? = when (error) {
    null -> null
    PublishError.LOCATION -> stringResource(R.string.create_post_error_location)
    PublishError.CATALOG -> stringResource(R.string.create_post_error_catalog)
    PublishError.NOT_EDITABLE -> stringResource(R.string.edit_post_load_error)
    PublishError.GENERIC -> stringResource(R.string.create_post_error_generic)
}

// Datos de ejemplo solo para las previews.
internal val previewCatalogState = CreatePostUiState(
    categories = listOf(Category("adoption", "Adopción"), Category("lost", "Perdidos"), Category("found", "Encontrados")),
    animalTypes = listOf(AnimalType("dog", "Perro"), AnimalType("cat", "Gato")),
)

@Preview(name = "Paso 1", showBackground = true, heightDp = 900)
@Composable
private fun CreatePostStep1Preview() {
    PetAdoptionAppTheme {
        CreatePostContent(
            state = previewCatalogState.copy(title = "Luna busca hogar", categoryId = "adoption"),
            actions = CreatePostActions(),
        )
    }
}

@Preview(name = "Paso 2", showBackground = true, heightDp = 1000)
@Composable
private fun CreatePostStep2Preview() {
    PetAdoptionAppTheme {
        CreatePostContent(
            state = previewCatalogState.copy(step = 2, animalTypeId = "dog", breed = "Criollo", size = AnimalSize.MEDIUM, age = "2", ageUnit = AgeUnit.YEARS, vaccinated = true),
            actions = CreatePostActions(),
        )
    }
}

@Preview(name = "Paso 4", showBackground = true, heightDp = 900)
@Composable
private fun CreatePostStep4Preview() {
    PetAdoptionAppTheme {
        CreatePostContent(
            state = previewCatalogState.copy(
                step = 4,
                profileLocation = co.edu.uniquindio.petadoptionapp.domain.model.location.Location(4.53, -75.68, "Armenia", "Colombia", "Calle 20 Norte"),
            ),
            actions = CreatePostActions(),
        )
    }
}
