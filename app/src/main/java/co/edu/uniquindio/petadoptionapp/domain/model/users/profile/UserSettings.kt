package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

/**
 Preferencias de seguridad y privacidad de un [User].

 Es la ÚNICA fuente de la política de privacidad: ninguna pantalla debe
 decidir por su cuenta qué se muestra; todas preguntan a estas funciones.

 Política actual:
 - Perfil [ProfileVisibility.PUBLIC]: otros usuarios ven todo.
 - Perfil [ProfileVisibility.PRIVATE]: otros usuarios NO ven nombre real, teléfono, reputación ni lista de publicaciones.
 El alias y la foto siempre se muestran, para poder identificar a la persona en comentarios y chats.

 Las funciones reciben `isOwner` porque el dueño siempre ve su propio perfil completo, sin importar su configuración.
 */
data class UserSettings(

    // Si al iniciar sesión se pide el código de verificación en dos pasos. El código vive en TwoFactorChallenge.
    val twoFactorEnabled: Boolean = false,
    // Define si el perfil es público o privado.
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC
) {

    // ¿El perfil es público?
    fun hasProfilePublic(): Boolean =
        profileVisibility == ProfileVisibility.PUBLIC

    // ¿Que consulta puede ver el nombre real?
    fun canViewName(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()

    // ¿Que consulta puede ver el teléfono?
    fun canViewPhone(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()

    // ¿Qué consulta puede ver puntos, nivel e insignias?
    fun canViewReputation(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()

    // ¿Que consulta puede ver la lista de publicaciones?
    fun canViewPosts(isOwner: Boolean): Boolean = isOwner || hasProfilePublic()
}