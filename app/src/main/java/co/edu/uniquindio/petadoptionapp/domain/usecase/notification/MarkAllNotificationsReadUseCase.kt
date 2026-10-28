package co.edu.uniquindio.petadoptionapp.domain.usecase.notification

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.NotificationRepository

/**
Documenta el caso de uso que marca como leídas todas las notificaciones de la persona con sesión.
 */
class MarkAllNotificationsReadUseCase(
    // Inyecta el repositorio de notificaciones.
    private val notificationRepository: NotificationRepository,
    // Inyecta el repositorio de autenticación.
    private val authRepository: AuthRepository
) {
    // Define la función asíncrona principal que devuelve un resultado vacío.
    suspend operator fun invoke(): Result<Unit> {
        // Obtiene la cuenta actual de la sesión y retorna un fallo si el usuario no está autenticado.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Marca todas las notificaciones del usuario como leídas en el repositorio utilizando su ID y retorna el resultado.
        return notificationRepository.markAllAsRead(session.id)
    }
}