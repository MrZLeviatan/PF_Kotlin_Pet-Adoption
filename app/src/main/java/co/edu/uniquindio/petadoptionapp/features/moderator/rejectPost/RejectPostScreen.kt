package co.edu.uniquindio.petadoptionapp.features.moderator.rejectpost

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.card.NoticeBanner
import co.edu.uniquindio.petadoptionapp.core.component.form.AppChipGroup
import co.edu.uniquindio.petadoptionapp.core.component.form.AppTextArea
import co.edu.uniquindio.petadoptionapp.core.component.form.RequiredLabel
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.PostValidator
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

/**
Pantalla de rechazo de una publicación por parte del moderador que la tiene en revisión.
Observa RejectPostUiState desde RejectPostViewModel: motivo elegido (chips), descripción con su error,
si hay un envío en curso y si falló el último intento. Envía al ViewModel los eventos onReasonSelected,
onDescriptionChange y onConfirmClick (con el texto del motivo ya resuelto desde strings.xml).
Caso de uso (vía ViewModel): RejectPostUseCase, que guarda el motivo y notifica al autor.
Navegación: llega por la ruta MainRoutes.RejectPost con el postId; onBack vuelve atrás y, cuando el
ViewModel emite RejectPostEvent.Rejected, se llama a onRejected para que el grafo salga de la pantalla.
**/

// Margen horizontal de la pantalla.
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla donde el moderador indica por qué rechaza una publicación (la que tiene en revisión).
Al confirmar llama a RejectPostUseCase, que guarda el motivo y notifica al autor.
Conecta el [RejectPostViewModel] con la UI; [RejectPostContent] solo dibuja.
 */
