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

/**
Pantalla "Métricas" del usuario: se abre desde Mi Perfil al tocar la tarjeta de nivel.
Muestra la tarjeta de usuario, el progreso de nivel, el conteo de publicaciones
(activas, en revisión y finalizadas) y la lista de insignias desbloqueadas con su fecha.
El estado (MetricsUiState) lo produce MetricsViewModel a partir de ObserveCurrentUserUseCase,
ObserveBadgesUseCase, LoadBadgesUseCase, EvaluateMyBadgesUseCase y GetPostStatsUseCase.
La navegación la resuelve AppNavigation: onBack regresa al perfil y onShare recibe la
captura de la pantalla para lanzar el menú de compartir del sistema.
**/

// Margen horizontal común del contenido de la pantalla.
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla de métricas: nivel, estado de publicaciones e insignias conseguidas.
Conecta el [MetricsViewModel] con la UI; [MetricsContent] solo dibuja.

"Compartir" envía una captura de las tarjetas: se graba lo que ya se dibujó en pantalla
(un GraphicsLayer de Compose), sin librerías extra. Si la captura falla, se pasa `null`
y quien comparte puede enviar solo el texto.
**/
@Composable
fun MetricsScreen(
    // Callback para volver a la pantalla anterior.
    onBack: () -> Unit,
    // Callback que recibe la captura (o null) para compartirla.
    onShare: (Bitmap?) -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: MetricsViewModel = hiltViewModel(),
) {
    // Recolecta el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Capa donde se graba el contenido de la pantalla para la captura.
    val captureLayer = rememberGraphicsLayer()
    // Alcance de corrutinas ligado a la composición, para generar la captura de forma asíncrona.
    val scope = rememberCoroutineScope()

    // Al volver a la pantalla se recalculan las publicaciones.
    LifecycleResumeEffect(Unit) {
        // Pide al ViewModel recargar las estadísticas en cada ON_RESUME.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido sin estado.
    MetricsContent(
        // Estado actual de las métricas.
        uiState = uiState,
        // Acción de la flecha atrás.
        onBack = onBack,
        // Acción del botón compartir: genera la captura y la entrega.
        onShareClick = {
            // La conversión a imagen es suspendida, por eso se lanza en una corrutina.
            scope.launch {
                // Puede fallar con imágenes de hardware en Android antiguos: en ese caso se comparte sin imagen.
                val bitmap = runCatching { captureLayer.toImageBitmap().asAndroidBitmap() }.getOrNull()
                // Entrega la captura (o null) a quien navega para compartir.
                onShare(bitmap)
            }
        },
        // Graba todo el contenido desplazable (no solo la parte visible) con su fondo.
        contentModifier = Modifier.drawWithContent {
            // Registra el dibujo del contenido dentro de la capa de captura.
            captureLayer.record { this@drawWithContent.drawContent() }
            // Dibuja la capa en pantalla para que el contenido siga visible.
            drawLayer(captureLayer)
        },
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MetricsContent(
    // Estado con los datos a pintar.
    uiState: MetricsUiState,
    // Acción de volver.
    onBack: () -> Unit,
    // Acción de compartir.
    onShareClick: () -> Unit,
    // Se aplica al contenido desplazable; la pantalla real lo usa para la captura.
    contentModifier: Modifier = Modifier,
) {
    // Formato de fecha mediano según el idioma del dispositivo, para la fecha de cada insignia.
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    // Columna raíz: barra superior y contenido desplazable.
    Column(
        // Ocupa toda la pantalla con el fondo del tema y respeta las barras del sistema.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        // Barra superior con título, flecha atrás y botón de compartir.
        AppTopBar(
            // Título "Métricas".
            title = stringResource(R.string.metrics_title),
            // Acción de la flecha atrás.
            onBack = onBack,
            // Margen horizontal de la barra.
            modifier = Modifier.padding(horizontal = 8.dp),
            // Acciones a la derecha de la barra.
            actions = {
                // Botón de compartir con fondo de superficie.
                IconButton(
                    onClick = onShareClick,
                    // Colores del contenedor y del icono según el tema.
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AppTheme.colors.surface,
                        contentColor = AppTheme.colors.textPrimary,
                    ),
                ) {
                    // Icono de compartir con descripción accesible.
                    Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.common_share))
                }
            },
        )

        // Contenido desplazable con las tarjetas de métricas.
        Column(
            // Ocupa el espacio restante, permite scroll y aplica el modificador de captura.
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .then(contentModifier)
                // El fondo va dentro de lo grabado: así la captura no sale transparente.
                .background(AppTheme.colors.background)
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            // Separa las tarjetas 16 dp entre sí.
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Si falló la carga de estadísticas, muestra un aviso arriba.
            if (uiState.hasError) {
                // Texto de error centrado con el color de error del tema.
                Text(
                    text = stringResource(R.string.metrics_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.stateError,
                    textAlign = TextAlign.Center,
                )
            }

            // Tarjeta de resumen del usuario: avatar (foto o iniciales), nombre y ubicación.
            UserSummaryCard(
                // Nombre del usuario.
                name = uiState.userName,
                // Ubicación o texto por defecto si está vacía.
                location = uiState.location.ifBlank { stringResource(R.string.profile_location_empty) },
                // Foto de perfil, si existe.
                photoUrl = uiState.photoUrl,
            )

            // Tarjeta con el progreso de nivel.
            LevelProgressCard(
                // Nombre traducido del nivel.
                levelName = levelName(uiState.level),
                // Número del nivel.
                levelNumber = uiState.levelNumber,
                // Puntos acumulados.
                points = uiState.points,
                // El componente pide un número: en el nivel máximo se usa el total (barra llena).
                nextLevelPoints = uiState.nextLevelPoints ?: uiState.points,
            )

            // Sección con el conteo de publicaciones por estado.
            SectionCard(
                title = stringResource(R.string.metrics_posts_title),
                subtitle = stringResource(R.string.metrics_posts_subtitle),
            ) {
                // Fila con tres tarjetas de estadística del mismo ancho.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Publicaciones activas.
                    StatCard(
                        label = stringResource(R.string.metrics_posts_active),
                        value = uiState.activePosts.toString(),
                        // Reparte el ancho por igual entre las tres tarjetas.
                        modifier = Modifier.weight(1f),
                    )
                    // Publicaciones en revisión.
                    StatCard(
                        label = stringResource(R.string.metrics_posts_review),
                        value = uiState.reviewPosts.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    // Publicaciones finalizadas.
                    StatCard(
                        label = stringResource(R.string.metrics_posts_finished),
                        value = uiState.finishedPosts.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Sección de insignias desbloqueadas.
            SectionCard(
                title = stringResource(R.string.metrics_badges_title),
                subtitle = stringResource(R.string.metrics_badges_subtitle),
            ) {
                // Si aún no tiene insignias, muestra un mensaje vacío.
                if (uiState.badges.isEmpty()) {
                    // Texto de estado vacío.
                    Text(
                        text = stringResource(R.string.metrics_badges_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textPrimary,
                    )
                } else {
                    // Lista de insignias separadas 8 dp.
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Recorre cada insignia desbloqueada.
                        uiState.badges.forEach { badge ->
                            // Fila de insignia con icono, nombre, descripción y fecha de desbloqueo.
                            BadgeItem(
                                // Icono según el tipo de logro (ver BadgeIcons.kt).
                                icon = badge.requirementType.icon(),
                                title = badge.name,
                                description = badge.description,
                                // Fecha de desbloqueo formateada.
                                tag = dateFormat.format(Date(badge.unlockedAt))
                            )
                        }
                    }
                }
            }
        }
    }
}

// Vista previa con datos completos y una insignia.
@Preview(name = "Con datos", showBackground = true, heightDp = 1300)
@Composable
private fun MetricsContentPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido con un estado de ejemplo.
        MetricsContent(
            uiState = MetricsUiState(
                userName = "ana_77", location = "Armenia, Colombia",
                points = 150, nextLevelPoints = 300, levelNumber = 2,
                activePosts = 2, reviewPosts = 1, finishedPosts = 1,
                // Una insignia de ejemplo desbloqueada ahora mismo.
                badges = listOf(
                    BadgeUi("first_post", "Primera publicación", "Crea tu primera publicación",
                        BadgeRequirementType.POSTS_CREATED, System.currentTimeMillis())
                ),
            ),
            onBack = {}, onShareClick = {},
        )
    }
}

// Vista previa del estado inicial, sin insignias.
@Preview(name = "Sin insignias", showBackground = true, heightDp = 1000)
@Composable
private fun MetricsContentEmptyPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido con el estado por defecto.
        MetricsContent(uiState = MetricsUiState(), onBack = {}, onShareClick = {})
    }
}
