package co.edu.uniquindio.petadoptionapp.domain.model.reputation

import kotlinx.serialization.Serializable

/**
Modelo de dominio: insignia del catálogo del sistema.

El catálogo se precarga en la base de datos (igual que los moderadores)
y es común a todos los usuarios. Aquí se define QUÉ insignias existen y
QUÉ hay que lograr para ganarlas. Las que gana cada persona se guardan
aparte como [UnlockedBadge].

- requirementType: qué se mide para desbloquearla.
- requirementValue: cantidad que hay que alcanzar (ej. 10 verificadas).
La usan ReputationRepository, EvaluateMyBadgesUseCase y la pantalla de insignias.
**/
// Permite convertir la insignia a/desde el formato de almacenamiento.
@Serializable
// Declara la insignia como clase de datos inmutable.
data class Badge(
    // Identificador de la insignia en el catálogo.
    val id: String = "",
    // Nombre visible de la insignia.
    val name: String = "",
    // Descripción de lo que hay que lograr.
    val description: String = "",
    // Dirección de la imagen del ícono.
    val iconUrl: String = "",
    // Qué logro se mide para desbloquearla.
    val requirementType: BadgeRequirementType = BadgeRequirementType.POSTS_CREATED,
    // Cantidad mínima que hay que alcanzar en ese logro.
    val requirementValue: Int = 1
)
