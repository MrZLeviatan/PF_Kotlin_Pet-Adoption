package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Reputation

/**
Modelo de dominio: vista de un perfil tal como la ve OTRA persona.

Los campos opcionales son nulos cuando la privacidad del dueño impide
mostrarlos. La pantalla de perfil ajeno trabaja solo con esta clase:
si el dato no está aquí, no hay forma de mostrarlo por accidente.
La construye [User.toPublicProfile] y la devuelve GetUserProfileUseCase.
**/
// Declara el perfil público como clase de datos inmutable.
data class PublicProfile(
    // Id de la cuenta.
    val id: String,
    // Alias público (siempre visible).
    val userName: String,
    // Foto de perfil (siempre visible).
    val photoUrl: String?,
    // `true` si el dueño tiene el perfil en privado.
    val isPrivate: Boolean,
    // Nombre real; nulo si la privacidad lo oculta.
    val name: String? = null,
    // Teléfono; nulo si la privacidad lo oculta.
    val phone: String? = null,
    // Reputación; nula si la privacidad la oculta.
    val reputation: Reputation? = null,
    // Ids de publicaciones; nulos si la privacidad los oculta.
    val postIds: List<String>? = null
)
