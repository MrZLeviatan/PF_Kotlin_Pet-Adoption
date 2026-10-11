package co.edu.uniquindio.petadoptionapp.features.user.home

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
// Importación de los temas a usar
import co.edu.uniquindio.petadoptionapp.core.theme.BordeMarron
import co.edu.uniquindio.petadoptionapp.core.theme.FondoChip
import co.edu.uniquindio.petadoptionapp.core.theme.FondoClaro
import co.edu.uniquindio.petadoptionapp.core.theme.FondoCrema
import co.edu.uniquindio.petadoptionapp.core.theme.FondoMarron
import co.edu.uniquindio.petadoptionapp.core.theme.ItemSeleccionado
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron

// Publicación quemada (de mentira). Después vendrán de una base de datos
private val burnedPost = FeedPost(
    id = "1",
    imageUrl = "https://res.cloudinary.com/dehltwwbu/image/upload/v1791690961/hero-photo_i8ti3d.png",
    type = "Adopción",
    distanceKm = 2.4,
    title = "Luna busca un hogar lleno de amor",
    species = "Perro",
    breed = "Mestiza",
    size = "Mediana",
    vaccinesInfo = "Vacunas al día",
    interestedCount = 12
)

@Composable
fun HomeUserScreen(
    // Recibe los espacios o padding de la barra del sistema, por defecto vacíos.
    padding: PaddingValues = PaddingValues(),

    // Se ejecuta al tocar una tarjeta y entrega el id (se usará para abrir el detalle).
    onPostClick: (String) -> Unit = {},
    // El ViewModel que guarda el estado de la pantalla. viewModel() lo crea y lo recuerda.
    viewModel: HomeUserViewModel = viewModel()
) {
    // Observa el estado del ViewModel. Cada vez que cambia, la pantalla se redibuja.
    val state by viewModel.uiState.collectAsState()

    // Se ejecuta una sola vez al abrir la pantalla y entrega la publicación quemada al ViewModel.
    LaunchedEffect(Unit) {
        viewModel.setPosts(listOf(burnedPost))
    }

    // Organiza los elementos verticalmente: cabecera, buscador y lista de publicaciones.
    Column(
        modifier = Modifier
            // Hace que la columna ocupe todo el tamaño de la pantalla.
            .fillMaxSize()
            // Aplica el color de fondo crema de la app.
            .background(FondoCrema)
            // Ajusta el margen interno para no solaparse con las barras del sistema.
            .padding(padding)
    ) {
        // Muestra la cabecera con el saludo, ubicación y notificaciones.
        HomeHeader(
            userName = state.userName,
            city = state.city,
            notifications = state.notifications
        )

        // Muestra la barra de búsqueda. Recibe el texto actual y avisa al ViewModel cuando cambia.
        SearchField(
            query = state.query,
            onQueryChange = viewModel::onQueryChange
        )

        // Si el buscador no encontró nada, muestra un mensaje. Si no, muestra la lista.
        if (state.posts.isEmpty() && state.query.isNotBlank()) {
            // Caja que ocupa el espacio sobrante y centra el mensaje.
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Mensaje de búsqueda sin resultados.
                Text("No se encontraron publicaciones", color = TextoMarron, fontSize = 14.sp)
            }
        } else {
            // Contenedor de lista optimizado con desplazamiento vertical.
            LazyColumn(
                // Toma todo el peso y espacio sobrante disponible en la columna.
                modifier = Modifier.weight(1f),
                // Asigna los márgenes de espacio interno horizontal y vertical de la lista.
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                // Agrega una separación de 16dp entre cada elemento de la lista.
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Por cada publicación del estado, dibuja una tarjeta.
                items(state.posts, key = { it.id }) { post ->
                    FeedPostCard(post = post, onClick = { onPostClick(post.id) })
                }
            }
        }
    }
}

