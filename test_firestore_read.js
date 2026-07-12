async function run() {
  const url = 'https://firestore.googleapis.com/v1/projects/lyo-ai-food-delivery/databases/(default)/documents/users';
  try {
    const res = await fetch(url);
    const data = await res.json();
    console.log('Users list:', JSON.stringify(data, null, 2));
    
    const resAdmins = await fetch('https://firestore.googleapis.com/v1/projects/lyo-ai-food-delivery/databases/(default)/documents/admins');
    const dataAdmins = await resAdmins.json();
    console.log('Admins list:', JSON.stringify(dataAdmins, null, 2));
  } catch (e) {
    console.error('Error fetching:', e);
  }
}
run();
