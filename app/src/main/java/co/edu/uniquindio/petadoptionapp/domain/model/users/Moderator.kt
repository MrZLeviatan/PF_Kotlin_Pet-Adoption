package co.edu.uniquindio.petadoptionapp.domain.model.users

/**
 Perfil de un moderador de la plataforma.

 Se construye por composición: un [Moderator] "tiene" una [Account] con
 rol [UserRole.MODERATOR]. No tiene reputación, ubicación ni chats: su
 función es revisar publicaciones, no participar en la comunidad.

 Los moderadores NO se registran desde la app: se precargan en Firebase
 Auth y en la colección `moderators`, usando como id el mismo `uid`.
 Pueden recuperar su contraseña por correo igual que un usuario.

 NO contiene la contraseña: la gestiona únicamente Firebase Auth.
 */
data class Moderator(
    val account: Account = Account(role = UserRole.MODERATOR),
    val name: String = "",
    // Teléfono de contacto (se ve en su perfil).
    val phone: String = "",
    // Verificación en dos pasos al iniciar sesión.
    val twoFactorEnabled: Boolean = false,
    // Desactiva al moderador sin borrar su historial.
    val active: Boolean = true,
    // Publicaciones que ha verificado.
    val approvedCount: Int = 0,
    // Publicaciones que ha rechazado.
    val rejectedCount: Int = 0,
    // Última modificación, en milisegundos (epoch).
    val updatedAt: Long = System.currentTimeMillis()
) {

    // Total de publicaciones revisadas.
    fun reviewedCount(): Int = approvedCount + rejectedCount

    // ¿Puede ejercer? Debe estar activo y tener rol de moderador.
    fun canModerate(): Boolean = active && account.role == UserRole.MODERATOR
}