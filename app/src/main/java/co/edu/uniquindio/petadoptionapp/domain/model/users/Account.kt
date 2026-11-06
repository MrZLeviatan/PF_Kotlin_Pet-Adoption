package co.edu.uniquindio.petadoptionapp.domain.model.users

import kotlinx.serialization.Serializable


/**
Modelo de dominio: datos de identidad de una cuenta. Es la pieza compartida por
[User] y [Moderator], que la reutilizan por composición
(cada uno "tiene" una cuenta).

IMPORTANTE: esta clase se guarda en Firestore, por eso NO contiene la
contraseña. La contraseña la gestiona Firebase Auth (la almacena cifrada)
y solo viaja en memoria mediante [Credentials].

Todas las propiedades tienen un valor por defecto porque Firestore crea
primero un objeto vacío y luego lo llena; sin valores por defecto, la
lectura de documentos fallaría.
La usan AuthRepository (sesión actual), UserRepository y casi todos los casos de uso.
**/
// Permite convertir la cuenta a/desde el formato de almacenamiento.
@Serializable
// Declara la cuenta como clase de datos inmutable.
data class Account(
    // Corresponde al `uid` que entrega Firebase Auth al registrar la cuenta; así se enlaza el login con el perfil sin consultas adicionales.
    val id: String = "",
    // Correo electrónico de la cuenta.
    val email: String = "",
    // Alias público y único de la persona.
    val userName: String = "",
    // Rol de la cuenta (usuario o moderador).
    val role: UserRole = UserRole.USER,
    // Fecha de creación en milisegundos desde 1970 (epoch). Es simple de ordenar y compatible con cualquier base de datos.
    val createdAt: Long = System.currentTimeMillis()
)
