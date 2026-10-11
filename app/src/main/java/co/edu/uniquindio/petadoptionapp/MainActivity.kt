package co.edu.uniquindio.petadoptionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permite que la app se dibuje detrás de la barra de estado y la de navegación del sistema
        enableEdgeToEdge()
        // Aquí empieza la interfaz de la app
        setContent {
            // Aplica los colores y estilos de tu tema a todo lo de adentro
            PetAdoptionAppTheme {
                // Carga el enrutador con todas las rutas
                AppNavigation()
            }
        }
    }
}