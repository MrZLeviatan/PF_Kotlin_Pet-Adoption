package co.edu.uniquindio.petadoptionapp.features.dashboard

// Importación general de materiales y componentes visuales (androidx)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importación de los temas a usar
import co.edu.uniquindio.petadoptionapp.core.theme.FondoMarron
import co.edu.uniquindio.petadoptionapp.core.theme.ItemSeleccionado
import co.edu.uniquindio.petadoptionapp.core.theme.ItemNoSeleccionado
import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole

// Define el componente visual de la barra de navegación inferior.
@Composable
fun BottomNavigationBar(
    // Lista de items a mostrar (Filtrado por tipo de Rol)
    items: List<DashboardDestination>,
    // Posición del item seleccionado (0 = Inicio, 1 = Explorar...)
    selectedIndex: Int,
    // Función que se ejecuta al tocar un item, recibe su posición
    onItemClick: (Int) -> Unit
) {
    // NavigationBar es la barra completa. containerColor es su color de fondo
    NavigationBar(containerColor = FondoMarron) {

        // Recorre los "items" recibidos (ya filtrados por rol),
        items.forEachIndexed { index, destination ->

            // Revisa si este item es el botón central "+"
            if (destination == DashboardDestination.ADD) {

                // Botón central "+"
                NavigationBarItem(
                    // El "+" nunca se marca como seleccionado
                    selected = false,
                    // Avisa que se tocó este item
                    onClick = { onItemClick(index) },
                    icon = {
                        // Box dibuja el círculo amarillo
                        Box(
                            modifier = Modifier
                                // Tamaño del círculo
                                .size(52.dp)
                                // Color amarillo y forma redonda
                                .background(ItemSeleccionado, CircleShape),
                            // Centra el "+" dentro del círculo
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                // El ícono "+"
                                imageVector = destination.icon,
                                // Descripción para accesibilidad
                                contentDescription = "Crear",
                                // Color oscuro para el "+"
                                tint = ItemNoSeleccionado
                            )
                        }
                    },
                    // Quitamos el óvalo de fondo que Material pone por defecto al seleccionar
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )

            } else {

                // Items normales: Inicio, Explorar, Mensajes, Perfil
                NavigationBarItem(
                    // Es true solo si este es el item seleccionado
                    selected = selectedIndex == index,
                    // Avisa que se tocó este item
                    onClick = { onItemClick(index) },
                    icon = {
                        Icon(
                            // Ícono del item
                            imageVector = destination.icon,
                            // Descripción para accesibilidad
                            contentDescription = destination.label
                        )
                    },
                    label = {
                        Text(
                            // Texto bajo el ícono
                            text = destination.label,
                            // Letra en negrita, como en la imagen
                            fontWeight = FontWeight.Bold,
                            // Tamaño del texto
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        // Ícono y texto amarillo si está seleccionado
                        selectedIconColor = ItemSeleccionado,
                        selectedTextColor = ItemSeleccionado,
                        // Ícono y texto oscuro si no está seleccionado
                        unselectedIconColor = ItemNoSeleccionado,
                        unselectedTextColor = ItemNoSeleccionado,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

// Preview del USER: Inicio, Explorar, +, Mensajes, Perfil
@Preview(showBackground = true)
@Composable
fun BottomNavigationBarUserPreview() {
    // Guarda cuál item está seleccionado. Solo para que puedas probar el cambio visual
    var selected by remember { mutableIntStateOf(0) }

    BottomNavigationBar(
        // Solo los items que puede ver el USER
        items = DashboardDestination.entries.filter { UserRole.USER in it.roles },
        // Muestra el item seleccionado
        selectedIndex = selected,
        // Al tocar un item, lo marca como seleccionado
        onItemClick = { selected = it }
    )
}

// Preview del MONITOR: Dashboard, Publicaciones, Mensajes, Perfil
@Preview(showBackground = true)
@Composable
fun BottomNavigationBarMonitorPreview() {
    var selected by remember { mutableIntStateOf(0) }

    BottomNavigationBar(
        // Solo los items que puede ver el MONITOR
        items = DashboardDestination.entries.filter { UserRole.MONITOR in it.roles },
        selectedIndex = selected,
        onItemClick = { selected = it }
    )
}