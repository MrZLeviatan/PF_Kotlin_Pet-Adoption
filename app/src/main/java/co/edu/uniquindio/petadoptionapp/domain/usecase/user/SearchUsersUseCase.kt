package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import jakarta.inject.Inject

/**
Documenta el caso de uso que busca usuarios por su alias.
Evita consultas con textos muy cortos para cuidar la cuota gratuita de lecturas.
La persona que busca se toma de la sesión, nunca de un parámetro; puede ser un usuario o un moderador.
Solo se buscan usuarios: los moderadores no aparecen en los resultados (viven en otra colección).
 */
class SearchUsersUseCase (
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository,
    // Inyecta el repositorio de autenticación para leer la sesión (usuario o moderador).
    private val authRepository: AuthRepository
) {
    companion object {
        // Define el mínimo de caracteres para buscar.
        const val MIN_QUERY_LENGTH = 2
        // Define la cantidad máxima de resultados.
        const val MAX_RESULTS = 10
    }

    suspend operator fun invoke(query: String): Result<List<UserSummary>> {

        // Hace falta una sesión (de usuario o de moderador) para buscar personas.
        val session = authRepository.currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Limpia el texto; si es muy corto, devuelve lista vacía sin consultar.
        val clean = query.trim().removePrefix("@")
        if (clean.length < MIN_QUERY_LENGTH) return Result.success(emptyList())

        // Busca y se quita a sí mismo de los resultados.
        return userRepository.searchByUserName(clean, MAX_RESULTS)
            .map { list -> list.filter { it.id != session.id } }
    }
}