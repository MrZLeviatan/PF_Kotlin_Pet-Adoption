package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.users.Moderator
import co.edu.uniquindio.petadoptionapp.domain.model.users.User

interface UserRepository {
    /** Busca un perfil de usuario por id; null si no existe. */
    suspend fun findById(id: String): User?

    /** Busca un moderador por id; null si no existe. */
    suspend fun findModeratorById(id: String): Moderator?

    /** Guarda el perfil nuevo. */
    suspend fun save(user: User): Result<Unit>

    /** ¿Ya hay alguien con este alias? */
    suspend fun existsByUserName(userName: String): Boolean

    /** ¿Ya hay alguien con este teléfono? */
    suspend fun existsByPhone(phone: String): Boolean
}