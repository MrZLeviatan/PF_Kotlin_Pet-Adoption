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
Conecta el [CreatePostViewModel] con la UI (observa CreatePostUiState y le pasa los eventos
mediante CreatePostActions); [CreatePostContent] solo dibuja.
No decide a dónde navegar: avisa con onBack, onViewPost u onGoHome y AppNavigation resuelve el destino.
**/
@Composable
fun CreatePostScreen(
    // Acción para salir del formulario.
    onBack: () -> Unit,
    // Acción para abrir el detalle de la publicación guardada.
    onViewPost: (postId: String) -> Unit,
    // Acción para volver al inicio.
    onGoHome: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: CreatePostViewModel = hiltViewModel(),
) {
    // Observa el estado del formulario respetando el ciclo de vida.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // El "atrás" del sistema retrocede de paso en vez de salir (en el paso 1 sí sale).
    BackHandler(enabled = state.step > 1) { viewModel.previousStep() }

    // Dibuja el contenido con el estado y las acciones conectadas al ViewModel.
    CreatePostContent(
        // Estado actual.
        state = state,
        // Acciones del formulario.
        actions = CreatePostActions(
            // Atrás: retrocede de paso o sale si está en el primero.
            onBack = { if (state.step > 1) viewModel.previousStep() else onBack() },
            // Continuar o publicar.
            onNext = viewModel::onNextClick,
            // Cambio del título.
            onTitleChange = viewModel::onTitleChange,
            // Selección de categoría.
            onCategorySelected = viewModel::onCategorySelected,
            // Cambio de la descripción.
            onDescriptionChange = viewModel::onDescriptionChange,
            // Selección del tipo de animal.
            onAnimalTypeSelected = viewModel::onAnimalTypeSelected,
            // Cambio de la raza.
            onBreedChange = viewModel::onBreedChange,
            // Selección del tamaño.
            onSizeSelected = viewModel::onSizeSelected,
            // Cambio de la edad.
            onAgeChange = viewModel::onAgeChange,
            // Selección de la unidad de edad.
            onAgeUnitSelected = viewModel::onAgeUnitSelected,
            // Cambio de vacunado.
            onVaccinatedChange = viewModel::onVaccinatedChange,
            // Cambio de esterilizado.
            onNeuteredChange = viewModel::onNeuteredChange,
            // Agregar fotos.
            onAddPhotos = viewModel::onAddPhotos,
            // Quitar una foto.
            onRemovePhoto = viewModel::onRemovePhoto,
            // Cambio entre ubicación actual y mapa.
            onUseCurrentLocationChange = viewModel::onUseCurrentLocationChange,
            // Inicio de la localización.
            onLocatingStarted = viewModel::onLocatingStarted,
            // Resultado de la localización.
            onCurrentLocationResult = viewModel::onCurrentLocationResult,
            // Punto elegido en el mapa.
            onMapLocationPicked = viewModel::onMapLocationPicked,
            // Ver la publicación guardada (solo si ya hay id).
            onViewPost = { state.savedPostId?.let(onViewPost) },
            // Volver al inicio.
            onGoHome = onGoHome,
        ),
    )
}

