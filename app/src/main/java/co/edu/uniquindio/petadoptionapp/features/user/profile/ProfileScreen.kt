package co.edu.uniquindio.petadoptionapp.features.user.profile

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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.MenuActionButton
import co.edu.uniquindio.petadoptionapp.core.component.card.LevelProgressCard
import co.edu.uniquindio.petadoptionapp.core.component.card.UserSummaryCard
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UserLevel

private val ScreenHorizontalPadding = 24.dp

/**
Pestaña "Mi Perfil" del dashboard.
Conecta el [ProfileViewModel] con la UI; [ProfileContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
 */
@Composable
fun ProfileScreen(
    onMyPostsClick: () -> Unit,
    onMyLikesClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMetricsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        onMyPostsClick = onMyPostsClick,
        onMyLikesClick = onMyLikesClick,
        onEditProfileClick = onEditProfileClick,
        onSettingsClick = onSettingsClick,
        onMetricsClick = onMetricsClick,
        modifier = modifier,

    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onMyPostsClick: () -> Unit,
    onMyLikesClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMetricsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Tarjeta superior: avatar, nombre y ubicación.
        UserSummaryCard(
            name = uiState.userName,
            displayName = stringResource(R.string.profile_username, uiState.userName),
            location = uiState.location.ifBlank { stringResource(R.string.profile_location_empty) },
            contentPadding = PaddingValues(vertical = 24.dp, horizontal = 20.dp),
            photoUrl = uiState.photoUrl,
        )

        Spacer(Modifier.height(16.dp))

        // Progreso dentro del nivel actual (no desde cero), por eso se pasa `progress`.
        LevelProgressCard(
            levelName = levelName(uiState.level),
            levelNumber = uiState.levelNumber,
            points = uiState.points,
            nextLevelPoints = uiState.nextLevelPoints,
            progress = uiState.progress,
            // Toda la tarjeta abre las métricas.
            onClick = onMetricsClick,
            onClickLabel = stringResource(R.string.profile_open_metrics),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.profile_diary_title),
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.align(Alignment.Start),
        )

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MenuActionButton(
                text = stringResource(R.string.profile_my_posts),
                icon = Icons.Outlined.Description,
                onClick = onMyPostsClick,
            )
            MenuActionButton(
                text = stringResource(R.string.profile_my_likes),
                icon = Icons.Outlined.FavoriteBorder,
                onClick = onMyLikesClick,
            )
            MenuActionButton(
                text = stringResource(R.string.profile_edit),
                icon = Icons.Outlined.Edit,
                onClick = onEditProfileClick,
            )
            MenuActionButton(
                text = stringResource(R.string.profile_settings),
                icon = Icons.Outlined.Settings,
                onClick = onSettingsClick,
            )
        }
    }
}

// Traduce el nivel del dominio al texto del recurso.
@Composable
fun levelName(level: UserLevel): String = stringResource(
    when (level) {
        UserLevel.ANIMAL_FRIEND -> R.string.level_animal_friend
        UserLevel.PROTECTOR -> R.string.level_protector
        UserLevel.GUARDIAN -> R.string.level_guardian
        UserLevel.PET_HERO -> R.string.level_pet_hero
    }
)

@Preview(name = "Nivel intermedio", showBackground = true, heightDp = 800)
@Composable
private fun ProfileContentPreview() {
    PetAdoptionAppTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userName = "ana_77",
                location = "Armenia, Colombia",
                level = UserLevel.PROTECTOR,
                levelNumber = 2,
                points = 150,
                currentLevelPoints = 100,
                nextLevelPoints = 300,
            ),
            onMyPostsClick = {},
            onMyLikesClick = {},
            onEditProfileClick = {},
            onSettingsClick = {},
            onMetricsClick = {}
        )
    }
}

@Preview(name = "Nivel máximo y nombre largo", showBackground = true, heightDp = 800)
@Composable
private fun ProfileContentMaxLevelPreview() {
    PetAdoptionAppTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userName = "María Fernanda Rodríguez Hernández",
                location = "San José de Cúcuta, Colombia",
                level = UserLevel.PET_HERO,
                levelNumber = 4,
                points = 900,
                currentLevelPoints = 700,
                nextLevelPoints = null,
            ),
            onMyPostsClick = {},
            onMyLikesClick = {},
            onEditProfileClick = {},
            onSettingsClick = {},
            onMetricsClick = {}
        )
    }
}
