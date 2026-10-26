package co.edu.uniquindio.petadoptionapp.domain.usecase.notification

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository

/**
Documenta el caso de uso que cuenta las notificaciones sin leer para el ícono de la app.
 */
class CountUnreadNotificationsUseCase(
    // Inyecta el repositorio de notificaciones.
    private val notificationRepository: NotificationRepository,
    // Inyecta el repositorio de autenticación para leer la sesión actual.
    private val authRepository: AuthRepository
) {
    // Define la función asíncrona principal que devuelve un resultado con la cantidad de notificaciones no leídas.
    suspend operator fun invoke(): Result<Int> {
        // Obtiene la cuenta actual de la sesión y retorna un fallo si el usuario no está autenticado.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Solicita al repositorio el conteo de notificaciones no leídas utilizando el ID del usuario y retorna el resultado.
        return notificationRepository.countUnread(session.id)
    }
}