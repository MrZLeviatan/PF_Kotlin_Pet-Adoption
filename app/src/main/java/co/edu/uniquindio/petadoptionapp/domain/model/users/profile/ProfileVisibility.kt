package co.edu.uniquindio.petadoptionapp.domain.model.users.profile


/**
Modelo de dominio: nivel de privacidad del perfil de una persona ([UserSettings.profileVisibility]).

Se usa un enum (y no un booleano `isPublic`) porque deja espacio para
crecer sin migrar datos, por ejemplo agregando más adelante un nivel
intermedio como "solo personas con las que he chateado".
Lo consulta [UserSettings] para decidir qué datos ve otra persona.
**/
// Enumera los niveles de privacidad.
enum class ProfileVisibility {

    // Cualquier usuario puede ver el perfil completo.
    PUBLIC,

    // Otros usuarios solo ven lo básico (alias, foto, nivel).
    PRIVATE
}
