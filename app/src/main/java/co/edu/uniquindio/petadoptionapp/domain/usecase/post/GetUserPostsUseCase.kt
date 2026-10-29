package co.edu.uniquindio.petadoptionapp.domain.usecase.post

import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.PostRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository

/**
Caso de uso que lista las publicaciones que se pueden ver en el perfil de otra persona.
Aplica la misma política de privacidad que el perfil público ([UserSettings.canViewPosts]):
si el perfil es privado devuelve una lista vacía, sin revelar cuántas publicaciones tiene.
Quien consulta se toma de la sesión, nunca de un parámetro.
Un moderador ve todas las que siguen en proceso, aunque el perfil sea privado: verificadas, en revisión
y sin moderador (no las rechazadas, finalizadas ni eliminadas).
 */
class GetUserPostsUseCase(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(userId: String): Result<List<Post>> {

        // 1. Sin sesión abierta no se puede consultar.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // 2. El perfil debe existir y no estar eliminado.
        val user = userRepository.findById(userId)?.takeIf { !it.deleted }
            ?: return Result.failure(UserException.UserNotFound)

        // 3. Privacidad: el dueño siempre ve las suyas; los demás, solo si el perfil lo permite.
        if (session.role == UserRole.MODERATOR) {
            return postRepository.getByAuthor(userId).map { posts ->
                posts.filter { it.status == PostStatus.IN_PROGRESS && it.verification.status != VerificationStatus.REJECTED }
            }
        }
        val isOwner = session.id == userId
        if (!user.settings.canViewPosts(isOwner)) return Result.success(emptyList())

        // 4. Los demás solo ven lo que también saldría en el feed (verificadas y en proceso).
        return postRepository.getByAuthor(userId)
            .map { posts -> if (isOwner) posts else posts.filter { it.showsInPublicFeed() } }
    }
}
