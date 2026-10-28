package co.edu.uniquindio.petadoptionapp.domain.model.users.profile


/**
 Nivel de privacidad del perfil de una persona.

 Se usa un enum (y no un booleano `isPublic`) porque deja espacio para
 crecer sin migrar datos, por ejemplo agregando más adelante un nivel
  intermedio como "solo personas con las que he chateado".
 */
enum class ProfileVisibility {

    // Cualquier usuario puede ver el perfil completo.
    PUBLIC,

    // Otros usuarios solo ven lo básico (alias, foto, nivel).
    PRIVATE
}