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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import co.edu.uniquindio.petadoptionapp.core.component.icon.IconCircle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.util.agoText
import co.edu.uniquindio.petadoptionapp.core.component.card.BadgeItem
import co.edu.uniquindio.petadoptionapp.core.component.card.LevelProgressCard
import co.edu.uniquindio.petadoptionapp.core.component.card.NoticeBanner
import co.edu.uniquindio.petadoptionapp.core.component.card.PostMetaLine
import co.edu.uniquindio.petadoptionapp.core.component.card.PostSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.card.SectionCard
import co.edu.uniquindio.petadoptionapp.core.component.card.UserSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.text.InfoRow
import co.edu.uniquindio.petadoptionapp.core.component.text.InfoRowAction
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
    onCallClick: (phone: String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UserProfileContent(uiState = uiState, onBack = onBack, onPostClick = onPostClick, onCallClick = onCallClick)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun UserProfileContent(
    uiState: UserProfileUiState,
    onBack: () -> Unit,
    onPostClick: (String) -> Unit,
    onCallClick: (String) -> Unit = {},
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
            uiState.isDeleted -> DeletedAccountMessage()
            uiState.notFound -> CenteredMessage(stringResource(R.string.user_profile_not_found))
            uiState.hasError -> CenteredMessage(stringResource(R.string.user_profile_error), isError = true)
            else -> ProfileBody(uiState = uiState, onPostClick = onPostClick, onCallClick = onCallClick)
        }
    }
}

@Composable
private fun ProfileBody(uiState: UserProfileUiState, onPostClick: (String) -> Unit, onCallClick: (String) -> Unit) {
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
                // Un moderador lo ve completo igual; solo se le avisa que es privado.
                message = stringResource(if (uiState.viewerIsModerator) R.string.user_profile_private_moderator else R.string.user_profile_private),
                icon = Icons.Outlined.Lock,
            )
        }

        // Contacto: solo si el perfil lo deja ver.
        if (uiState.name != null || uiState.phone != null) {
            ContactCard(name = uiState.name, phone = uiState.phone, onCallClick = onCallClick)
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

        // Publicaciones activas (las privadas ni siquiera llegan del dominio, salvo para un moderador).
        if (!uiState.isPrivate || uiState.viewerIsModerator) {
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

// Tarjeta de contacto: cada dato con su icono y etiqueta; el teléfono trae el botón de llamar.
@Composable
private fun ContactCard(name: String?, phone: String?, onCallClick: (String) -> Unit) {
    SectionCard(
        title = stringResource(R.string.user_profile_contact),
        subtitle = stringResource(R.string.user_profile_contact_subtitle),
        icon = Icons.Outlined.ContactPhone,
    ) {
        name?.let {
            InfoRow(icon = Icons.Outlined.Person, label = stringResource(R.string.user_profile_name), value = it)
        }
        if (name != null && phone != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppTheme.colors.surfaceTag)
        }
        phone?.let {
            val formatted = formatPhone(it)
            InfoRow(
                icon = Icons.Outlined.Phone,
                label = stringResource(R.string.user_profile_phone),
                value = formatted,
                trailing = {
                    InfoRowAction(
                        icon = Icons.Outlined.Call,
                        contentDescription = stringResource(R.string.user_profile_call, formatted),
                        onClick = { onCallClick(it) },
                    )
                },
            )
        }
    }
}

// "3001234567" → "300 123 4567" (formato de celular en Colombia); otros largos se dejan igual.
private fun formatPhone(phone: String): String =
    if (phone.length == 10) "${phone.take(3)} ${phone.substring(3, 6)} ${phone.takeLast(4)}" else phone

@Composable
private fun PublicPostItem(post: PublicPostUi, onClick: () -> Unit) {
    // Mientras carga (o si falla) se ve la imagen local, igual que en "Mis publicaciones".
    val fallback = painterResource(R.drawable.hero_image)
    val image = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    PostSummaryCard(
        image = if (post.photoUrl == null) fallback else image,
        typeLabel = post.categoryName,
        // Para los demás solo llegan verificadas: siempre "activas". Un moderador ve además en qué va la revisión.
        statusColor = when (post.review) {
            null, PostReviewState.VERIFIED -> AppTheme.colors.stateSuccess
            PostReviewState.IN_REVIEW_BY_ME, PostReviewState.IN_REVIEW_BY_OTHER -> AppTheme.colors.stateWarning
            PostReviewState.UNASSIGNED -> AppTheme.colors.textLink
        },
        title = post.title,
        metaLines = listOfNotNull(
            post.review?.let { review ->
                PostMetaLine(
                    Icons.Outlined.VerifiedUser,
                    stringResource(
                        when (review) {
                            PostReviewState.VERIFIED -> R.string.mod_status_verified
                            PostReviewState.IN_REVIEW_BY_ME -> R.string.user_profile_review_mine
                            PostReviewState.IN_REVIEW_BY_OTHER -> R.string.user_profile_review_other
                            PostReviewState.UNASSIGNED -> R.string.mod_status_pending
                        }
                    ),
                )
            },
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

@Composable
private fun DeletedAccountMessage() {
    // Ícono, título y explicación: se entiende que la persona ya no está, no que algo falló.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconCircle(icon = Icons.Outlined.PersonOff, size = 72.dp, containerColor = AppTheme.colors.surfaceTag)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.user_profile_deleted_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.user_profile_deleted_message),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
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
