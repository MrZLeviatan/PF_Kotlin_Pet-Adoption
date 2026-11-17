<h1 align="center">

_Firebase: configuración paso a paso_

</h1>

Esta carpeta tiene todo lo que Firebase necesita fuera de la app: las **reglas de seguridad**, los **índices** de la base de datos, la **plantilla del correo** del código A2F y el **script que carga los datos de prueba**. Todo funciona con el plan gratuito, sin tarjeta.

```text
firebase/
├── firestore.rules          Quién puede leer y escribir cada colección
├── firestore.indexes.json   Índices que necesitan las consultas de la app
├── emailjs-template.html    Diseño del correo con el código de verificación
└── seed/                    Script para cargar los datos de prueba
    ├── seed.mjs             El script (Node.js + Firebase Admin SDK)
    ├── seed.json            Los datos (generados desde SeedData.kt)
    └── .env.example         Plantilla para las contraseñas de prueba
```

<br>

## 1. Consola de Firebase

1. **Authentication → Método de acceso:** activa *Correo electrónico/contraseña*.
2. **Authentication → Plantillas:** idioma *español* y nombre del remitente (por ejemplo, "Huellas"). Es el correo de "¿Olvidaste tu contraseña?".
3. **Firestore Database → Crear base de datos:** modo producción. La región no se puede cambiar después (sugerida: `southamerica-east1`).
4. Comprueba que `app/google-services.json` esté en su lugar (ver el [README principal](../README.md#poner-en-marcha-el-proyecto-desarrolladores)).

<br>

## 2. Reglas e índices

Se publican desde la **raíz del proyecto**, que ya trae `firebase.json` y `.firebaserc`:

```bash
npx firebase-tools login
```
```bash
npx firebase-tools deploy --only firestore
```

- Los índices tardan unos minutos en quedar listos (*Firestore → Índices*). Mientras tanto, algunas listas pueden salir vacías.
- `.firebaserc` apunta al proyecto `kog-maw-adoption`. Si usas tu propio proyecto, cambia ese nombre o agrega `--project tu-proyecto` al comando.

**Qué protegen las reglas, en pocas palabras:**
- Cada persona solo edita su perfil, sus publicaciones y sus comentarios.
- Los chats y las solicitudes de adopción solo los ven sus participantes.
- Nadie puede aprobar su propia publicación ni crearse un perfil de moderador.
- Los catálogos (categorías, tipos de animal, insignias) los lee cualquiera y solo los cambia un moderador.

<br>

## 3. Datos de prueba

Cargan 4 usuarios, 5 moderadores, 5 categorías (con su ícono de mapa), 14 publicaciones, comentarios, chats y notificaciones.

1. **Llave de administrador:** Consola → ⚙ *Configuración del proyecto* → *Cuentas de servicio* → **Generar nueva clave privada**. Guárdala como `firebase/seed/service-account.json`.
2. **Contraseñas:** copia `firebase/seed/.env.example` como `firebase/seed/.env` y escribe las contraseñas de las cuentas de prueba (mínimo 6 caracteres).
3. **Cargar** desde la raíz del proyecto:
   ```bash
   npm --prefix firebase/seed install
   ```
   ```bash
   npm --prefix firebase/seed run seed -- --reset
   ```
   `--reset` borra **todo** Firestore antes de cargar (también lo creado desde la app). Sin `--reset`, solo agrega o actualiza.

> 🔒 `service-account.json` y `.env` están en `.gitignore`. La llave de administrador da control total del proyecto: nunca la compartas.

**Cuentas que quedan creadas** (con las contraseñas de tu `.env`):

| Rol | Correos |
|---|---|
| Usuario | `n@gmail.com` (ana_77), `luis@correo.com`, `camila@correo.com`, `andres@correo.com` |
| Moderador | `mod@petadoption.com`, `mod2@petadoption.com` … `mod5@petadoption.com` |

**Para cambiar los datos:** edita `app/src/test/.../seed/SeedData.kt` y regenera `seed.json`:
```bash
./gradlew :app:testDebugUnitTest --tests "*SeedExportTest*"
```

**Moderadores nuevos:** créalos en *Authentication → Agregar usuario* y agrega a mano su documento `moderators/{uid}` con los mismos campos que `m1`. Por seguridad, la app nunca permite crear moderadores.

<br>

## 4. EmailJS (correo del código A2F)

1. En [emailjs.com](https://www.emailjs.com): **Email Services** → conecta tu Gmail → copia el *Service ID*.
2. **Email Templates** → nueva plantilla. En la pestaña de código pega `emailjs-template.html`; en *To Email* escribe `{{to_email}}`; en el asunto, `{{code}} es tu código de verificación`. Copia el *Template ID*.
3. **Account → General:** copia la *Public Key*.
4. **Account → Security:** activa **"Allow EmailJS API for non-browser applications"** (sin esto, EmailJS rechaza a la app). Si activas *Use Private Key*, copia también la *Private Key*.
5. Escribe los valores en `local.properties`:
   ```properties
   EMAILJS_SERVICE_ID=service_...
   EMAILJS_TEMPLATE_ID=template_...
   EMAILJS_PUBLIC_KEY=...
   EMAILJS_PRIVATE_KEY=...
   ```

Cada persona activa la verificación en dos pasos desde **Ajustes → Verificación en dos pasos**.

<br>

## Problemas comunes

| Síntoma | Causa probable |
|---|---|
| "No pudimos enviar el código de verificación" | Faltan llaves de EmailJS, no está activado *non-browser applications* o falta la *Private Key* en modo estricto. El motivo exacto sale en Logcat con el filtro `EmailJsCodeSender`. |
| Listas vacías o error *"requires an index"* | Los índices aún se están creando o no se publicaron. |
| *PERMISSION_DENIED* | Las reglas no se publicaron, o la acción no está permitida para ese rol. |
| "Correo ya utilizado" con un correo que no aparece en la app | Hay una cuenta en *Authentication* sin perfil en Firestore. Bórrala desde la consola. |
