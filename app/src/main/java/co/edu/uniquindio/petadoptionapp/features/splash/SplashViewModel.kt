package co.edu.uniquindio.petadoptionapp.features.splash

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.RestoreSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
Recupera la sesión guardada en el teléfono mientras se muestra la marca.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val restoreSession: RestoreSessionUseCase,
) : ViewModel() {

    // true si hay una sesión válida (va al inicio); false si hay que iniciar sesión.
    suspend fun hasSession(): Boolean = restoreSession() != null
}
