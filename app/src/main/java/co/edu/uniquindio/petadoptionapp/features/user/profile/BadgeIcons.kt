package co.edu.uniquindio.petadoptionapp.features.user.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType

/**
Utilidad de presentación que asigna un icono de Material a cada tipo de logro
de las insignias (BadgeRequirementType). El dominio solo conoce el tipo de requisito;
el icono es una decisión visual, por eso vive en la capa de features y no en el modelo.
La comparten la pantalla de Métricas (insignias propias) y el perfil público de otras
personas (UserProfileScreen), de modo que una misma insignia se ve igual en ambas.
**/

// Función de extensión que devuelve el icono correspondiente al tipo de logro de la insignia.
fun BadgeRequirementType.icon(): ImageVector = when (this) {
    // Insignias por cantidad de publicaciones creadas: icono de documento.
    BadgeRequirementType.POSTS_CREATED -> Icons.Outlined.Description
    // Insignias por publicaciones verificadas: icono de usuario verificado.
    BadgeRequirementType.POSTS_VERIFIED -> Icons.Outlined.VerifiedUser
    // Insignias por publicaciones resueltas (adopciones o reencuentros): icono de estrella.
    BadgeRequirementType.POSTS_RESOLVED -> Icons.Outlined.Star
    // Insignias por comentarios escritos: icono de burbuja de chat.
    BadgeRequirementType.COMMENTS_WRITTEN -> Icons.Outlined.ChatBubbleOutline
    // Insignias por votos recibidos: icono de corazón.
    BadgeRequirementType.VOTES_RECEIVED -> Icons.Outlined.FavoriteBorder
    // Insignias por puntos totales acumulados: icono de medalla premium.
    BadgeRequirementType.TOTAL_POINTS -> Icons.Outlined.WorkspacePremium
}
