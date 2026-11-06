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

/**
Pantalla de perfil público de otra persona. Se abre al tocar el autor de una publicación,
un comentario o un chat (ruta MainRoutes.UserProfile(userId)).
Muestra foto y alias siempre; y, si la privacidad lo permite (o si mira un moderador),
datos de contacto con botón de llamar, nivel, insignias y publicaciones activas.
También cubre los estados de carga, usuario inexistente, cuenta eliminada y error.
El estado (UserProfileUiState) lo produce UserProfileViewModel con GetUserProfileUseCase,
GetUserPostsUseCase y los catálogos de insignias y categorías. La navegación la resuelve
AppNavigation mediante onBack, onPostClick (abrir el detalle) y onCallClick (marcador del teléfono).
**/

// Margen horizontal común del contenido.
private val ScreenHorizontalPadding = 24.dp

/**
Perfil de otra persona: foto, alias y, si su privacidad lo permite, contacto, nivel,
insignias y publicaciones activas. La privacidad la aplica el dominio: aquí solo se
dibuja lo que llega.
Conecta el [UserProfileViewModel] con la UI; [UserProfileContent] solo dibuja.
**/
@Composable
fun UserProfileScreen(
    // Callback para volver atrás.
    onBack: () -> Unit,
    // Callback para abrir el detalle de una publicación.
    onPostClick: (postId: String) -> Unit,
    // Callback para llamar al teléfono mostrado.
    onCallClick: (phone: String) -> Unit,
    // ViewModel inyectado por Hilt (recibe el userId desde la ruta).
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    // Recolecta el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Dibuja el contenido sin estado con el estado y los callbacks.
    UserProfileContent(uiState = uiState, onBack = onBack, onPostClick = onPostClick, onCallClick = onCallClick)
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun UserProfileContent(
    // Estado del perfil.
    uiState: UserProfileUiState,
    // Volver atrás.
    onBack: () -> Unit,
    // Abrir una publicación por id.
    onPostClick: (String) -> Unit,
    // Llamar a un teléfono (vacío por defecto para las vistas previas).
    onCallClick: (String) -> Unit = {},
) {
    // Columna raíz: barra superior y cuerpo.
    Column(
        // Ocupa toda la pantalla con el fondo del tema y respeta las barras del sistema.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        // Barra superior con título y flecha atrás.
        AppTopBar(
            title = stringResource(R.string.user_profile_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // Elige qué dibujar según el estado de la carga.
        when {
            // Cargando: no se dibuja nada bajo la barra.
            uiState.isLoading -> Unit
            // Cuenta eliminada: mensaje explicativo.
            uiState.isDeleted -> DeletedAccountMessage()
            // Usuario inexistente.
            uiState.notFound -> CenteredMessage(stringResource(R.string.user_profile_not_found))
            // Error inesperado.
            uiState.hasError -> CenteredMessage(stringResource(R.string.user_profile_error), isError = true)
            // Perfil cargado: dibuja el cuerpo completo.
            else -> ProfileBody(uiState = uiState, onPostClick = onPostClick, onCallClick = onCallClick)
        }
    }
}

// Cuerpo del perfil cargado: resumen, aviso de privacidad, contacto, nivel, insignias y publicaciones.
@Composable
private fun ProfileBody(uiState: UserProfileUiState, onPostClick: (String) -> Unit, onCallClick: (String) -> Unit) {
    // Formato de fecha mediano para las insignias.
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    // Columna desplazable con las secciones.
    Column(
        // Permite scroll vertical y aplica márgenes.
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        // Separa las secciones 16 dp.
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Alias y foto: siempre visibles, incluso en perfiles privados.
        UserSummaryCard(
            name = uiState.userName,
            displayName = stringResource(R.string.user_profile_username, uiState.userName),
            // No se muestra ubicación en perfiles ajenos.
            location = null,
            photoUrl = uiState.photoUrl,
        )

        // Si el perfil es privado, muestra un aviso.
        if (uiState.isPrivate) {
            // Banner informativo con candado.
            NoticeBanner(
                // Un moderador lo ve completo igual; solo se le avisa que es privado.
                message = stringResource(if (uiState.viewerIsModerator) R.string.user_profile_private_moderator else R.string.user_profile_private),
                icon = Icons.Outlined.Lock,
            )
        }

        // Contacto: solo si el perfil lo deja ver.
        if (uiState.name != null || uiState.phone != null) {
            // Tarjeta con nombre y teléfono.
            ContactCard(name = uiState.name, phone = uiState.phone, onCallClick = onCallClick)
        }

        // Nivel e insignias: dependen de la misma regla de privacidad (reputación).
        uiState.level?.let { level ->
            // Tarjeta de progreso de nivel (sin acción de clic en perfiles ajenos).
            LevelProgressCard(
                levelName = levelName(level),
                levelNumber = uiState.levelNumber,
                points = uiState.points,
                nextLevelPoints = uiState.nextLevelPoints,
                // Avance dentro del nivel actual.
                progress = uiState.progress,
            )

            // Sección de insignias.
            SectionCard(title = stringResource(R.string.metrics_badges_title)) {
                // Sin insignias: mensaje vacío.
                if (uiState.badges.isEmpty()) {
                    StatusMessage(text = stringResource(R.string.user_profile_badges_empty))
                } else {
                    // Lista de insignias separadas 8 dp.
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Una fila por insignia.
                        uiState.badges.forEach { badge ->
                            // Insignia con icono por tipo de logro y fecha de desbloqueo.
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
            // Sección de publicaciones.
            SectionCard(title = stringResource(R.string.user_profile_posts)) {
                // Sin publicaciones: mensaje vacío.
                if (uiState.posts.isEmpty()) {
                    StatusMessage(text = stringResource(R.string.user_profile_posts_empty))
                } else {
                    // Lista de tarjetas separadas 12 dp.
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Una tarjeta por publicación; al tocarla abre su detalle.
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
    // Sección con título, subtítulo e icono de contacto.
    SectionCard(
        title = stringResource(R.string.user_profile_contact),
        subtitle = stringResource(R.string.user_profile_contact_subtitle),
        icon = Icons.Outlined.ContactPhone,
    ) {
        // Fila del nombre, si es visible.
        name?.let {
            InfoRow(icon = Icons.Outlined.Person, label = stringResource(R.string.user_profile_name), value = it)
        }
        // Separador solo cuando hay nombre y teléfono a la vez.
        if (name != null && phone != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AppTheme.colors.surfaceTag)
        }
        // Fila del teléfono, si es visible.
        phone?.let {
            // Teléfono con espacios para leerlo mejor.
            val formatted = formatPhone(it)
            // Fila con icono, etiqueta, número y botón de llamar.
            InfoRow(
                icon = Icons.Outlined.Phone,
                label = stringResource(R.string.user_profile_phone),
                value = formatted,
                // Acción al final de la fila.
                trailing = {
                    // Botón de llamar; se envía el número sin formato.
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

// Tarjeta resumen de una publicación del perfil, con estado y metadatos.
@Composable
private fun PublicPostItem(post: PublicPostUi, onClick: () -> Unit) {
    // Mientras carga (o si falla) se ve la imagen local, igual que en "Mis publicaciones".
    val fallback = painterResource(R.drawable.hero_image)
    // Painter asíncrono (Coil) para la foto remota.
    val image = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    // Tarjeta resumen reutilizable.
    PostSummaryCard(
        // Sin foto se usa directamente la imagen local.
        image = if (post.photoUrl == null) fallback else image,
        // Etiqueta con el nombre de la categoría.
        typeLabel = post.categoryName,
        // Para los demás solo llegan verificadas: siempre "activas". Un moderador ve además en qué va la revisión.
        statusColor = when (post.review) {
            // Verde: activa o verificada.
            null, PostReviewState.VERIFIED -> AppTheme.colors.stateSuccess
            // Amarillo: en revisión.
            PostReviewState.IN_REVIEW_BY_ME, PostReviewState.IN_REVIEW_BY_OTHER -> AppTheme.colors.stateWarning
            // Azul: sin asignar.
            PostReviewState.UNASSIGNED -> AppTheme.colors.textLink
        },
        title = post.title,
        // Líneas de metadatos; listOfNotNull descarta la de revisión cuando no aplica.
        metaLines = listOfNotNull(
            // Línea de estado de revisión (solo moderadores).
            post.review?.let { review ->
                PostMetaLine(
                    Icons.Outlined.VerifiedUser,
                    // Texto según el estado de revisión.
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
            // Línea con hace cuánto se publicó.
            PostMetaLine(
                Icons.Outlined.CalendarToday,
                stringResource(R.string.my_posts_published, agoText(post.ageHours)),
            ),
            // Línea con el número de interesados (en plural según la cantidad).
            PostMetaLine(
                Icons.Outlined.FavoriteBorder,
                pluralStringResource(R.plurals.my_posts_interested, post.importantCount, post.importantCount),
            ),
        ),
        // Al tocar la tarjeta se abre el detalle.
        onClick = onClick,
    )
}

// Mensaje para cuando la persona eliminó su cuenta.
@Composable
private fun DeletedAccountMessage() {
    // Ícono, título y explicación: se entiende que la persona ya no está, no que algo falló.
    Column(
        // Ancho completo con márgenes amplios arriba y abajo.
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 64.dp),
        // Centra los elementos.
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Círculo con icono de persona tachada.
        IconCircle(icon = Icons.Outlined.PersonOff, size = 72.dp, containerColor = AppTheme.colors.surfaceTag)
        // Espacio bajo el icono.
        Spacer(Modifier.height(16.dp))
        // Título del mensaje.
        Text(
            text = stringResource(R.string.user_profile_deleted_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        // Espacio entre título y explicación.
        Spacer(Modifier.height(8.dp))
        // Explicación del mensaje.
        Text(
            text = stringResource(R.string.user_profile_deleted_message),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

// Mensaje de estado centrado (no encontrado o error).
@Composable
private fun CenteredMessage(text: String, isError: Boolean = false) {
    // Reutiliza StatusMessage con márgenes propios de la pantalla.
    StatusMessage(
        text = text,
        // Si es error, se pinta con el color de error.
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 48.dp),
    )
}

// Estado de ejemplo de un perfil público completo para las vistas previas.
private val previewPublic = UserProfileUiState(
    userName = "luis_9",
    name = "Luis Gómez",
    phone = "3001234567",
    level = UserLevel.PROTECTOR,
    levelNumber = 2,
    points = 150,
    currentLevelPoints = 100,
    nextLevelPoints = 300,
    // Una insignia de ejemplo.
    badges = listOf(
        BadgeUi("first_post", "Primera publicación", "Crea tu primera publicación",
            BadgeRequirementType.POSTS_CREATED, System.currentTimeMillis()),
    ),
    // Una publicación de ejemplo sin foto.
    posts = listOf(
        PublicPostUi("p1", "Luna busca un hogar lleno de amor", "Adopción", null, 30, 3),
    ),
    isLoading = false,
)

// Vista previa de un perfil público con todos los datos.
@Preview(name = "Perfil público", showBackground = true, heightDp = 1300)
@Composable
private fun UserProfilePublicPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Pinta el contenido con el estado de ejemplo.
        UserProfileContent(uiState = previewPublic, onBack = {}, onPostClick = {})
    }
}

// Vista previa de un perfil privado visto por un usuario normal.
@Preview(name = "Perfil privado", showBackground = true, heightDp = 800)
@Composable
private fun UserProfilePrivatePreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Solo alias y aviso de privacidad.
        UserProfileContent(
            uiState = UserProfileUiState(userName = "maria_r", isPrivate = true, isLoading = false),
            onBack = {},
            onPostClick = {},
        )
    }
}
