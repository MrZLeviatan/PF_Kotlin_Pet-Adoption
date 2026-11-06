package co.edu.uniquindio.petadoptionapp.features.splash

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.RestoreSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
ViewModel de la pantalla de carga (Splash).
Recupera la sesión guardada en el teléfono mientras se muestra la marca de la aplicación.
No maneja estado de UI ni eventos: solo expone la función suspendida hasSession(), que la
SplashScreen invoca dentro de su LaunchedEffect.
Usa el caso de uso RestoreSessionUseCase para leer la sesión persistida y validarla.
El resultado (true/false) se entrega a AppNavigation mediante el callback onFinished de la
pantalla, y es la navegación principal quien decide si ir al dashboard o al login.
**/
// Marca la clase como ViewModel administrado por Hilt para que pueda inyectarse con hiltViewModel().
@HiltViewModel
// Declara el ViewModel y solicita a Hilt sus dependencias por constructor.
class SplashViewModel @Inject constructor(
    // Caso de uso que restaura la sesión almacenada localmente (devuelve null si no hay sesión válida).
    private val restoreSession: RestoreSessionUseCase,
) : ViewModel() {

    // Devuelve true si hay una sesión válida (se irá al inicio) o false si hay que iniciar sesión;
    // compara el resultado del caso de uso contra null para convertirlo en un booleano.
    suspend fun hasSession(): Boolean = restoreSession() != null
}
