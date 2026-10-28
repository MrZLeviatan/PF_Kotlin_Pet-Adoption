package co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption

import co.edu.uniquindio.petadoptionapp.domain.error.AdoptionException
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.repository.ChatRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso con el que el autor acepta o rechaza una solicitud de adopción.
Solo el autor de la publicación responde, y solo si la solicitud sigue pendiente y la publicación en proceso.
Aceptar no finaliza la publicación: la entrega ocurre fuera de la app y el autor la finaliza cuando se concrete.
 */
class RespondAdoptionRequestUseCase(
    // Inyecta el repositorio de chats.
    private val chatRepository: ChatRepository,
    // Inyecta el repositorio de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase
) {
    // Define la función asíncrona principal que recibe el ID de la solicitud y un indicador booleano de aceptación, devolviendo un resultado vacío.
    suspend operator fun invoke(requestId: String, accept: Boolean): Result<Unit> {

        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // La solicitud debe existir y ser de una publicación de esta persona (si no, se trata como inexistente).
        // Busca la solicitud de adopción por su ID y asegura que pertenezca al autor de la publicación en sesión, retornando un fallo si no se encuentra o no le pertenece.
        val request = chatRepository.findAdoptionRequestById(requestId)
            ?.takeIf { it.postAuthorId == session.id }
            ?: return Result.failure(AdoptionException.RequestNotFound)

        // Solo se responde lo pendiente (así no se activa el `require` del modelo).
        // Comprueba que la solicitud aún esté esperando respuesta y retorna un fallo si ya ha sido respondida previamente.
        if (!request.awaitsResponse()) return Result.failure(AdoptionException.AlreadyResponded)

        // La publicación debe seguir en proceso.
        // Busca la publicación asociada a la solicitud y valida que exista y se encuentre en estado de proceso, retornando un fallo en caso contrario.
        val post = postRepository.findById(request.postId)
        if (post == null || post.status != PostStatus.IN_PROGRESS)
            return Result.failure(PostException.PostNotActive)

        // Aplica la decisión y guarda solo si el estado sigue pendiente (la transacción evita pisar un cambio simultáneo).
        // Genera la solicitud actualizada según la decisión (aceptada o rechazada) y solicita al repositorio actualizar su estado de forma atómica asegurando que el estado anterior sea pendiente.
        val updated = if (accept) request.accept() else request.decline()
        return chatRepository.updateRequestStatus(requestId, AdoptionRequestStatus.PENDING, updated)
            .onSuccess {
                // `extra` indica si fue aceptada o rechazada, para que la UI elija el texto.
                notify(
                    request.requesterId, NotificationType.ADOPTION_RESPONSE,
                    session.id, session.userName, request.postId,
                    extra = if (accept) "ACCEPTED" else "DECLINED"
                )
            }
    }
}