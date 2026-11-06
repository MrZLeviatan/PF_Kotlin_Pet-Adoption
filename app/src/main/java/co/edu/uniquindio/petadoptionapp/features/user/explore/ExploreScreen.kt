package co.edu.uniquindio.petadoptionapp.features.user.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.petadoptionapp.core.component.map.MarkerShape
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.PrimaryButton
import co.edu.uniquindio.petadoptionapp.core.component.map.AppMap
import co.edu.uniquindio.petadoptionapp.core.component.map.MapMarker
import co.edu.uniquindio.petadoptionapp.core.component.map.rememberCurrentLocationRequester
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import coil3.compose.AsyncImage

/**
Pantalla Explorar (como en el diseño): un mapa MapLibre con las publicaciones cercanas a la persona
(ubicación actual), cada una con un marcador cuyo color, ícono y forma dependen de su categoría.
Arriba tiene el buscador de lugares con sus sugerencias, los botones "Filtros" y "Todos" y una
leyenda de marcadores; a la derecha un botón para volver a mi ubicación; y al tocar una publicación
aparece abajo su resumen con "Ver publicación". Cada publicación está en su punto aproximado, nunca
en la dirección exacta.
Estado: observa ExploreUiState desde ExploreViewModel. Eventos: ubicación obtenida, recentrar,
tocar marcador o mapa, buscar lugar, elegir sugerencia, abrir / aplicar / limpiar filtros.
Casos de uso: no los llama directamente; el ViewModel usa GetFeedUseCase, SearchAddressUseCase y los
casos de uso del catálogo y del usuario.
Navegación: no decide a dónde navegar; avisa con onPostClick y AppNavigation abre el detalle de la
publicación. El diálogo de filtros (ExploreFiltersDialog) se abre dentro de esta misma pantalla.
**/

