package co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption

import co.edu.uniquindio.petadoptionapp.domain.error.AdoptionException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Caso de uso con el que el solicitante cancela su solicitud de adopción mientras siga pendiente.
Al cancelarla, el botón "Me interesa adoptar" vuelve a estar disponible para esa publicación.
Lo usa el ViewModel del detalle de publicación / chat de adopción.
**/
// Define la clase del caso de uso para que el solicitante cancele su solicitud de adopción inyectando el repositorio de chats y el caso de uso de requerir usuario.
class CancelAdoptionRequestUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la solicitud y devuelve un resultado vacío.
    suspend operator fun invoke(requestId: String): Result<Unit> {
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // Debe existir y ser de quien la cancela.
        // Busca la solicitud de adopción por su ID y asegura que pertenezca al usuario solicitante en sesión, retornando un fallo si no se encuentra o no le pertenece.
        val request = chatRepository.findAdoptionRequestById(requestId)
            // Solo la conserva si quien cancela es el solicitante.
            ?.takeIf { it.requesterId == session.id }
            // Si no existe o no es suya, falla con RequestNotFound.
            ?: return Result.failure(AdoptionException.RequestNotFound)

        // Comprueba que la solicitud aún esté pendiente de respuesta y retorna un fallo si ya ha sido respondida previamente.
        if (!request.awaitsResponse()) return Result.failure(AdoptionException.AlreadyResponded)

        // Solicita al repositorio actualizar el estado de la solicitud de forma atómica asegurando que el estado anterior sea pendiente y aplicando la cancelación, y retorna el resultado.
        return chatRepository.updateRequestStatus(requestId, AdoptionRequestStatus.PENDING, request.cancel())
    }
}