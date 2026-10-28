package co.edu.uniquindio.petadoptionapp.domain.usecase.auth

import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 Dice quién está dentro de la app ahora mismo.

 La pantalla de arranque lo usa para decidir qué mostrar:
 null -> Login, USER -> inicio de usuario, MODERATOR -> panel de moderación.
 */
class ObserveSessionUseCase(

    // Recibe por inyección el repositorio de auth para poder consultar los datos de sesión.
    private val authRepository: AuthRepository
) {
    /**
     El operador `invoke` permite llamar a esta clase directamente como si fuera una función (ej: `observeSessionUseCase()`).

     - Qué emite: Un [StateFlow] que contiene la [Account?] activa (o `null` si no hay sesión).
     Encapsula la regla de negocio para que la UI o el ViewModel solo pidan "observar la sesión" de forma limpia y directa, sin tocar el repositorio completo.
     */
    operator fun invoke(): StateFlow<Account?> = authRepository.currentAccount
}