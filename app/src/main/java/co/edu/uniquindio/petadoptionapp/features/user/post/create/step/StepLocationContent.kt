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
 Paso 4: dónde está o fue vista la mascota.
 - "Ubicación actual": pide el permiso de ubicación, lee el GPS y busca la dirección; se ve en el mapa.
 - "Seleccionar mapa": la ciudad (lista desplegable) y la dirección (buscada o tocando el mapa) fijan el punto.
 En la publicación se muestra solo una zona aproximada, nunca el punto exacto.
 */
@Composable
fun StepLocationContent(state: CreatePostUiState, actions: CreatePostActions) {
    val requestLocation = rememberCurrentLocationRequester { point ->
        actions.onCurrentLocationResult(point?.latitude, point?.longitude)
    }
    val locate = {
        actions.onLocatingStarted()
        requestLocation()
    }

    FormCard(title = stringResource(R.string.create_post_step_location)) {
        Text(stringResource(R.string.create_post_location_hint), fontSize = 13.sp, color = AppTheme.colors.textPrimary)

        Spacer(modifier = Modifier.height(12.dp))

        OptionSelector(
            options = listOf(
                SelectorOption(stringResource(R.string.create_post_location_current), Icons.Outlined.MyLocation),
                SelectorOption(stringResource(R.string.create_post_location_map), Icons.Outlined.Map),
            ),
            selectedIndex = if (state.useCurrentLocation) 0 else 1,
            onSelect = { index ->
                actions.onUseCurrentLocationChange(index == 0)
                // Al elegir "Ubicación actual" por primera vez, se pide de una vez.
                if (index == 0 && state.currentLocation == null && !state.isLocating) locate()
            },
            fillWidth = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.useCurrentLocation) {
            CurrentLocationSection(state = state, onLocate = locate)
        } else {
            LocationPicker(
                initial = state.mapLocation,
                onLocationChange = actions.onMapLocationPicked,
                key = "post-location",
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    InfoTip(icon = Icons.Outlined.VerifiedUser, text = stringResource(R.string.create_post_location_privacy))
}

// Ubicación actual: el mapa con el punto y su dirección, o el botón para obtenerla (y el aviso si no se pudo).
@Composable
private fun CurrentLocationSection(state: CreatePostUiState, onLocate: () -> Unit) {
    val current = state.currentLocation
    val point = current?.toGeoPoint()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (point != null) {
            AppMap(
                center = point,
                zoom = 15.0,
                markers = listOf(MapMarker("current", point, AppTheme.colors.stateError, kind = MarkerKind.PIN, icon = Icons.Filled.MyLocation)),
                interactive = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp)),
            )
            IconTextRow(
                icon = Icons.Outlined.LocationOn,
                text = listOf(current.address, current.city, current.country).filter { it.isNotBlank() }.joinToString(", "),
            )
        }
        when {
            state.isLocating -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.textPrimary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.create_post_locating), fontSize = 13.sp, color = AppTheme.colors.textPrimary)
            }
            state.locationUnavailable -> ErrorText(text = stringResource(R.string.create_post_location_unavailable))
        }
        if (!state.isLocating) {
            SecondaryButton(
                text = stringResource(if (point == null) R.string.create_post_use_my_location else R.string.create_post_refresh_location),
                icon = Icons.Outlined.MyLocation,
                onClick = onLocate,
            )
        }
    }
}
