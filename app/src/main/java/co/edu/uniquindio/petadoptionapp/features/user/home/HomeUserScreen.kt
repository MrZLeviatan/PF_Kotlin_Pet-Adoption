package co.edu.uniquindio.petadoptionapp.features.user.home

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importación de los temas a usar
import co.edu.uniquindio.petadoptionapp.core.theme.BordeMarron
import co.edu.uniquindio.petadoptionapp.core.theme.FondoClaro
import co.edu.uniquindio.petadoptionapp.core.theme.FondoCrema
import co.edu.uniquindio.petadoptionapp.core.theme.ItemSeleccionado
import co.edu.uniquindio.petadoptionapp.core.theme.TextoMarron

@Composable
fun HomeUserScreen(
    // Recibe los espacios o padding de la barra del sistema, por defecto vacíos.
    padding: PaddingValues = PaddingValues()
) {
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
        HomeHeader(userName = "Nicolas", city = "Armenia, Colombia", notifications = 3)

        // Muestra la barra de búsqueda.
        SearchField()

        // Contenedor de lista optimizado con desplazamiento vertical.
        LazyColumn(
            // Toma todo el peso y espacio sobrante disponible en la columna.
            modifier = Modifier.weight(1f),
            // Asigna los márgenes de espacio interno horizontal y vertical de la lista.
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            // Agrega una separación de 16dp entre cada elemento de la lista.
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Espacio reservado para mapear dinámicamente las tarjetas de publicaciones.
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
                // Mueve la campana 3dp a la izquierda dentro del círculo
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

// Componente privado para el cuadro de texto de búsqueda.
@Composable
private fun SearchField() {
    // Estado mutable recordado para guardar la consulta de búsqueda introducida.
    var query by remember { mutableStateOf("") }

    // Campo de texto estilizado con bordes.
    OutlinedTextField(
        // Enlaza el texto actual guardado en la variable query.
        value = query,
        // Actualiza la variable query cada vez que se teclea un carácter.
        onValueChange = { query = it },
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