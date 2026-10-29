package co.edu.uniquindio.petadoptionapp.data.repository.memory

import co.edu.uniquindio.petadoptionapp.data.repository.send.SeedData
import co.edu.uniquindio.petadoptionapp.domain.error.AuthException
import co.edu.uniquindio.petadoptionapp.domain.model.auth.Credentials
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.repository.AuthRepository
import co.edu.uniquindio.petadoptionapp.domain.repository.LoginResult
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
Documenta la implementación en memoria del repositorio de autenticación.
Simula Firebase Auth: las cuentas y contraseñas viven en mapas temporales y se pierden al cerrar la app.
Solo ejecuta: las validaciones de formato y las reglas de flujo viven en los casos de uso.
 */
// Anota la clase como un singleton para que Hilt gestione una única instancia compartida en toda la aplicación.
@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    // Guarda las cuentas conocidas, con el id como clave.
    private val accounts = mutableMapOf<String, Account>()

    // Guarda las contraseñas, con el correo en minúsculas como clave.
    private val passwords = mutableMapOf<String, String>()

    // Guarda la cuenta con la sesión abierta; null si nadie ha iniciado sesión.
    private val _currentAccount = MutableStateFlow<Account?>(null)
    // Expone un flujo de estado inmutable para observar la cuenta autenticada actual desde la capa de presentación o dominio.
    override val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    // Carga las cuentas y contraseñas de prueba al crear la instancia.
    init {
        // Combina las listas de usuarios y moderadores de prueba, mapea sus cuentas y las almacena en el mapa indexadas por su ID.
        (SeedData.users.map { it.account } + SeedData.moderators.map { it.account })
            .forEach { accounts[it.id] = it }
        // Rellena el mapa de contraseñas utilizando el correo en minúsculas como clave y la contraseña asociada.
        SeedData.passwords.forEach { (email, password) -> passwords[email.lowercase()] = password }
    }

    // Busca una cuenta por correo, ignorando mayúsculas y espacios.
    private fun findByEmail(email: String): Account? =
        accounts.values.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }

    // Define la función asíncrona para iniciar sesión recibiendo credenciales y devolviendo un resultado con el objeto de resultado de inicio de sesión.
    override suspend fun login(credentials: Credentials): Result<LoginResult> {
        // Busca la cuenta asociada al correo electrónico proporcionado.
        val account = findByEmail(credentials.email)
        // Mismo error si falla el correo o la contraseña, para no revelar qué correos existen.
        if (account == null || passwords[account.email.lowercase()] != credentials.password)
            return Result.failure(AuthException.WrongPassword)

        // Sin A2F en esta fase: abre la sesión directamente asignando la cuenta al flujo de estado.
        _currentAccount.value = account
        // Retorna un resultado exitoso que contiene el resultado de inicio de sesión con la cuenta autenticada.
        return Result.success(LoginResult.Success(account))
    }

    // Define la función asíncrona para registrar un nuevo usuario con credenciales, devolviendo un resultado con el ID generado.
    override suspend fun register(credentials: Credentials): Result<String> {
        // Firebase rechaza correos repetidos; aquí se simula validando si ya existe una cuenta con el correo ingresado.
        if (findByEmail(credentials.email) != null)
            return Result.failure(AuthException.EmailAlreadyInUse)

        // Genera un identificador único universal (UUID) en formato de cadena para la nueva cuenta.
        val id = UUID.randomUUID().toString()
        // Crea una instancia de la cuenta con el ID, el correo recortado y el rol predeterminado de usuario.
        val account = Account(id = id, email = credentials.email.trim(), role = UserRole.USER)
        // Almacena la nueva cuenta y su contraseña correspondiente en sus respectivos mapas en memoria.
        accounts[id] = account
        passwords[account.email.lowercase()] = credentials.password

        // Firebase abre la sesión al registrar; se imita igual actualizando el estado de la cuenta actual.
        _currentAccount.value = account
        // Retorna un resultado exitoso con el ID generado de la cuenta.
        return Result.success(id)
    }

    // Guarda el alias y el rol en la cuenta (también para los próximos inicios de sesión) y en la sesión abierta.
    override suspend fun updateSessionAccount(account: Account): Result<Unit> {
        val stored = accounts[account.id] ?: return Result.failure(AuthException.NotAuthenticated)
        val updated = stored.copy(userName = account.userName, role = account.role)
        accounts[account.id] = updated
        if (_currentAccount.value?.id == account.id) _currentAccount.value = updated
        return Result.success(Unit)
    }

    // Define la función asíncrona para cerrar la sesión actual limpiando el flujo de la cuenta autenticada.
    override suspend fun logout() {
        // Establece el valor de la cuenta actual en nulo para finalizar la sesión.
        _currentAccount.value = null
    }

    // Define la función asíncrona para reautenticar al usuario validando su contraseña actual.
    override suspend fun reauthenticate(password: String): Result<Unit> {
        // Obtiene la cuenta actual en sesión o retorna un fallo si no hay ningún usuario autenticado.
        val account = _currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Comprueba si la contraseña almacenada coincide con la proporcionada, retornando éxito o un fallo de contraseña incorrecta.
        return if (passwords[account.email.lowercase()] == password) Result.success(Unit)
        else Result.failure(AuthException.WrongPassword)
    }

    // Define la función asíncrona para eliminar la cuenta del usuario actual.
    override suspend fun deleteAccount(): Result<Unit> {
        // Obtiene la cuenta actual en sesión o retorna un fallo si no está autenticado.
        val account = _currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Elimina la cuenta y su contraseña de los mapas de almacenamiento correspondientes.
        accounts.remove(account.id)
        passwords.remove(account.email.lowercase())
        // Limpia el estado de la sesión actual asignando nulo.
        _currentAccount.value = null
        // Retorna un resultado vacío exitoso.
        return Result.success(Unit)
    }

    // Siempre éxito: no revela qué correos están registrados por motivos de seguridad.
    override suspend fun sendPasswordReset(email: String): Result<Unit> = Result.success(Unit)

    // Define la función asíncrona para cambiar la contraseña del usuario actual.
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        // Obtiene la cuenta actual en sesión o retorna un fallo si no está autenticado.
        val account = _currentAccount.value
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Valida que la contraseña actual proporcionada coincida con la almacenada, retornando un fallo en caso contrario.
        if (passwords[account.email.lowercase()] != currentPassword)
            return Result.failure(AuthException.WrongPassword)
        // Actualiza la contraseña en el mapa con el nuevo valor y retorna éxito.
        passwords[account.email.lowercase()] = newPassword
        return Result.success(Unit)
    }

    // A2F simulada: el código no se envía realmente, siempre se considera exitoso.
    override suspend fun requestTwoFactorCode(accountId: String): Result<Unit> = Result.success(Unit)

    // Define la función asíncrona para verificar el código de doble factor (A2F).
    override suspend fun verifyTwoFactorCode(accountId: String, code: String): Result<Account> {
        // Busca la cuenta por su ID o retorna un fallo de no autenticado si no existe.
        val account = accounts[accountId]
            ?: return Result.failure(AuthException.NotAuthenticated)
        // Valida que el código ingresado coincida con el código falso predeterminado, retornando un error si es incorrecto.
        if (code != FAKE_CODE) return Result.failure(AuthException.WrongCode)
        // Establece la cuenta autenticada en el flujo de estado y retorna el resultado exitoso con la cuenta.
        _currentAccount.value = account
        return Result.success(account)
    }

    // Declara un objeto de acompañamiento privado para constantes internas de la implementación.
    private companion object {
        // Código fijo de prueba para la A2F simulada.
        const val FAKE_CODE = "123456"
    }
}