package co.edu.uniquindio.petadoptionapp.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
Expone el rol de quien tiene la sesión abierta para que el dashboard arme su barra y sus pantallas.
El rol se lee de la sesión, nunca de un parámetro de navegación.
Estado de UI: un único StateFlow<UserRole?> (role); no recibe eventos de la pantalla.
Usa el caso de uso ObserveSessionUseCase, que emite la sesión actual cada vez que cambia.
MainScreen lo consume: si el rol es null no dibuja nada (AppNavigation ya lleva al login) y, si
cambia, reconstruye la navegación interna desde la pantalla de inicio de ese rol.
**/
// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel y pide a Hilt el caso de uso por constructor.
class DashboardViewModel @Inject constructor(
    // Caso de uso que expone la sesión actual como un flujo observable.
    observeSession: ObserveSessionUseCase
) : ViewModel() {

    // Rol actual de la sesión; null = no hay sesión (por ejemplo, justo después de cerrar sesión).
    val role: StateFlow<UserRole?> = observeSession()
        // Transforma cada sesión emitida en solo su rol (o null si no hay sesión).
        .map { it?.role }
        // Convierte el flujo en StateFlow: se mantiene activo 5 s tras la última suscripción y arranca con el rol actual.
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), observeSession().value?.role)
}
