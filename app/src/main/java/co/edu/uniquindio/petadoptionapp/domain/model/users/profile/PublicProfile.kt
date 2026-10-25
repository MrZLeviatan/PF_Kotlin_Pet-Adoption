package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Reputation

/**
 Vista de un perfil tal como la ve OTRA persona.

 Los campos opcionales son nulos cuando la privacidad del dueño impide
 mostrarlos. La pantalla de perfil ajeno trabaja solo con esta clase:
 si el dato no está aquí, no hay forma de mostrarlo por accidente.
 */
data class PublicProfile(
    val id: String,
    val userName: String,
    val photoUrl: String?,
    val isPrivate: Boolean,
    val name: String? = null,
    val phone: String? = null,
    val reputation: Reputation? = null,
    val postIds: List<String>? = null
)