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

/**
Pantalla "Mi Perfil" (pestaña del dashboard del usuario).
Muestra una tarjeta con avatar, nombre y ubicación; una tarjeta con el progreso de nivel
de reputación (que al tocarse abre Métricas) y un menú "Mi diario" con accesos a
Mis publicaciones, Mis likes, Editar perfil y Configuración.
El estado (ProfileUiState) lo provee ProfileViewModel, que a su vez observa al usuario
con ObserveCurrentUserUseCase. Esta pantalla no navega por sí misma: recibe un callback
por cada destino y AppNavigation decide a qué ruta ir.
**/

// Margen horizontal común de la pantalla, reutilizado en el contenedor principal.
private val ScreenHorizontalPadding = 24.dp

/**
Pestaña "Mi Perfil" del dashboard.
Conecta el [ProfileViewModel] con la UI; [ProfileContent] solo dibuja.
No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
**/
@Composable
fun ProfileScreen(
    // Callback para abrir la lista de publicaciones propias.
    onMyPostsClick: () -> Unit,
    // Callback para abrir la lista de publicaciones a las que el usuario dio like.
    onMyLikesClick: () -> Unit,
    // Callback para abrir la edición del perfil.
    onEditProfileClick: () -> Unit,
    // Callback para abrir la configuración de la cuenta.
    onSettingsClick: () -> Unit,
    // Callback para abrir la pantalla de métricas y reputación.
    onMetricsClick: () -> Unit,
    // Modificador externo que permite al contenedor ajustar tamaño o márgenes.
    modifier: Modifier = Modifier,
    // ViewModel inyectado por Hilt y ligado al ciclo de vida del destino de navegación.
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    // Recolecta el estado respetando el ciclo de vida (se pausa en segundo plano).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Delega el dibujo al contenido sin estado, pasando estado y callbacks.
    ProfileContent(
        // Estado actual del perfil.
        uiState = uiState,
        // Acción de "Mis publicaciones".
        onMyPostsClick = onMyPostsClick,
        // Acción de "Mis likes".
        onMyLikesClick = onMyLikesClick,
        // Acción de "Editar perfil".
        onEditProfileClick = onEditProfileClick,
        // Acción de "Configuración".
        onSettingsClick = onSettingsClick,
        // Acción de abrir métricas al tocar la tarjeta de nivel.
        onMetricsClick = onMetricsClick,
        // Modificador recibido desde fuera.
        modifier = modifier,

    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ProfileContent(
    // Estado con los datos del perfil que se van a pintar.
    uiState: ProfileUiState,
    // Callback de "Mis publicaciones".
    onMyPostsClick: () -> Unit,
    // Callback de "Mis likes".
    onMyLikesClick: () -> Unit,
    // Callback de "Editar perfil".
    onEditProfileClick: () -> Unit,
    // Callback de "Configuración".
    onSettingsClick: () -> Unit,
    // Callback de "Métricas".
    onMetricsClick: () -> Unit,
    // Modificador externo opcional.
    modifier: Modifier = Modifier,
) {
    // Columna principal que apila todas las secciones del perfil.
    Column(
        // Ocupa toda la pantalla, aplica el fondo del tema, permite scroll vertical y añade márgenes.
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 24.dp),
        // Centra horizontalmente los hijos de la columna.
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Tarjeta superior: avatar, nombre y ubicación.
        UserSummaryCard(
            // Nombre base usado para generar las iniciales del avatar.
            name = uiState.userName,
            // Nombre mostrado con el formato del recurso (por ejemplo "@usuario").
            displayName = stringResource(R.string.profile_username, uiState.userName),
            // Ubicación del usuario o un texto por defecto si está vacía.
            location = uiState.location.ifBlank { stringResource(R.string.profile_location_empty) },
            // Relleno interno de la tarjeta.
            contentPadding = PaddingValues(vertical = 24.dp, horizontal = 20.dp),
            // Foto de perfil, si existe.
            photoUrl = uiState.photoUrl,
        )

        // Separación vertical entre la tarjeta de usuario y la de nivel.
        Spacer(Modifier.height(16.dp))

        // Progreso dentro del nivel actual (no desde cero), por eso se pasa `progress`.
        LevelProgressCard(
            // Nombre traducido del nivel actual.
            levelName = levelName(uiState.level),
            // Número del nivel.
            levelNumber = uiState.levelNumber,
            // Puntos acumulados.
            points = uiState.points,
            // Puntos necesarios para el siguiente nivel (null si es el máximo).
            nextLevelPoints = uiState.nextLevelPoints,
            // Fracción de avance dentro del nivel actual.
            progress = uiState.progress,
            // Toda la tarjeta abre las métricas.
            onClick = onMetricsClick,
            // Etiqueta de accesibilidad que describe la acción del clic.
            onClickLabel = stringResource(R.string.profile_open_metrics),
        )

        // Separación antes de la sección "Mi diario".
        Spacer(Modifier.height(24.dp))

        // Título de la sección de accesos rápidos.
        Text(
            // Texto del título tomado de strings.xml.
            text = stringResource(R.string.profile_diary_title),
            // Estilo tipográfico de título mediano.
            style = MaterialTheme.typography.titleMedium,
            // Color principal de texto del tema.
            color = AppTheme.colors.textPrimary,
            // Alinea el título a la izquierda aunque la columna centre a sus hijos.
            modifier = Modifier.align(Alignment.Start),
        )

        // Separación entre el título y los botones del menú.
        Spacer(Modifier.height(12.dp))

        // Columna con los botones del menú, separados 12 dp entre sí.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Botón "Mis publicaciones".
            MenuActionButton(
                text = stringResource(R.string.profile_my_posts),
                icon = Icons.Outlined.Description,
                onClick = onMyPostsClick,
            )
            // Botón "Mis likes".
            MenuActionButton(
                text = stringResource(R.string.profile_my_likes),
                icon = Icons.Outlined.FavoriteBorder,
                onClick = onMyLikesClick,
            )
            // Botón "Editar perfil".
            MenuActionButton(
                text = stringResource(R.string.profile_edit),
                icon = Icons.Outlined.Edit,
                onClick = onEditProfileClick,
            )
            // Botón "Configuración".
            MenuActionButton(
                text = stringResource(R.string.profile_settings),
                icon = Icons.Outlined.Settings,
                onClick = onSettingsClick,
            )
        }
    }
}

