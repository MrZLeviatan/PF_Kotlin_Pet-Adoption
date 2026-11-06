package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

import kotlinx.serialization.Serializable

/**
Modelo de dominio: preferencias de seguridad y privacidad de un [User].

Es la ÚNICA fuente de la política de privacidad: ninguna pantalla debe
decidir por su cuenta qué se muestra; todas preguntan a estas funciones.

Política actual:
- Perfil [ProfileVisibility.PUBLIC]: otros usuarios ven todo.
- Perfil [ProfileVisibility.PRIVATE]: otros usuarios NO ven nombre real, teléfono, reputación ni lista de publicaciones.
El alias y la foto siempre se muestran, para poder identificar a la persona en comentarios y chats.

Las funciones reciben `isOwner` porque el dueño siempre ve su propio perfil completo, sin importar su configuración.
La usan [User.toPublicProfile], UpdateSettingsUseCase y la pantalla de ajustes.
**/
// Permite convertir los ajustes a/desde el formato de almacenamiento.
@Serializable
// Declara los ajustes como clase de datos inmutable.
data class UserSettings(

    // Si al iniciar sesión se pide el código de verificación en dos pasos. El código vive en TwoFactorChallenge.
    val twoFactorEnabled: Boolean = false,
    // Define si el perfil es público o privado.
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC
) {

    // ¿El perfil es público?
    fun hasProfilePublic(): Boolean =
        // Compara la visibilidad configurada con PUBLIC.
        profileVisibility == ProfileVisibility.PUBLIC

    // ¿Quien consulta puede ver el nombre real? Sí si es el dueño o el perfil es público.
    fun canViewName(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()

    // ¿Quien consulta puede ver el teléfono? Sí si es el dueño o el perfil es público.
    fun canViewPhone(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()

    // ¿Quien consulta puede ver puntos, nivel e insignias? Sí si es el dueño o el perfil es público.
    fun canViewReputation(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()

    // ¿Quien consulta puede ver la lista de publicaciones? Sí si es el dueño o el perfil es público.
    fun canViewPosts(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()
}
