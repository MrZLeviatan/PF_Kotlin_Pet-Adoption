package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import co.edu.uniquindio.petadoptionapp.core.component.button.SecondaryButton
import co.edu.uniquindio.petadoptionapp.core.component.map.AppMap
import co.edu.uniquindio.petadoptionapp.core.component.map.LocationPicker
import co.edu.uniquindio.petadoptionapp.core.component.map.MapMarker
import co.edu.uniquindio.petadoptionapp.core.component.map.MarkerKind
import co.edu.uniquindio.petadoptionapp.core.component.map.rememberCurrentLocationRequester
import co.edu.uniquindio.petadoptionapp.core.component.map.toGeoPoint
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.FormCard
import co.edu.uniquindio.petadoptionapp.core.component.card.InfoTip
import co.edu.uniquindio.petadoptionapp.core.component.form.OptionSelector
import co.edu.uniquindio.petadoptionapp.core.component.form.SelectorOption
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostActions
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState

/**
Paso 4 del asistente de crear/editar publicación: dónde está o fue vista la mascota.
- "Ubicación actual": pide el permiso de ubicación, lee el GPS y busca la dirección; se ve en el mapa.
- "Seleccionar mapa": la ciudad (lista desplegable) y la dirección (buscada o tocando el mapa) fijan el punto.
En la publicación se muestra solo una zona aproximada, nunca el punto exacto.
Lee el estado de CreatePostUiState (modo elegido, ubicación actual, punto del mapa, banderas
de localización) y avisa los cambios a CreatePostViewModel mediante CreatePostActions.
**/
@Composable
fun StepLocationContent(state: CreatePostUiState, actions: CreatePostActions) {
    // Crea el lanzador que pide permiso y lee la ubicación actual; entrega el punto (o null) al ViewModel.
    val requestLocation = rememberCurrentLocationRequester { point ->
        // Envía latitud y longitud (null si no se pudo obtener).
        actions.onCurrentLocationResult(point?.latitude, point?.longitude)
    }
    // Acción reutilizable para localizar: marca el inicio y lanza la solicitud.
    val locate = {
        // Avisa al ViewModel que empezó la búsqueda de ubicación.
        actions.onLocatingStarted()
        // Pide la ubicación al sistema.
        requestLocation()
    }

    // Tarjeta del formulario con el título del paso.
    FormCard(title = stringResource(R.string.create_post_step_location)) {
        // Texto de ayuda del paso.
        Text(stringResource(R.string.create_post_location_hint), fontSize = 13.sp, color = AppTheme.colors.textPrimary)

        // Espacio vertical.
        Spacer(modifier = Modifier.height(12.dp))

        // Selector entre "Ubicación actual" y "Seleccionar en mapa".
        OptionSelector(
            // Las dos opciones con su ícono.
            options = listOf(
                SelectorOption(stringResource(R.string.create_post_location_current), Icons.Outlined.MyLocation),
                SelectorOption(stringResource(R.string.create_post_location_map), Icons.Outlined.Map),
            ),
            // 0 si usa la ubicación actual, 1 si usa el mapa.
            selectedIndex = if (state.useCurrentLocation) 0 else 1,
            // Al elegir una opción.
            onSelect = { index ->
                // Avisa al ViewModel el modo elegido.
                actions.onUseCurrentLocationChange(index == 0)
                // Al elegir "Ubicación actual" por primera vez, se pide de una vez.
                if (index == 0 && state.currentLocation == null && !state.isLocating) locate()
            },
            // Las opciones ocupan todo el ancho.
            fillWidth = true,
        )

        // Espacio vertical.
        Spacer(modifier = Modifier.height(12.dp))

        // Muestra la sección según el modo elegido.
        if (state.useCurrentLocation) {
            // Sección de ubicación actual (mapa fijo, dirección y botón).
            CurrentLocationSection(state = state, onLocate = locate)
        } else {
            // Selector de ubicación en el mapa (ciudad, búsqueda y toque).
            LocationPicker(
                // Punto inicial ya elegido, si lo hay.
                initial = state.mapLocation,
                // Avisa al ViewModel el punto elegido.
                onLocationChange = actions.onMapLocationPicked,
                // Clave para conservar el estado del selector.
                key = "post-location",
            )
        }
    }

    // Espacio vertical.
    Spacer(modifier = Modifier.height(16.dp))

    // Aviso de privacidad: solo se muestra una zona aproximada.
    InfoTip(icon = Icons.Outlined.VerifiedUser, text = stringResource(R.string.create_post_location_privacy))
}

// Ubicación actual: el mapa con el punto y su dirección, o el botón para obtenerla (y el aviso si no se pudo).
@Composable
private fun CurrentLocationSection(state: CreatePostUiState, onLocate: () -> Unit) {
    // Ubicación actual obtenida (o null).
    val current = state.currentLocation
    // Convierte la ubicación a punto geográfico para el mapa.
    val point = current?.toGeoPoint()
    // Columna con mapa, dirección, estado y botón.
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Si ya hay punto, muestra el mapa y la dirección.
        if (point != null) {
            // Mapa centrado en la ubicación actual.
            AppMap(
                // Centro del mapa.
                center = point,
                // Nivel de acercamiento.
                zoom = 15.0,
                // Un marcador tipo pin en la ubicación actual.
                markers = listOf(MapMarker("current", point, AppTheme.colors.stateError, kind = MarkerKind.PIN, icon = Icons.Filled.MyLocation)),
                // Mapa sin interacción (solo para ver).
                interactive = false,
                // Ancho completo, alto fijo, esquinas redondeadas y borde.
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp)),
            )
            // Fila con ícono y la dirección encontrada.
            IconTextRow(
                // Ícono de ubicación.
                icon = Icons.Outlined.LocationOn,
                // Une dirección, ciudad y país, omitiendo los vacíos.
                text = listOf(current.address, current.city, current.country).filter { it.isNotBlank() }.joinToString(", "),
            )
        }
        // Muestra el estado de la localización.
        when {
            // Mientras localiza: indicador de carga con texto.
            state.isLocating -> Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador circular pequeño.
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.textPrimary)
                // Espacio horizontal.
                Spacer(Modifier.width(8.dp))
                // Texto "Localizando...".
                Text(stringResource(R.string.create_post_locating), fontSize = 13.sp, color = AppTheme.colors.textPrimary)
            }
            // Si no se pudo obtener la ubicación, muestra el error.
            state.locationUnavailable -> ErrorText(text = stringResource(R.string.create_post_location_unavailable))
        }
        // Si no está localizando, muestra el botón para obtener o actualizar la ubicación.
        if (!state.isLocating) {
            // Botón secundario.
            SecondaryButton(
                // "Usar mi ubicación" si aún no hay punto; "Actualizar ubicación" si ya hay.
                text = stringResource(if (point == null) R.string.create_post_use_my_location else R.string.create_post_refresh_location),
                // Ícono de ubicación.
                icon = Icons.Outlined.MyLocation,
                // Lanza la localización.
                onClick = onLocate,
            )
        }
    }
}