// Composable principal de Explorar, conectado al ViewModel.
@Composable
fun ExploreScreen(
    // Márgenes que impone el Scaffold contenedor.
    padding: PaddingValues = PaddingValues(),
    // Acción para abrir el detalle de una publicación; la resuelve la navegación.
    onPostClick: (postId: String) -> Unit = {},
    // ViewModel de Explorar, inyectado por Hilt.
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    // Observa el estado del ViewModel respetando el ciclo de vida.
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Función que pide permiso y ubicación actual, y entrega el resultado a onCurrentLocation.
    val requestLocation = rememberCurrentLocationRequester(viewModel::onCurrentLocation)
    // Función que pide la ubicación actual para recentrar el mapa (botón "mi ubicación").
    val recenter = rememberCurrentLocationRequester(viewModel::onRecenter)

    // Al abrir, pide la ubicación actual (si se niega, se usa la del perfil).
    LaunchedEffect(Unit) { requestLocation() }

    // Contenedor que superpone el mapa y los controles flotantes.
    Box(
        modifier = Modifier
            // Ocupa toda la pantalla.
            .fillMaxSize()
            // Respeta los márgenes del Scaffold.
            .padding(padding),
    ) {
        // Mapa con los marcadores de las publicaciones.
        AppMap(
            // Centro del mapa.
            center = state.center,
            // Nivel de acercamiento inicial.
            zoom = 13.5,
            // Convierte cada publicación en un marcador del mapa.
            markers = state.posts.map { post ->
                // Estilo según la categoría de la publicación.
                val style = categoryMarkerStyle(state.categories.firstOrNull { it.id == post.categoryId })
                // Marcador con id, punto, color, si está seleccionado, ícono y forma.
                MapMarker(post.id, post.point, style.color, selected = post.id == state.selectedPostId, icon = style.icon, shape = style.shape)
            },
            // Ubicación del usuario (marcador "Tú").
            userLocation = state.userLocation,
            // Tocar un marcador selecciona la publicación.
            onMarkerClick = viewModel::onMarkerClick,
            // Tocar el mapa quita la selección.
            onMapClick = { viewModel.onMapClick() },
            // El mapa ocupa todo el contenedor.
            modifier = Modifier.fillMaxSize(),
        )

        // Arriba: buscador, sugerencias y los botones "Filtros" / "Todos".
        Column(
            modifier = Modifier
                // Alineada arriba al centro.
                .align(Alignment.TopCenter)
                // Ocupa todo el ancho.
                .fillMaxWidth()
                // Márgenes alrededor.
                .padding(16.dp),
            // Separación entre elementos.
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Buscador de lugares o direcciones.
            PlaceSearchField(query = state.query, onQueryChange = viewModel::onQueryChange)
            // Si hay sugerencias, las muestra en una tarjeta.
            if (state.suggestions.isNotEmpty()) {
                // Tarjeta con la lista de sugerencias.
                Column(
                    modifier = Modifier
                        // Ocupa todo el ancho.
                        .fillMaxWidth()
                        // Sombra con esquinas redondeadas.
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        // Fondo de superficie redondeado.
                        .background(AppTheme.colors.surface, RoundedCornerShape(16.dp)),
                ) {
                    // Recorre las sugerencias con su índice.
                    state.suggestions.forEachIndexed { index, place ->
                        // Pone una línea divisoria entre sugerencias (no antes de la primera).
                        if (index > 0) HorizontalDivider(color = AppTheme.colors.surfaceTag)
                        // Fila con ícono de ubicación y el texto del lugar.
                        IconTextRow(
                            // Ícono de ubicación.
                            icon = Icons.Outlined.LocationOn,
                            // Dirección y ciudad unidas por coma, omitiendo las vacías.
                            text = listOf(place.address, place.city).filter { it.isNotBlank() }.joinToString(", "),
                            // Estilo de cuerpo pequeño.
                            style = MaterialTheme.typography.bodySmall,
                            // Máximo dos líneas.
                            maxLines = 2,
                            modifier = Modifier
                                // Ocupa todo el ancho.
                                .fillMaxWidth()
                                // Tocarla centra el mapa en ese lugar.
                                .clickable(role = Role.Button) { viewModel.onSuggestionClick(place) }
                                // Margen interno.
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        )
                    }
                }
            }
            // Fila con los botones "Filtros" y "Todos".
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón "Filtros" que abre el diálogo de filtros.
                MapChip(
                    // Muestra "Filtros (N)" si hay filtros activos; si no, solo "Filtros".
                    text = if (state.filters.activeCount > 0) stringResource(R.string.explore_filters_count, state.filters.activeCount)
                    else stringResource(R.string.explore_filters),
                    // Ícono de ajustes.
                    icon = Icons.Outlined.Tune,
                    // Abre los filtros.
                    onClick = viewModel::onOpenFilters,
                )
                // Botón "Todos" que quita los filtros.
                MapChip(text = stringResource(R.string.explore_all), onClick = viewModel::onClearFiltersClick)
            }
            // Leyenda: qué significa cada marcador (categorías del catálogo y "Tú").
            MapLegend(categories = state.categories)
        }

        // Volver a mi ubicación.
        SmallFloatingActionButton(
            // Pide la ubicación y recentra el mapa.
            onClick = recenter,
            // Color de fondo.
            containerColor = AppTheme.colors.surface,
            // Color del ícono.
            contentColor = AppTheme.colors.textPrimary,
            modifier = Modifier
                // Alineado al centro del borde derecho.
                .align(Alignment.CenterEnd)
                // Margen derecho.
                .padding(end = 16.dp),
        ) {
            // Ícono de "mi ubicación" con descripción accesible.
            Icon(Icons.Outlined.MyLocation, contentDescription = stringResource(R.string.explore_my_location))
        }

        // Abajo: el resumen de la publicación elegida, o el aviso si no hay nada cerca.
        Box(
            modifier = Modifier
                // Alineado abajo al centro.
                .align(Alignment.BottomCenter)
                // Ocupa todo el ancho.
                .fillMaxWidth(),
        ) {
            // Publicación seleccionada, si hay.
            val selected = state.selectedPost
            // Decide qué mostrar abajo.
            when {
                // Si hay publicación seleccionada, muestra su resumen con "Ver publicación".
                selected != null -> PostSummarySheet(post = selected, onViewPost = { onPostClick(selected.id) })
                // Si ya terminó de localizar y cargar y no hay publicaciones, muestra un aviso.
                !state.isLocating && !state.isLoading && state.posts.isEmpty() -> Text(
                    // Texto "No hay publicaciones cerca".
                    text = stringResource(R.string.explore_empty),
                    // Estilo de etiqueta mediana.
                    style = MaterialTheme.typography.labelMedium,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        // Centrado en el contenedor.
                        .align(Alignment.Center)
                        // Margen exterior.
                        .padding(16.dp)
                        // Fondo en forma de píldora.
                        .background(AppTheme.colors.surface, RoundedCornerShape(50))
                        // Margen interior.
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    // Si se abrieron los filtros, muestra el diálogo.
    if (state.showFilters) {
        // Diálogo de filtros a pantalla completa.
        ExploreFiltersDialog(state = state, viewModel = viewModel)
    }
}

// Campo de búsqueda redondeado para lugares o direcciones sobre el mapa.
@Composable
private fun PlaceSearchField(query: String, onQueryChange: (String) -> Unit) {
    // Campo de texto de Material 3.
    TextField(
        // Texto actual.
        value = query,
        // Notifica cada cambio.
        onValueChange = onQueryChange,
        modifier = Modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Sombra redondeada.
            .shadow(4.dp, RoundedCornerShape(28.dp))
            // Borde fino redondeado.
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(28.dp)),
        // Texto de ayuda cuando está vacío.
        placeholder = { Text(stringResource(R.string.explore_search_placeholder)) },
        // Ícono de lupa decorativo.
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        // Una sola línea.
        singleLine = true,
        // Forma redondeada.
        shape = RoundedCornerShape(28.dp),
        // Colores del campo.
        colors = TextFieldDefaults.colors(
            // Fondo con foco.
            focusedContainerColor = AppTheme.colors.background,
            // Fondo sin foco.
            unfocusedContainerColor = AppTheme.colors.background,
            // Oculta la línea indicadora con foco.
            focusedIndicatorColor = Color.Transparent,
            // Oculta la línea indicadora sin foco.
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

// Botón redondeado amarillo sobre el mapa ("Filtros", "Todos").
@Composable
private fun MapChip(text: String, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    // Superficie táctil con forma de píldora.
    Surface(
        // Acción al tocar.
        onClick = onClick,
        // Forma de píldora.
        shape = RoundedCornerShape(50),
        // Fondo con el color de acción principal.
        color = AppTheme.colors.actionPrimary,
        // Color del contenido.
        contentColor = AppTheme.colors.onActionPrimary,
        // Sombra ligera.
        shadowElevation = 3.dp,
    ) {
        // Fila con ícono opcional y texto.
        Row(
            // Margen interno.
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            // Centra verticalmente.
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Si hay ícono, lo muestra.
            if (icon != null) {
                // Ícono decorativo.
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                // Espacio entre ícono y texto.
                Spacer(Modifier.width(6.dp))
            }
            // Texto del botón en negrita.
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// Resumen de la publicación tocada (foto, categoría y distancia, título, mascota, interesados, "Ver publicación").
@Composable
private fun PostSummarySheet(post: ExplorePostUi, onViewPost: () -> Unit) {
    // Imagen de respaldo si la publicación no tiene foto o falla la carga.
    val fallback = painterResource(R.drawable.hero_image)
    // Hoja inferior con el resumen.
    Column(
        modifier = Modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Sombra con esquinas superiores redondeadas.
            .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            // Fondo con el color de marca.
            .background(AppTheme.colors.brand, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            // Margen interno.
            .padding(16.dp),
        // Centra horizontalmente el contenido.
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Pequeña barra decorativa (asa) en la parte superior de la hoja.
        Box(
            Modifier
                // Tamaño del asa.
                .size(width = 36.dp, height = 4.dp)
                // Fondo semitransparente redondeado.
                .background(AppTheme.colors.textPrimary.copy(alpha = 0.3f), RoundedCornerShape(50)),
        )
        // Espacio bajo el asa.
        Spacer(Modifier.height(12.dp))
        // Fila con la foto y los datos.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Foto de la publicación cargada con Coil.
            AsyncImage(
                // URL de la foto.
                model = post.imageUrl,
                // Imagen decorativa (el título ya describe la publicación).
                contentDescription = null,
                // Recorta para llenar el espacio.
                contentScale = ContentScale.Crop,
                // Color mientras carga.
                placeholder = ColorPainter(AppTheme.colors.surfaceTag),
                // Imagen de respaldo si falla.
                error = fallback,
                // Imagen de respaldo si la URL es null.
                fallback = fallback,
                modifier = Modifier
                    // Tamaño cuadrado.
                    .size(96.dp)
                    // Esquinas redondeadas.
                    .clip(RoundedCornerShape(16.dp)),
            )
            // Espacio entre foto y datos.
            Spacer(Modifier.width(14.dp))
            // Columna con los datos que ocupa el resto del ancho.
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Fila de etiquetas: categoría y distancia.
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Etiqueta de la categoría.
                    Tag(text = post.categoryName, icon = Icons.Outlined.Pets)
                    // Etiqueta de la distancia, solo si se conoce.
                    post.distanceKm?.let { Tag(text = stringResource(R.string.explore_distance_km, it), icon = Icons.Outlined.LocationOn) }
                }
                // Título de la publicación.
                Text(
                    // Texto del título.
                    text = post.title,
                    // Estilo de título mediano.
                    style = MaterialTheme.typography.titleMedium,
                    // En negrita.
                    fontWeight = FontWeight.Bold,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                    // Máximo dos líneas.
                    maxLines = 2,
                    // Corta con puntos suspensivos.
                    overflow = TextOverflow.Ellipsis,
                )
                // Detalles de la mascota: tipo, raza (si hay) y tamaño, omitiendo los vacíos.
                val details = listOfNotNull(post.animalName, post.breed.takeIf { it.isNotBlank() }, post.size?.let { sizeText(it) })
                // Solo muestra la fila si hay algún detalle.
                if (details.isNotEmpty()) {
                    // Fila con huella y los detalles separados por "•".
                    IconTextRow(icon = Icons.Outlined.Pets, text = details.joinToString(" • "), iconSize = 14.dp, spacing = 6.dp, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                // Fila con la cantidad de interesados.
                IconTextRow(
                    // Ícono de corazón.
                    icon = Icons.Outlined.FavoriteBorder,
                    // Texto en plural "N interesados".
                    text = pluralStringResource(R.plurals.my_posts_interested, post.importantCount, post.importantCount),
                    // Tamaño del ícono.
                    iconSize = 14.dp,
                    // Separación entre ícono y texto.
                    spacing = 6.dp,
                    // Estilo de cuerpo pequeño.
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        // Espacio antes del botón.
        Spacer(Modifier.height(14.dp))
        // Botón "Ver publicación" que abre el detalle.
        PrimaryButton(text = stringResource(R.string.explore_view_post), onClick = onViewPost)
    }
}

// Etiqueta pequeña con ícono y texto (categoría o distancia) dentro del resumen.
@Composable
private fun Tag(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    // Fila con fondo redondeado.
    Row(
        modifier = Modifier
            // Fondo de etiqueta redondeado.
            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(10.dp))
            // Margen interno.
            .padding(horizontal = 8.dp, vertical = 3.dp),
        // Centra verticalmente.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ícono decorativo pequeño.
        Icon(icon, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(12.dp))
        // Espacio entre ícono y texto.
        Spacer(Modifier.width(4.dp))
        // Texto de la etiqueta en una línea.
        Text(text, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary, maxLines = 1)
    }
}

// Devuelve el texto traducido del tamaño del animal; se reutiliza en el resumen y en los filtros.
@Composable
internal fun sizeText(size: AnimalSize): String = stringResource(
    // Elige el recurso de texto según el tamaño.
    when (size) {
        // Pequeño.
        AnimalSize.SMALL -> R.string.home_size_small
        // Mediano.
        AnimalSize.MEDIUM -> R.string.home_size_medium
        // Grande.
        AnimalSize.LARGE -> R.string.home_size_large
    }
)

// Cómo se ve el marcador de una categoría: color, ícono y forma (lugares en cuadrado, como en el diseño).
internal data class CategoryMarkerStyle(val color: Color, val icon: ImageVector, val shape: MarkerShape)

// El ícono, el color y la forma vienen de la categoría (Firestore): una categoría nueva no requiere recompilar.
// Sin categoría (o con un ícono desconocido) se usa la huella con el color de la marca.
@Composable
internal fun categoryMarkerStyle(category: Category?): CategoryMarkerStyle {
    // Traduce el nombre del ícono guardado en la categoría a un ícono de Material.
    val icon = when (category?.icon) {
        // Lupa (por ejemplo, mascotas perdidas).
        "search" -> Icons.Filled.Search
        // Bandera.
        "flag" -> Icons.Filled.Flag
        // Casa.
        "home" -> Icons.Filled.Home
        // Hospital (veterinarias).
        "hospital" -> Icons.Filled.LocalHospital
        // Corazón.
        "heart" -> Icons.Filled.Favorite
        // Estrella.
        "star" -> Icons.Filled.Star
        // Cualquier otro valor: huella.
        else -> Icons.Filled.Pets
    }
    // Color de la categoría en formato hexadecimal.
    val color = category?.color
        // Intenta convertir el texto en color; si no es válido devuelve null.
        ?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
        // Si no hay color válido, usa el color de la marca.
        ?: AppTheme.colors.brand
    // Los lugares (veterinarias, refugios…) se dibujan en cuadrado; el resto en círculo.
    val shape = if (category?.isPlace == true) MarkerShape.ROUNDED_SQUARE else MarkerShape.CIRCLE
    // Devuelve el estilo calculado.
    return CategoryMarkerStyle(color, icon, shape)
}

// Leyenda desplazable: un mini marcador por categoría y el de la persona.
@Composable
private fun MapLegend(categories: List<Category>) {
    // Fila con desplazamiento horizontal.
    Row(
        modifier = Modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Permite desplazar horizontalmente si no caben.
            .horizontalScroll(rememberScrollState()),
        // Separación entre elementos.
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Un elemento por categoría.
        categories.forEach { category ->
            // Estilo del marcador de la categoría.
            val style = categoryMarkerStyle(category)
            // Elemento de la leyenda con nombre y mini marcador.
            LegendItem(name = category.name, color = style.color, icon = style.icon, shape = style.shape)
        }
        // Elemento final "Tú", con el azul del marcador del usuario.
        LegendItem(name = stringResource(R.string.explore_legend_you), color = Color(0xFF2F80ED), icon = Icons.Filled.Person, shape = MarkerShape.CIRCLE)
    }
}

// Elemento de la leyenda: mini marcador y nombre dentro de una píldora.
@Composable
private fun LegendItem(name: String, color: Color, icon: ImageVector, shape: MarkerShape) {
    // Fila con forma de píldora.
    Row(
        modifier = Modifier
            // Sombra ligera.
            .shadow(2.dp, RoundedCornerShape(50))
            // Fondo de superficie en forma de píldora.
            .background(AppTheme.colors.surface, RoundedCornerShape(50))
            // Margen interno.
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        // Centra verticalmente.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Mini marcador.
        MarkerBadge(color = color, icon = icon, shape = shape)
        // Espacio entre marcador y nombre.
        Spacer(Modifier.width(6.dp))
        // Nombre en una línea.
        Text(name, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary, maxLines = 1)
    }
}

// Mini marcador de una categoría (leyenda y filtros): mismo color, ícono y forma que en el mapa.
@Composable
internal fun CategoryMarkerIcon(category: Category) {
    // Calcula el estilo de la categoría.
    val style = categoryMarkerStyle(category)
    // Dibuja el mini marcador.
    MarkerBadge(color = style.color, icon = style.icon, shape = style.shape)
}

// Mini marcador genérico: fondo de color (círculo o cuadrado redondeado) con un ícono centrado.
@Composable
private fun MarkerBadge(color: Color, icon: ImageVector, shape: MarkerShape) {
    // Contenedor con el fondo del marcador.
    Box(
        modifier = Modifier
            // Tamaño del mini marcador.
            .size(20.dp)
            // Fondo de color con forma de círculo o cuadrado redondeado.
            .background(color, if (shape == MarkerShape.CIRCLE) CircleShape else RoundedCornerShape(6.dp)),
        // Centra el ícono.
        contentAlignment = Alignment.Center,
    ) {
        // Ícono oscuro sobre colores claros (amarillo), blanco sobre los demás, igual que en el mapa.
        val tint = if (color.luminance() > 0.5f) Color(0xFF3A2E24) else Color.White
        // Ícono decorativo.
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
    }
}
