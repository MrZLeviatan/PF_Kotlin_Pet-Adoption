package co.edu.uniquindio.petadoptionapp.domain.repository

import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import kotlinx.coroutines.flow.StateFlow

/**
 Contrato de identidad y sesión. Define las operaciones de autenticación.
 Las validaciones de formato y reglas de flujo viven en los casos de uso, aquí solo se ejecuta.
 */
interface AuthRepository {

    // Sesión


    /**
    Flujo reactivo de la cuenta autenticada actual.

    - Qué emite: Un [StateFlow] que contiene la [Account] activa, o `null` si no hay sesión o falta la A2F.
    Permite que la UI y otras capas observen en tiempo real el estado de la sesión para redirigir vistas automáticamente.

    - StateFlow es una "caja" que avisa la pantalla cuando el contenido cambia.
     */
    val currentAccount: StateFlow<Account?>


    /**
    Finaliza la sesión actual del usuario.

    - Qué recibe: Ningún valor de retorno (`Unit` implícito).
    - Limpia las credenciales y tokens almacenados en el dispositivo para desconectar al usuario de la aplicación.
     */
    suspend fun logout()


    // Login

    /**
     Intenta autenticar al usuario en el sistema.

     - Qué envía ([credentials]): Las credenciales de acceso (correo electrónico y contraseña).
     - Qué recibe: Un [Result] envuelto en un [LoginResult] que indica si entró directo ([LoginResult.Success]) o si requiere doble factor ([LoginResult.TwoFactorRequired]).
     Es el punto de entrada principal para verificar la identidad del usuario contra el backend o Firebase.

     - suspend: Habla con internet, así que tarda. La pantalla no se congela mientras espera.
     - Result: Puede salir bien o mal (contraseña incorrecta, sin internet). Así la app no se cierra por un error.
     */
    suspend fun login(credentials: Credentials): Result<LoginResult>


    // A2F (Autenticación de Dos Factores)

    /**
     Solicita la generación y envío de un código de verificación adicional.

     - Qué envía ([accountId]): El identificador único de la cuenta que requiere el segundo factor.
     - Qué recibe: Un [Result<Unit>] que avisa si el envío del código fue exitoso o falló (No devuelve datos)
     Inicia el protocolo de seguridad enviando un código temporal al correo del usuario.
     */
    suspend fun requestTwoFactorCode(accountId: String): Result<Unit>

    /**
     Válida el código de doble factor ingresado por el usuario.

     - Qué envía: El [accountId] del usuario y el [code] (código de 6 dígitos) recibido por correo.
     - Qué recibe: Un [Result] con la [Account] completa si el código es válido.
     Supera la barrera de seguridad de la A2F para completar formalmente el inicio de sesión.
     */
    suspend fun verifyTwoFactorCode(accountId: String, code: String): Result<Account>


    // Crear cuenta

    /**
     Registra un nuevo usuario en la plataforma.
     *
     - Qué envía: Las [credentials] (correo y contraseña).
     - Qué recibe: Un [Result] con la UUID recién creada.

     Firebase Auth solo conoce email y contraseña. El alias, el nombre y el teléfono son perfil, y se guardan en Firestore con UserRepository.
     */
    suspend fun register(credentials: Credentials): Result<String>

    /**
     Completa la cuenta de la sesión con los datos que Firebase Auth no guarda (alias y rol).

     - Qué envía ([account]): La cuenta con su alias, ya guardada en el perfil.
     - Qué recibe: Un [Result<Unit>] que confirma que la sesión quedó actualizada.
     Sin esto, una cuenta recién creada tendría el alias vacío en la sesión (y así saldría en
     comentarios, notificaciones y chats). Con Firebase, el alias se lee del documento del usuario.
     */
    suspend fun updateSessionAccount(account: Account): Result<Unit>


    // Recuperación de contraseña

    /**
     Envía instrucciones para restablecer la contraseña olvidada.

     - Qué envía ([email]): El correo electrónico registrado de la cuenta.
     - Qué recibe: Un [Result<Unit>] que confirma si el proceso de recuperación dio inicio.
     Permite a un usuario recuperar el acceso a su cuenta si no recuerda su contraseña.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /**
     Cambia la contraseña de un usuario con sesión activa.

     - Qué envía: La [currentPassword] (contraseña actual por seguridad) y la [newPassword] (nueva contraseña).
     - Qué recibe: Un [Result<Unit>] que confirma si la modificación fue exitosa.
     Permite al usuario actualizar su clave desde los ajustes de su perfil exigiendo verificar su identidad actual.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>


    // Eliminar cuenta

    /**
    Confirma la identidad de la sesión actual antes de una operación sensible.

    - Qué envía ([password]): La contraseña vigente de la cuenta.
    - Qué recibe: Un [Result<Unit>] que confirma si la contraseña es correcta.
    Firebase exige haber iniciado sesión recientemente antes de borrar una cuenta.
     */
    suspend fun reauthenticate(password: String): Result<Unit>


    /**
    Elimina la cuenta de Firebase Auth de la sesión actual.

    - Qué recibe: Un [Result<Unit>] que confirma si la cuenta fue eliminada.
    Al borrarla, la sesión se cierra. El perfil de Firestore se borra aparte con UserRepository.
     */
    suspend fun deleteAccount(): Result<Unit>

}


/**
 Modela los posibles resultados del proceso de inicio de sesión.
 Es una clase sellada ([sealed class]) para limitar las respuestas posibles.
 */
sealed class LoginResult {

    /**
     Resultado cuando el login es exitoso de inmediato.
     - Contiene: La [account] con los datos del usuario.
     */
    data class Success(val account: Account) : LoginResult()

    /**
     Resultado cuando las credenciales son correctas, pero falta el segundo factor.
     - Contiene: El [accountId] necesario para continuar solicitando el código A2F.
     */
    data class TwoFactorRequired(val accountId: String) : LoginResult()
}