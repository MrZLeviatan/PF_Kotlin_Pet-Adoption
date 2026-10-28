package co.edu.uniquindio.petadoptionapp.features.user.profile.metric

import android.graphics.Bitmap
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
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.BadgeItem
import co.edu.uniquindio.petadoptionapp.core.component.card.LevelProgressCard
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.card.StatCard
import co.edu.uniquindio.petadoptionapp.core.component.card.UserSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType
import co.edu.uniquindio.petadoptionapp.features.user.profile.icon
import co.edu.uniquindio.petadoptionapp.features.user.profile.levelName
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla de métricas: nivel, estado de publicaciones e insignias conseguidas.
Conecta el [MetricsViewModel] con la UI; [MetricsContent] solo dibuja.

"Compartir" envía una captura de las tarjetas: se graba lo que ya se dibujó en pantalla
(un GraphicsLayer de Compose), sin librerías extra. Si la captura falla, se pasa `null`
y quien comparte puede enviar solo el texto.
 */
@Composable
fun MetricsScreen(
    onBack: () -> Unit,
    onShare: (Bitmap?) -> Unit,
    viewModel: MetricsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Capa donde se graba el contenido de la pantalla para la captura.
    val captureLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    // Al volver a la pantalla se recalculan las publicaciones.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    MetricsContent(
        uiState = uiState,
        onBack = onBack,
        onShareClick = {
            scope.launch {
                // Puede fallar con imágenes de hardware en Android antiguos: en ese caso se comparte sin imagen.
                val bitmap = runCatching { captureLayer.toImageBitmap().asAndroidBitmap() }.getOrNull()
                onShare(bitmap)
            }
        },
        // Graba todo el contenido desplazable (no solo la parte visible) con su fondo.
        contentModifier = Modifier.drawWithContent {
            captureLayer.record { this@drawWithContent.drawContent() }
            drawLayer(captureLayer)
        },
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MetricsContent(
    uiState: MetricsUiState,
    onBack: () -> Unit,
    onShareClick: () -> Unit,
    // Se aplica al contenido desplazable; la pantalla real lo usa para la captura.
    contentModifier: Modifier = Modifier,
) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
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
                    Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.common_share))
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .then(contentModifier)
                // El fondo va dentro de lo grabado: así la captura no sale transparente.
                .background(AppTheme.colors.background)
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (uiState.hasError) {
                Text(
                    text = stringResource(R.string.metrics_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.stateError,
                    textAlign = TextAlign.Center,
                )
            }

            // Agrega el parámetro photoUrl a UserSummaryCard (usa UserAvatar por dentro).
            UserSummaryCard(
                name = uiState.userName,
                location = uiState.location.ifBlank { stringResource(R.string.profile_location_empty) },
                photoUrl = uiState.photoUrl,
            )

            LevelProgressCard(
                levelName = levelName(uiState.level),
                levelNumber = uiState.levelNumber,
                points = uiState.points,
                // El componente pide un número: en el nivel máximo se usa el total (barra llena).
                nextLevelPoints = uiState.nextLevelPoints ?: uiState.points,
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
                if (uiState.badges.isEmpty()) {
                    Text(
                        text = stringResource(R.string.metrics_badges_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textPrimary,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.badges.forEach { badge ->
                            BadgeItem(
                                icon = badge.requirementType.icon(),
                                title = badge.name,
                                description = badge.description,
                                tag = dateFormat.format(Date(badge.unlockedAt))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Con datos", showBackground = true, heightDp = 1300)
@Composable
private fun MetricsContentPreview() {
    PetAdoptionAppTheme {
        MetricsContent(
            uiState = MetricsUiState(
                userName = "ana_77", location = "Armenia, Colombia",
                points = 150, nextLevelPoints = 300, levelNumber = 2,
                activePosts = 2, reviewPosts = 1, finishedPosts = 1,
                badges = listOf(
                    BadgeUi("first_post", "Primera publicación", "Crea tu primera publicación",
                        BadgeRequirementType.POSTS_CREATED, System.currentTimeMillis())
                ),
            ),
            onBack = {}, onShareClick = {},
        )
    }
}

@Preview(name = "Sin insignias", showBackground = true, heightDp = 1000)
@Composable
private fun MetricsContentEmptyPreview() {
    PetAdoptionAppTheme {
        MetricsContent(uiState = MetricsUiState(), onBack = {}, onShareClick = {})
    }
}