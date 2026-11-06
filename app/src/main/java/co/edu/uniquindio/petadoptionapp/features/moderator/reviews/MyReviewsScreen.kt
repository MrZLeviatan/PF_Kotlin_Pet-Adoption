package co.edu.uniquindio.petadoptionapp.features.moderator.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.PostMetaLine
import co.edu.uniquindio.petadoptionapp.core.component.card.PostSummaryCard
import co.edu.uniquindio.petadoptionapp.core.component.indicator.StatusIndicator
import co.edu.uniquindio.petadoptionapp.core.component.navigation.AppTabRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.core.util.agoText
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityColor
import co.edu.uniquindio.petadoptionapp.features.moderator.priorityText
import coil3.compose.rememberAsyncImagePainter

/**
Pantalla "Publicaciones" del moderador (pestaña de su barra inferior).
Observa MyReviewsUiState desde MyReviewsViewModel: lista de publicaciones que tomó para revisar y
aún no decide, pestañas por prioridad con su contador, secciones visibles y banderas de carga/error.
Eventos: refresh() al volver a primer plano y onTabSelected() al cambiar de pestaña.
Casos de uso (vía ViewModel): GetMyReviewsUseCase, ObserveCategoriesUseCase y LoadCatalogUseCase.
Navegación: onPostClick entrega el id de la publicación para que el grafo abra su detalle, donde se
aprueba o se rechaza (esta última opción lleva a RejectPostScreen).
**/

// Margen horizontal de la pantalla.
private val ScreenHorizontalPadding = 24.dp

/**
 Pestaña "Publicaciones" del moderador: las que tomó para revisar y aún no decide
 (ni verificadas ni rechazadas), divididas por prioridad, con una pestaña por prioridad.
 Mismo formato que "Me gusta" del usuario. Al tocar una se abre su detalle para aprobarla o rechazarla.
 */
