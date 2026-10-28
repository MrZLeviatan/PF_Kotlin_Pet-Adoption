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

// El icono es presentación: depende del tipo de logro de la insignia.
// Lo comparten Métricas y el perfil de otras personas.
fun BadgeRequirementType.icon(): ImageVector = when (this) {
    BadgeRequirementType.POSTS_CREATED -> Icons.Outlined.Description
    BadgeRequirementType.POSTS_VERIFIED -> Icons.Outlined.VerifiedUser
    BadgeRequirementType.POSTS_RESOLVED -> Icons.Outlined.Star
    BadgeRequirementType.COMMENTS_WRITTEN -> Icons.Outlined.ChatBubbleOutline
    BadgeRequirementType.VOTES_RECEIVED -> Icons.Outlined.FavoriteBorder
    BadgeRequirementType.TOTAL_POINTS -> Icons.Outlined.WorkspacePremium
}
