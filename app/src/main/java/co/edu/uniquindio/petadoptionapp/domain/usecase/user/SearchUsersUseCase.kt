package co.edu.uniquindio.petadoptionapp.domain.usecase.user

import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import co.edu.uniquindio.petadoptionapp.domain.repository.UserRepository
import jakarta.inject.Inject

/**
Caso de uso que busca usuarios por su alias.
Evita consultas con textos muy cortos para cuidar la cuota gratuita de lecturas.
La persona que busca se toma de la sesión, nunca de un parámetro; puede ser un usuario o un moderador.
Solo se buscan usuarios: los moderadores no aparecen en los resultados (viven en otra colección).
Lo usa el ViewModel de la pantalla de búsqueda de personas.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class SearchUsersUseCase (
    // Inyecta el repositorio de usuarios.
    private val userRepository: UserRepository,
    // Inyecta el repositorio de autenticación para leer la sesión (usuario o moderador).
    private val authRepository: AuthRepository
) {
    // Constantes de configuración de la búsqueda.
    companion object {
        // Define el mínimo de caracteres para buscar.
        const val MIN_QUERY_LENGTH = 2
        // Define la cantidad máxima de resultados.
        const val MAX_RESULTS = 10
    }

    // Función principal: busca por alias y devuelve resúmenes públicos.
    suspend operator fun invoke(query: String): Result<List<UserSummary>> {

        // Hace falta una sesión (de usuario o de moderador) para buscar personas.
        val session = authRepository.currentAccount.value
            // Si no hay sesión, falla con NotAuthenticated.
            ?: return Result.failure(AuthException.NotAuthenticated)

        // Limpia el texto; si es muy corto, devuelve lista vacía sin consultar.
        val clean = query.trim().removePrefix("@")
        // Con textos muy cortos no consulta.
        if (clean.length < MIN_QUERY_LENGTH) return Result.success(emptyList())

        // Busca y se quita a sí mismo de los resultados.
        return userRepository.searchByUserName(clean, MAX_RESULTS)
            // Excluye a la persona que busca.
            .map { list -> list.filter { it.id != session.id } }
    }
}
