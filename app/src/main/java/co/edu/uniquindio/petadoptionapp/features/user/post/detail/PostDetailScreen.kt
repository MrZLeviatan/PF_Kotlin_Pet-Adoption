package co.edu.uniquindio.petadoptionapp.features.user.post.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import co.edu.uniquindio.petadoptionapp.core.theme.BordeMarron
import co.edu.uniquindio.petadoptionapp.core.theme.FondoAviso
import co.edu.uniquindio.petadoptionapp.core.theme.FondoChip
import co.edu.uniquindio.petadoptionapp.core.theme.FondoClaro
import co.edu.uniquindio.petadoptionapp.core.theme.FondoCrema
import co.edu.uniquindio.petadoptionapp.core.theme.FondoMarron
import co.edu.uniquindio.petadoptionapp.core.theme.ItemSeleccionado
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron

// Publicación quemada (de mentira).
private val burnedDetail = PostDetailInfo(
    id = "1",
    imageUrls = listOf(
        "https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png"
    ),
    type = "Adopción",
    verified = true,
    title = "Luna busca un hogar lleno de amor",
    location = "Bogotá, Chapinero • 1.2 km",
    publishedAgo = "Publicado hace 3 días",
    animalType = "Perro",
    breed = "Mestiza (Labrador)",
    size = "Mediana (15 kg)",
    vaccines = "Al día",
    descriptionTitle = "Sobre Luna",
    description = "Luna es una perrita mestiza de aproximadamente 2 años. Fue rescatada de la calle hace 6 meses. Es cariñosa, juguetona y se lleva muy bien con niños y otros perros. Está esterilizada, desparasitada y con todas sus vacunas al día. Busca una familia responsable que le dé mucho amor.",
    approxLocation = "Chapinero, Bogotá • Colombia",
    totalComments = 8,
    comments = listOf(
        PostComment("1", "María García", "MG", "hace 2h", "Qué hermosa Luna! Me encantaría conocerla. ¿Tienen horarios de visita?", 3),
        PostComment("2", "Carlos Rodríguez", "CR", "hace 5h", "Ya contacté a la fundación. Luna es increíble, muy sociable y cariñosa. Recomiendo visitarla.", 7),
        PostComment("3", "Ana Martínez", "AM", "hace 1d", "Ojalá encuentre pronto un hogar, es preciosa.", 1)
    )
)

@Composable
fun PostDetailScreen(
    postId: String,                              // Id de la publicación a mostrar (después servirá para buscarla)
    onBack: () -> Unit = {},                     // Volver al feed
    viewModel: PostDetailViewModel = viewModel()
) {
    // Observa el estado: cada vez que cambia, la pantalla se redibuja
    val state by viewModel.uiState.collectAsState()

    // Se ejecuta al abrir la pantalla: entrega la publicación quemada al ViewModel
    LaunchedEffect(postId) {
        viewModel.setPost(burnedDetail)
    }

    // Si la publicación aún no llega, muestra solo el fondo
    val post = state.post
    if (post == null) {
        Box(modifier = Modifier.fillMaxSize().background(FondoCrema))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .statusBarsPadding()      // La foto empieza debajo de la barra de estado
            .imePadding()             // Sube todo cuando aparece el teclado
    ) {
        // 1) Galería fija arriba
        PostImageHeader(imageUrls = post.imageUrls, onBack = onBack)

        // 2) Contenido con scroll en el medio (weight = ocupa el espacio que sobra)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)    // 16dp entre secciones
        ) {
            PostTitleCard(post)
            PostInfoCard(post)
            PostActions()
            PostDescription(post)
            PostLocation(post)
            HorizontalDivider(color = BordeMarron.copy(alpha = 0.5f))
            PostComments(totalComments = state.totalComments, comments = state.comments)
        }

        // 3) Barra de comentarios fija abajo
        CommentInputBar(
            userInitials = state.userInitials,
            text = state.commentText,
            onTextChange = viewModel::onCommentTextChange,
            onSend = viewModel::onSendComment
        )
    }
}

// Galería de fotos con botones, puntos y contador
@Composable
private fun PostImageHeader(imageUrls: List<String>, onBack: () -> Unit) {
    // Recuerda en qué foto está el usuario
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {

        // Permite deslizar entre fotos
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = imageUrls[page],                      // Link de la foto
                contentDescription = "Foto ${page + 1}",
                contentScale = ContentScale.Crop,             // Llena el espacio sin deformar
                placeholder = ColorPainter(Color.LightGray),  // Gris mientras carga
                modifier = Modifier.fillMaxSize()
            )
        }

        // Botón volver (arriba a la izquierda)
        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            description = "Volver",
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
        )

        // Botón compartir (arriba a la derecha, aún sin acción)
        RoundIconButton(
            icon = Icons.Outlined.Share,
            description = "Compartir",
            onClick = { },
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
        )

        // Puntos indicadores (abajo a la izquierda)
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(imageUrls.size) { index ->
                val selected = index == pagerState.currentPage      // ¿Es la foto actual?
                Box(
                    modifier = Modifier
                        .size(if (selected) 8.dp else 6.dp)          // El actual es más grande
                        .background(
                            if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                            CircleShape
                        )
                )
            }
        }

        // Contador "2/5" (abajo a la derecha)
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.55f)
        ) {
            Text(
                text = "${pagerState.currentPage + 1}/${imageUrls.size}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

// Botón redondo claro con un ícono (volver, compartir)
@Composable
private fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = FondoClaro
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = TextoMarron, modifier = Modifier.size(20.dp))
        }
    }
}

