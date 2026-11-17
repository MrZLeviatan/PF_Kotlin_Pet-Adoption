# Configuración de Firebase

## Propósito

Esta carpeta contiene los recursos que Firebase necesita fuera del código de la aplicación: las **reglas de seguridad** y los **índices** de Cloud Firestore, la **plantilla del correo** de verificación en dos pasos y el **script de carga de datos de prueba**. Todos los servicios utilizados funcionan con el plan gratuito de Firebase (Spark).

<br>

## Contenido por archivo

| Archivo | Descripción |
|---|---|
| `firestore.rules` | Reglas de seguridad: determinan quién puede leer, crear, modificar o eliminar cada documento. |
| `firestore.indexes.json` | Índices compuestos requeridos por las consultas que combinan filtros y orden (feed, comentarios, chats, notificaciones, historial de puntos). |
| `emailjs-template.html` | Diseño del correo con el código de verificación en dos pasos. Recibe las variables `to_email`, `user_name`, `code` y `minutes`. |
| `seed/seed.mjs` | Script de Node.js que, con el SDK de administración de Firebase, crea las cuentas de prueba con identificadores fijos y carga los documentos iniciales. |
| `seed/seed.json` | Datos de prueba. Se genera automáticamente desde `SeedData.kt` y no contiene contraseñas. |
| `seed/.env.example` | Plantilla para definir las contraseñas de las cuentas de prueba. |
| `seed/package.json` | Dependencias del script (`firebase-admin`). |

En la raíz del proyecto, `firebase.json` indica a Firebase CLI dónde se encuentran las reglas y los índices, y `.firebaserc` define el proyecto por defecto.

---

<br>

## 1. Configuración en la consola

1. **Authentication > Método de acceso:** habilitar *Correo electrónico/contraseña*.
2. **Authentication > Plantillas:** seleccionar el idioma español y definir el nombre del remitente. Esta plantilla corresponde al correo de restablecimiento de contraseña.
3. **Firestore Database > Crear base de datos:** seleccionar el modo de producción. La región no puede modificarse posteriormente; se sugiere `southamerica-east1`.
4. **Configuración del proyecto > Tus apps:** registrar la aplicación Android con el paquete `co.edu.uniquindio.petadoptionapp` y copiar `google-services.json` en `app/`.

---

## 2. Reglas e índices

Se publican desde la raíz del proyecto con Firebase CLI:

```bash
npx firebase-tools login
```
```bash
npx firebase-tools deploy --only firestore
```

`.firebaserc` apunta al proyecto `kog-maw-adoption`. Para utilizar otro proyecto, se debe modificar ese archivo o agregar `--project <id-del-proyecto>` al comando. La creación de los índices puede tardar algunos minutos; su estado se consulta en *Firestore > Índices*.

### Resumen de las reglas

| Colección | Lectura | Escritura |
|---|---|---|
| `users` | Personas autenticadas | El titular; otras personas solo pueden modificar la reputación |
| `moderators` | Personas autenticadas | El titular, únicamente la verificación en dos pasos; no se pueden crear desde la aplicación |
| `emails`, `userNames`, `phones` | Consulta individual, incluso sin sesión | Solo creación por el titular; nunca sobrescritura |
| `twoFactorChallenges` | El titular | El titular |
| `categories`, `animalTypes`, `badges` | Cualquier persona | Moderadores |
| `posts` | Personas autenticadas | El autor (sin aprobarse a sí mismo), los moderadores y los contadores de votos y comentarios |
| `posts/{id}/comments` | Personas autenticadas | El autor, los moderadores y los contadores |
| `chats` y `messages` | Participantes | Participantes |
| `adoptionRequests` | Solicitante y autor de la publicación | Solicitante y autor de la publicación |
| `notifications` | Destinatario | Creación por personas autenticadas; lectura y borrado por el destinatario |
| `pointTransactions` | El titular | Personas autenticadas |

