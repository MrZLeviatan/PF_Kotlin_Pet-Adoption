package co.edu.uniquindio.petadoptionapp.features.user.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.button.SecondaryButton
import co.edu.uniquindio.petadoptionapp.core.component.form.AppChipGroup
import co.edu.uniquindio.petadoptionapp.core.component.form.FormDropdown
import co.edu.uniquindio.petadoptionapp.core.component.form.SwitchRow
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.location.CityCatalog
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize

/**
 Pantalla "Filtros" de Explorar (como en el diseño): categoría, tipo de animal, tamaño, vacunas al día,
 ciudad y distancia máxima. Abajo dice cuántas mascotas hay con esos filtros y "Aplicar filtros (N)".
 Tocar de nuevo un filtro elegido lo quita.
 */
@Composable
internal fun ExploreFiltersDialog(state: ExploreUiState, viewModel: ExploreViewModel) {
    val draft = state.draft
    Dialog(
        onDismissRequest = viewModel::onCloseFilters,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .systemBarsPadding(),
        ) {
            AppTopBar(
                title = stringResource(R.string.explore_filters),
                onBack = viewModel::onCloseFilters,
                modifier = Modifier
                    .background(AppTheme.colors.brand)
                    .padding(horizontal = 8.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SecondaryButton(
                    text = stringResource(R.string.explore_clear_filters),
                    icon = Icons.Outlined.Delete,
                    onClick = viewModel::onClearDraft,
                )

                FilterSection(stringResource(R.string.explore_filter_category)) {
                    AppChipGroup(
                        labels = state.categories.map { it.name },
                        selectedIndex = state.categories.indexOfFirst { it.id == draft.categoryId }.takeIf { it >= 0 },
                        onSelected = { i ->
                            val id = state.categories[i].id
                            viewModel.onDraftChange { f -> f.copy(categoryId = id.takeIf { id != f.categoryId }) }
                        },
                    )
                }
                FilterSection(stringResource(R.string.explore_filter_animal)) {
                    AppChipGroup(
                        labels = state.animalTypes.map { it.name },
                        selectedIndex = state.animalTypes.indexOfFirst { it.id == draft.animalTypeId }.takeIf { it >= 0 },
                        onSelected = { i ->
                            val id = state.animalTypes[i].id
                            viewModel.onDraftChange { f -> f.copy(animalTypeId = id.takeIf { id != f.animalTypeId }) }
                        },
                    )
                }
                FilterSection(stringResource(R.string.create_post_size_label)) {
                    val sizes = AnimalSize.entries
                    AppChipGroup(
                        labels = sizes.map { sizeText(it) },
                        selectedIndex = draft.size?.let { sizes.indexOf(it) },
                        onSelected = { i -> viewModel.onDraftChange { f -> f.copy(size = sizes[i].takeIf { s -> s != f.size }) } },
                    )
                }
                FilterSection(stringResource(R.string.explore_filter_vaccines)) {
                    SwitchRow(
                        title = stringResource(R.string.explore_filter_vaccines_only),
                        checked = draft.vaccinatedOnly,
                        onCheckedChange = { checked -> viewModel.onDraftChange { it.copy(vaccinatedOnly = checked) } },
                    )
                }
                FilterSection(stringResource(R.string.explore_filter_location)) {
                    // Primera opción: alrededor de mi ubicación; luego las ciudades.
                    val options = listOf(stringResource(R.string.explore_near_me)) + CityCatalog.all.map { "${it.name}, ${it.country}" }
                    FormDropdown(
                        label = stringResource(R.string.map_city_label),
                        options = options,
                        selectedIndex = draft.city?.let { CityCatalog.all.indexOf(it) + 1 } ?: 0,
                        onSelect = { i -> viewModel.onDraftChange { it.copy(city = CityCatalog.all.getOrNull(i - 1)) } },
                    )
                }
                FilterSection(stringResource(R.string.explore_filter_distance, draft.radiusKm)) {
                    Slider(
                        value = draft.radiusKm.toFloat(),
                        onValueChange = { v -> viewModel.onDraftChange { it.copy(radiusKm = v.toInt().coerceAtLeast(1)) } },
                        valueRange = 1f..ExploreFilters.MAX_RADIUS_KM.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = AppTheme.colors.actionSecondary,
                            activeTrackColor = AppTheme.colors.actionSecondary,
                            inactiveTrackColor = AppTheme.colors.surfaceTag,
                        ),
                    )
                }
            }

            HorizontalDivider(color = AppTheme.colors.border)
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = pluralStringResource(R.plurals.explore_found, state.draftCount, state.draftCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                PrimaryButton(
                    text = stringResource(R.string.explore_apply_filters, state.draftCount),
                    onClick = viewModel::onApplyFilters,
                )
            }
        }
    }
}

// Título de cada grupo de filtros, separado del anterior por una línea (como en el diseño).
@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(color = AppTheme.colors.border.copy(alpha = 0.5f))
        Row { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) }
        content()
    }
}
