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
Selector de ubicación reutilizable, usado en los formularios que necesitan una dirección con coordenadas
(por ejemplo, al crear o editar una publicación o al completar el perfil).

Combina tres formas de elegir el lugar, todas coordinadas por [LocationPickerViewModel]:
- Una lista desplegable de ciudades ([CityCatalog]) que centra el mapa en la ciudad elegida.
- Un campo de texto que busca direcciones mientras se escribe (geocodificación) y muestra sugerencias debajo.
- Un [AppMap] donde tocar un punto lo selecciona y trae su dirección (geocodificación inversa), marcado con un pin.
Cada vez que la ubicación elegida tiene coordenadas, se avisa a la pantalla mediante el callback.
**/

/**
Elegir una ubicación en el mapa: ciudad (lista desplegable) que centra el mapa, dirección que se busca
escribiendo (con sugerencias) y el mapa, donde tocar un punto lo elige y trae su dirección.
[onLocationChange] recibe la ubicación elegida (con coordenadas) cada vez que cambia.
[key] separa el estado si hay más de un selector en la misma pantalla.
**/
@Composable
fun LocationPicker(
    // Ubicación inicial (por ejemplo, la ya guardada al editar); puede ser nula.
    initial: Location?,
    // Acción que recibe la ubicación elegida cada vez que cambia.
    onLocationChange: (Location) -> Unit,
    // Modificador para el contenedor del selector.
    modifier: Modifier = Modifier,
    // Clave que identifica la instancia del ViewModel.
    key: String = "location-picker",
    // ViewModel inyectado por Hilt, separado por la clave anterior.
    viewModel: LocationPickerViewModel = hiltViewModel(key = key),
) {
    // Observa el estado del ViewModel respetando el ciclo de vida de la pantalla.
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Guarda siempre la versión más reciente del callback de cambio.
    val currentOnChange by rememberUpdatedState(onLocationChange)

    // Al entrar en composición, inicializa el ViewModel con la ubicación inicial (una sola vez).
    LaunchedEffect(Unit) { viewModel.start(initial) }
    // Cuando cambia la ubicación elegida y ya tiene coordenadas, se avisa a la pantalla.
    LaunchedEffect(state.selected) { state.selected?.takeIf { it.hasCoordinates() }?.let(currentOnChange) }

    // Organiza los elementos en columna con 12 dp de separación.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Ciudad: lista desplegable que mueve el mapa a esa ciudad.
        FormDropdown(
            // Etiqueta del campo de ciudad.
            label = stringResource(R.string.map_city_label),
            // Opciones con el formato "Ciudad, País".
            options = CityCatalog.all.map { "${it.name}, ${it.country}" },
            // Índice de la ciudad seleccionada actualmente.
            selectedIndex = state.cityIndex,
            // Al elegir una ciudad, el ViewModel centra el mapa en ella.
            onSelect = viewModel::onCitySelected,
        )

        // Dirección: se busca escribiendo; las sugerencias aparecen debajo.
        Column {
            // Campo de texto de búsqueda de dirección.
            OutlinedTextField(
                // Texto escrito por la persona.
                value = state.query,
                // Cada cambio se envía al ViewModel, que busca sugerencias.
                onValueChange = viewModel::onQueryChange,
                // Ocupa todo el ancho disponible.
                modifier = Modifier.fillMaxWidth(),
                // Etiqueta del campo.
                label = { Text(stringResource(R.string.map_address_label)) },
                // Texto de ayuda cuando el campo está vacío.
                placeholder = { Text(stringResource(R.string.map_address_placeholder)) },
                // Ícono de lupa al inicio del campo.
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                // Mientras se busca o se resuelve una dirección, muestra un indicador de carga al final del campo.
                trailingIcon = if (state.isSearching || state.isResolving) {
                    { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.textPrimary) }
                } else null,
                // Limita el campo a una sola línea.
                singleLine = true,
                // Bordes redondeados.
                shape = RoundedCornerShape(12.dp),
                // Colores comunes de los campos de formulario de la app.
                colors = formFieldColors(),
            )
            // Solo dibuja la lista si hay sugerencias.
            if (state.suggestions.isNotEmpty()) {
                // Contenedor de la lista de sugerencias.
                Column(
                    modifier = Modifier
                        // Pequeña separación respecto al campo.
                        .padding(top = 4.dp)
                        // Ocupa todo el ancho.
                        .fillMaxWidth()
                        // Recorta el contenido con esquinas redondeadas.
                        .clip(RoundedCornerShape(12.dp))
                        // Fondo de superficie del tema.
                        .background(AppTheme.colors.surface)
                        // Borde fino alrededor de la lista.
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp)),
                ) {
                    // Dibuja cada sugerencia con su posición.
                    state.suggestions.forEachIndexed { index, suggestion ->
                        // Separa las sugerencias con una línea, excepto antes de la primera.
                        if (index > 0) HorizontalDivider(color = AppTheme.colors.surfaceTag)
                        // Fila tocable de la sugerencia.
                        Row(
                            modifier = Modifier
                                // Ocupa todo el ancho.
                                .fillMaxWidth()
                                // Al tocarla, el ViewModel la toma como ubicación elegida.
                                .clickable(role = Role.Button) { viewModel.onSuggestionClick(suggestion) }
                                // Espaciado interno de la fila.
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            // Centra verticalmente el contenido.
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Ícono de ubicación con el texto de la sugerencia.
                            IconTextRow(
                                // Ícono de marcador de ubicación.
                                icon = Icons.Outlined.LocationOn,
                                // Une dirección y ciudad, omitiendo las partes vacías.
                                text = listOf(suggestion.address, suggestion.city).filter { it.isNotBlank() }.joinToString(", "),
                                // Tipografía pequeña.
                                style = MaterialTheme.typography.bodySmall,
                                // Máximo dos líneas de texto.
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }

        // Mapa: tocar un punto lo elige.
        val pin = state.selected?.toGeoPoint()
        // Mapa centrado en la cámara del estado, con el pin rojo de la ubicación elegida.
        AppMap(
            // Centro de la cámara definido por el ViewModel.
            center = state.cameraCenter,
            // Zoom cercano, a nivel de calle.
            zoom = 15.0,
            // Si hay punto elegido, dibuja un único marcador tipo pin con el color de error (rojo).
            markers = listOfNotNull(pin?.let { MapMarker("selected", it, AppTheme.colors.stateError, kind = MarkerKind.PIN, icon = Icons.Filled.FilledLocationOn) }),
            // Al tocar el mapa, el ViewModel elige ese punto y busca su dirección.
            onMapClick = viewModel::onMapClick,
            modifier = Modifier
                // Ocupa todo el ancho.
                .fillMaxWidth()
                // Altura fija del mapa.
                .height(240.dp)
                // Recorta el mapa con esquinas redondeadas.
                .clip(RoundedCornerShape(16.dp))
                // Borde fino alrededor del mapa.
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp)),
        )
        // Pista que indica que se puede tocar el mapa para elegir un punto.
        IconTextRow(
            // Ícono de mano tocando.
            icon = Icons.Outlined.TouchApp,
            // Texto de la pista.
            text = stringResource(R.string.map_tap_hint),
            // Ícono más pequeño que el predeterminado.
            iconSize = 14.dp,
            // Separación entre ícono y texto.
            spacing = 6.dp,
            // Tipografía pequeña.
            style = MaterialTheme.typography.bodySmall,
        )
        // Si la ubicación elegida tiene dirección, la muestra como confirmación.
        state.selected?.takeIf { it.address.isNotBlank() }?.let { selected ->
            // Texto "Seleccionado: dirección, ciudad".
            Text(
                // Une dirección y ciudad, omitiendo las partes vacías.
                text = stringResource(R.string.map_selected, listOf(selected.address, selected.city).filter { it.isNotBlank() }.joinToString(", ")),
                // Tipografía de etiqueta mediana.
                style = MaterialTheme.typography.labelMedium,
                // Color de texto principal del tema.
                color = AppTheme.colors.textPrimary,
                // Máximo dos líneas.
                maxLines = 2,
                // Si no cabe, termina con puntos suspensivos.
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
