package co.edu.uniquindio.petadoptionapp.domain.model.users.profile

/**
Modelo de dominio: razones viables para la eliminación de una cuenta ([AccountDeletionFeedback.reason]).
Se muestran como opciones en el diálogo de eliminar cuenta.
**/
// Enumera las razones: ya no usa la app (NOT_USING), encontró mascota (FOUND_PET), privacidad (PRIVACY) u otra (OTHER).
enum class DeletionReason { NOT_USING, FOUND_PET, PRIVACY, OTHER }
