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
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase
) : ViewModel() {

    // null = no hay sesión (por ejemplo, justo después de cerrar sesión).
    val role: StateFlow<UserRole?> = observeSession()
        .map { it?.role }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), observeSession().value?.role)
}