package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointTransaction
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

// Define la clase del caso de uso para obtener el historial de puntos del usuario inyectando el repositorio de reputación y el caso de uso de requerir usuario.
class GetMyPointHistoryUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define un objeto de acompañamiento que establece la cantidad máxima de transacciones por página en 20.
    companion object { const val PAGE_SIZE = 20 }

    // Define la función asíncrona principal que recibe un identificador opcional de transacción para la paginación y devuelve un resultado con la lista de transacciones de puntos.
    suspend operator fun invoke(afterTransactionId: String? = null): Result<List<PointTransaction>> {
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }
        // Solicita al repositorio el historial de transacciones de puntos del usuario utilizando su ID, el tamaño de página y el paginador, retornando el resultado.
        return reputationRepository.getTransactions(session.id, PAGE_SIZE, afterTransactionId)
    }
}