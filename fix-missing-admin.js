const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

// Accept UID as a command-line argument
const uid = process.argv[2];

if (!uid) {
  console.error('❌ Error: Please provide a UID as a command-line argument.');
  console.error('Usage: node fix-missing-admin.js <UID>');
  process.exit(1);
}

// 1. Resolve path to serviceAccountKey.json and verify it exists
const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');

if (!fs.existsSync(serviceAccountPath)) {
  console.error('❌ Error: serviceAccountKey.json not found in the same folder.');
  console.error(`Expected location: ${serviceAccountPath}`);
  process.exit(1);
}

try {
  // Initialize firebase-admin
  const serviceAccount = require(serviceAccountPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  console.log('🔄 Firebase Admin SDK initialized successfully.');
} catch (error) {
  console.error('❌ Error: Failed to initialize Firebase Admin with serviceAccountKey.json:', error.message);
  process.exit(1);
}

async function run() {
  try {
    console.log(`🔄 Fetching Firebase Auth record for UID: ${uid}...`);
    const userRecord = await admin.auth().getUser(uid);
    
    // Print user details for visual confirmation
    console.log('\n--- User Record Found ---');
    console.log(`UID:   ${userRecord.uid}`);
    console.log(`Email: ${userRecord.email || 'None'}`);
    console.log(`Phone: ${userRecord.phoneNumber || 'None'}`);
    console.log('-------------------------\n');

    const db = admin.firestore();
    
    // Prepare document data
    const docData = {
      uid: userRecord.uid,
      email: userRecord.email || '',
      phone: userRecord.phoneNumber || '',
      role: 'ADMIN',
      name: 'Ananth Einstein',
      isActive: true,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    console.log(`🔄 Creating/overwriting document at users/${uid}...`);
    await db.collection('users').document(uid).set(docData);
    
    console.log('✅ Success! Document successfully written to Firestore.');
    console.log('--- Document Content Written ---');
    console.log(JSON.stringify({
      ...docData,
      createdAt: '[Firestore Server Timestamp]',
      updatedAt: '[Firestore Server Timestamp]'
    }, null, 2));
    console.log('--------------------------------');
  } catch (error) {
    if (error.code === 'auth/user-not-found') {
      console.error(`❌ Error: No user found in Firebase Auth with UID: ${uid}`);
    } else {
      console.error('❌ Error occurred during execution:', error.message);
    }
    process.exit(1);
  }
}

run();
