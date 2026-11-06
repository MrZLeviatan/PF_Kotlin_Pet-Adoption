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
Pantalla "Filtros" de Explorar (como en el diseño), mostrada como diálogo a pantalla completa:
categoría, tipo de animal, tamaño, vacunas al día, ciudad y distancia máxima. Abajo dice cuántas
mascotas hay con esos filtros y el botón "Aplicar filtros (N)". Tocar de nuevo un filtro elegido lo quita.
Estado: lee ExploreUiState (el borrador draft, su conteo draftCount y los catálogos de categorías y
tipos de animal). Eventos: cada cambio se envía a ExploreViewModel.onDraftChange; además
onClearDraft (limpiar), onCloseFilters (volver sin aplicar) y onApplyFilters (aplicar).
Casos de uso: no los llama directamente; el ViewModel usa GetFeedUseCase para contar resultados.
Navegación: no navega; se abre y se cierra desde ExploreScreen según state.showFilters.
**/

// Diálogo de filtros de Explorar; recibe el estado y el ViewModel para enviar los cambios.
@Composable
internal fun ExploreFiltersDialog(state: ExploreUiState, viewModel: ExploreViewModel) {
    // Borrador de filtros que se está editando.
    val draft = state.draft
    // Diálogo de Compose que contiene toda la pantalla de filtros.
    Dialog(
        // Cerrar el diálogo (atrás o fuera) cierra los filtros sin aplicar.
        onDismissRequest = viewModel::onCloseFilters,
        // Desactiva el ancho por defecto para que ocupe toda la pantalla.
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Columna principal: barra superior, filtros y pie con el botón de aplicar.
        Column(
            modifier = Modifier
                // Ocupa toda la pantalla.
                .fillMaxSize()
                // Fondo con el color de fondo del tema.
                .background(AppTheme.colors.background)
                // Respeta las barras del sistema (estado y navegación).
                .systemBarsPadding(),
        ) {
            // Barra superior con título y botón de volver.
            AppTopBar(
                // Título "Filtros".
                title = stringResource(R.string.explore_filters),
                // Volver cierra los filtros sin aplicar.
                onBack = viewModel::onCloseFilters,
                modifier = Modifier
                    // Fondo con el color de marca.
                    .background(AppTheme.colors.brand)
                    // Margen horizontal.
                    .padding(horizontal = 8.dp),
            )
            // Columna desplazable con los grupos de filtros.
            Column(
                modifier = Modifier
                    // Ocupa el espacio entre la barra superior y el pie.
                    .weight(1f)
                    // Permite desplazamiento vertical si no caben todos los filtros.
                    .verticalScroll(rememberScrollState())
                    // Márgenes internos.
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                // Separación entre grupos.
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Botón para limpiar todos los filtros del borrador.
                SecondaryButton(
                    // Texto "Limpiar filtros".
                    text = stringResource(R.string.explore_clear_filters),
                    // Ícono de papelera.
                    icon = Icons.Outlined.Delete,
                    // Restablece el borrador.
                    onClick = viewModel::onClearDraft,
                )

                // Grupo "Categoría".
                FilterSection(stringResource(R.string.explore_filter_category)) {
                    // Chips de selección única con las categorías.
                    AppChipGroup(
                        // Nombres de las categorías.
                        labels = state.categories.map { it.name },
                        // Índice de la categoría del borrador; null si no hay ninguna elegida.
                        selectedIndex = state.categories.indexOfFirst { it.id == draft.categoryId }.takeIf { it >= 0 },
                        // Al tocar un chip:
                        onSelected = { i ->
                            // Id de la categoría tocada.
                            val id = state.categories[i].id
                            // La elige, o la quita si ya estaba elegida.
                            viewModel.onDraftChange { f -> f.copy(categoryId = id.takeIf { id != f.categoryId }) }
                        },
                        // El mismo ícono y color que su marcador en el mapa.
                        leading = { i -> CategoryMarkerIcon(state.categories[i]) },
                    )
                }
                // Grupo "Tipo de animal".
                FilterSection(stringResource(R.string.explore_filter_animal)) {
                    // Chips de selección única con los tipos de animal.
                    AppChipGroup(
                        // Nombres de los tipos de animal.
                        labels = state.animalTypes.map { it.name },
                        // Índice del tipo del borrador; null si no hay ninguno elegido.
                        selectedIndex = state.animalTypes.indexOfFirst { it.id == draft.animalTypeId }.takeIf { it >= 0 },
                        // Al tocar un chip:
                        onSelected = { i ->
                            // Id del tipo tocado.
                            val id = state.animalTypes[i].id
                            // Lo elige, o lo quita si ya estaba elegido.
                            viewModel.onDraftChange { f -> f.copy(animalTypeId = id.takeIf { id != f.animalTypeId }) }
                        },
                    )
                }
                // Grupo "Tamaño".
                FilterSection(stringResource(R.string.create_post_size_label)) {
                    // Todos los tamaños posibles del enum.
                    val sizes = AnimalSize.entries
                    // Chips de selección única con los tamaños.
                    AppChipGroup(
                        // Texto traducido de cada tamaño.
                        labels = sizes.map { sizeText(it) },
                        // Índice del tamaño del borrador, si hay.
                        selectedIndex = draft.size?.let { sizes.indexOf(it) },
                        // Elige el tamaño tocado, o lo quita si ya estaba elegido.
                        onSelected = { i -> viewModel.onDraftChange { f -> f.copy(size = sizes[i].takeIf { s -> s != f.size }) } },
                    )
                }
                // Grupo "Vacunas".
                FilterSection(stringResource(R.string.explore_filter_vaccines)) {
                    // Interruptor para mostrar solo animales vacunados.
                    SwitchRow(
                        // Texto del interruptor.
                        title = stringResource(R.string.explore_filter_vaccines_only),
                        // Estado actual en el borrador.
                        checked = draft.vaccinatedOnly,
                        // Guarda el nuevo valor en el borrador.
                        onCheckedChange = { checked -> viewModel.onDraftChange { it.copy(vaccinatedOnly = checked) } },
                    )
                }
                // Grupo "Ubicación".
                FilterSection(stringResource(R.string.explore_filter_location)) {
                    // Primera opción: alrededor de mi ubicación; luego las ciudades.
                    val options = listOf(stringResource(R.string.explore_near_me)) + CityCatalog.all.map { "${it.name}, ${it.country}" }
                    // Lista desplegable de ubicaciones.
                    FormDropdown(
                        // Etiqueta "Ciudad".
                        label = stringResource(R.string.map_city_label),
                        // Opciones disponibles.
                        options = options,
                        // Índice elegido: la ciudad del borrador desplazada en 1 (por "cerca de mí"), o 0 si no hay ciudad.
                        selectedIndex = draft.city?.let { CityCatalog.all.indexOf(it) + 1 } ?: 0,
                        // Guarda la ciudad elegida (índice 0 deja la ciudad en null = cerca de mí).
                        onSelect = { i -> viewModel.onDraftChange { it.copy(city = CityCatalog.all.getOrNull(i - 1)) } },
                    )
                }
                // Grupo "Distancia máxima: N km".
                FilterSection(stringResource(R.string.explore_filter_distance, draft.radiusKm)) {
                    // Control deslizante para el radio de búsqueda.
                    Slider(
                        // Valor actual del radio.
                        value = draft.radiusKm.toFloat(),
                        // Guarda el nuevo radio como entero, mínimo 1 km.
                        onValueChange = { v -> viewModel.onDraftChange { it.copy(radiusKm = v.toInt().coerceAtLeast(1)) } },
                        // Rango permitido: de 1 km al máximo definido.
                        valueRange = 1f..ExploreFilters.MAX_RADIUS_KM.toFloat(),
                        // Colores del control según el tema.
                        colors = SliderDefaults.colors(
                            // Color del indicador.
                            thumbColor = AppTheme.colors.actionSecondary,
                            // Color de la parte activa de la pista.
                            activeTrackColor = AppTheme.colors.actionSecondary,
                            // Color de la parte inactiva de la pista.
                            inactiveTrackColor = AppTheme.colors.surfaceTag,
                        ),
                    )
                }
            }

            // Línea que separa los filtros del pie.
            HorizontalDivider(color = AppTheme.colors.border)
            // Pie con el conteo de resultados y el botón de aplicar.
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Texto "Hay N mascotas encontradas".
                Text(
                    // Texto en plural según el conteo del borrador.
                    text = pluralStringResource(R.plurals.explore_found, state.draftCount, state.draftCount),
                    // Estilo de cuerpo mediano.
                    style = MaterialTheme.typography.bodyMedium,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                    // Texto centrado.
                    textAlign = TextAlign.Center,
                    // Ocupa todo el ancho.
                    modifier = Modifier.fillMaxWidth(),
                )
                // Espacio antes del botón.
                Spacer(Modifier.height(10.dp))
                // Botón principal "Aplicar filtros (N)".
                PrimaryButton(
                    // Texto con el conteo.
                    text = stringResource(R.string.explore_apply_filters, state.draftCount),
                    // Aplica el borrador.
                    onClick = viewModel::onApplyFilters,
                )
            }
        }
    }
}

// Título de cada grupo de filtros, separado del anterior por una línea (como en el diseño).
@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    // Columna con línea, título y contenido del grupo.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Línea divisoria semitransparente.
        HorizontalDivider(color = AppTheme.colors.border.copy(alpha = 0.5f))
        // Título del grupo en negrita.
        Row { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) }
        // Contenido del grupo (chips, interruptor, lista o control deslizante).
        content()
    }
}
