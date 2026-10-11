package co.edu.uniquindio.petadoptionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.features.registro_usuario.RegistroScreen
import co.edu.uniquindio.petadoptionapp.features.registro_usuario.RegistroViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PetAdoptionAppTheme {
                val registroViewModel: RegistroViewModel = viewModel()

                RegistroScreen(
                    viewModel = registroViewModel,
                    onIniciarSesion = {
                        // La navegación al inicio de sesión
                        // se conectará posteriormente.
                    }
                )
            }
        }
    }
}



/*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.features.splash.SplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PetAdoptionAppTheme {
            val registroViewModel: RegistroViewModel = viewModel()
            RegistroScreen(
                    viewModel = registroViewModel,
                    onIniciarSesion = {
                        // La navegación al inicio de sesión
                        // se conectará posteriormente.
                    }
                )

                SplashScreen()
            }
        }
    }
}

*/

/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PetAdoptionAppTheme {
        Greeting("Android")
    }
}*/