// Traduce el nivel del dominio al texto del recurso (público: también lo usan Métricas y el perfil público).
@Composable
fun levelName(level: UserLevel): String = stringResource(
    // Elige el identificador del recurso de texto según el nivel.
    when (level) {
        // Nivel 1: Amigo de los animales.
        UserLevel.ANIMAL_FRIEND -> R.string.level_animal_friend
        // Nivel 2: Protector.
        UserLevel.PROTECTOR -> R.string.level_protector
        // Nivel 3: Guardián.
        UserLevel.GUARDIAN -> R.string.level_guardian
        // Nivel 4: Héroe de mascotas.
        UserLevel.PET_HERO -> R.string.level_pet_hero
    }
)

// Vista previa con un usuario en un nivel intermedio y barra a mitad de camino.
@Preview(name = "Nivel intermedio", showBackground = true, heightDp = 800)
@Composable
private fun ProfileContentPreview() {
    // Aplica el tema de la app para que la vista previa se vea igual que en ejecución.
    PetAdoptionAppTheme {
        // Pinta el contenido con datos de ejemplo y callbacks vacíos.
        ProfileContent(
            // Estado de ejemplo: nivel Protector con 150 de 300 puntos.
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

// Vista previa del caso límite: nivel máximo y textos largos para revisar desbordes.
@Preview(name = "Nivel máximo y nombre largo", showBackground = true, heightDp = 800)
@Composable
private fun ProfileContentMaxLevelPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido con datos de ejemplo extremos.
        ProfileContent(
            // Estado de ejemplo: nivel máximo (sin siguiente nivel) y nombre/ubicación largos.
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
