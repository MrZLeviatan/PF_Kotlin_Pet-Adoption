package co.edu.uniquindio.petadoptionapp.features.user.publicprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.BadgeItem
import co.edu.uniquindio.petadoptionapp.core.component.card.LevelProgressCard
import co.edu.uniquindio.petadoptionapp.core.component.card.NoticeBanner
import co.edu.uniquindio.petadoptionapp.core.component.card.PostMetaLine
import co.edu.uniquindio.petadoptionapp.core.component.card.PostSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.card.UserSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UserLevel
import co.edu.uniquindio.petadoptionapp.features.user.profile.icon
import co.edu.uniquindio.petadoptionapp.features.user.profile.levelName
import co.edu.uniquindio.petadoptionapp.features.user.profile.metric.BadgeUi
import coil3.compose.rememberAsyncImagePainter
import java.text.DateFormat
import java.util.Date

private val ScreenHorizontalPadding = 24.dp

/**
Perfil de otra persona: foto, alias y, si su privacidad lo permite, contacto, nivel,
insignias y publicaciones activas. La privacidad la aplica el dominio: aquí solo se
dibuja lo que llega.
Conecta el [UserProfileViewModel] con la UI; [UserProfileContent] solo dibuja.
 */
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onPostClick: (postId: String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UserProfileContent(uiState = uiState, onBack = onBack, onPostClick = onPostClick)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun UserProfileContent(
    uiState: UserProfileUiState,
    onBack: () -> Unit,
    onPostClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.user_profile_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        when {
            uiState.isLoading -> Unit
            uiState.notFound -> CenteredMessage(stringResource(R.string.user_profile_not_found))
            uiState.hasError -> CenteredMessage(stringResource(R.string.user_profile_error), isError = true)
            else -> ProfileBody(uiState = uiState, onPostClick = onPostClick)
        }
    }
}

@Composable
private fun ProfileBody(uiState: UserProfileUiState, onPostClick: (String) -> Unit) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Alias y foto: siempre visibles, incluso en perfiles privados.
        UserSummaryCard(
            name = uiState.userName,
            displayName = stringResource(R.string.user_profile_username, uiState.userName),
            location = null,
            photoUrl = uiState.photoUrl,
        )

        if (uiState.isPrivate) {
            NoticeBanner(
                message = stringResource(R.string.user_profile_private),
                icon = Icons.Outlined.Lock,
            )
        }

        // Contacto: solo si el perfil lo deja ver.
        if (uiState.name != null || uiState.phone != null) {
            SectionCard(title = stringResource(R.string.user_profile_contact), icon = Icons.Outlined.Person) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.name?.let { IconTextRow(icon = Icons.Outlined.Person, text = it) }
                    uiState.phone?.let { IconTextRow(icon = Icons.Outlined.Phone, text = it) }
                }
            }
        }

        // Nivel e insignias: dependen de la misma regla de privacidad (reputación).
        uiState.level?.let { level ->
            LevelProgressCard(
                levelName = levelName(level),
                levelNumber = uiState.levelNumber,
                points = uiState.points,
                nextLevelPoints = uiState.nextLevelPoints,
                progress = uiState.progress,
            )

            SectionCard(title = stringResource(R.string.metrics_badges_title)) {
                if (uiState.badges.isEmpty()) {
                    StatusMessage(text = stringResource(R.string.user_profile_badges_empty))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.badges.forEach { badge ->
                            BadgeItem(
                                icon = badge.requirementType.icon(),
                                title = badge.name,
                                description = badge.description,
                                tag = dateFormat.format(Date(badge.unlockedAt)),
                            )
                        }
                    }
                }
            }
        }

        // Publicaciones activas (las privadas ni siquiera llegan del dominio).
        if (!uiState.isPrivate) {
            SectionCard(title = stringResource(R.string.user_profile_posts)) {
                if (uiState.posts.isEmpty()) {
                    StatusMessage(text = stringResource(R.string.user_profile_posts_empty))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.posts.forEach { post ->
                            PublicPostItem(post = post, onClick = { onPostClick(post.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicPostItem(post: PublicPostUi, onClick: () -> Unit) {
    // Mientras carga (o si falla) se ve la imagen local, igual que en "Mis publicaciones".
    val fallback = painterResource(R.drawable.hero_image)
    val image = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    PostSummaryCard(
        image = if (post.photoUrl == null) fallback else image,
        typeLabel = post.categoryName,
        // Solo llegan publicaciones verificadas y en proceso: siempre "activas".
        statusColor = AppTheme.colors.stateSuccess,
        title = post.title,
        metaLines = listOf(
            PostMetaLine(
                Icons.Outlined.CalendarToday,
                stringResource(R.string.my_posts_published, agoText(post.ageHours)),
            ),
            PostMetaLine(
                Icons.Outlined.FavoriteBorder,
                pluralStringResource(R.plurals.my_posts_interested, post.importantCount, post.importantCount),
            ),
        ),
        onClick = onClick,
    )
}

// Menos de 24 horas se muestra en horas; desde ahí, en días (mismo criterio que "Mis publicaciones").
@Composable
private fun agoText(ageHours: Int): String =
    if (ageHours < 24) {
        pluralStringResource(R.plurals.common_time_hours_ago, ageHours, ageHours)
    } else {
        val days = ageHours / 24
        pluralStringResource(R.plurals.common_time_days_ago, days, days)
    }

@Composable
private fun CenteredMessage(text: String, isError: Boolean = false) {
    StatusMessage(
        text = text,
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 48.dp),
    )
}

private val previewPublic = UserProfileUiState(
    userName = "luis_9",
    name = "Luis Gómez",
    phone = "3001234567",
    level = UserLevel.PROTECTOR,
    levelNumber = 2,
    points = 150,
    currentLevelPoints = 100,
    nextLevelPoints = 300,
    badges = listOf(
        BadgeUi("first_post", "Primera publicación", "Crea tu primera publicación",
            BadgeRequirementType.POSTS_CREATED, System.currentTimeMillis()),
    ),
    posts = listOf(
        PublicPostUi("p1", "Luna busca un hogar lleno de amor", "Adopción", null, 30, 3),
    ),
    isLoading = false,
)

@Preview(name = "Perfil público", showBackground = true, heightDp = 1300)
@Composable
private fun UserProfilePublicPreview() {
    PetAdoptionAppTheme {
        UserProfileContent(uiState = previewPublic, onBack = {}, onPostClick = {})
    }
}

@Preview(name = "Perfil privado", showBackground = true, heightDp = 800)
@Composable
private fun UserProfilePrivatePreview() {
    PetAdoptionAppTheme {
        UserProfileContent(
            uiState = UserProfileUiState(userName = "maria_r", isPrivate = true, isLoading = false),
            onBack = {},
            onPostClick = {},
        )
    }
}