// Componente privado para la cabecera del inicio con saludo e icono con contador.
@Composable
private fun HomeHeader(
    // Nombre del usuario actual.
    userName: String,
    // Nombre de la ciudad o localización actual.
    city: String,
    // Cantidad de notificaciones pendientes.
    notifications: Int
) {
    // Organiza en una fila el saludo (izquierda) y la campana (derecha).
    Row(
        modifier = Modifier
            // Ajusta el ancho al máximo de la pantalla.
            .fillMaxWidth()
            // Aplica márgenes internos a los lados y arriba/abajo.
            .padding(horizontal = 16.dp, vertical = 20.dp),
        // Distribuye los elementos con espacio máximo entre ellos (extremos).
        horizontalArrangement = Arrangement.SpaceBetween,
        // Alinea los elementos verticalmente al centro.
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna vertical para agrupar el texto del saludo y la ubicación.
        Column {
            // Muestra el nombre de usuario saludándolo.
            Text(
                // Formatea el texto insertando el nombre recibido.
                text = "Bienvenido, $userName",
                // Define el tamaño de letra en 24sp.
                fontSize = 24.sp,
                // Aplica un grosor extra negrita a la letra.
                fontWeight = FontWeight.ExtraBold,
                // Asigna el color de texto marrón definido en el tema.
                color = TextoMarron
            )
            // Fila para situar el texto de la ciudad junto con su icono.
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Muestra el texto de la ubicación actual.
                Text(text = city, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextoMarron)
                // Muestra el icono de pin de ubicación.
                Icon(
                    // Asigna la forma de vector del pin de localización.
                    imageVector = Icons.Filled.LocationOn,
                    // Omite la descripción de accesibilidad al ser decorativo junto al texto.
                    contentDescription = null,
                    // Establece el color rojo para resaltar el pin.
                    tint = Color.Red,
                    // Define el tamaño del icono en 16dp.
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Contenedor cuadrado redondeado para encapsular el icono de notificaciones.
        Box(
            modifier = Modifier
                // Fija el tamaño del botón en 44dp de ancho y alto.
                .size(44.dp)
                // Pinta el fondo claro en forma circular.
                .background(FondoClaro, CircleShape)
                // Agrega un borde circular marrón de 1dp.
                .border(1.dp, BordeMarron, CircleShape),
            // Centra todo el contenido interno.
            contentAlignment = Alignment.Center
        ) {
            // Componente que permite superponer una insignia contadora sobre el icono.
            BadgedBox(
                // Mueve la campana un poco a la izquierda dentro del círculo.
                modifier = Modifier.offset(x = (-1).dp),
                // Define la insignia flotante.
                badge = {
                    // Contenedor de la insignia de notificación con color personalizado.
                    Badge(containerColor = ItemSeleccionado, contentColor = TextoMarron) {
                        // Convierte el entero de notificaciones a texto para mostrarlo.
                        Text(text = "$notifications")
                    }
                }
            ) {
                // Icono de la campana de notificaciones.
                Icon(
                    // Utiliza la versión lineal de campana.
                    imageVector = Icons.Outlined.Notifications,
                    // Descripción textual para motores de accesibilidad.
                    contentDescription = "Notificaciones",
                    // Asigna el color de icono en tono marrón.
                    tint = TextoMarron
                )
            }
        }
    }
}

// Componente privado para el cuadro de texto de búsqueda, recibe y avisa cuando cambia (el estado vive en el ViewModel).
@Composable
private fun SearchField(
    // Texto actual del buscador, viene del ViewModel.
    query: String,
    // Función que se ejecuta cada vez que el usuario escribe.
    onQueryChange: (String) -> Unit
) {
    // Campo de texto estilizado con bordes.
    OutlinedTextField(
        // Muestra el texto que llega desde el ViewModel.
        value = query,
        // Avisa al ViewModel cada vez que se teclea un carácter.
        onValueChange = onQueryChange,
        modifier = Modifier
            // Ajusta el ancho al máximo del contenedor.
            .fillMaxWidth()
            // Agrega márgenes a los lados del buscador.
            .padding(horizontal = 16.dp),
        // Muestra un texto indicativo previo cuando no se ha escrito nada.
        placeholder = { Text("Buscar mascotas, fundaciones...") },
        // Coloca el icono de la lupa al inicio a la izquierda del campo de texto.
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar") },
        // Restringe el campo de texto a una sola línea horizontal.
        singleLine = true,
        // Asigna bordes redondeados con radio de 24dp.
        shape = RoundedCornerShape(24.dp),
        // Configuración de colores de fondo y bordes para estados enfocados e inactivos.
        colors = OutlinedTextFieldDefaults.colors(
            // Fondo cuando el usuario tiene el foco en el campo.
            focusedContainerColor = FondoClaro,
            // Fondo cuando el campo está inactivo.
            unfocusedContainerColor = FondoClaro,
            // Color del borde al enfocar.
            focusedBorderColor = BordeMarron,
            // Color del borde estando inactivo.
            unfocusedBorderColor = BordeMarron
        )
    )
}

// Tarjeta de una publicación del feed.
@Composable
private fun FeedPostCard(
    // Datos de la publicación a dibujar.
    post: FeedPost,
    // Acción al tocar la tarjeta.
    onClick: () -> Unit
) {
    // Contenedor con esquinas redondeadas; toda la tarjeta es tocable.
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        // Fondo marrón de la tarjeta.
        colors = CardDefaults.cardColors(containerColor = FondoMarron),
        // Borde delgado alrededor.
        border = BorderStroke(1.dp, BordeMarron)
    ) {
        // Descarga y muestra la foto desde el link de Cloudinary.
        AsyncImage(
            // Link de la imagen.
            model = post.imageUrl,
            // Descripción para accesibilidad.
            contentDescription = post.title,
            // Recorta la foto para llenar el espacio sin deformarla.
            contentScale = ContentScale.Crop,
            // Cuadro gris mientras carga la imagen.
            placeholder = ColorPainter(Color.LightGray),
            // Ocupa todo el ancho de la tarjeta con 200dp de alto.
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        // Apila el contenido de texto debajo de la foto.
        Column(modifier = Modifier.padding(12.dp)) {

            // Fila con las dos etiquetas: tipo y distancia.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Etiqueta del tipo de publicación (ej: Adopción).
                FeedTag(text = post.type, icon = Icons.Outlined.Pets)
                // Etiqueta de la distancia con el pin en rojo.
                FeedTag(text = "${post.distanceKm} km", icon = Icons.Filled.LocationOn, iconColor = Color.Red)
            }

            // Espacio vacío de 8dp.
            Spacer(modifier = Modifier.height(8.dp))

            // Título de la publicación.
            Text(post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoMarron)

            // Espacio vacío de 6dp.
            Spacer(modifier = Modifier.height(6.dp))

            // Tres líneas de información, cada una con su ícono.
            FeedInfoRow(Icons.Outlined.Pets, "${post.species} • ${post.breed} • ${post.size}")
            FeedInfoRow(Icons.Outlined.VerifiedUser, post.vaccinesInfo)
            FeedInfoRow(Icons.Outlined.FavoriteBorder, "${post.interestedCount} personas interesadas", bold = true)

            // Espacio vacío de 12dp.
            Spacer(modifier = Modifier.height(12.dp))

            // Botón amarillo "Es Importante" (aún sin acción).
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ItemSeleccionado, contentColor = TextoMarron)
            ) {
                // Estrella vacía a la izquierda del texto.
                Icon(Icons.Outlined.StarBorder, contentDescription = null, modifier = Modifier.size(20.dp))
                // Espacio entre la estrella y el texto.
                Spacer(modifier = Modifier.width(8.dp))
                // Texto del botón.
                Text("Es Importante", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Etiqueta pequeña con ícono y texto (ej: "Adopción").
@Composable
private fun FeedTag(
    // Texto de la etiqueta.
    text: String,
    // Ícono a la izquierda del texto.
    icon: ImageVector,
    // Color del ícono; si no se indica, usa el marrón.
    iconColor: Color = TextoMarron
) {
    // Fondo con forma de pastilla.
    Surface(shape = RoundedCornerShape(8.dp), color = FondoChip) {
        // Ícono y texto en una fila, centrados verticalmente.
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

// Una línea de información con ícono a la izquierda y texto a la derecha.
@Composable
private fun FeedInfoRow(
    // Ícono de la línea.
    icon: ImageVector,
    // Texto de la línea.
    text: String,
    // Si es true, el texto va en negrita.
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextoMarron, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextoMarron,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Vista previa de la pantalla en Android Studio.
@Preview(showBackground = true)
@Composable
fun HomeUserScreenPreview() {
    HomeUserScreen()
}