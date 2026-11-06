package co.edu.uniquindio.petadoptionapp.domain.model.post

/**
Modelo de dominio: tamaño aproximado del animal de una publicación ([Post]).
Lo elige el autor al crear o editar la publicación y se usa en los filtros del feed.
**/
// Enumera los tamaños posibles: pequeño (SMALL), mediano (MEDIUM) y grande (LARGE).
enum class AnimalSize { SMALL, MEDIUM, LARGE }
