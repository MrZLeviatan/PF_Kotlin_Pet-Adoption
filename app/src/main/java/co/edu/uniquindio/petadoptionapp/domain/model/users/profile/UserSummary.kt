package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

/**
Modelo de dominio: resultado resumido de una búsqueda de usuarios.
Solo contiene datos que son públicos incluso en perfiles privados.
Lo devuelve SearchUsersUseCase (vía UserRepository) a la pantalla de búsqueda.
**/
// Declara el resumen de usuario como clase de datos inmutable.
data class UserSummary(
    // Id de la cuenta.
    val id: String = "",
    // Alias público.
    val userName: String = "",
    // Foto de perfil; nula si no tiene.
    val photoUrl: String? = null
)