---

## 3. Datos de prueba

Cargan 4 usuarios, 5 moderadores, 5 categorías con su configuración de marcador, 5 tipos de animal, 4 insignias, 14 publicaciones en distintos estados, comentarios, votos, chats, solicitudes de adopción, notificaciones e historial de puntos.

### Procedimiento

1. **Llave de administrador.** En *Configuración del proyecto > Cuentas de servicio*, seleccionar **Generar nueva clave privada** y guardar el archivo como `firebase/seed/service-account.json`.
2. **Contraseñas.** Copiar `firebase/seed/.env.example` como `firebase/seed/.env` y definir `SEED_USER_PASSWORD` y `SEED_MODERATOR_PASSWORD` (mínimo 6 caracteres).
3. **Instalación y carga**, desde la raíz del proyecto:

```bash
npm --prefix firebase/seed install
```
```bash
npm --prefix firebase/seed run seed -- --reset
```

La opción `--reset` elimina todo el contenido de Firestore, incluido el creado desde la aplicación, y las cuentas que usen los correos de prueba con un identificador distinto. Sin esta opción, el script agrega o actualiza los datos existentes.

> La llave de administrador otorga control total sobre el proyecto. `service-account.json` y `.env` están excluidos de git y no deben compartirse.

### Cuentas creadas

| Rol | Correos |
|---|---|
| Usuario | `n@gmail.com`, `luis@correo.com`, `camila@correo.com`, `andres@correo.com` |
| Moderador | `mod@petadoption.com`, `mod2@petadoption.com`, `mod3@petadoption.com`, `mod4@petadoption.com`, `mod5@petadoption.com` |

Las contraseñas son las definidas en `firebase/seed/.env`.

### Modificación de los datos

Los datos se definen en `app/src/test/java/co/edu/uniquindio/petadoptionapp/seed/SeedData.kt`. Tras modificarlos, se regenera `seed.json` con:

```bash
./gradlew :app:testDebugUnitTest --tests "*SeedExportTest*"
```

### Moderadores adicionales

La aplicación no permite crear moderadores. Para agregar uno, se crea la cuenta en *Authentication > Agregar usuario* y se crea manualmente el documento `moderators/{uid}` con la misma estructura que el moderador `m1`.

---

<br>

## 4. EmailJS

1. En [emailjs.com](https://www.emailjs.com), sección **Email Services**, conectar una cuenta de correo y copiar el *Service ID*.
2. En **Email Templates**, crear una plantilla con el contenido de `emailjs-template.html`. En el campo *To Email* escribir `{{to_email}}` y en el asunto `{{code}} es tu código de verificación`. Copiar el *Template ID*.
3. En **Account > General**, copiar la *Public Key*.
4. En **Account > Security**, habilitar **Allow EmailJS API for non-browser applications**. Si se activa *Use Private Key*, copiar también la *Private Key*.
5. Registrar los valores en `local.properties` (ver el [README principal](../README.md#configuración-de-llaves)).

Cada persona activa la verificación en dos pasos desde **Configuración > Verificación en dos pasos**.

---

<br>

## Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| "No pudimos enviar el código de verificación" | Llaves de EmailJS ausentes, acceso para aplicaciones no habilitado o llave privada faltante en modo estricto | Revisar la configuración de EmailJS; el motivo exacto aparece en Logcat con el filtro `EmailJsCodeSender` |
| Listas vacías o error *requires an index* | Índices no publicados o en construcción | Ejecutar el despliegue y esperar a que los índices estén habilitados |
| Error *PERMISSION_DENIED* | Reglas no publicadas o acción no permitida para el rol | Publicar las reglas y verificar el rol de la cuenta |
| "Correo ya utilizado" para una cuenta inexistente en la aplicación | Cuenta en Authentication sin perfil en Firestore | Eliminar la cuenta desde *Authentication > Usuarios* |
