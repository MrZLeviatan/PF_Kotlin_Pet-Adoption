package co.edu.uniquindio.petadoptionapp.domain.model.users


/**
 Datos de identidad de una cuenta. Es la pieza compartida por
 [User] y [Moderator], que la reutilizan por composición
 (cada uno "tiene" una cuenta).

 IMPORTANTE: esta clase se guarda en Firestore, por eso NO contiene la
 contraseña. La contraseña la gestiona Firebase Auth (la almacena cifrada)
 y solo viaja en memoria mediante [Credentials].

 Todas las propiedades tienen un valor por defecto porque Firestore crea
 primero un objeto vacío y luego lo llena; sin valores por defecto, la
 lectura de documentos fallaría.

 - id Identificador único. Corresponde al `uid` que entrega Firebase Auth al registrar la cuenta; así se enlaza el login con el perfil sin consultas adicionales.
 - createdAt Fecha de creación en milisegundos desde 1970 (epoch). Es simple de ordenar y compatible con cualquier base de datos.
 */
data class Account(
    val id: String = "",
    val email: String = "",
    val userName: String = "",
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis()
)