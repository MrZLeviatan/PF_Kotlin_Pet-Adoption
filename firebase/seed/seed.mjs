// Carga los datos de arranque en Firebase: cuentas de Auth (u1, u2, m1) y documentos de Firestore.
// Uso (desde firebase/seed):  npm install  y luego  npm run seed  (o  npm run seed -- --reset  para empezar de cero)
// Necesita la llave de cuenta de servicio en firebase/seed/service-account.json (nunca la subas a git).
// Se puede correr varias veces: actualiza lo que ya existe.

import { readFileSync, existsSync } from "node:fs";
import { initializeApp, cert } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";

const keyPath = new URL("./service-account.json", import.meta.url);
if (!existsSync(keyPath)) {
  console.error("Falta firebase/seed/service-account.json (Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada).");
  process.exit(1);
}

initializeApp({ credential: cert(JSON.parse(readFileSync(keyPath, "utf8"))) });
const auth = getAuth();
const db = getFirestore();
const seed = JSON.parse(readFileSync(new URL("./seed.json", import.meta.url), "utf8"));

// Corre las fechas: los datos se ven como si se hubieran creado hace poco, no el día que se generó el archivo.
const shift = Date.now() - seed.generatedAt;
const TIME_FIELDS = new Set(["createdAt", "updatedAt", "sentAt", "lastMessageAt", "reviewedAt", "finishedAt", "analyzedAt", "respondedAt", "deletedAt", "unlockedAt", "expiresAt"]);
const TIME_MAPS = new Set(["lastReadAt", "deletedAt"]);

function shiftTimes(value) {
  if (Array.isArray(value)) return value.map(shiftTimes);
  if (value === null || typeof value !== "object") return value;
  const out = {};
  for (const [key, v] of Object.entries(value)) {
    if (TIME_FIELDS.has(key) && typeof v === "number") out[key] = v + shift;
    else if (TIME_MAPS.has(key) && v && typeof v === "object") out[key] = Object.fromEntries(Object.entries(v).map(([k, t]) => [k, t + shift]));
    else out[key] = shiftTimes(v);
  }
  return out;
}

// 0. Con --reset (npm run seed -- --reset) se borra antes TODO lo de Firestore: datos viejos y lo creado desde la app.
if (process.argv.includes("--reset")) {
  const names = [...Object.keys(seed.collections), "twoFactorChallenges", "deletionFeedback"];
  for (const name of names) await db.recursiveDelete(db.collection(name));
  console.log("Firestore vaciado.");
}

// 1. Cuentas de Firebase Auth con uid fijo (los datos las referencian por ese id).
for (const { uid, email, password, displayName } of seed.auth) {
  try {
    await auth.updateUser(uid, { email, password, displayName });
    console.log(`Cuenta actualizada: ${email}`);
  } catch (e) {
    if (e.code !== "auth/user-not-found") throw e;
    await auth.createUser({ uid, email, password, displayName });
    console.log(`Cuenta creada: ${email}`);
  }
}

// 2. Documentos (un lote acepta hasta 500 escrituras).
async function writeDocs(path, docs) {
  const entries = Object.entries(docs);
  for (let i = 0; i < entries.length; i += 450) {
    const batch = db.batch();
    for (const [id, data] of entries.slice(i, i + 450)) batch.set(db.collection(path).doc(id), shiftTimes(data));
    await batch.commit();
  }
  console.log(`${path}: ${entries.length} documentos`);
}

for (const [name, docs] of Object.entries(seed.collections)) await writeDocs(name, docs);
for (const { path, docs } of seed.subcollections) await writeDocs(path, docs);

console.log("Listo. Datos de arranque cargados.");
