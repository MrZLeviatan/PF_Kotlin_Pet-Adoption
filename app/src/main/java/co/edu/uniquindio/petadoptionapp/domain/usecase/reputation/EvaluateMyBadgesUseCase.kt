package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso que revisa si la persona con sesión cumple los requisitos de alguna insignia que aún no tiene.
Se llama al abrir la app y después de sus propias acciones (publicar, comentar).
Solo consulta las estadísticas de los tipos de requisito que le faltan, para gastar pocas lecturas.
Cada persona evalúa su propio perfil, así solo escribe su propio documento.
 */
class EvaluateMyBadgesUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository,
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository,
    // Inyecta el repositorio de publicaciones para los conteos de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase
) {
    // Define la función asíncrona principal que evalúa y desbloquea las insignias pendientes, devolviendo un resultado con la lista de nuevas insignias obtenidas.
    suspend operator fun invoke(): Result<List<Badge>> {

        // 1. Solo un usuario con sesión tiene insignias.
        // Obtiene la sesión del usuario actual o retorna un fallo si no está autenticado.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. El perfil debe existir y estar activo (de él salen los puntos y las insignias ganadas).
        // Busca el perfil del usuario asegurando que exista y no esté eliminado, retornando un fallo si no se encuentra.
        val user = userRepository.findById(session.id)?.takeIf { !it.deleted }
            ?: return Result.failure(UserException.ProfileNotFound)

        // 3. Carga el catálogo una sola vez por sesión.
        // Comprueba si el catálogo de insignias está vacío y, de ser así, lo actualiza desde el repositorio retornando un fallo si la operación falla.
        if (reputationRepository.badges.value.isEmpty())
            reputationRepository.refreshBadges().getOrElse { return Result.failure(it) }

        // 4. Solo se revisan las insignias que aún no tiene.
        // Filtra del catálogo aquellas insignias que el usuario todavía no ha obtenido.
        val locked = reputationRepository.badges.value.filter { !user.reputation.hasBadge(it.id) }
        // Si no hay insignias bloqueadas pendientes, retorna una lista vacía de forma exitosa.
        if (locked.isEmpty()) return Result.success(emptyList())

        // 5. Calcula cada estadística una sola vez, aunque varias insignias la usen.
        // Inicializa un mapa mutable para almacenar las estadísticas cacheadas y una lista mutable para las insignias recién desbloqueadas.
        val stats = mutableMapOf<BadgeRequirementType, Int>()
        val unlocked = mutableListOf<Badge>()

        // Itera sobre cada insignia bloqueada para evaluar su progreso.
        for (badge in locked) {
            // Obtiene la estadística calculada previamente o la calcula usando el método auxiliar, guardándola en caché.
            val progress = stats[badge.requirementType]
                ?: progressOf(badge.requirementType, session.id, user.reputation.points)
                    ?.also { stats[badge.requirementType] = it }
                ?: continue   // si no se pudo medir, se salta esta insignia

            // 6. Si cumple el requisito, la desbloquea (el repositorio evita duplicados).
            // Comprueba si el progreso actual alcanza o supera el valor requerido por la insignia.
            if (progress >= badge.requirementValue) {
                // Intenta desbloquear la insignia en el repositorio y, si se desbloquea por primera vez, la añade a la lista de nuevas insignias.
                reputationRepository.unlockBadge(session.id, badge.id)
                    .onSuccess { newlyUnlocked -> if (newlyUnlocked) unlocked += badge }
            }
        }

        // Pendiente: NotifyUseCase BADGE_UNLOCKED por cada insignia nueva.
        // Retorna un resultado exitoso con la lista de insignias desbloqueadas en esta evaluación.
        return Result.success(unlocked)
    }

    // Define el cálculo de cada estadística; devuelve null si la consulta falla.
    private suspend fun progressOf(type: BadgeRequirementType, userId: String, points: Int): Int? = when (type) {

        // Creadas: en proceso más finalizadas (las eliminadas no cuentan).
        // Cuenta las publicaciones creadas sumando aquellas en estado en proceso y finalizadas.
        BadgeRequirementType.POSTS_CREATED ->
            postRepository.countByAuthor(userId, PostStatus.IN_PROGRESS) +
                    postRepository.countByAuthor(userId, PostStatus.FINISHED)

        // Verificadas: se cuentan sobre las publicaciones, no sobre el registro de puntos (que es de mejor esfuerzo).
        // Cuenta las publicaciones verificadas sumando aquellas en proceso y finalizadas que han sido verificadas.
        BadgeRequirementType.POSTS_VERIFIED ->
            postRepository.countByAuthor(userId, PostStatus.IN_PROGRESS, VerificationStatus.VERIFIED) +
                    postRepository.countByAuthor(userId, PostStatus.FINISHED, VerificationStatus.VERIFIED)

        // Cuenta las publicaciones finalizadas que han sido resueltas.
        BadgeRequirementType.POSTS_RESOLVED ->
            postRepository.countByAuthor(userId, PostStatus.FINISHED)

        // Comentarios y votos: salen del registro de puntos (los comentarios cuentan hasta el tope diario).
        // Cuenta los comentarios escritos consultando el historial de puntos de creación de comentarios.
        BadgeRequirementType.COMMENTS_WRITTEN ->
            reputationRepository.countByReason(userId, PointReason.COMMENT_CREATED).getOrNull()

        // Cuenta los votos importantes recibidos consultando el historial de puntos correspondientes.
        BadgeRequirementType.VOTES_RECEIVED ->
            reputationRepository.countByReason(userId, PointReason.IMPORTANT_RECEIVED).getOrNull()

        // Retorna el total de puntos acumulados del usuario.
        BadgeRequirementType.TOTAL_POINTS -> points
    }
}