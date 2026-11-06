package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
Modelo de dominio: prioridad sugerida por la IA al analizar la publicación ([AiReview]).
La usan la cola de moderación y las estadísticas del moderador para ordenar y agrupar
las publicaciones pendientes.
**/
// Enumera las prioridades de menor a mayor: baja (LOW), media (MEDIUM), alta (HIGH) y urgente (URGENT).
enum class PriorityLevel { LOW, MEDIUM, HIGH, URGENT }
