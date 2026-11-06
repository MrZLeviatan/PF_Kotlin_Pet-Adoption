package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole

/**
Barra de navegación inferior del dashboard.
Es un composable sin estado: recibe la lista de opciones ya filtradas por rol, la posición
seleccionada y un callback onItemClick. No llama casos de uso ni navega por sí misma;
MainScreen recibe el índice tocado y decide si navegar a la ruta interna o abrir crear publicación.
El botón "+" (DashboardDestination.ADD) se dibuja como un círculo destacado y nunca queda seleccionado.
**/
// Barra de navegación inferior. Solo dibuja: recibe los items ya filtrados por rol.
@Composable
fun BottomNavigationBar(
    // Items a mostrar (ya filtrados por rol).
    items: List<DashboardDestination>,
    // Posición del item seleccionado; -1 si ninguno.
    selectedIndex: Int,
    // Se ejecuta al tocar un item, con su posición.
    onItemClick: (Int) -> Unit
) {
    // Colores de la barra tomados de la paleta del tema (AppTheme.colors).
    // Color de fondo de la barra.
    val containerColor = AppTheme.colors.brand
    // Color del ícono y texto del item seleccionado.
    val selectedColor = AppTheme.colors.stateSelected
    // Color del ícono y texto de los items no seleccionados.
    val unselectedColor = AppTheme.colors.stateUnselected
    // Color de fondo del círculo del botón "+".
    val createButtonColor = AppTheme.colors.actionPrimary
    // Color del ícono "+".
    val createIconColor = AppTheme.colors.textPrimary

    // Contenedor Material 3 de la barra inferior con el color de fondo de la marca.
    NavigationBar(containerColor = containerColor) {
        // Recorre las opciones con su posición para crear un item por cada una.
        items.forEachIndexed { index, destination ->

            // Botón central "+": un círculo que nunca se marca como seleccionado.
            if (destination == DashboardDestination.ADD) {
                // Item de la barra para el botón de crear.
                NavigationBarItem(
                    // Nunca aparece como seleccionado.
                    selected = false,
                    // Notifica el toque con la posición del item.
                    onClick = { onItemClick(index) },
                    // Ícono personalizado: un círculo con el "+" dentro.
                    icon = {
                        // Caja circular que sirve de fondo del botón.
                        Box(
                            modifier = Modifier
                                // Tamaño fijo de 52 dp.
                                .size(52.dp)
                                // Fondo circular con el color de acción principal.
                                .background(createButtonColor, CircleShape),
                            // Centra el ícono dentro del círculo.
                            contentAlignment = Alignment.Center
                        ) {
                            // Ícono "+" de la opción.
                            Icon(
                                // Vector del ícono definido en el enum.
                                imageVector = destination.icon,
                                // Descripción accesible para lectores de pantalla.
                                contentDescription = stringResource(destination.labelRes),
                                // Color del ícono.
                                tint = createIconColor
                            )
                        }
                    },
                    // Quita el óvalo que Material dibuja por defecto.
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            } else {
                // Items normales.
                // Texto de la opción resuelto desde strings.xml.
                val label = stringResource(destination.labelRes)
                // Item estándar con ícono y texto.
                NavigationBarItem(
                    // Se marca como seleccionado si coincide con la posición actual.
                    selected = selectedIndex == index,
                    // Notifica el toque con la posición del item.
                    onClick = { onItemClick(index) },
                    // Ícono de la opción, con el texto como descripción accesible.
                    icon = { Icon(imageVector = destination.icon, contentDescription = label) },
                    // Etiqueta bajo el ícono con estilo labelMedium.
                    label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
                    // Colores según el estado seleccionado / no seleccionado.
                    colors = NavigationBarItemDefaults.colors(
                        // Ícono seleccionado.
                        selectedIconColor = selectedColor,
                        // Texto seleccionado.
                        selectedTextColor = selectedColor,
                        // Ícono no seleccionado.
                        unselectedIconColor = unselectedColor,
                        // Texto no seleccionado.
                        unselectedTextColor = unselectedColor,
                        // Sin óvalo indicador de Material.
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

// Vista previa de la barra con las opciones del rol usuario.
@Preview(name = "Usuario", showBackground = true)
@Composable
private fun BottomNavigationBarUserPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja la barra con datos de ejemplo.
        BottomNavigationBar(
            // Filtra las opciones visibles para el usuario.
            items = DashboardDestination.entries.filter { UserRole.USER in it.roles },
            // Marca el primer item como seleccionado.
            selectedIndex = 0,
            // Callback vacío: en la vista previa no se navega.
            onItemClick = {}
        )
    }
}

// Vista previa de la barra con las opciones del rol moderador.
@Preview(name = "Moderador", showBackground = true)
@Composable
private fun BottomNavigationBarModeratorPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja la barra con datos de ejemplo.
        BottomNavigationBar(
            // Filtra las opciones visibles para el moderador.
            items = DashboardDestination.entries.filter { UserRole.MODERATOR in it.roles },
            // Marca el primer item como seleccionado.
            selectedIndex = 0,
            // Callback vacío: en la vista previa no se navega.
            onItemClick = {}
        )
    }
}
