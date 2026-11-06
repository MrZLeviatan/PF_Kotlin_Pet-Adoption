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

/**
Componentes visuales para gestionar las fotos de un formulario (por ejemplo, Crear/Editar publicación):
- [PhotoPickerGrid]: cuadrícula de 2 columnas con las fotos elegidas y un cuadro "+ Añadir".
- [PhotoSourceSheet]: hoja inferior para elegir si la foto viene de la cámara o de la galería
  (la lógica de permisos y lanzamiento está en [ImageSourcePicker]).
Las imágenes se cargan con Coil ([AsyncImage]), que admite tanto rutas locales como URLs remotas.
**/

// Columnas de la cuadrícula de fotos.
private const val COLUMNS = 2

/**
Cuadrícula de fotos de 2 columnas: las elegidas (la primera marcada "Principal", cada una con su X
para quitarla) y un cuadro punteado "+ Añadir" mientras no se llegue a [maxPhotos].
Sin fotos, el cuadro de añadir ocupa todo el ancho.
**/
@Composable
fun PhotoPickerGrid(
    // Rutas o URLs de las fotos elegidas; la primera es la principal.
    photos: List<String>,
    // Número máximo de fotos permitidas.
    maxPhotos: Int,
    // Acción al tocar el cuadro de añadir (normalmente abre la hoja cámara/galería).
    onAddClick: () -> Unit,
    // Acción que recibe la posición de la foto que se quiere quitar.
    onRemove: (index: Int) -> Unit,
    // Modificador para el contenedor.
    modifier: Modifier = Modifier,
) {
    // Sin fotos se muestra solo el cuadro de añadir, grande y cuadrado.
    if (photos.isEmpty()) {
        AddPhotoBox(
            modifier = modifier
                // Ocupa todo el ancho.
                .fillMaxWidth()
                // Mantiene proporción cuadrada.
                .aspectRatio(1f),
            // Al tocarlo se pide añadir una foto.
            onClick = onAddClick,
        )
        // No hay nada más que dibujar.
        return
    }

    // Las fotos y, al final, el cuadro para añadir otra (si aún hay espacio).
    val slots = buildList<String?> {
        // Agrega todas las fotos elegidas.
        addAll(photos)
        // Un null representa el cuadro de añadir, solo si no se llegó al máximo.
        if (photos.size < maxPhotos) add(null)
    }

    // Columna de filas con 12 dp de separación vertical.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Divide los elementos en filas de COLUMNS elementos.
        slots.chunked(COLUMNS).forEachIndexed { rowIndex, rowItems ->
            // Fila con 12 dp de separación horizontal.
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dibuja cada elemento de la fila.
                rowItems.forEachIndexed { colIndex, photo ->
                    // Posición absoluta del elemento en la lista de fotos.
                    val index = rowIndex * COLUMNS + colIndex
                    // Cada celda reparte el ancho por igual y es cuadrada.
                    val itemModifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                    // Un null es el cuadro de añadir; si no, es una foto.
                    if (photo == null) {
                        // Cuadro punteado para añadir otra foto.
                        AddPhotoBox(modifier = itemModifier, onClick = onAddClick)
                    } else {
                        // Foto elegida; la primera se marca como principal.
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
    // Color del borde leído fuera de drawBehind (allí no se puede leer el tema de Compose).
    val borderColor = AppTheme.colors.border
    // Contenedor tocable del cuadro.
    Box(
        modifier = modifier
            // Recorta con esquinas redondeadas (también el efecto de toque).
            .clip(RoundedCornerShape(16.dp))
            // Lo hace tocable con rol de botón para accesibilidad.
            .clickable(role = Role.Button, onClick = onClick)
            // Borde punteado: no existe como modificador, se dibuja.
            .drawBehind {
                // Rectángulo redondeado con trazo discontinuo (14 px de línea, 10 px de espacio).
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                )
            },
        // Centra el contenido.
        contentAlignment = Alignment.Center,
    ) {
        // Ícono y texto apilados y centrados.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Ícono de cámara decorativo.
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = AppTheme.colors.textPrimary)
            // Texto "+ Añadir".
            Text(stringResource(R.string.photo_add), fontSize = 12.sp, color = AppTheme.colors.textPrimary)
        }
    }
}

