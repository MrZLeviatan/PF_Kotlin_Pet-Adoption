/**
Script de Node.js que carga los datos de arranque (semilla) en el proyecto de Firebase.
Lee firebase/seed/seed.json, que genera la prueba SeedExportTest a partir de los modelos de la app,
y usa el SDK de administración (firebase-admin) con una cuenta de servicio, por lo que no pasa por
las reglas de seguridad. Crea o actualiza las cuentas de Firebase Auth con el mismo uid que usan los
documentos y escribe todas las colecciones y subcolecciones de Firestore que luego lee la capa de datos.
**/
// Carga los datos de arranque en Firebase: cuentas de Auth (u1, u2, m1) y documentos de Firestore.
// Uso (desde firebase/seed):  npm install  y luego  npm run seed  (o  npm run seed -- --reset  para empezar de cero)
// Necesita la llave de cuenta de servicio en firebase/seed/service-account.json (nunca la subas a git).
// Se puede correr varias veces: actualiza lo que ya existe.

// Funciones de Node para leer archivos y comprobar si existen.
import { readFileSync, existsSync } from "node:fs";
// Inicialización del SDK de administración y credenciales de cuenta de servicio.
import { initializeApp, cert } from "firebase-admin/app";
// Cliente de administración de Firebase Authentication.
import { getAuth } from "firebase-admin/auth";
// Cliente de administración de Firestore.
import { getFirestore } from "firebase-admin/firestore";

// Ruta de la llave de la cuenta de servicio, relativa a este script.
const keyPath = new URL("./service-account.json", import.meta.url);
// Si la llave no existe, explica cómo obtenerla y termina con error.
if (!existsSync(keyPath)) {
  // Mensaje con los pasos para generar la llave en la consola de Firebase.
  console.error("Falta firebase/seed/service-account.json (Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada).");
  // Termina el proceso con código de error.
  process.exit(1);
}

// Inicializa Firebase Admin con la llave leída del archivo JSON.
initializeApp({ credential: cert(JSON.parse(readFileSync(keyPath, "utf8"))) });
// Obtiene el cliente de Authentication.
const auth = getAuth();
// Obtiene el cliente de Firestore.
const db = getFirestore();
// Lee y parsea el archivo de semilla generado por SeedExportTest.
const seed = JSON.parse(readFileSync(new URL("./seed.json", import.meta.url), "utf8"));

// Corre las fechas: los datos se ven como si se hubieran creado hace poco, no el día que se generó el archivo.
// Diferencia en milisegundos entre ahora y el momento en que se generó la semilla.
const shift = Date.now() - seed.generatedAt;
// Campos que contienen fechas en milisegundos y deben desplazarse.
const TIME_FIELDS = new Set(["createdAt", "updatedAt", "sentAt", "lastMessageAt", "reviewedAt", "finishedAt", "analyzedAt", "respondedAt", "deletedAt", "unlockedAt", "expiresAt"]);
// Campos que son mapas {uid: fecha} y cuyos valores deben desplazarse.
const TIME_MAPS = new Set(["lastReadAt", "deletedAt"]);

// Recorre un valor JSON de forma recursiva y desplaza todas sus fechas.
function shiftTimes(value) {
  // Si es un arreglo, procesa cada elemento.
  if (Array.isArray(value)) return value.map(shiftTimes);
  // Si es primitivo o null, lo devuelve sin cambios.
  if (value === null || typeof value !== "object") return value;
  // Objeto resultado.
  const out = {};
  // Recorre cada campo del objeto.
  for (const [key, v] of Object.entries(value)) {
    // Si es un campo de fecha numérico, le suma el desplazamiento.
    if (TIME_FIELDS.has(key) && typeof v === "number") out[key] = v + shift;
    // Si es un mapa de fechas, desplaza cada valor del mapa.
    else if (TIME_MAPS.has(key) && v && typeof v === "object") out[key] = Object.fromEntries(Object.entries(v).map(([k, t]) => [k, t + shift]));
    // En otro caso, procesa el valor de forma recursiva.
    else out[key] = shiftTimes(v);
  }
  // Devuelve el objeto con las fechas desplazadas.
  return out;
}

// 0. Con --reset (npm run seed -- --reset) se borra antes TODO lo de Firestore: datos viejos y lo creado desde la app.
// Solo si se pasó la opción --reset.
if (process.argv.includes("--reset")) {
  // Colecciones a vaciar: las de la semilla más las que solo crea la app.
  const names = [...Object.keys(seed.collections), "twoFactorChallenges", "deletionFeedback"];
  // Borra cada colección con todas sus subcolecciones.
  for (const name of names) await db.recursiveDelete(db.collection(name));
  // Informa que se vació Firestore.
  console.log("Firestore vaciado.");
}

