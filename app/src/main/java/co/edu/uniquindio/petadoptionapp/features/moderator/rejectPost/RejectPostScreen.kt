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

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla donde el moderador indica por qué rechaza una publicación (la que tiene en revisión).
Al confirmar llama a RejectPostUseCase, que guarda el motivo y notifica al autor.
Conecta el [RejectPostViewModel] con la UI; [RejectPostContent] solo dibuja.
 */
@Composable
fun RejectPostScreen(
    onBack: () -> Unit,
    onRejected: () -> Unit,
    viewModel: RejectPostViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnRejected by rememberUpdatedState(onRejected)
    val reasonLabel = uiState.selectedReason?.let { stringResource(it.labelRes()) }.orEmpty()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RejectPostEvent.Rejected -> currentOnRejected()
            }
        }
    }

    RejectPostContent(
        uiState = uiState,
        onBack = onBack,
        onReasonSelected = viewModel::onReasonSelected,
        onDescriptionChange = viewModel::onDescriptionChange,
        onConfirmClick = { viewModel.onConfirmClick(reasonLabel) },
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun RejectPostContent(
    uiState: RejectPostUiState,
    onBack: () -> Unit,
    onReasonSelected: (RejectReason) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
) {
    val reasons = RejectReason.entries
    val reasonLabels = reasons.map { stringResource(it.labelRes()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
            // Sube el contenido cuando aparece el teclado, para no tapar el botón.
            .imePadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.reject_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // El formulario se desplaza; el botón queda fijo abajo.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NoticeBanner(message = stringResource(R.string.reject_notice))

            RequiredLabel(
                text = stringResource(R.string.reject_reasons_title),
                modifier = Modifier.padding(top = 12.dp),
            )

            AppChipGroup(
                labels = reasonLabels,
                selectedIndex = uiState.selectedReason?.let { reasons.indexOf(it) },
                onSelected = { index -> onReasonSelected(reasons[index]) },
            )

            RequiredLabel(
                text = stringResource(R.string.reject_description_title),
                modifier = Modifier.padding(top = 12.dp),
            )

            AppTextArea(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.reject_description_placeholder),
                maxLength = RejectPostUiState.MAX_DESCRIPTION_LENGTH,
                errorText = when (uiState.descriptionError) {
                    null -> null
                    DescriptionError.EMPTY -> stringResource(R.string.create_post_error_required)
                    DescriptionError.TOO_SHORT ->
                        stringResource(R.string.create_post_error_min, PostValidator.MIN_REJECTION_DESCRIPTION_LENGTH)
                },
            )

            if (uiState.hasError) ErrorText(text = stringResource(R.string.mod_reject_error))
        }

        PrimaryButton(
            text = stringResource(R.string.reject_confirm_button),
            onClick = onConfirmClick,
            enabled = uiState.isFormFilled,
            modifier = Modifier.padding(
                horizontal = ScreenHorizontalPadding,
                vertical = 16.dp,
            ),
        )
    }
}

@StringRes
private fun RejectReason.labelRes() = when (this) {
    RejectReason.INSUFFICIENT_INFO -> R.string.reject_reason_insufficient_info
    RejectReason.INVALID_IMAGE -> R.string.reject_reason_invalid_image
    RejectReason.SUSPICIOUS_CONTENT -> R.string.reject_reason_suspicious_content
    RejectReason.POSSIBLE_ANIMAL_SALE -> R.string.reject_reason_animal_sale
    RejectReason.WRONG_LOCATION -> R.string.reject_reason_wrong_location
    RejectReason.DUPLICATED -> R.string.reject_reason_duplicated
}

@Preview(name = "Vacío", showBackground = true, heightDp = 800)
@Composable
private fun RejectPostEmptyPreview() {
    PetAdoptionAppTheme {
        RejectPostContent(
            uiState = RejectPostUiState(),
            onBack = {}, onReasonSelected = {}, onDescriptionChange = {}, onConfirmClick = {},
        )
    }
}

@Preview(name = "Con motivo y descripción", showBackground = true, heightDp = 800)
@Composable
private fun RejectPostFilledPreview() {
    PetAdoptionAppTheme {
        RejectPostContent(
            uiState = RejectPostUiState(
                selectedReason = RejectReason.SUSPICIOUS_CONTENT,
                description = "Las fotos parecen tomadas de otra publicación y el teléfono no responde.",
            ),
            onBack = {}, onReasonSelected = {}, onDescriptionChange = {}, onConfirmClick = {},
        )
    }
}