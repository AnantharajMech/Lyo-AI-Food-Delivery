const crypto = require('crypto');

const API_KEY = 'AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk';
const PACKAGE = 'com.lyo.fooddelivery';
const SHA1 = '1B59478F8C41118B7018DD48804265B639F12DE3';

function hashPassword(pass) {
  return crypto.createHash('sha256').update(pass).digest('hex');
}

async function run() {
  const email = '9999900001@lyofoods.in';
  const hashedPass = hashPassword('123456');

  // We want to test if we can authenticate on lyo-food-delivery
  // To do that, let's call identitytoolkit with the API key.
  // Wait, does the Auth system return lyo-food-delivery or lyo-ai-food-delivery?
  // Let's call signUp and signIn and decode the returned token to see its audience!
  const url = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
  
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
      console.log('Authentication Successful!');
      const parts = data.idToken.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString('utf8'));
        console.log('Audience / Project ID of Auth:', payload.aud);
      }
    } else {
      console.log('Authentication Failed:', data);
    }
  } catch (err) {
    console.error('Error:', err);
  }
}

run();
