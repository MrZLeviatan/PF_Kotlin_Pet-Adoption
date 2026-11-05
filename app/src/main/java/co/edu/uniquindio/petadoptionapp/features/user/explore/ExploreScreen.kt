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
 Explorar (como en el diseño): el mapa con las publicaciones cercanas a la persona (ubicación actual),
 el buscador de lugares, "Filtros" y "Todos" arriba, y al tocar una publicación su resumen abajo
 con "Ver publicación". Cada publicación está en su punto aproximado, nunca en la dirección exacta.
 */
@Composable
fun ExploreScreen(
    padding: PaddingValues = PaddingValues(),
    onPostClick: (postId: String) -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val requestLocation = rememberCurrentLocationRequester(viewModel::onCurrentLocation)
    val recenter = rememberCurrentLocationRequester(viewModel::onRecenter)

    // Al abrir, pide la ubicación actual (si se niega, se usa la del perfil).
    LaunchedEffect(Unit) { requestLocation() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        AppMap(
            center = state.center,
            zoom = 13.5,
            markers = state.posts.map { post ->
                val style = categoryMarkerStyle(post.categoryId)
                MapMarker(post.id, post.point, style.color, selected = post.id == state.selectedPostId, icon = style.icon, shape = style.shape)
            },
            userLocation = state.userLocation,
            onMarkerClick = viewModel::onMarkerClick,
            onMapClick = { viewModel.onMapClick() },
            modifier = Modifier.fillMaxSize(),
        )

        // Arriba: buscador, sugerencias y los botones "Filtros" / "Todos".
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlaceSearchField(query = state.query, onQueryChange = viewModel::onQueryChange)
            if (state.suggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.surface, RoundedCornerShape(16.dp)),
                ) {
                    state.suggestions.forEachIndexed { index, place ->
                        if (index > 0) HorizontalDivider(color = AppTheme.colors.surfaceTag)
                        IconTextRow(
                            icon = Icons.Outlined.LocationOn,
                            text = listOf(place.address, place.city).filter { it.isNotBlank() }.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) { viewModel.onSuggestionClick(place) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MapChip(
                    text = if (state.filters.activeCount > 0) stringResource(R.string.explore_filters_count, state.filters.activeCount)
                    else stringResource(R.string.explore_filters),
                    icon = Icons.Outlined.Tune,
                    onClick = viewModel::onOpenFilters,
                )
                MapChip(text = stringResource(R.string.explore_all), onClick = viewModel::onClearFiltersClick)
            }
            // Leyenda: qué significa cada marcador (categorías del catálogo y "Tú").
            MapLegend(categories = state.categories.map { it.id to it.name })
        }

        // Volver a mi ubicación.
        SmallFloatingActionButton(
            onClick = recenter,
            containerColor = AppTheme.colors.surface,
            contentColor = AppTheme.colors.textPrimary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = stringResource(R.string.explore_my_location))
        }

        // Abajo: el resumen de la publicación elegida, o el aviso si no hay nada cerca.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            val selected = state.selectedPost
            when {
                selected != null -> PostSummarySheet(post = selected, onViewPost = { onPostClick(selected.id) })
                !state.isLocating && !state.isLoading && state.posts.isEmpty() -> Text(
                    text = stringResource(R.string.explore_empty),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                        .background(AppTheme.colors.surface, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    if (state.showFilters) {
        ExploreFiltersDialog(state = state, viewModel = viewModel)
    }
}

@Composable
private fun PlaceSearchField(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(28.dp)),
        placeholder = { Text(stringResource(R.string.explore_search_placeholder)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.background,
            unfocusedContainerColor = AppTheme.colors.background,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

// Botón redondeado amarillo sobre el mapa ("Filtros", "Todos").
@Composable
private fun MapChip(text: String, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = AppTheme.colors.actionPrimary,
        contentColor = AppTheme.colors.onActionPrimary,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// Resumen de la publicación tocada (foto, categoría y distancia, título, mascota, interesados, "Ver publicación").
@Composable
private fun PostSummarySheet(post: ExplorePostUi, onViewPost: () -> Unit) {
    val fallback = painterResource(R.drawable.hero_image)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(AppTheme.colors.brand, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = 36.dp, height = 4.dp)
                .background(AppTheme.colors.textPrimary.copy(alpha = 0.3f), RoundedCornerShape(50)),
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(AppTheme.colors.surfaceTag),
                error = fallback,
                fallback = fallback,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(text = post.categoryName, icon = Icons.Outlined.Pets)
                    post.distanceKm?.let { Tag(text = stringResource(R.string.explore_distance_km, it), icon = Icons.Outlined.LocationOn) }
                }
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val details = listOfNotNull(post.animalName, post.breed.takeIf { it.isNotBlank() }, post.size?.let { sizeText(it) })
                if (details.isNotEmpty()) {
                    IconTextRow(icon = Icons.Outlined.Pets, text = details.joinToString(" • "), iconSize = 14.dp, spacing = 6.dp, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                IconTextRow(
                    icon = Icons.Outlined.FavoriteBorder,
                    text = pluralStringResource(R.plurals.my_posts_interested, post.importantCount, post.importantCount),
                    iconSize = 14.dp,
                    spacing = 6.dp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton(text = stringResource(R.string.explore_view_post), onClick = onViewPost)
    }
}

@Composable
private fun Tag(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .background(AppTheme.colors.surfaceTag, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary, maxLines = 1)
    }
}

@Composable
internal fun sizeText(size: AnimalSize): String = stringResource(
    when (size) {
        AnimalSize.SMALL -> R.string.home_size_small
        AnimalSize.MEDIUM -> R.string.home_size_medium
        AnimalSize.LARGE -> R.string.home_size_large
    }
)

/** Cómo se ve el marcador de una categoría: color, ícono y forma (lugares en cuadrado, como en el diseño). */
internal data class CategoryMarkerStyle(val color: Color, val icon: ImageVector, val shape: MarkerShape)

// Adopción: café con huella. Perdidos: rojo con lupa. Encontrados: verde con bandera.
// Hogar temporal: amarillo con casa. Veterinaria: amarillo con hospital (cuadrados: son lugares).
@Composable
internal fun categoryMarkerStyle(categoryId: String): CategoryMarkerStyle = when (categoryId) {
    "adoption" -> CategoryMarkerStyle(AppTheme.colors.actionSecondary, Icons.Filled.Pets, MarkerShape.CIRCLE)
    "lost" -> CategoryMarkerStyle(AppTheme.colors.stateError, Icons.Filled.Search, MarkerShape.CIRCLE)
    "found" -> CategoryMarkerStyle(AppTheme.colors.stateSuccess, Icons.Filled.Flag, MarkerShape.CIRCLE)
    "temporary" -> CategoryMarkerStyle(AppTheme.colors.actionPrimary, Icons.Filled.Home, MarkerShape.ROUNDED_SQUARE)
    "veterinary" -> CategoryMarkerStyle(AppTheme.colors.actionPrimary, Icons.Filled.LocalHospital, MarkerShape.ROUNDED_SQUARE)
    else -> CategoryMarkerStyle(AppTheme.colors.brand, Icons.Filled.Pets, MarkerShape.CIRCLE)
}

// Leyenda desplazable: un mini marcador por categoría y el de la persona.
@Composable
private fun MapLegend(categories: List<Pair<String, String>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        categories.forEach { (id, name) ->
            val style = categoryMarkerStyle(id)
            LegendItem(name = name, color = style.color, icon = style.icon, shape = style.shape)
        }
        LegendItem(name = stringResource(R.string.explore_legend_you), color = Color(0xFF2F80ED), icon = Icons.Filled.Person, shape = MarkerShape.CIRCLE)
    }
}

@Composable
private fun LegendItem(name: String, color: Color, icon: ImageVector, shape: MarkerShape) {
    Row(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(50))
            .background(AppTheme.colors.surface, RoundedCornerShape(50))
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, if (shape == MarkerShape.CIRCLE) CircleShape else RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(6.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textPrimary, maxLines = 1)
    }
}