@Composable
fun RejectPostScreen(
    // Acción para volver atrás.
    onBack: () -> Unit,
    // Acción a ejecutar cuando el rechazo se completó.
    onRejected: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: RejectPostViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Mantiene la última versión del callback para usarla dentro del efecto.
    val currentOnRejected by rememberUpdatedState(onRejected)
    // Texto localizado del motivo elegido (vacío si no hay ninguno).
    val reasonLabel = uiState.selectedReason?.let { stringResource(it.labelRes()) }.orEmpty()

    // Recolecta los eventos de un solo uso del ViewModel mientras la pantalla existe.
    LaunchedEffect(viewModel) {
        // Escucha cada evento emitido.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Al rechazar con éxito, avisa a la navegación.
                RejectPostEvent.Rejected -> currentOnRejected()
            }
        }
    }

    // Dibuja el formulario conectando estado y eventos.
    RejectPostContent(
        // Estado actual.
        uiState = uiState,
        // Acción de volver.
        onBack = onBack,
        // Selección de motivo hacia el ViewModel.
        onReasonSelected = viewModel::onReasonSelected,
        // Cambios de descripción hacia el ViewModel.
        onDescriptionChange = viewModel::onDescriptionChange,
        // Confirmación enviando el texto del motivo.
        onConfirmClick = { viewModel.onConfirmClick(reasonLabel) },
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun RejectPostContent(
    // Estado del formulario.
    uiState: RejectPostUiState,
    // Acción de volver.
    onBack: () -> Unit,
    // Acción al elegir un motivo.
    onReasonSelected: (RejectReason) -> Unit,
    // Acción al cambiar la descripción.
    onDescriptionChange: (String) -> Unit,
    // Acción al confirmar el rechazo.
    onConfirmClick: () -> Unit,
) {
    // Todos los motivos posibles en orden.
    val reasons = RejectReason.entries
    // Textos localizados de cada motivo para los chips.
    val reasonLabels = reasons.map { stringResource(it.labelRes()) }

    // Columna principal de la pantalla.
    Column(
        // Ocupa todo, pinta el fondo y respeta las barras del sistema.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
            // Sube el contenido cuando aparece el teclado, para no tapar el botón.
            .imePadding(),
    ) {
        // Barra superior con título y botón de volver.
        AppTopBar(
            // Título de la pantalla.
            title = stringResource(R.string.reject_title),
            // Acción de volver.
            onBack = onBack,
            // Margen horizontal.
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // El formulario se desplaza; el botón queda fijo abajo.
        Column(
            // Ocupa el espacio restante, permite desplazamiento y aplica márgenes.
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            // Separación entre elementos.
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Aviso informativo sobre el rechazo.
            NoticeBanner(message = stringResource(R.string.reject_notice))

            // Etiqueta obligatoria de la sección de motivos.
            RequiredLabel(
                // Texto de la etiqueta.
                text = stringResource(R.string.reject_reasons_title),
                // Margen superior.
                modifier = Modifier.padding(top = 12.dp),
            )

            // Grupo de chips con los motivos (selección única).
            AppChipGroup(
                // Textos de los chips.
                labels = reasonLabels,
                // Índice del motivo elegido (null si ninguno).
                selectedIndex = uiState.selectedReason?.let { reasons.indexOf(it) },
                // Convierte el índice tocado en el motivo y lo notifica.
                onSelected = { index -> onReasonSelected(reasons[index]) },
            )

            // Etiqueta obligatoria de la descripción.
            RequiredLabel(
                // Texto de la etiqueta.
                text = stringResource(R.string.reject_description_title),
                // Margen superior.
                modifier = Modifier.padding(top = 12.dp),
            )

            // Área de texto para la descripción del rechazo.
            AppTextArea(
                // Texto actual.
                value = uiState.description,
                // Notifica cada cambio.
                onValueChange = onDescriptionChange,
                // Texto de ayuda.
                placeholder = stringResource(R.string.reject_description_placeholder),
                // Longitud máxima permitida (muestra el contador).
                maxLength = RejectPostUiState.MAX_DESCRIPTION_LENGTH,
                // Mensaje de error según el tipo de error de la descripción.
                errorText = when (uiState.descriptionError) {
                    // Sin error.
                    null -> null
                    // Campo obligatorio vacío.
                    DescriptionError.EMPTY -> stringResource(R.string.create_post_error_required)
                    // Texto demasiado corto, indicando el mínimo.
                    DescriptionError.TOO_SHORT ->
                        stringResource(R.string.create_post_error_min, PostValidator.MIN_REJECTION_DESCRIPTION_LENGTH)
                },
            )

            // Si el rechazo falló, muestra un mensaje de error.
            if (uiState.hasError) ErrorText(text = stringResource(R.string.mod_reject_error))
        }

        // Botón fijo para confirmar el rechazo.
        PrimaryButton(
            // Texto del botón.
            text = stringResource(R.string.reject_confirm_button),
            // Acción al pulsarlo.
            onClick = onConfirmClick,
            // Solo habilitado con el formulario válido y sin envío en curso.
            enabled = uiState.isFormFilled,
            // Márgenes alrededor del botón.
            modifier = Modifier.padding(
                horizontal = ScreenHorizontalPadding,
                vertical = 16.dp,
            ),
        )
    }
}

// Devuelve el recurso de texto de cada motivo de rechazo.
@StringRes
private fun RejectReason.labelRes() = when (this) {
    // Información insuficiente.
    RejectReason.INSUFFICIENT_INFO -> R.string.reject_reason_insufficient_info
    // Imagen no válida.
    RejectReason.INVALID_IMAGE -> R.string.reject_reason_invalid_image
    // Contenido sospechoso.
    RejectReason.SUSPICIOUS_CONTENT -> R.string.reject_reason_suspicious_content
    // Posible venta de animales.
    RejectReason.POSSIBLE_ANIMAL_SALE -> R.string.reject_reason_animal_sale
    // Ubicación incorrecta.
    RejectReason.WRONG_LOCATION -> R.string.reject_reason_wrong_location
    // Publicación duplicada.
    RejectReason.DUPLICATED -> R.string.reject_reason_duplicated
}

// Vista previa del formulario vacío.
@Preview(name = "Vacío", showBackground = true, heightDp = 800)
@Composable
private fun RejectPostEmptyPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Contenido con el estado inicial.
        RejectPostContent(
            // Estado vacío.
            uiState = RejectPostUiState(),
            // Callbacks vacíos para la vista previa.
            onBack = {}, onReasonSelected = {}, onDescriptionChange = {}, onConfirmClick = {},
        )
    }
}

// Vista previa del formulario con motivo y descripción.
@Preview(name = "Con motivo y descripción", showBackground = true, heightDp = 800)
@Composable
private fun RejectPostFilledPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Contenido con un estado diligenciado.
        RejectPostContent(
            // Estado con motivo y descripción de ejemplo.
            uiState = RejectPostUiState(
                // Motivo elegido.
                selectedReason = RejectReason.SUSPICIOUS_CONTENT,
                // Descripción de ejemplo.
                description = "Las fotos parecen tomadas de otra publicación y el teléfono no responde.",
            ),
            // Callbacks vacíos para la vista previa.
            onBack = {}, onReasonSelected = {}, onDescriptionChange = {}, onConfirmClick = {},
        )
    }
}
