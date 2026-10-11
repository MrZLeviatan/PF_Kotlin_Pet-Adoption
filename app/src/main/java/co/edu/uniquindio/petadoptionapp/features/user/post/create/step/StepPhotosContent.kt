package co.edu.uniquindio.petadoptionapp.features.user.post.create.step

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import co.edu.uniquindio.petadoptionapp.core.component.InfoTip
import co.edu.uniquindio.petadoptionapp.core.component.FormCard
import co.edu.uniquindio.petadoptionapp.core.component.requiredText
import co.edu.uniquindio.petadoptionapp.core.theme.*

import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostUiState
import co.edu.uniquindio.petadoptionapp.features.user.post.create.CreatePostViewModel
import co.edu.uniquindio.petadoptionapp.features.user.post.create.MAX_PHOTOS


// Indica que se están utilizando APIs experimentales de Material 3 (como ModalBottomSheet).
@OptIn(ExperimentalMaterial3Api::class)
// Componente visual correspondiente al tercer paso del formulario (Gestión de fotografías de la mascota).
@Composable
fun StepPhotosContent(state: CreatePostUiState, viewModel: CreatePostViewModel) {
    // Estado mutable para controlar si se muestra u oculta el menú inferior desplegable ("Agregar foto").
    var showSheet by remember { mutableStateOf(false) }

    // Tarjeta contenedora blanca con título centrado para la sección de fotografías.
    FormCard(title = "Fotografías de la Mascota") {
        // Texto indicador que especifica el requisito obligatorio de subir al menos 1 foto.
        Text(requiredText("Mínimo 1 foto obligatoria"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextoMarron)
        // Inserta un espacio vertical de 12dp.
        Spacer(modifier = Modifier.height(12.dp))

        // Evalúa si la lista de fotos está vacía.
        if (state.photos.isEmpty()) {
            // Si no hay fotos, muestra un cuadro grande de ancho completo para añadir la primera.
            AddPhotoBox(modifier = Modifier.fillMaxWidth().aspectRatio(1f), onClick = { showSheet = true })
        } else {
            // Si ya hay fotos, construye una cuadrícula de 2 columnas donde el último espacio disponible es "+ Añadir".
            val slots = buildList<String?> {
                addAll(state.photos)
                // Si aún no se alcanza el límite máximo, añade un elemento nulo para representar el botón de añadir.
                if (state.photos.size < MAX_PHOTOS) add(null)
            }

            // Organiza las filas de la cuadrícula con una separación vertical de 12dp.
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Divide la lista en bloques de 2 elementos (chunked) para formar filas de la cuadrícula.
                slots.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEachIndexed { colIndex, photo ->
                            // Calcula el índice real de la foto en la lista completa.
                            val index = rowIndex * 2 + colIndex
                            // Asigna un modificador que reparte el ancho equitativamente y mantiene proporción cuadrada.
                            val itemModifier = Modifier.weight(1f).aspectRatio(1f)

                            // Si el elemento es nulo, renderiza el cuadro para añadir más fotos.
                            if (photo == null) {
                                AddPhotoBox(modifier = itemModifier, onClick = { showSheet = true })
                            } else {
                                // Si hay una foto, renderiza el componente visual de la imagen con su botón de eliminar.
                                PhotoItem(
                                    url = photo,
                                    // Marca como principal únicamente a la primera foto de la lista (índice 0).
                                    isMain = index == 0,
                                    onRemove = { viewModel.removePhoto(index) },
                                    modifier = itemModifier
                                )
                            }
                        }
                        // Si la fila tiene un solo elemento, añade un espacio vacío flexible para mantener la alineación.
                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Inserta un espacio vertical de 16dp fuera de la tarjeta.
    Spacer(modifier = Modifier.height(16.dp))
    // Muestra una caja de aviso informativo con sugerencias para tomar una buena foto.
    InfoTip(
        icon = Icons.Outlined.Image,
        text = "Usa una foto nítida, bien iluminada y enfocada para que la mascota se vea con claridad."
    )

    // Menú inferior flotante (ModalBottomSheet) que se despliega al presionar añadir foto.
    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, containerColor = FondoClaro) {
            // Columna interna que organiza las opciones del menú inferior.
            Column(
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título principal del menú inferior centrado.
                Text("Agregar foto", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoMarron)

                // Opción para capturar desde la cámara (simula la adición de foto aleatoria y cierra el menú).
                SheetOption("Cámara", Icons.Outlined.CameraAlt) { viewModel.addRandomPhoto(); showSheet = false }
                // Opción para seleccionar desde la galería (simula la adición de foto aleatoria y cierra el menú).
                SheetOption("Galería", Icons.Outlined.Image) { viewModel.addRandomPhoto(); showSheet = false }
            }
        }
    }
}

// Componente privado para el cuadro vacío con borde punteado y texto "+ Añadir".
@Composable
private fun AddPhotoBox(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            // Recorta las esquinas con un radio redondeado de 16dp.
            .clip(RoundedCornerShape(16.dp))
            // Hace que el cuadro sea interactivo al tacto para abrir el menú de selección.
            .clickable(onClick = onClick)
            // Dibuja un borde punteado personalizado utilizando operaciones de dibujo de bajo nivel.
            .drawBehind {
                drawRoundRect(
                    color = BordeMarron,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                )
            },
        // Centra el contenido (icono y texto) dentro del cuadro.
        contentAlignment = Alignment.Center
    ) {
        // Columna para organizar verticalmente el icono de la cámara y el texto.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = TextoMarron)
            Text("+ Añadir", fontSize = 12.sp, color = TextoMarron)
        }
    }
}

// Componente privado que renderiza una foto cargada, su botón de eliminar y la etiqueta "Principal".
@Composable
private fun PhotoItem(url: String, isMain: Boolean, onRemove: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        // Carga la imagen de forma asíncrona desde internet utilizando la librería Coil.
        AsyncImage(
            model = url,
            contentDescription = "Foto de la mascota",
            // Ajusta la escala de la imagen rellenando y recortando el espacio disponible.
            contentScale = ContentScale.Crop,
            // Muestra un color gris claro temporal mientras se descarga la imagen.
            placeholder = ColorPainter(Color.LightGray),
            modifier = Modifier.fillMaxSize()
        )
        // Botón redondo con icono "X" ubicado en la esquina superior derecha para eliminar la foto.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Quitar foto", tint = Color.White, modifier = Modifier.size(14.dp))
        }
        // Muestra condicionalmente la etiqueta flotante "Principal" solo si es la primera foto.
        if (isMain) {
            Surface(modifier = Modifier.align(Alignment.BottomStart).padding(6.dp), shape = RoundedCornerShape(6.dp), color = FondoAviso) {
                Text("Principal", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = TextoMarron)
            }
        }
    }
}

// Componente privado reutilizable para las opciones interactivas del menú inferior ("Agregar foto").
@Composable
private fun SheetOption(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = ItemSeleccionado) {
        // Fila horizontal para alinear el icono y el texto de la opción.
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = TextoMarron)
            Text(text, fontWeight = FontWeight.SemiBold, color = TextoMarron)
        }
    }
}