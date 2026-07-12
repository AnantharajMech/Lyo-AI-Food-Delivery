const admin = require('firebase-admin');

// Initialize the Admin SDK
admin.initializeApp({
  projectId: 'lyo-ai-food-delivery'
});

const db = admin.firestore();

async function run() {
  try {
    console.log('Fetching vendors via firebase-admin...');
    const snapshot = await db.collection('vendors').limit(2).get();
    console.log('Successfully fetched vendors. Count:', snapshot.size);
    snapshot.forEach(doc => {
      console.log(doc.id, '=>', doc.data());
    });
  } catch (err) {
    console.error('Error fetching vendors via admin:', err);
  }
}

run();
