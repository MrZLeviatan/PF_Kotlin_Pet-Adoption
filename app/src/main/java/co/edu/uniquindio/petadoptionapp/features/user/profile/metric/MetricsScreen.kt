package co.edu.uniquindio.petadoptionapp.features.user.profile.metric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.BadgeItem
import co.edu.uniquindio.petadoptionapp.core.component.card.LevelProgressCard
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.card.StatCard
import co.edu.uniquindio.petadoptionapp.core.component.card.UserSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla de métricas: nivel, estado de publicaciones e insignias conseguidas.
Conecta el [MetricsViewModel] con la UI; [MetricsContent] solo dibuja.
 */
@Composable
fun MetricsScreen(
    onBack: () -> Unit,
    onShareClick: () -> Unit,
    viewModel: MetricsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MetricsContent(uiState = uiState, onBack = onBack, onShareClick = onShareClick)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MetricsContent(
    uiState: MetricsUiState,
    onBack: () -> Unit,
    onShareClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        // Fuera del scroll: la barra se queda fija arriba.
        AppTopBar(
            title = stringResource(R.string.metrics_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
            actions = {
                IconButton(
                    onClick = onShareClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AppTheme.colors.surface,
                        contentColor = AppTheme.colors.textPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.common_share),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UserSummaryCard(name = uiState.userName, location = uiState.location)

            LevelProgressCard(
                levelName = uiState.levelName,
                levelNumber = uiState.levelNumber,
                points = uiState.points,
                nextLevelPoints = uiState.nextLevelPoints,
            )

            SectionCard(
                title = stringResource(R.string.metrics_posts_title),
                subtitle = stringResource(R.string.metrics_posts_subtitle),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = stringResource(R.string.metrics_posts_active),
                        value = uiState.activePosts.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.metrics_posts_review),
                        value = uiState.reviewPosts.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.metrics_posts_finished),
                        value = uiState.finishedPosts.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            SectionCard(
                title = stringResource(R.string.metrics_badges_title),
                subtitle = stringResource(R.string.metrics_badges_subtitle),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.badges.forEach { badge ->
                        BadgeItem(
                            icon = badge.type.icon(),
                            title = badge.title,
                            description = badge.description,
                            tag = badge.tag,
                        )
                    }
                }
            }
        }
    }
}

// El icono es presentación: vive en la pantalla y no en el estado.
private fun BadgeType.icon(): ImageVector = when (this) {
    BadgeType.GUARDIAN -> Icons.Outlined.WorkspacePremium
    BadgeType.PROFILE_COMPLETE -> Icons.Outlined.Person
    BadgeType.VERIFIED_CONTACT -> Icons.Outlined.Phone
}

@Preview(name = "Datos de prueba", showBackground = true, heightDp = 1300)
@Composable
private fun MetricsContentPreview() {
    PetAdoptionAppTheme {
        MetricsContent(uiState = MetricsUiState(), onBack = {}, onShareClick = {})
    }
}

@Preview(name = "Sin insignias", showBackground = true, heightDp = 1000)
@Composable
private fun MetricsContentEmptyPreview() {
    PetAdoptionAppTheme {
        MetricsContent(
            uiState = MetricsUiState(
                activePosts = 0,
                reviewPosts = 0,
                finishedPosts = 0,
                badges = emptyList(),
            ),
            onBack = {},
            onShareClick = {},
        )
    }
}