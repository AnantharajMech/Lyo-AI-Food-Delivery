const API_KEY = 'AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk';
const SHA1 = '1B59478F8C41118B7018DD48804265B639F12DE3';
const PACKAGE = 'com.lyo.fooddelivery';

function hashPassword(password) {
  const crypto = require('crypto');
  return crypto.createHash('sha256').update(password, 'utf8').digest('hex');
}

async function tryLogin(email, password) {
  // Try both endpoints, prioritizing the local Auth Emulator
  const endpoints = [
    `http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=any`,
    `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`,
    `https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=${API_KEY}`
  ];

  for (const url of endpoints) {
    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Android-Package': PACKAGE,
          'X-Android-Cert': SHA1
        },
        body: JSON.stringify({ email, password, returnSecureToken: true })
      });
      const data = await res.json();
      if (res.ok) {
        console.log(`✅ SUCCESS for ${email} / ${password} on URL: ${url.substring(0, 50)}...: ID Token: ${data.idToken.substring(0, 20)}...`);
        return data;
      } else {
        console.log(`❌ FAILED for ${email} / ${password} on URL: ${url.substring(0, 50)}...:`, data.error ? data.error.message : JSON.stringify(data));
      }
    } catch (err) {
      console.error(`Error for ${email} on ${url}:`, err.message);
    }
  }
  return null;
}

async function trySignUp(email, password) {
  try {
    const res = await fetch(`http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signUp?key=any`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, returnSecureToken: true })
    });
    if (res.ok) {
      console.log(`👤 REGISTERED ${email} with password ${password} in Auth Emulator.`);
      return true;
    }
  } catch (e) {
    // ignore
  }
  return false;
}

async function run() {
  const p1 = '123456';
  const h1 = hashPassword(p1);
  const p2 = '1234';
  const h2 = hashPassword(p2);

  const emails = [
    'superadmin@lyofresh.in',
    'superadmin@lyofoods.in',
    'anantharajmech@lyofoods.in',
    'anantharajmech@lyofresh.in'
  ];

  for (const email of emails) {
    for (const p of [p1, h1, p2, h2]) {
      await trySignUp(email, p);
      await tryLogin(email, p);
    }
  }
}

run();
