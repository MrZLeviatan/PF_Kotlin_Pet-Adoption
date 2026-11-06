package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
Modelo de dominio: métricas del dashboard personal de un usuario. No se guardan: se calculan con conteos.
Lo produce GetPostStatsUseCase (a partir de PostRepository) y lo muestra la pantalla de perfil/dashboard.
**/
// Declara las métricas como clase de datos inmutable.
data class PostStats(
    // Publicaciones verificadas y en proceso.
    val active: Int = 0,
    // Publicaciones finalizadas.
    val finished: Int = 0,
    // Publicaciones esperando o en revisión de un moderador.
    val pendingVerification: Int = 0
)
