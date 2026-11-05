package co.edu.uniquindio.petadoptionapp.core.component.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn as FilledLocationOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.form.FormDropdown
import co.edu.uniquindio.petadoptionapp.core.component.form.formFieldColors
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.location.CityCatalog
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location

/**
 Elegir una ubicación en el mapa: ciudad (lista desplegable) que centra el mapa, dirección que se busca
 escribiendo (con sugerencias) y el mapa, donde tocar un punto lo elige y trae su dirección.
 [onLocationChange] recibe la ubicación elegida (con coordenadas) cada vez que cambia.
 [key] separa el estado si hay más de un selector en la misma pantalla.
 */
@Composable
fun LocationPicker(
    initial: Location?,
    onLocationChange: (Location) -> Unit,
    modifier: Modifier = Modifier,
    key: String = "location-picker",
    viewModel: LocationPickerViewModel = hiltViewModel(key = key),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnChange by rememberUpdatedState(onLocationChange)

    LaunchedEffect(Unit) { viewModel.start(initial) }
    LaunchedEffect(state.selected) { state.selected?.takeIf { it.hasCoordinates() }?.let(currentOnChange) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Ciudad: lista desplegable que mueve el mapa a esa ciudad.
        FormDropdown(
            label = stringResource(R.string.map_city_label),
            options = CityCatalog.all.map { "${it.name}, ${it.country}" },
            selectedIndex = state.cityIndex,
            onSelect = viewModel::onCitySelected,
        )

        // Dirección: se busca escribiendo; las sugerencias aparecen debajo.
        Column {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.map_address_label)) },
                placeholder = { Text(stringResource(R.string.map_address_placeholder)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = if (state.isSearching || state.isResolving) {
                    { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.textPrimary) }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors(),
            )
            if (state.suggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp)),
                ) {
                    state.suggestions.forEachIndexed { index, suggestion ->
                        if (index > 0) HorizontalDivider(color = AppTheme.colors.surfaceTag)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) { viewModel.onSuggestionClick(suggestion) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconTextRow(
                                icon = Icons.Outlined.LocationOn,
                                text = listOf(suggestion.address, suggestion.city).filter { it.isNotBlank() }.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }

        // Mapa: tocar un punto lo elige.
        val pin = state.selected?.toGeoPoint()
        AppMap(
            center = state.cameraCenter,
            zoom = 15.0,
            markers = listOfNotNull(pin?.let { MapMarker("selected", it, AppTheme.colors.stateError, kind = MarkerKind.PIN, icon = Icons.Filled.FilledLocationOn) }),
            onMapClick = viewModel::onMapClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp)),
        )
        IconTextRow(
            icon = Icons.Outlined.TouchApp,
            text = stringResource(R.string.map_tap_hint),
            iconSize = 14.dp,
            spacing = 6.dp,
            style = MaterialTheme.typography.bodySmall,
        )
        state.selected?.takeIf { it.address.isNotBlank() }?.let { selected ->
            Text(
                text = stringResource(R.string.map_selected, listOf(selected.address, selected.city).filter { it.isNotBlank() }.joinToString(", ")),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
