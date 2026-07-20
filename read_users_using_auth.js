const crypto = require('crypto');

const API_KEY = 'AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk';
const PROJECT_ID = 'lyo-ai-food-delivery';
const PACKAGE = 'com.lyo.fooddelivery';
const SHA1 = '1B59478F8C41118B7018DD48804265B639F12DE3';

function hashPassword(pass) {
  return crypto.createHash('sha256').update(pass).digest('hex');
}

async function authenticate(email, password) {
  const url = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
  const hashedPass = hashPassword(password);

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
    return data.idToken;
  } else {
    throw new Error(data.error ? data.error.message : JSON.stringify(data));
  }
}

async function fetchCollection(collection, token) {
  const url = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/${collection}?pageSize=100`;
  const res = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  if (!res.ok) {
    throw new Error(`Failed to fetch ${collection}: ${await res.text()}`);
  }
  return await res.json();
}

async function run() {
  try {
    console.log('Authenticating Admin...');
    const token = await authenticate('9999900005@lyofoods.in', '123456');
    console.log('Admin Authenticated successfully.');

    console.log('\nFetching Users collection...');
    const usersData = await fetchCollection('users', token);
    console.log('Users found:');
    if (usersData.documents) {
      usersData.documents.forEach(doc => {
        const fields = doc.fields || {};
        const name = fields.name ? fields.name.stringValue : 'N/A';
        const email = fields.email ? fields.email.stringValue : 'N/A';
        const phone = fields.phone ? fields.phone.stringValue : 'N/A';
        const role = fields.role ? fields.role.stringValue : 'N/A';
        const passwordHash = fields.passwordHash ? fields.passwordHash.stringValue : 'N/A';
        console.log(`- Name: "${name}", Email: "${email}", Phone: "${phone}", Role: "${role}", PassHash: "${passwordHash}"`);
      });
    } else {
      console.log('No user documents found.');
    }

    console.log('\nFetching Admins collection...');
    const adminsData = await fetchCollection('admins', token);
    console.log('Admins found:');
    if (adminsData.documents) {
      adminsData.documents.forEach(doc => {
        console.log(JSON.stringify(doc.fields, null, 2));
      });
    } else {
      console.log('No admin documents found.');
    }

  } catch (e) {
    console.error('Error:', e.message);
  }
}

run();