// Foto elegida: la imagen, la X para quitarla y la etiqueta "Principal" en la primera.
@Composable
private fun PhotoItem(url: String, isMain: Boolean, onRemove: () -> Unit, modifier: Modifier) {
    // Contenedor con esquinas redondeadas que superpone imagen, botón y etiqueta.
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        // Carga la imagen de forma asíncrona con Coil.
        AsyncImage(
            // Ruta local o URL de la foto.
            model = url,
            // Descripción para lectores de pantalla.
            contentDescription = stringResource(R.string.photo_description),
            // Recorta la imagen para llenar el cuadro sin deformarla.
            contentScale = ContentScale.Crop,
            // Fondo gris mientras carga.
            placeholder = ColorPainter(Color.LightGray),
            // Ocupa todo el cuadro.
            modifier = Modifier.fillMaxSize(),
        )
        // X blanca sobre fondo oscuro: se lee sobre cualquier foto.
        Box(
            modifier = Modifier
                // Esquina superior derecha.
                .align(Alignment.TopEnd)
                // Separación del borde.
                .padding(6.dp)
                // Tamaño del botón.
                .size(24.dp)
                // Fondo circular negro semitransparente.
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                // Al tocarlo se quita la foto.
                .clickable(role = Role.Button, onClick = onRemove),
            // Centra el ícono.
            contentAlignment = Alignment.Center,
        ) {
            // Ícono de cerrar con descripción accesible "Quitar foto".
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.photo_remove), tint = Color.White, modifier = Modifier.size(14.dp))
        }
        // Solo la primera foto lleva la etiqueta "Principal".
        if (isMain) {
            // Etiqueta con fondo de aviso.
            Surface(
                modifier = Modifier
                    // Esquina inferior izquierda.
                    .align(Alignment.BottomStart)
                    // Separación del borde.
                    .padding(6.dp),
                // Esquinas ligeramente redondeadas.
                shape = RoundedCornerShape(6.dp),
                // Color de fondo de avisos del tema.
                color = AppTheme.colors.surfaceNotice,
            ) {
                // Texto "Principal".
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

/**
Hoja inferior "Agregar foto" para elegir el origen: cámara o galería.
Es solo visual: las acciones de cada opción las define quien la usa (normalmente [ImageSourcePicker]).
**/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(
    // Acción al cerrar la hoja (tocar fuera o deslizar hacia abajo).
    onDismiss: () -> Unit,
    // Acción de la opción "Cámara".
    onCameraClick: () -> Unit,
    // Acción de la opción "Galería".
    onGalleryClick: () -> Unit,
) {
    // Hoja modal que aparece desde abajo con el fondo del tema.
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppTheme.colors.background) {
        // Columna con el título y las opciones.
        Column(
            modifier = Modifier
                // Margen lateral.
                .padding(horizontal = 24.dp)
                // Margen inferior para separar del borde de la pantalla.
                .padding(bottom = 32.dp),
            // Separación entre elementos.
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Título centrado "Agregar foto".
            Text(
                stringResource(R.string.photo_sheet_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.textPrimary,
            )
            // Opción para tomar una foto con la cámara.
            SheetOption(stringResource(R.string.photo_camera), Icons.Outlined.CameraAlt, onCameraClick)
            // Opción para elegir una foto de la galería.
            SheetOption(stringResource(R.string.photo_gallery), Icons.Outlined.Image, onGalleryClick)
        }
    }
}

// Opción de la hoja: botón ancho con icono y texto.
@Composable
private fun SheetOption(text: String, icon: ImageVector, onClick: () -> Unit) {
    // Superficie tocable de ancho completo con esquinas redondeadas y color de selección.
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = AppTheme.colors.stateSelected) {
        // Fila con ícono y texto.
        Row(
            // Espaciado interno del botón.
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            // Centra verticalmente ícono y texto.
            verticalAlignment = Alignment.CenterVertically,
            // Separación entre ícono y texto.
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Ícono decorativo de la opción.
            Icon(icon, contentDescription = null, tint = AppTheme.colors.textPrimary)
            // Texto de la opción en seminegrita.
            Text(text, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
        }
    }
}

// Vista previa en Android Studio con tres fotos de ejemplo (sin imagen real) y espacio para añadir más.
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PhotoPickerGridPreview() {
    // Aplica el tema de la app.
    PetAdoptionAppTheme {
        // Cuadrícula de ejemplo.
        PhotoPickerGrid(photos = listOf("a", "b", "c"), maxPhotos = 5, onAddClick = {}, onRemove = {})
    }
}
