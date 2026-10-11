package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import co.edu.uniquindio.petadoptionapp.domain.model.enums.UserRole
import co.edu.uniquindio.petadoptionapp.features.user.home.HomeUserScreen

// Pantalla contenedora: junta el contenido con la barra de navegación inferior
@Composable
fun MainScreen(
    // Rol del usuario. Por defecto Usuario
    role: UserRole = UserRole.USUARIO
) {
    // Items de la barra: solo los que puede ver este rol
    val items = DashboardDestination.entries.filter { role in it.roles }

    // Guarda cuál item está seleccionado (0 = el primero, Inicio)
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Scaffold = esqueleto de la pantalla (barra inferior + contenido)
    Scaffold(
        // Aquí va la barra inferior
        bottomBar = {
            BottomNavigationBar(
                items = items,
                selectedIndex = selectedIndex,
                // Al tocar un item, lo marca como seleccionado
                onItemClick = { selectedIndex = it }
            )
        }
    ) { padding ->
        // "padding" = espacio que ocupan la barra inferior y la barra de estado
        // Este es el contenido que se ve encima de la barra
        HomeUserScreen(padding = padding)
    }
}

// Preview para ver la pantalla completa en Android Studio
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}