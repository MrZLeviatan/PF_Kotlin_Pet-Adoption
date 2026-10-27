package co.edu.uniquindio.petadoptionapp.features.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.avatar.InitialAvatar
import co.edu.uniquindio.petadoptionapp.core.component.button.MenuActionButton
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UserLevel
import coil3.compose.AsyncImage
import java.text.NumberFormat

private val ScreenHorizontalPadding = 24.dp
private val CardShape = RoundedCornerShape(24.dp)

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
        UserCard(uiState)

        Spacer(Modifier.height(16.dp))

        LevelCard(uiState)

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

// Tarjeta superior: avatar, nombre y ubicación.
@Composable
private fun UserCard(uiState: ProfileUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, CardShape)
            .border(1.dp, AppTheme.colors.border, CardShape)
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InitialAvatar(name = uiState.userName, size = 88.dp)

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.profile_username, uiState.userName),
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,

        )

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = AppTheme.colors.textPrimary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = uiState.location.ifBlank { stringResource(R.string.profile_location_empty) },
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

// Tarjeta de nivel: nombre, insignia, puntos y barra de progreso.
@Composable
private fun LevelCard(uiState: ProfileUiState) {
    // Formato según el idioma del teléfono (3.240 en Colombia, 3,240 en inglés).
    val formatter = remember { NumberFormat.getIntegerInstance() }
    val points = formatter.format(uiState.points)

    // Texto de la derecha de la barra: puntos del siguiente nivel o aviso de nivel máximo.
    val nextText = uiState.nextLevelPoints?.let {
        stringResource(R.string.profile_points_short, formatter.format(it))
    } ?: stringResource(R.string.profile_progress_max)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, CardShape)
            .border(1.dp, AppTheme.colors.border, CardShape)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // Se guarda normal y se pasa a mayúsculas aquí, para que TalkBack lo lea como frase.
                    text = stringResource(R.string.profile_level_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = levelName(uiState.level),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
            }
            LevelBadge(level = uiState.levelNumber)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.profile_points, points),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
        )

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { uiState.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = AppTheme.colors.actionPrimary,
            trackColor = AppTheme.colors.surfaceTag,
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.profile_progress, uiState.progressPercent),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = nextText,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

// Insignia "LVL 5".
@Composable
private fun LevelBadge(level: Int) {
    Row(
        modifier = Modifier
            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.profile_level_badge, level),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
        )
    }
}

// Traduce el nivel del dominio al texto del recurso.
@Composable
private fun levelName(level: UserLevel): String = stringResource(
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
        )
    }
}