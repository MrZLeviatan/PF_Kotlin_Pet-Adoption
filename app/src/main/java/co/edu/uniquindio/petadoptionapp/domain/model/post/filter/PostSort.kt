package co.edu.uniquindio.petadoptionapp.domain.model.post.filter

/**
Modelo de dominio: orden en que se muestra el feed ([PostFilter.sortBy]).
Lo elige la persona en la pantalla del feed y lo aplica PostRepository al consultar.
**/
// Enumera los criterios de orden disponibles.
enum class PostSort {
    // Las más nuevas primero.
    RECENT,

    // Las más destacadas ("Es importante") primero.
    MOST_IMPORTANT
}
