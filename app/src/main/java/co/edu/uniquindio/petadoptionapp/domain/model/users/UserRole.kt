package co.edu.uniquindio.petadoptionapp.domain.model.users

/**
Modelo de dominio: posibles roles dentro de la aplicación a nivel usuario ([Account.role]).
Lo consultan la sesión, la navegación y los casos de uso que exigen permisos de moderador.
**/
// Enumera los roles: persona registrada (USER) y moderador (MODERATOR).
enum class UserRole { USER, MODERATOR }