@Composable
fun MyReviewsScreen(
    // Márgenes que entrega el Scaffold contenedor.
    padding: PaddingValues = PaddingValues(),
    // Acción al tocar una publicación (abre su detalle).
    onPostClick: (postId: String) -> Unit = {},
    // ViewModel inyectado por Hilt.
    viewModel: MyReviewsViewModel = hiltViewModel(),
) {
    // Observa el estado respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver del detalle se recarga: las aprobadas o rechazadas salen de la lista.
    LifecycleResumeEffect(Unit) {
        // Pide al ViewModel recargar la lista.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido conectando estado y eventos.
    MyReviewsContent(
        // Estado actual.
        uiState = uiState,
        // Márgenes del contenedor.
        padding = padding,
        // Cambio de pestaña hacia el ViewModel.
        onTabSelected = viewModel::onTabSelected,
        // Clic en publicación hacia la navegación.
        onPostClick = onPostClick,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun MyReviewsContent(
    // Estado a dibujar.
    uiState: MyReviewsUiState,
    // Márgenes del contenedor.
    padding: PaddingValues,
    // Acción al elegir pestaña (null = "Todas").
    onTabSelected: (String?) -> Unit,
    // Acción al tocar una publicación.
    onPostClick: (String) -> Unit,
) {
    // Pestañas por prioridad calculadas en el estado.
    val tabs = uiState.tabs
    // "Todas" va primero; luego una pestaña por prioridad.
    val tabLabels = listOf(stringResource(R.string.my_likes_tab_all, uiState.posts.size)) +
        tabs.map { stringResource(R.string.my_likes_tab_category, priorityText(it.priority), it.count) }
    // Índice de la pestaña elegida (0 = "Todas"; las demás se desplazan en uno).
    val selectedIndex = uiState.selectedKey?.let { key -> tabs.indexOfFirst { it.key == key } + 1 } ?: 0
    // Secciones a mostrar según la pestaña.
    val sections = uiState.visibleSections

    // Columna principal de la pantalla.
    Column(
        // Ocupa todo, pinta el fondo y aplica los márgenes del contenedor.
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(padding),
    ) {
        // Es una pestaña de la barra inferior: título sin flecha de volver.
        Text(
            // Texto del título.
            text = stringResource(R.string.mod_reviews_title),
            // Estilo de titular pequeño.
            style = MaterialTheme.typography.headlineSmall,
            // Texto en negrita.
            fontWeight = FontWeight.Bold,
            // Color principal.
            color = AppTheme.colors.textPrimary,
            // Márgenes y semántica de encabezado para accesibilidad.
            modifier = Modifier
                .padding(horizontal = ScreenHorizontalPadding, vertical = 20.dp)
                .semantics { heading() },
        )

        // Solo muestra las pestañas si hay publicaciones.
        if (uiState.posts.isNotEmpty()) {
            // Fila de pestañas.
            AppTabRow(
                // Textos de las pestañas.
                tabs = tabLabels,
                // Pestaña marcada.
                selectedIndex = selectedIndex,
                // Convierte el índice en la clave de la pestaña (null para "Todas").
                onTabSelected = { index -> onTabSelected(if (index == 0) null else tabs[index - 1].key) },
            )
        }

        // Lista desplazable de secciones y publicaciones.
        LazyColumn(
            // Ancho completo y el resto del alto disponible.
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            // Márgenes internos.
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            // Separación entre elementos.
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Mensajes de estado según carga, error o vacío.
            when {
                // Mientras carga no se muestra nada.
                uiState.isLoading -> Unit
                // Si hubo error, se muestra el mensaje de error.
                uiState.hasError -> item(key = "error") { CenteredStatus(stringResource(R.string.mod_reviews_error), isError = true) }
                // Si no hay secciones, se muestra el mensaje de vacío.
                sections.isEmpty() -> item(key = "empty") { CenteredStatus(stringResource(R.string.mod_reviews_empty)) }
            }

            // Recorre cada sección (pestaña y sus publicaciones).
            sections.forEach { (tab, posts) ->
                // Encabezado de la sección.
                item(key = "header-${tab.key}") {
                    // Encabezado de prioridad: su punto de color y "Urgente (1)".
                    Row(
                        // Margen superior y semántica de encabezado.
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .semantics { heading() },
                        // Centra verticalmente punto y texto.
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Punto de color de la prioridad.
                        StatusIndicator(color = priorityColor(tab.priority))
                        // Espacio entre punto y texto.
                        Spacer(Modifier.width(8.dp))
                        // Texto con prioridad y contador.
                        Text(
                            // "Prioridad (N)".
                            text = stringResource(R.string.my_likes_tab_category, priorityText(tab.priority), tab.count),
                            // Estilo de título pequeño.
                            style = MaterialTheme.typography.titleSmall,
                            // Color principal.
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
                // Una tarjeta por publicación de la sección.
                items(posts, key = { it.id }) { post ->
                    // Tarjeta que abre el detalle al tocarla.
                    ReviewPostItem(post = post, onClick = { onPostClick(post.id) })
                }
            }
        }
    }
}

// Tarjeta resumida de una publicación en revisión.
@Composable
private fun ReviewPostItem(post: ReviewPostUi, onClick: () -> Unit) {
    // Mientras carga (o si falla) se ve la imagen local, igual que en "Me gusta".
    val fallback = painterResource(R.drawable.hero_image)
    // Painter que carga la foto remota con Coil.
    val photo = rememberAsyncImagePainter(model = post.photoUrl, placeholder = fallback, error = fallback)

    // Tarjeta común de resumen de publicación.
    PostSummaryCard(
        // Imagen local si no hay URL; si la hay, la remota.
        image = if (post.photoUrl == null) fallback else photo,
        // Categoría como etiqueta.
        typeLabel = post.categoryName,
        // Color del indicador según la prioridad.
        statusColor = priorityColor(post.priority),
        // Título de la publicación.
        title = post.title,
        // Líneas de metadatos (fecha y, si existe, motivo de la IA).
        metaLines = listOfNotNull(
            // Línea con la antigüedad de la publicación.
            PostMetaLine(Icons.Outlined.CalendarToday, stringResource(R.string.my_posts_published, agoText(post.ageHours))),
            // Línea con el motivo de la prioridad, solo si tiene texto.
            post.priorityReason.takeIf { it.isNotBlank() }?.let { PostMetaLine(Icons.Outlined.AutoAwesome, it) },
        ),
        // Acción al tocar la tarjeta.
        onClick = onClick,
    )
}

// Mensaje de estado centrado (vacío o error).
@Composable
private fun CenteredStatus(text: String, isError: Boolean = false) {
    // Componente común de mensajes.
    StatusMessage(
        // Texto a mostrar.
        text = text,
        // Indica si es un error.
        isError = isError,
        // Ancho completo con margen vertical.
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
    )
}

// Datos de ejemplo para las vistas previas.
private val previewReviews = listOf(
    // Publicación urgente de ejemplo.
    ReviewPostUi("p6", "Perrita herida necesita hogar urgente", "Adopción", null, 5, PriorityLevel.URGENT, "Animal herido que necesita atención"),
    // Publicación de prioridad alta de ejemplo.
    ReviewPostUi("p7", "Gatitos abandonados en una caja", "Encontrados", null, 26, PriorityLevel.HIGH, "Animales muy pequeños sin cuidado"),
    // Publicación de prioridad baja de ejemplo.
    ReviewPostUi("p8", "Jornada de vacunación gratuita", "Veterinaria", null, 3, PriorityLevel.LOW, "Evento informativo"),
)

// Vista previa con publicaciones en la pestaña "Todas".
@Preview(name = "Todas", showBackground = true, heightDp = 1000)
@Composable
private fun MyReviewsPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Contenido con los datos de ejemplo.
        MyReviewsContent(uiState = MyReviewsUiState(posts = previewReviews, isLoading = false), padding = PaddingValues(), onTabSelected = {}, onPostClick = {})
    }
}

// Vista previa con la lista vacía.
@Preview(name = "Vacía", showBackground = true, heightDp = 500)
@Composable
private fun MyReviewsEmptyPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Contenido sin publicaciones.
        MyReviewsContent(uiState = MyReviewsUiState(isLoading = false), padding = PaddingValues(), onTabSelected = {}, onPostClick = {})
    }
}
