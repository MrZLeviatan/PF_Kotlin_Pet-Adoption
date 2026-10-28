package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

/**
Resultado resumido de una búsqueda de usuarios.
Solo contiene datos que son públicos incluso en perfiles privados.
 */
data class UserSummary(
    val id: String = "",
    val userName: String = "",
    val photoUrl: String? = null
)