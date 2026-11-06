package co.edu.uniquindio.petadoptionapp.core.component.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.edu.uniquindio.petadoptionapp.R
import coil3.compose.AsyncImage

/**
Visor de fotos a pantalla completa, usado al tocar las fotos de una publicación (detalle) o de un mensaje.
Se muestra dentro de un [Dialog] que ocupa toda la pantalla; las fotos se recorren con un [HorizontalPager] y cada una
se carga con Coil ([AsyncImage]). El zoom se implementa con gestos de transformación (pellizcar y arrastrar) y doble
toque, aplicando escala y desplazamiento con [graphicsLayer] para que sea fluido sin volver a medir la imagen.
**/

// Ampliación máxima permitida al pellizcar (4 veces el tamaño original).
private const val MAX_ZOOM = 4f

/**
Visor de fotos a pantalla completa (fondo negro): se desliza entre las fotos, se amplía pellizcando
o con doble toque, y se cierra con la X o con "atrás" (vuelve a la pantalla que lo abrió).
Empieza en la foto [initialPage]. Mientras una foto está ampliada no se cambia de página, para poder moverla.
**/
@Composable
fun PhotoViewerDialog(
    // Rutas o URLs de las fotos a mostrar.
    imageUrls: List<String>,
    // Posición de la foto con la que se abre el visor.
    initialPage: Int,
    // Acción para cerrar el visor.
    onDismiss: () -> Unit,
) {
    // Número de páginas; al menos 1 para que el pager no quede vacío (se mostraría la imagen de respaldo).
    val pageCount = imageUrls.size.coerceAtLeast(1)
    // Estado del pager, empezando en la foto pedida (ajustada al rango válido).
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, pageCount - 1), pageCount = { pageCount })
    // Indica si la foto visible está ampliada; mientras lo esté se bloquea el deslizamiento entre páginas.
    var zoomed by remember { mutableStateOf(false) }

    // Diálogo que se muestra encima de la pantalla actual; "atrás" o tocar fuera llaman a onDismiss.
    Dialog(
        onDismissRequest = onDismiss,
        // Sin ancho por defecto de diálogo y dibujando bajo las barras del sistema, para ocupar toda la pantalla.
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        // Contenedor de pantalla completa con fondo negro.
        Box(
            modifier = Modifier
                // Ocupa toda la pantalla.
                .fillMaxSize()
                // Fondo negro para resaltar las fotos.
                .background(Color.Black),
        ) {
            // Carrusel horizontal de fotos.
            HorizontalPager(
                // Estado que controla la página actual.
                state = pagerState,
                // Desactiva el deslizamiento mientras la foto está ampliada, para poder moverla.
                userScrollEnabled = !zoomed,
                // Ocupa toda la pantalla.
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                // Foto ampliable de la página actual.
                ZoomablePhoto(
                    // Foto de esta página (null si la lista estaba vacía).
                    url = imageUrls.getOrNull(page),
                    // Descripción accesible "Foto X de N".
                    description = stringResource(R.string.post_detail_photo, page + 1, pageCount),
                    // Indica si esta página es la visible.
                    isCurrent = page == pagerState.currentPage,
                    // Solo la foto visible actualiza el estado de zoom.
                    onZoomChange = { if (page == pagerState.currentPage) zoomed = it },
                )
            }

            // Botón para cerrar el visor.
            IconButton(
                // Al tocarlo se cierra el visor.
                onClick = onDismiss,
                modifier = Modifier
                    // Evita que quede bajo la barra de estado o el recorte de cámara.
                    .safeDrawingPadding()
                    // Esquina superior derecha.
                    .align(Alignment.TopEnd)
                    // Separación del borde.
                    .padding(8.dp),
                // Fondo negro semitransparente e ícono blanco para que se vea sobre cualquier foto.
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                ),
            ) {
                // Ícono de cerrar con descripción accesible.
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.photo_viewer_close))
            }

            // El contador solo tiene sentido si hay más de una foto.
            if (pageCount > 1) {
                // Contador "2/5", igual que en el detalle.
                Surface(
                    modifier = Modifier
                        // Evita la barra de navegación del sistema.
                        .safeDrawingPadding()
                        // Parte inferior central.
                        .align(Alignment.BottomCenter)
                        // Separación del borde.
                        .padding(16.dp),
                    // Forma de pastilla.
                    shape = RoundedCornerShape(50),
                    // Fondo blanco muy transparente.
                    color = Color.White.copy(alpha = 0.2f),
                ) {
                    // Texto con la página actual y el total.
                    Text(
                        text = "${pagerState.currentPage + 1}/$pageCount",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

/**
Una foto que se amplía pellizcando (1x a 4x) o con doble toque, y se mueve mientras está ampliada.
Avisa con [onZoomChange] si quedó ampliada, para que el visor bloquee el cambio de página.
**/
@Composable
private fun ZoomablePhoto(url: String?, description: String, isCurrent: Boolean, onZoomChange: (Boolean) -> Unit) {
    // Escala actual de la foto (1 = tamaño normal).
    var scale by remember { mutableFloatStateOf(1f) }
    // Desplazamiento actual de la foto ampliada.
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Al salir de esta foto se reinicia el zoom.
    LaunchedEffect(isCurrent) {
        // Solo se reinicia cuando la página deja de ser la visible.
        if (!isCurrent) {
            // Vuelve al tamaño normal.
            scale = 1f
            // Quita el desplazamiento.
            offset = Offset.Zero
        }
    }

    // Estado que recibe los gestos de pellizcar (zoom) y arrastrar (desplazamiento).
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        // Aplica el cambio de zoom limitado entre 1x y MAX_ZOOM.
        scale = (scale * zoomChange).coerceIn(1f, MAX_ZOOM)
        // Solo permite mover la foto si está ampliada; si no, la centra.
        offset = if (scale > 1f) offset + panChange else Offset.Zero
        // Avisa si la foto quedó ampliada.
        onZoomChange(scale > 1f)
    }

    // Imagen de respaldo si la foto falla o no hay URL.
    val fallback = painterResource(R.drawable.hero_image)
    // Carga la foto de forma asíncrona con Coil.
    AsyncImage(
        // Ruta o URL de la foto.
        model = url,
        // Descripción accesible.
        contentDescription = description,
        // Muestra la foto completa sin recortarla.
        contentScale = ContentScale.Fit,
        // Imagen a mostrar si la carga falla.
        error = fallback,
        // Imagen a mostrar si la URL es nula.
        fallback = fallback,
        modifier = Modifier
            // Ocupa toda la página.
            .fillMaxSize()
            // Detecta el doble toque.
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    // Si está en tamaño normal, se amplía; si no, se reduce.
                    val zoomIn = scale == 1f
                    // Alterna entre 2x y 1x.
                    scale = if (zoomIn) 2f else 1f
                    // Centra la foto.
                    offset = Offset.Zero
                    // Avisa el nuevo estado de zoom.
                    onZoomChange(zoomIn)
                })
            }
            // Solo se mueve con un dedo cuando está ampliada; si no, el deslizamiento cambia de foto.
            .transformable(state = transformState, canPan = { scale > 1f })
            // Aplica escala y desplazamiento en la capa gráfica, sin recalcular el diseño.
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
    )
}
