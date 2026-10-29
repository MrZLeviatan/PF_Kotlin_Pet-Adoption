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

private const val MAX_ZOOM = 4f

/**
 Visor de fotos a pantalla completa (fondo negro): se desliza entre las fotos, se amplía pellizcando
 o con doble toque, y se cierra con la X o con "atrás" (vuelve a la pantalla que lo abrió).
 Empieza en la foto [initialPage]. Mientras una foto está ampliada no se cambia de página, para poder moverla.
 */
@Composable
fun PhotoViewerDialog(
    imageUrls: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit,
) {
    val pageCount = imageUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, pageCount - 1), pageCount = { pageCount })
    // Zoom de la foto visible; al cambiar de página vuelve a 1.
    var zoomed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !zoomed,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                ZoomablePhoto(
                    url = imageUrls.getOrNull(page),
                    description = stringResource(R.string.post_detail_photo, page + 1, pageCount),
                    isCurrent = page == pagerState.currentPage,
                    onZoomChange = { if (page == pagerState.currentPage) zoomed = it },
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .safeDrawingPadding()
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.photo_viewer_close))
            }

            if (pageCount > 1) {
                // Contador "2/5", igual que en el detalle.
                Surface(
                    modifier = Modifier
                        .safeDrawingPadding()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f),
                ) {
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

// Una foto que se amplía pellizcando (1x a 4x) o con doble toque, y se mueve mientras está ampliada.
@Composable
private fun ZoomablePhoto(url: String?, description: String, isCurrent: Boolean, onZoomChange: (Boolean) -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Al salir de esta foto se reinicia el zoom.
    LaunchedEffect(isCurrent) {
        if (!isCurrent) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, MAX_ZOOM)
        offset = if (scale > 1f) offset + panChange else Offset.Zero
        onZoomChange(scale > 1f)
    }

    val fallback = painterResource(R.drawable.hero_image)
    AsyncImage(
        model = url,
        contentDescription = description,
        contentScale = ContentScale.Fit,
        error = fallback,
        fallback = fallback,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    val zoomIn = scale == 1f
                    scale = if (zoomIn) 2f else 1f
                    offset = Offset.Zero
                    onZoomChange(zoomIn)
                })
            }
            // Solo se mueve con un dedo cuando está ampliada; si no, el deslizamiento cambia de foto.
            .transformable(state = transformState, canPan = { scale > 1f })
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
    )
}
