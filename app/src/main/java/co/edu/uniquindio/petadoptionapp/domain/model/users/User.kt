package co.edu.uniquindio.petadoptionapp.domain.model.users

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Reputation
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.PublicProfile
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings

/**
 Perfil de una persona que se registra desde la aplicación.

 Se construye por composición: un [User] "tiene" una [Account] con sus datos
 de identidad, en lugar de repetir esos campos. Así la misma [Account]
 se reutiliza en [Moderator].

 NO contiene [Credentials]: esta clase se guarda en Firestore y la
 contraseña nunca debe almacenarse allí.

 Todas las propiedades tienen valor por defecto porque Firestore crea
 primero un objeto vacío y luego lo llena.
 */
data class User (

    // Cuenta asociada al usuario.
    val account: Account = Account(role = UserRole.USER),
    val name: String = "",
    val phone: String = "",
    val photoUrl: String? = "https://res.cloudinary.com/dehltwwbu/image/upload/v1793019358/IconDefault_b3nesx.jpg",
    // Locación geográfica del usuario.
    val location: Location = Location(),
    // Reputación (métricas) del usuario.
    val reputation: Reputation = Reputation(),
    // Ids de los chats en los que participa la persona.
    val chatIds: List<String> = emptyList(),
    // Preferencias de seguridad y privacidad (perfil público o privado).
    val settings: UserSettings = UserSettings(),
    // Última modificación del perfil, en milisegundos desde 1970 (epoch).
    val updatedAt: Long = System.currentTimeMillis(),
    // Marca la cuenta como eliminada (borrado lógico).
    val deleted: Boolean = false,
    // Momento de eliminación, en milisegundos (epoch); nulo si la cuenta sigue activa.
    val deletedAt: Long? = null,


    ){

    /**
     Construye lo que otra persona puede ver de este perfil, aplicando la
     política de [UserSettings]. Los datos ocultos quedan en `null`.

     - viewerId Id de quien consulta el perfil.
     - postIds Ids de las publicaciones del usuario (se consultan aparte, porque no se guardan dentro de [User]).
     */
    fun toPublicProfile(viewerId: String, postIds: List<String> = emptyList()): PublicProfile {
        val isOwner = viewerId == account.id
        return PublicProfile(
            id = account.id,
            userName = account.userName,
            photoUrl = photoUrl,
            isPrivate = !settings.hasProfilePublic(),
            name = name.takeIf { settings.canViewName(isOwner) },
            phone = phone.takeIf { settings.canViewPhone(isOwner) },
            reputation = reputation.takeIf { settings.canViewReputation(isOwner) },
            postIds = postIds.takeIf { settings.canViewPosts(isOwner) }
        )
    }

}