package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import jakarta.inject.Inject

/**
Documenta el caso de uso que busca usuarios por su alias.
Evita consultas con textos muy cortos para cuidar la cuota gratuita de lecturas.
La persona que busca se toma de la sesión, nunca de un parámetro.
 */
class SearchUsersUseCase (
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository,
    // Inyecta el caso de uso que confirma que hay una sesión de usuario.
    private val requireUser: RequireUserUseCase
) {
    companion object {
        // Define el mínimo de caracteres para buscar.
        const val MIN_QUERY_LENGTH = 2
        // Define la cantidad máxima de resultados.
        const val MAX_RESULTS = 10
    }

    suspend operator fun invoke(query: String): Result<List<UserSummary>> {

        // Solo un usuario con sesión puede buscar personas.
        val session = requireUser().getOrElse { return Result.failure(it) }

        // Limpia el texto; si es muy corto, devuelve lista vacía sin consultar.
        val clean = query.trim().removePrefix("@")
        if (clean.length < MIN_QUERY_LENGTH) return Result.success(emptyList())

        // Busca y se quita a sí mismo de los resultados.
        return userRepository.searchByUserName(clean, MAX_RESULTS)
            .map { list -> list.filter { it.id != session.id } }
    }
}