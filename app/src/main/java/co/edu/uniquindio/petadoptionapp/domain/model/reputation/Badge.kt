package co.edu.uniquindio.petadoptionapp.domain.model.reputation

/**
 Insignia del catálogo del sistema.

 El catálogo sé precarga en la base de datos (igual que los moderadores)
 y es común a todos los usuarios. Aquí se define QUÉ insignias existen y
 QUÉ hay que lograr para ganarlas. Las que gana cada persona se guardan
 aparte como [UnlockedBadge].

 - requirementType Qué se mide para desbloquearla.
 - Cantidad que hay que alcanzar (ej. 10 verificadas).
 */
data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val requirementType: BadgeRequirementType = BadgeRequirementType.POSTS_CREATED,
    val requirementValue: Int = 1
)