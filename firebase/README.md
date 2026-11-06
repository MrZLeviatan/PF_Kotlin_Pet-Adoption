# Firebase: puesta en marcha

La app usa **Firebase Authentication** (correo y contraseña), **Firestore** (base de datos) y
**EmailJS** (correo con el código A2F). Todo funciona con el plan gratuito, sin tarjeta.

## 1. Consola de Firebase (proyecto `kog-maw-adoption`)

1. **Authentication → Método de acceso**: activa *Correo electrónico/contraseña*.
2. **Authentication → Templates**: idioma *español*; nombre del remitente, por ejemplo "Huellas".
   Es el correo de "¿Olvidaste tu contraseña?".
3. **Firestore Database → Crear base de datos**: modo producción.
4. `app/google-services.json` ya está en su lugar.

## 2. Reglas e índices de Firestore

Desde la raíz del proyecto (usa `firebase.json` y `.firebaserc`):

```
npx firebase-tools login
npx firebase-tools deploy --only firestore
```

Publica `firebase/firestore.rules` y `firebase/firestore.indexes.json`. Los índices tardan unos minutos en quedar listos.

## 3. Datos de arranque (4 usuarios, 5 moderadores, 5 categorías, 14 publicaciones, chats…)

1. Consola → ⚙ Configuración del proyecto → *Cuentas de servicio* → **Generar nueva clave privada**.
   Guárdala como `firebase/seed/service-account.json` (está en `.gitignore`: nunca la subas).
2. Copia `seed/.env` como `firebase/seed/.env` y elige las contraseñas de las cuentas de prueba
   (el `.env` está en `.gitignore`; el repositorio es público, así que nunca van en el código).
3. En `firebase/seed`: `npm install` y luego `npm run seed`.
   Para empezar de cero (borra **todo** Firestore, también lo creado desde la app): `npm run seed -- --reset`.

Cuentas de prueba (contraseñas en tu `firebase/seed/.env`):
- Usuarios: `n@gmail.com` (ana_77), `luis@correo.com`, `camila@correo.com`, `andres@correo.com`.
- Moderadores: `mod@petadoption.com`, `mod2@petadoption.com` … `mod5@petadoption.com`.

Si cambias `SeedData.kt`, regenera `seed.json` con
`./gradlew :app:testDebugUnitTest --tests "*SeedExportTest*"`.

**Moderadores nuevos:** créalos en *Authentication → Agregar usuario* y agrega a mano el documento
`moderators/{uid}` (mismos campos que `m1`). La app nunca deja crear moderadores.

## 4. EmailJS (código A2F)

1. Crea una cuenta en emailjs.com → **Email Services** → conecta tu Gmail → copia el *Service ID*.
2. **Email Templates** → nueva plantilla con el contenido de `firebase/emailjs-template.html`.
   En *To Email* pon `{{to_email}}`. Copia el *Template ID*.
3. **Account → General**: copia la *Public Key*.
4. **Account → Security**: activa **"Allow EmailJS API for non-browser applications"** (sin esto EmailJS rechaza la app).
   Si además activas "Use Private Key", copia la *Private Key*.
5. En `local.properties`:

```
EMAILJS_SERVICE_ID=service_xxx
EMAILJS_TEMPLATE_ID=template_xxx
EMAILJS_PUBLIC_KEY=xxxxxxxx
EMAILJS_PRIVATE_KEY=        # opcional
```

La A2F se activa por cuenta en *Ajustes → Verificación en dos pasos*.
