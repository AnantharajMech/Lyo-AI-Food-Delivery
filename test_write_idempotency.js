const API_KEY = 'AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk';
const PACKAGE = 'com.lyo.fooddelivery';
const SHA1 = '1B59478F8C41118B7018DD48804265B639F12DE3';
const PROJECT_ID = 'lyo-ai-food-delivery';

function hashPassword(password) {
  const crypto = require('crypto');
  return crypto.createHash('sha256').update(password, 'utf8').digest('hex');
}

function toFirestoreJson(val) {
  if (val === null || val === undefined) return { nullValue: null };
  if (typeof val === 'string') return { stringValue: val };
  if (typeof val === 'number') {
    if (Number.isInteger(val)) return { integerValue: String(val) };
    return { doubleValue: val };
  }
  if (typeof val === 'boolean') return { booleanValue: val };
  if (Array.isArray(val)) {
    return { arrayValue: { values: val.map(toFirestoreJson) } };
  }
  if (typeof val === 'object') {
    const fields = {};
    for (const [k, v] of Object.entries(val)) {
      fields[k] = toFirestoreJson(v);
    }
    return { mapValue: { fields } };
  }
  return { nullValue: null };
}

function toDocumentJson(obj) {
  const fields = {};
  for (const [k, v] of Object.entries(obj)) {
    fields[k] = toFirestoreJson(v);
  }
  return { fields };
}

async function run() {
  const email = '9999900001@lyofoods.in';
  const urlAuth = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
  const hashedPass = hashPassword('123456');
  
  try {
    const resAuth = await fetch(urlAuth, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Android-Package': PACKAGE,
        'X-Android-Cert': SHA1
      },
      body: JSON.stringify({ email, password: hashedPass, returnSecureToken: true })
    });
    const dataAuth = await resAuth.json();
    if (!resAuth.ok) {
      console.log('Login failed:', dataAuth);
      return;
    }
    
    const token = dataAuth.idToken;
    const uid = dataAuth.localId;
    console.log(`Login Succeeded. UID: ${uid}`);
    
    const urlWrite = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/idempotency_keys/test_key_${uid}`;
    const body = toDocumentJson({
      uid: uid,
      timestamp: Date.now()
    });
    
    const resWrite = await fetch(urlWrite, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(body)
    });
    
    const textWrite = await resWrite.text();
    console.log(`Status of write: ${resWrite.status}`);
    console.log(`Response of write: ${textWrite}`);
  } catch (err) {
    console.error('Error:', err);
  }
}

run();
