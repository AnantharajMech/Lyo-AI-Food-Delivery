async function run() {
  const url = 'https://firestore.googleapis.com/v1/projects/lyo-food-delivery/databases/(default)/documents/vendors/non_existent_id';
  try {
    const res = await fetch(url);
    const text = await res.text();
    console.log(`Status: ${res.status}`);
    console.log(`Response: ${text}`);
  } catch (e) {
    console.error('Error fetching:', e);
  }
}
run();
