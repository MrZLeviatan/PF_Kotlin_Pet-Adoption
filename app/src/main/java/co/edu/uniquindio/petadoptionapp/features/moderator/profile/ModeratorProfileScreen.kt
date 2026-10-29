package co.edu.uniquindio.petadoptionapp.features.moderator.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.MenuActionButton
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.card.UserSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.text.InfoRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModeratorPerformance
import co.edu.uniquindio.petadoptionapp.features.moderator.stats.PerformanceSummaryCard

private val ScreenHorizontalPadding = 24.dp

/**
 Perfil del moderador (pestaña Perfil): su alias, el resumen de su desempeño (al tocarlo abre el dashboard),
 su información (nombre, teléfono y correo) y Configuración. No tiene editar perfil, me gusta ni publicaciones.
 */
@Composable
fun ModeratorProfileScreen(
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ModeratorProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    ModeratorProfileContent(uiState = uiState, onStatsClick = onStatsClick, onSettingsClick = onSettingsClick, modifier = modifier)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ModeratorProfileContent(
    uiState: ModeratorProfileUiState,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Avatar y alias; sin ubicación (los moderadores no tienen).
        UserSummaryCard(
            name = uiState.name.ifBlank { uiState.userName },
            displayName = stringResource(R.string.profile_username, uiState.userName),
            location = null,
            contentPadding = PaddingValues(vertical = 24.dp, horizontal = 20.dp),
        )

        // Mismo resumen que el dashboard; tocarlo lleva al dashboard completo.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.mod_stats_title),
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
            PerformanceSummaryCard(performance = uiState.performance, onClick = onStatsClick)
        }

        SectionCard(title = stringResource(R.string.mod_profile_info), icon = Icons.Outlined.Badge) {
            InfoRow(icon = Icons.Outlined.Person, label = stringResource(R.string.user_profile_name), value = uiState.name.ifBlank { "—" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppTheme.colors.surfaceTag)
            InfoRow(icon = Icons.Outlined.Phone, label = stringResource(R.string.mod_profile_phone), value = uiState.phone.ifBlank { "—" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppTheme.colors.surfaceTag)
            InfoRow(icon = Icons.Outlined.Email, label = stringResource(R.string.mod_profile_email), value = uiState.email.ifBlank { "—" })
        }

        Spacer(Modifier.height(4.dp))

        MenuActionButton(
            text = stringResource(R.string.profile_settings),
            icon = Icons.Outlined.Settings,
            onClick = onSettingsClick,
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ModeratorProfilePreview() {
    PetAdoptionAppTheme {
        ModeratorProfileContent(
            uiState = ModeratorProfileUiState(
                userName = "mod_01", name = "Nicolás Rojas", email = "mod@petadoption.com", phone = "3109876543",
                performance = ModeratorPerformance(approved = 12, rejected = 3, inReview = 2),
                isLoading = false,
            ),
            onStatsClick = {}, onSettingsClick = {},
        )
    }
}
