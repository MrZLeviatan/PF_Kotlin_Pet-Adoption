package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.PointReason
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.NotifyUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.RequireUserUseCase

/**
Documenta el caso de uso que revisa si la persona con sesión cumple los requisitos de alguna insignia que aún no tiene.
Se llama al abrir la app y después de sus propias acciones (publicar, comentar).
Solo consulta las estadísticas de los tipos de requisito que le faltan, para gastar pocas lecturas.
Cada persona evalúa su propio perfil, así solo escribe su propio documento.
Al desbloquear una insignia avisa a la propia persona (mejor esfuerzo: un fallo ahí no deshace el desbloqueo).
 */
class EvaluateMyBadgesUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository,
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository,
    // Inyecta el repositorio de publicaciones para los conteos de publicaciones.
    private val postRepository: PostRepository,
    // Inyecta el caso de uso para requerir un usuario autenticado.
    private val requireUser: RequireUserUseCase,
    // Inyecta el caso de uso que crea notificaciones.
    private val notify: NotifyUseCase
) {
    // Define la función asíncrona principal que evalúa y desbloquea las insignias pendientes, devolviendo la lista de nuevas insignias obtenidas.
    suspend operator fun invoke(): Result<List<Badge>> {

        // 1. Solo un usuario con sesión tiene insignias.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // 2. El perfil debe existir y estar activo (de él salen los puntos y las insignias ganadas).
        val user = userRepository.findById(session.id)?.takeIf { !it.deleted }
            ?: return Result.failure(UserException.ProfileNotFound)

        // 3. Carga el catálogo una sola vez por sesión.
        if (reputationRepository.badges.value.isEmpty())
            reputationRepository.refreshBadges().getOrElse { return Result.failure(it) }

        // 4. Solo se revisan las insignias que aún no tiene.
        val locked = reputationRepository.badges.value.filter { !user.reputation.hasBadge(it.id) }
        if (locked.isEmpty()) return Result.success(emptyList())

        // 5. Calcula cada estadística una sola vez, aunque varias insignias la usen.
        val stats = mutableMapOf<BadgeRequirementType, Int>()
        val unlocked = mutableListOf<Badge>()

        for (badge in locked) {
            // Obtiene la estadística cacheada o la calcula y la guarda.
            val progress = stats[badge.requirementType]
                ?: progressOf(badge.requirementType, session.id, user.reputation.points)
                    ?.also { stats[badge.requirementType] = it }
                ?: continue   // si no se pudo medir, se salta esta insignia

            // 6. Si cumple el requisito, la desbloquea (el repositorio evita duplicados).
            if (progress >= badge.requirementValue) {
                reputationRepository.unlockBadge(session.id, badge.id)
                    .onSuccess { newlyUnlocked ->
                        if (newlyUnlocked) {
                            unlocked += badge
                            // El id de la insignia va en `extra` para que la UI muestre su nombre.
                            notify(session.id, NotificationType.BADGE_UNLOCKED, extra = badge.id)
                        }
                    }
            } // <- esta llave faltaba
        }

        // Retorna las insignias desbloqueadas en esta evaluación.
        return Result.success(unlocked)
    }

    // Define el cálculo de cada estadística; devuelve null si la consulta falla.
    private suspend fun progressOf(type: BadgeRequirementType, userId: String, points: Int): Int? = when (type) {

        // Creadas: en proceso más finalizadas (las eliminadas no cuentan).
        BadgeRequirementType.POSTS_CREATED ->
            postRepository.countByAuthor(userId, PostStatus.IN_PROGRESS) +
                    postRepository.countByAuthor(userId, PostStatus.FINISHED)

        // Verificadas: se cuentan sobre las publicaciones, no sobre el registro de puntos (que es de mejor esfuerzo).
        BadgeRequirementType.POSTS_VERIFIED ->
            postRepository.countByAuthor(userId, PostStatus.IN_PROGRESS, VerificationStatus.VERIFIED) +
                    postRepository.countByAuthor(userId, PostStatus.FINISHED, VerificationStatus.VERIFIED)

        // Resueltas: las publicaciones finalizadas.
        BadgeRequirementType.POSTS_RESOLVED ->
            postRepository.countByAuthor(userId, PostStatus.FINISHED)

        // Comentarios y votos: salen del registro de puntos (los comentarios cuentan hasta el tope diario).
        BadgeRequirementType.COMMENTS_WRITTEN ->
            reputationRepository.countByReason(userId, PointReason.COMMENT_CREATED).getOrNull()

        BadgeRequirementType.VOTES_RECEIVED ->
            reputationRepository.countByReason(userId, PointReason.IMPORTANT_RECEIVED).getOrNull()

        // Total de puntos acumulados del usuario.
        BadgeRequirementType.TOTAL_POINTS -> points
    }
}