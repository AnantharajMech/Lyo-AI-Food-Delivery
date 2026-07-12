const API_KEY = 'AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk';
const PACKAGE = 'com.lyo.fooddelivery';
const SHA1 = '1B59478F8C41118B7018DD48804265B639F12DE3';

function hashPassword(password) {
  const crypto = require('crypto');
  return crypto.createHash('sha256').update(password, 'utf8').digest('hex');
}

async function run() {
  const email = '9999900001@lyofoods.in';
  const url = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
  const hashedPass = hashPassword('123456');
  
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
      console.log('Token successfully fetched!');
      const parts = data.idToken.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString('utf8'));
        console.log('Token Payload:', JSON.stringify(payload, null, 2));
      }
    } else {
      console.log('Login failed:', data);
    }
  } catch (err) {
    console.error('Error:', err);
  }
}

run();