/**
Acciones del formulario; los pasos las reciben en lugar del ViewModel, así se pueden previsualizar.
**/
data class CreatePostActions(
    // Volver atrás.
    val onBack: () -> Unit = {},
    // Continuar / publicar.
    val onNext: () -> Unit = {},
    // Cambio del título.
    val onTitleChange: (String) -> Unit = {},
    // Selección de categoría (posición).
    val onCategorySelected: (Int) -> Unit = {},
    // Cambio de la descripción.
    val onDescriptionChange: (String) -> Unit = {},
    // Selección del tipo de animal (posición).
    val onAnimalTypeSelected: (Int) -> Unit = {},
    // Cambio de la raza.
    val onBreedChange: (String) -> Unit = {},
    // Selección del tamaño (posición).
    val onSizeSelected: (Int) -> Unit = {},
    // Cambio de la edad.
    val onAgeChange: (String) -> Unit = {},
    // Selección de la unidad de edad (posición).
    val onAgeUnitSelected: (Int) -> Unit = {},
    // Cambio de vacunado.
    val onVaccinatedChange: (Boolean) -> Unit = {},
    // Cambio de esterilizado.
    val onNeuteredChange: (Boolean) -> Unit = {},
    // Fotos agregadas.
    val onAddPhotos: (List<String>) -> Unit = {},
    // Foto quitada (posición).
    val onRemovePhoto: (Int) -> Unit = {},
    // Cambio entre ubicación actual y mapa.
    val onUseCurrentLocationChange: (Boolean) -> Unit = {},
    // Inicio de la localización.
    val onLocatingStarted: () -> Unit = {},
    // Resultado de la localización (latitud y longitud, o null).
    val onCurrentLocationResult: (Double?, Double?) -> Unit = { _, _ -> },
    // Punto elegido en el mapa.
    val onMapLocationPicked: (co.edu.uniquindio.petadoptionapp.domain.model.location.Location) -> Unit = {},
    // Ver la publicación guardada.
    val onViewPost: () -> Unit = {},
    // Volver al inicio.
    val onGoHome: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun CreatePostContent(
    // Estado a dibujar.
    state: CreatePostUiState,
    // Acciones que dispara la interfaz.
    actions: CreatePostActions,
) {
    // Columna principal de la pantalla.
    Column(
        // Ocupa toda la pantalla con fondo y respeta barras del sistema y teclado.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceElevated)
            .systemBarsPadding()
            .imePadding(),
    ) {
        // Barra superior. El padding vertical de 4 dp más los 56 dp de la barra dan 64 dp de alto.
        AppTopBar(
            // Título según se esté creando o editando.
            title = stringResource(if (state.isEditing) R.string.edit_post_title else R.string.create_post_title),
            // Acción de volver.
            onBack = actions.onBack,
            // Márgenes de la barra.
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            // Estilo del título.
            titleStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        )

        // Editar: mientras se cargan los datos, o si ya no se puede editar, solo un aviso.
        if (state.isLoading || state.loadError) {
            // Mensaje de estado.
            StatusMessage(
                // Texto de carga o de error.
                text = stringResource(if (state.isLoading) R.string.edit_post_loading else R.string.edit_post_load_error),
                // Se pinta como error si falló la carga.
                isError = state.loadError,
                // Ancho completo con margen.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
            )
            // No dibuja el formulario.
            return@Column
        }

        // Columna desplazable con el contenido del paso.
        Column(
            // Ocupa el espacio restante, se desplaza verticalmente y tiene margen lateral.
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            // Centra horizontalmente el contenido.
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Indicador de progreso de los pasos.
            StepIndicator(
                // Paso actual.
                currentStep = state.step,
                // Total de pasos.
                totalSteps = CREATE_POST_STEPS,
                // Color de los pasos completados.
                completedColor = AppTheme.colors.brand,
                // Ancho de cada segmento.
                segmentWidth = 32.dp,
                // Alto de cada segmento.
                segmentHeight = 5.dp,
                // Separación entre segmentos.
                segmentSpacing = 6.dp,
                // Separación entre la barra y la etiqueta.
                labelSpacing = 4.dp,
                // Estilo de la etiqueta.
                labelStyle = LocalTextStyle.current.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            )
            // Espacio vertical.
            Spacer(modifier = Modifier.height(8.dp))
            // Subtítulo que explica el paso actual.
            Text(
                // Texto del subtítulo según el paso.
                text = stringResource(subtitleFor(state.step)),
                // Centrado.
                textAlign = TextAlign.Center,
                // Tamaño de letra.
                fontSize = 14.sp,
                // Peso de letra.
                fontWeight = FontWeight.Medium,
                // Color de texto principal.
                color = AppTheme.colors.textPrimary,
            )
            // Espacio vertical.
            Spacer(modifier = Modifier.height(24.dp))

            // Dibuja el contenido del paso actual.
            when (state.step) {
                // Paso 1: información.
                1 -> StepInfoContent(state, actions)
                // Paso 2: mascota.
                2 -> StepPetContent(state, actions)
                // Paso 3: fotos.
                3 -> StepPhotosContent(state, actions)
                // Paso 4: ubicación.
                else -> StepLocationContent(state, actions)
            }

            // Espacio final.
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Si hay error al publicar, muestra su mensaje.
        publishErrorText(state.publishError)?.let { message ->
            // Texto de error.
            ErrorText(
                // Mensaje traducido.
                text = message,
                // Ancho completo con margen lateral.
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
        }

        // Botón principal: Continuar, Guardar o Publicar.
        PrimaryButton(
            // Texto según el paso y el modo.
            text = stringResource(
                when {
                    // Antes del último paso: "Continuar".
                    state.step < CREATE_POST_STEPS -> R.string.create_post_next
                    // Último paso editando: "Guardar".
                    state.isEditing -> R.string.edit_post_save
                    // Último paso creando: "Publicar".
                    else -> R.string.create_post_publish
                }
            ),
            // Habilitado solo si el paso es válido.
            enabled = state.isStepValid,
            // Acción de avanzar o publicar.
            onClick = actions.onNext,
            // Márgenes y alto del botón.
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(52.dp),
            // Estilo del texto del botón.
            textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        )
    }

    // Publicada o editada: queda en revisión. Se puede ir a verla o volver al inicio.
    if (state.savedPostId != null) {
        // Diálogo de éxito con dos acciones.
        AppActionDialog(
            // Título según el modo.
            title = stringResource(if (state.isEditing) R.string.edit_post_saved_title else R.string.create_post_sent_title),
            // Mensaje según el modo.
            message = stringResource(if (state.isEditing) R.string.edit_post_saved_message else R.string.create_post_sent_message),
            // Texto resaltado con el tiempo estimado de revisión.
            highlight = stringResource(R.string.create_post_sent_time),
            // Texto del botón principal.
            primaryText = stringResource(R.string.create_post_view_post),
            // Acción del botón principal: ver la publicación.
            onPrimary = actions.onViewPost,
            // Texto del botón secundario.
            secondaryText = stringResource(R.string.create_post_go_home),
            // Acción del botón secundario: ir al inicio.
            onSecondary = actions.onGoHome,
        )
    }
}

// Devuelve el recurso del subtítulo de cada paso.
private fun subtitleFor(step: Int) = when (step) {
    // Paso 1.
    1 -> R.string.create_post_subtitle_info
    // Pasos 2 y 3 comparten subtítulo.
    2, 3 -> R.string.create_post_subtitle_pet
    // Paso 4.
    else -> R.string.create_post_subtitle_location
}

// Traduce el error de publicación a su mensaje (null si no hay error).
@Composable
private fun publishErrorText(error: PublishError?): String? = when (error) {
    // Sin error.
    null -> null
    // Ubicación inválida.
    PublishError.LOCATION -> stringResource(R.string.create_post_error_location)
    // Catálogo inválido.
    PublishError.CATALOG -> stringResource(R.string.create_post_error_catalog)
    // Ya no se puede editar.
    PublishError.NOT_EDITABLE -> stringResource(R.string.edit_post_load_error)
    // Error genérico.
    PublishError.GENERIC -> stringResource(R.string.create_post_error_generic)
}

// Datos de ejemplo solo para las previews.
internal val previewCatalogState = CreatePostUiState(
    // Categorías de ejemplo.
    categories = listOf(Category("adoption", "Adopción"), Category("lost", "Perdidos"), Category("found", "Encontrados")),
    // Tipos de animal de ejemplo.
    animalTypes = listOf(AnimalType("dog", "Perro"), AnimalType("cat", "Gato")),
)

// Vista previa del paso 1.
@Preview(name = "Paso 1", showBackground = true, heightDp = 900)
@Composable
private fun CreatePostStep1Preview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Contenido con título y categoría de ejemplo.
        CreatePostContent(
            // Estado de ejemplo del paso 1.
            state = previewCatalogState.copy(title = "Luna busca hogar", categoryId = "adoption"),
            // Acciones vacías.
            actions = CreatePostActions(),
        )
    }
}

// Vista previa del paso 2.
@Preview(name = "Paso 2", showBackground = true, heightDp = 1000)
@Composable
private fun CreatePostStep2Preview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Contenido con datos de mascota de ejemplo.
        CreatePostContent(
            // Estado de ejemplo del paso 2.
            state = previewCatalogState.copy(step = 2, animalTypeId = "dog", breed = "Criollo", size = AnimalSize.MEDIUM, age = "2", ageUnit = AgeUnit.YEARS, vaccinated = true),
            // Acciones vacías.
            actions = CreatePostActions(),
        )
    }
}

// Vista previa del paso 4.
@Preview(name = "Paso 4", showBackground = true, heightDp = 900)
@Composable
private fun CreatePostStep4Preview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Contenido con una ubicación actual de ejemplo.
        CreatePostContent(
            // Estado de ejemplo del paso 4.
            state = previewCatalogState.copy(
                // Paso 4.
                step = 4,
                // Ubicación de ejemplo en Armenia.
                currentLocation = co.edu.uniquindio.petadoptionapp.domain.model.location.Location(4.53, -75.68, "Armenia", "Colombia", "Calle 20 Norte"),
            ),
            // Acciones vacías.
            actions = CreatePostActions(),
        )
    }
}
