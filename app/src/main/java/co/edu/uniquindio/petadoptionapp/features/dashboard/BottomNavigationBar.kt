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
    // CAMBIAR por los nombres reales de tu paleta (AppTheme.colors.xxx).
    val containerColor = AppTheme.colors.brand
    val selectedColor = AppTheme.colors.stateSelected
    val unselectedColor = AppTheme.colors.stateUnselected
    val createButtonColor = AppTheme.colors.actionPrimary
    val createIconColor = AppTheme.colors.textPrimary

    NavigationBar(containerColor = containerColor) {
        items.forEachIndexed { index, destination ->

            // Botón central "+": un círculo que nunca se marca como seleccionado.
            if (destination == DashboardDestination.ADD) {
                NavigationBarItem(
                    selected = false,
                    onClick = { onItemClick(index) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(createButtonColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(destination.labelRes),
                                tint = createIconColor
                            )
                        }
                    },
                    // Quita el óvalo que Material dibuja por defecto.
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            } else {
                // Items normales.
                val label = stringResource(destination.labelRes)
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemClick(index) },
                    icon = { Icon(imageVector = destination.icon, contentDescription = label) },
                    label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview(name = "Usuario", showBackground = true)
@Composable
private fun BottomNavigationBarUserPreview() {
    PetAdoptionAppTheme {
        BottomNavigationBar(
            items = DashboardDestination.entries.filter { UserRole.USER in it.roles },
            selectedIndex = 0,
            onItemClick = {}
        )
    }
}

@Preview(name = "Moderador", showBackground = true)
@Composable
private fun BottomNavigationBarModeratorPreview() {
    PetAdoptionAppTheme {
        BottomNavigationBar(
            items = DashboardDestination.entries.filter { UserRole.MODERATOR in it.roles },
            selectedIndex = 0,
            onItemClick = {}
        )
    }
}