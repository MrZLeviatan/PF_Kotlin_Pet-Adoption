package co.edu.uniquindio.petadoptionapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
Única Activity de la aplicación (arquitectura single-activity). Es el punto de entrada visual:
Android la lanza al abrir la app y ella monta toda la interfaz de Jetpack Compose.
Aplica el tema de la app (core/theme) y delega el resto en AppNavigation, que decide qué
pantalla mostrar (inicio de sesión, inicio, perfil, etc.). Gracias a @AndroidEntryPoint,
Hilt puede inyectar dependencias en ella y en los ViewModels de las pantallas que aloja.
Se usa AppCompatActivity (y no ComponentActivity) para que el idioma elegido en Configuración
se aplique en todas las versiones de Android, incluso las anteriores a Android 13.
**/
// Marca la Activity como punto de entrada de Hilt para que pueda recibir dependencias inyectadas.
@AndroidEntryPoint
// Declara la Activity principal heredando de AppCompatActivity para soportar el cambio de idioma por app.
class MainActivity : AppCompatActivity() {
    // Se ejecuta cuando Android crea la Activity; aquí se configura la ventana y se monta la interfaz.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama a la implementación base para que Android restaure el estado y complete la creación.
        super.onCreate(savedInstanceState)
        // Permite que la app se dibuje detrás de la barra de estado y la de navegación del sistema.
        enableEdgeToEdge()
        // Define el contenido de la Activity con Compose: aquí empieza la interfaz de la app.
        setContent {
            // Aplica los colores, tipografías y formas del tema de la app a todo lo de adentro.
            PetAdoptionAppTheme {
                // Carga el enrutador principal con todas las rutas y decide la pantalla inicial.
                AppNavigation()
            }
        }
    }
}