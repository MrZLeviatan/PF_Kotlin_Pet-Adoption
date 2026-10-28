package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
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
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.FormCard
import co.edu.uniquindio.petadoptionapp.core.component.card.InfoTip
import co.edu.uniquindio.petadoptionapp.core.component.card.LocationMapPlaceholder
import co.edu.uniquindio.petadoptionapp.core.component.form.OptionSelector
import co.edu.uniquindio.petadoptionapp.core.component.form.SelectorOption
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostActions
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState

/**
 Paso 4: dónde está o fue vista la mascota.
 Por ahora se usa la ubicación del perfil ("Ubicación actual"); elegir el punto en el mapa
 llega con Mapbox, así que mientras tanto esa opción no deja publicar.
 */
@Composable
fun StepLocationContent(state: CreatePostUiState, actions: CreatePostActions) {
    FormCard(title = stringResource(R.string.create_post_step_location)) {
        Text(stringResource(R.string.create_post_location_hint), fontSize = 13.sp, color = AppTheme.colors.textPrimary)

        Spacer(modifier = Modifier.height(12.dp))

        OptionSelector(
            options = listOf(
                SelectorOption(stringResource(R.string.create_post_location_current), Icons.Outlined.MyLocation),
                SelectorOption(stringResource(R.string.create_post_location_map), Icons.Outlined.Map),
            ),
            selectedIndex = if (state.useCurrentLocation) 0 else 1,
            onSelect = { index -> actions.onUseCurrentLocationChange(index == 0) },
            fillWidth = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recuadro del mapa (vacío por ahora, como en las demás pantallas).
        LocationMapPlaceholder(onSetLocationClick = null, height = 220.dp)

        Spacer(modifier = Modifier.height(12.dp))

        when {
            !state.useCurrentLocation -> ErrorText(text = stringResource(R.string.create_post_location_map_soon))
            !state.profileLocation.hasCoordinates() -> ErrorText(text = stringResource(R.string.create_post_error_location))
            else -> IconTextRow(
                icon = Icons.Outlined.LocationOn,
                text = listOf(state.profileLocation.address, state.profileLocation.city, state.profileLocation.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", "),
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    InfoTip(icon = Icons.Outlined.VerifiedUser, text = stringResource(R.string.create_post_location_privacy))
}
