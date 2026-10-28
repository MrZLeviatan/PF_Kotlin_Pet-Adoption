package co.edu.uniquindio.petadoptionapp.core.component.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import coil3.compose.AsyncImage

// Columnas de la cuadrícula de fotos.
private const val COLUMNS = 2

/**
 Cuadrícula de fotos de 2 columnas: las elegidas (la primera marcada "Principal", cada una con su X
 para quitarla) y un cuadro punteado "+ Añadir" mientras no se llegue a [maxPhotos].
 Sin fotos, el cuadro de añadir ocupa todo el ancho.
 */
@Composable
fun PhotoPickerGrid(
    photos: List<String>,
    maxPhotos: Int,
    onAddClick: () -> Unit,
    onRemove: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        AddPhotoBox(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            onClick = onAddClick,
        )
        return
    }

    // Las fotos y, al final, el cuadro para añadir otra (si aún hay espacio).
    val slots = buildList<String?> {
        addAll(photos)
        if (photos.size < maxPhotos) add(null)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        slots.chunked(COLUMNS).forEachIndexed { rowIndex, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEachIndexed { colIndex, photo ->
                    val index = rowIndex * COLUMNS + colIndex
                    val itemModifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                    if (photo == null) {
                        AddPhotoBox(modifier = itemModifier, onClick = onAddClick)
                    } else {
                        PhotoItem(url = photo, isMain = index == 0, onRemove = { onRemove(index) }, modifier = itemModifier)
                    }
                }
                // Completa la fila para que la última foto no ocupe todo el ancho.
                if (rowItems.size < COLUMNS) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// Cuadro punteado "+ Añadir" que abre la elección de origen de la foto.
@Composable
private fun AddPhotoBox(modifier: Modifier, onClick: () -> Unit) {
    val borderColor = AppTheme.colors.border
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            // Borde punteado: no existe como modificador, se dibuja.
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = AppTheme.colors.textPrimary)
            Text(stringResource(R.string.photo_add), fontSize = 12.sp, color = AppTheme.colors.textPrimary)
        }
    }
}

// Foto elegida: la imagen, la X para quitarla y la etiqueta "Principal" en la primera.
@Composable
private fun PhotoItem(url: String, isMain: Boolean, onRemove: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        AsyncImage(
            model = url,
            contentDescription = stringResource(R.string.photo_description),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color.LightGray),
            modifier = Modifier.fillMaxSize(),
        )
        // X blanca sobre fondo oscuro: se lee sobre cualquier foto.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable(role = Role.Button, onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.photo_remove), tint = Color.White, modifier = Modifier.size(14.dp))
        }
        if (isMain) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                shape = RoundedCornerShape(6.dp),
                color = AppTheme.colors.surfaceNotice,
            ) {
                Text(
                    stringResource(R.string.photo_main),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = AppTheme.colors.textPrimary,
                )
            }
        }
    }
}

/** Hoja inferior "Agregar foto" para elegir el origen: cámara o galería. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppTheme.colors.background) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.photo_sheet_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.textPrimary,
            )
            SheetOption(stringResource(R.string.photo_camera), Icons.Outlined.CameraAlt, onCameraClick)
            SheetOption(stringResource(R.string.photo_gallery), Icons.Outlined.Image, onGalleryClick)
        }
    }
}

// Opción de la hoja: botón ancho con icono y texto.
@Composable
private fun SheetOption(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = AppTheme.colors.stateSelected) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = AppTheme.colors.textPrimary)
            Text(text, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PhotoPickerGridPreview() {
    PetAdoptionAppTheme {
        PhotoPickerGrid(photos = listOf("a", "b", "c"), maxPhotos = 5, onAddClick = {}, onRemove = {})
    }
}
