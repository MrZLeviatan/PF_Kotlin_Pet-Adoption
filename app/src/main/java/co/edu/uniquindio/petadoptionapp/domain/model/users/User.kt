package co.edu.uniquindio.petadoptionapp.domain.model.users

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Reputation
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.PublicProfile
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSettings
import kotlinx.serialization.Serializable

/**
Modelo de dominio: perfil de una persona que se registra desde la aplicación.

Se construye por composición: un [User] "tiene" una [Account] con sus datos
de identidad, en lugar de repetir esos campos. Así la misma [Account]
se reutiliza en [Moderator].

NO contiene [Credentials]: esta clase se guarda en Firestore y la
contraseña nunca debe almacenarse allí.

Todas las propiedades tienen valor por defecto porque Firestore crea
primero un objeto vacío y luego lo llena.
Lo usan UserRepository, los casos de uso de usecase/user, auth y reputation, y las pantallas de perfil.
**/
// Permite convertir el usuario a/desde el formato de almacenamiento.
@Serializable
// Declara el usuario como clase de datos inmutable.
data class User (

    // Cuenta asociada al usuario.
    val account: Account = Account(role = UserRole.USER),
    // Nombre real de la persona.
    val name: String = "",
    // Teléfono de contacto.
    val phone: String = "",
    // Foto de perfil; por defecto apunta a un ícono genérico alojado en Cloudinary.
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
    // [fullAccess]: quien mira es un moderador; ve todos los datos aunque el perfil sea privado.
    fun toPublicProfile(viewerId: String, postIds: List<String> = emptyList(), fullAccess: Boolean = false): PublicProfile {
        // Se trata como dueño si quien mira es la misma persona o tiene acceso completo (moderador).
        val isOwner = viewerId == account.id || fullAccess
        // Construye la vista pública aplicando la política de privacidad.
        return PublicProfile(
            // Id público de la cuenta.
            id = account.id,
            // El alias siempre se muestra.
            userName = account.userName,
            // La foto siempre se muestra.
            photoUrl = photoUrl,
            // Indica si el perfil es privado.
            isPrivate = !settings.hasProfilePublic(),
            // El nombre real solo se incluye si la política lo permite.
            name = name.takeIf { settings.canViewName(isOwner) },
            // El teléfono solo se incluye si la política lo permite.
            phone = phone.takeIf { settings.canViewPhone(isOwner) },
            // La reputación solo se incluye si la política lo permite.
            reputation = reputation.takeIf { settings.canViewReputation(isOwner) },
            // La lista de publicaciones solo se incluye si la política lo permite.
            postIds = postIds.takeIf { settings.canViewPosts(isOwner) }
        )
    }

}