// Tarjeta con etiquetas, título, ubicación y fecha
@Composable
private fun PostTitleCard(post: PostDetailInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FondoMarron)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Etiquetas: tipo y verificado
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailTag(text = post.type, icon = Icons.Outlined.Pets)
                if (post.verified) {
                    DetailTag(text = "Verificado", icon = Icons.Filled.CheckCircle, iconColor = Color(0xFF2E7D32))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Título grande
            Text(post.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextoMarron)

            Spacer(modifier = Modifier.height(12.dp))

            IconText(icon = Icons.Outlined.LocationOn, text = post.location)
            Spacer(modifier = Modifier.height(6.dp))
            IconText(icon = Icons.Outlined.CalendarMonth, text = post.publishedAgo)
        }
    }
}

// Tarjeta con los datos de la mascota en 2 columnas
@Composable
private fun PostInfoCard(post: PostDetailInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FondoMarron)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Primera fila: Tipo y Raza
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoCell("Tipo", post.animalType, Icons.Outlined.Pets, Modifier.weight(1f))
                InfoCell("Raza", post.breed, Icons.Outlined.Sell, Modifier.weight(1f))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BordeMarron)

            // Segunda fila: Tamaño y Vacunas
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoCell("Tamaño", post.size, Icons.Outlined.Straighten, Modifier.weight(1f))
                InfoCell("Vacunas", post.vaccines, Icons.Outlined.TaskAlt, Modifier.weight(1f))
            }
        }
    }
}

// Una celda de datos: cuadro con ícono + etiqueta pequeña + valor
@Composable
private fun InfoCell(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Cuadro claro con el ícono
        Box(
            modifier = Modifier.size(36.dp).background(FondoClaro, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextoMarron, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextoMarron)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextoMarron)
        }
    }
}

// Botones "Me Interesa Adoptar" y "Es Importante" (aún sin acción)
@Composable
private fun PostActions() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = ItemSeleccionado, contentColor = TextoMarron)
        ) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Me Interesa Adoptar", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = FondoMarron, contentColor = TextoMarron)
        ) {
            Icon(Icons.Outlined.StarBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Es Importante", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

// Título y texto de la descripción
@Composable
private fun PostDescription(post: PostDetailInfo) {
    Column {
        Text(post.descriptionTitle, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoMarron)
        Spacer(modifier = Modifier.height(8.dp))
        Text(post.description, fontSize = 14.sp, lineHeight = 20.sp, color = TextoMarron)
    }
}

// Ubicación aproximada con espacio reservado para el mapa
@Composable
private fun PostLocation(post: PostDetailInfo) {
    Column {
        Text("Ubicación aproximada", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoMarron)
        Spacer(modifier = Modifier.height(8.dp))

        // Espacio del mapa (aún no se implementa)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(FondoAviso, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Aquí irá el mapa", fontSize = 13.sp, color = TextoMarron)
        }

        Spacer(modifier = Modifier.height(8.dp))
        IconText(icon = Icons.Outlined.LocationOn, text = post.approxLocation, bold = true)
    }
}

// Encabezado "Comentarios (8)" y la lista de comentarios
@Composable
private fun PostComments(totalComments: Int, comments: List<PostComment>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Comentarios ($totalComments)", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoMarron)
            Text("Ver todos", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextoMarron)
        }

        // Un comentario por cada elemento de la lista
        comments.forEach { comment -> CommentItem(comment) }
    }
}

// Un comentario: avatar a la izquierda y contenido a la derecha
@Composable
private fun CommentItem(comment: PostComment) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Avatar(initials = comment.initials, size = 36.dp)

        Column {
            // Nombre y hora
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(comment.author, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextoMarron)
                Text("· ${comment.timeAgo}", fontSize = 12.sp, color = TextoMarron.copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.text, fontSize = 14.sp, color = TextoMarron)
            Spacer(modifier = Modifier.height(6.dp))

            // "Me gusta" y "Responder" (aún sin acción)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = TextoMarron, modifier = Modifier.size(16.dp))
                    Text("${comment.likes}", fontSize = 12.sp, color = TextoMarron)
                }
                Text("Responder", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoMarron)
            }
        }
    }
}

// Barra fija de abajo para escribir un comentario
@Composable
private fun CommentInputBar(
    userInitials: String,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FondoMarron)                   // Fondo marrón de la barra
            .navigationBarsPadding()                   // El marrón llega hasta abajo, sin tapar botones del sistema
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Avatar del usuario
        Avatar(initials = userInitials, size = 36.dp)

        // Campo de texto en forma de pastilla
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un comentario...", fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            // Botón redondo amarillo de enviar, dentro del campo
            trailingIcon = {
                Surface(onClick = onSend, shape = CircleShape, color = ItemSeleccionado, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Enviar", tint = TextoMarron, modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FondoClaro,
                unfocusedContainerColor = FondoClaro,
                focusedBorderColor = Color.Transparent,     // Sin borde, como en el diseño
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextoMarron,
                unfocusedTextColor = TextoMarron
            )
        )
    }
}

// Círculo amarillo con las iniciales de una persona
@Composable
private fun Avatar(initials: String, size: Dp) {
    Box(
        modifier = Modifier.size(size).background(ItemSeleccionado, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoMarron)
    }
}

// Etiqueta pequeña con ícono y texto (Adopción, Verificado)
@Composable
private fun DetailTag(text: String, icon: ImageVector, iconColor: Color = TextoMarron) {
    Surface(shape = RoundedCornerShape(8.dp), color = FondoChip) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextoMarron)
        }
    }
}

// Línea con ícono a la izquierda y texto a la derecha
@Composable
private fun IconText(icon: ImageVector, text: String, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = TextoMarron, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextoMarron,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostDetailScreenPreview() {
    PostDetailScreen(postId = "1")
}