// Contraseñas de las cuentas de prueba: en firebase/seed/.env (ignorado por git), nunca en el repositorio.
// Ruta del archivo .env junto a este script.
const envPath = new URL("./.env", import.meta.url);
// Si existe, lo lee y lo convierte en un objeto clave-valor; si no, usa un objeto vacío.
const env = existsSync(envPath)
  // Divide en líneas, quita espacios, ignora vacías y comentarios (#) y separa cada línea en clave=valor.
  ? Object.fromEntries(readFileSync(envPath, "utf8").split("\n").map(l => l.trim()).filter(l => l && !l.startsWith("#")).map(l => l.split("=").map(p => p.trim())))
  : {};
// Contraseña de prueba según el rol de la cuenta.
const PASSWORDS = { USER: env.SEED_USER_PASSWORD, MODERATOR: env.SEED_MODERATOR_PASSWORD };
// Valida que ambas contraseñas existan y tengan al menos 6 caracteres (mínimo de Firebase Auth).
if (!PASSWORDS.USER || !PASSWORDS.MODERATOR || PASSWORDS.USER.length < 6 || PASSWORDS.MODERATOR.length < 6) {
  // Explica qué falta y dónde ver el ejemplo.
  console.error("Falta firebase/seed/.env con SEED_USER_PASSWORD y SEED_MODERATOR_PASSWORD (mínimo 6 caracteres). Mira .env.example.");
  // Termina el proceso con código de error.
  process.exit(1);
}

// 1. Cuentas de Firebase Auth con uid fijo (los datos las referencian por ese id).
// Recorre cada cuenta definida en la semilla.
for (const { uid, email, role, displayName } of seed.auth) {
  // Elige la contraseña según el rol.
  const password = PASSWORDS[role];
  // Si el correo ya lo tiene otra cuenta (por ejemplo, creada a mano en la consola), choca con el uid fijo.
  // Busca si ya existe una cuenta con ese correo (null si no existe).
  const owner = await auth.getUserByEmail(email).catch(() => null);
  // Si existe pero con otro uid, hay conflicto.
  if (owner && owner.uid !== uid) {
    // Sin --reset no se borra nada: se informa el conflicto y se termina.
    if (!process.argv.includes("--reset")) {
      // Indica qué cuenta choca y cómo resolverlo.
      console.error(`El correo ${email} ya es de otra cuenta (uid ${owner.uid}). Bórrala en Authentication o corre con --reset.`);
      // Termina el proceso con código de error.
      process.exit(1);
    }
    // Con --reset, borra la cuenta en conflicto.
    await auth.deleteUser(owner.uid);
    // Informa la cuenta eliminada.
    console.log(`Cuenta repetida eliminada: ${email} (uid ${owner.uid})`);
  }
  // Intenta actualizar la cuenta existente con el uid fijo.
  try {
    // Actualiza correo, contraseña y nombre visible.
    await auth.updateUser(uid, { email, password, displayName });
    // Informa la actualización.
    console.log(`Cuenta actualizada: ${email}`);
  } catch (e) {
    // Cualquier error distinto de "usuario no encontrado" se propaga.
    if (e.code !== "auth/user-not-found") throw e;
    // Si la cuenta no existía, la crea con el uid fijo.
    await auth.createUser({ uid, email, password, displayName });
    // Informa la creación.
    console.log(`Cuenta creada: ${email}`);
  }
}

// 2. Documentos (un lote acepta hasta 500 escrituras).
// Escribe todos los documentos de una colección o subcolección (ruta) en lotes.
async function writeDocs(path, docs) {
  // Convierte el mapa {id: datos} en una lista de pares.
  const entries = Object.entries(docs);
  // Recorre la lista en bloques de 450 para no superar el límite de 500 escrituras por lote.
  for (let i = 0; i < entries.length; i += 450) {
    // Crea un lote de escritura.
    const batch = db.batch();
    // Agrega al lote cada documento con sus fechas desplazadas (set sobrescribe si ya existe).
    for (const [id, data] of entries.slice(i, i + 450)) batch.set(db.collection(path).doc(id), shiftTimes(data));
    // Confirma el lote.
    await batch.commit();
  }
  // Informa cuántos documentos se escribieron en la ruta.
  console.log(`${path}: ${entries.length} documentos`);
}

// Escribe cada colección de primer nivel.
for (const [name, docs] of Object.entries(seed.collections)) await writeDocs(name, docs);
// Escribe cada subcolección (comentarios y mensajes).
for (const { path, docs } of seed.subcollections) await writeDocs(path, docs);

// Mensaje final de éxito.
console.log("Listo. Datos de arranque cargados.");
