const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Robust helper function to send push notifications securely using UID-based lookup.
 * This function retrieves all active device tokens for the given UID from
 * the secure path `users/{uid}/deviceTokens/{token}` and fires a multicast.
 * Dead or invalid tokens are pruned automatically upon failure.
 */
async function sendNotificationToUser(uid, title, body, data) {
  if (!uid) {
    console.warn("sendNotificationToUser called with empty UID.");
    return;
  }
  
  const db = admin.firestore();
  
  // 1. Retrieve all registered device tokens for this UID
  const tokensSnapshot = await db.collection("users")
    .doc(uid)
    .collection("deviceTokens")
    .get();
    
  if (tokensSnapshot.empty) {
    console.log(`No registered device tokens found for UID: ${uid}`);
    return;
  }
  
  const tokens = [];
  const tokenDocIds = [];
  tokensSnapshot.forEach(doc => {
    const tokenData = doc.data();
    if (tokenData && tokenData.token) {
      tokens.push(tokenData.token);
      tokenDocIds.push(doc.id);
    }
  });
  
  if (tokens.length === 0) return;
  
  // 2. Build the payload adhering strictly to secure push conventions (no sensitive fields)
  const message = {
    notification: {
      title: title,
      body: body
    },
    data: data || {},
    tokens: tokens
  };
  
  // 3. Dispatch multicast to all devices of this user
  try {
    const response = await admin.messaging().sendEachForMulticast(message);
    console.log(`Dispatched push to user ${uid}: ${response.successCount} success, ${response.failureCount} failure.`);
    
    // 4. Securely prune invalid/unregistered tokens
    if (response.failureCount > 0) {
      const batch = db.batch();
      let hasDeletes = false;
      
      response.responses.forEach((resp, idx) => {
        if (!resp.success) {
          const error = resp.error;
          if (error && (
              error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered'
          )) {
            console.log(`Pruning stale/unregistered token ${tokens[idx]} for UID ${uid}`);
            const tokenDocRef = db.collection("users")
              .doc(uid)
              .collection("deviceTokens")
              .doc(tokenDocIds[idx]);
            batch.delete(tokenDocRef);
            hasDeletes = true;
          }
        }
      });
      
      if (hasDeletes) {
        await batch.commit();
      }
    }
  } catch (error) {
    console.error(`Error sending multicast push to user ${uid}:`, error);
  }
}

/**
 * TRIGGER: Triggered when a new order is created in 'ek_orders' collection.
 * ACTION: Sends a secure notification to all system Administrators.
 */
exports.onOrderCreated = functions.firestore
  .document('ek_orders/{orderId}')
  .onCreate(async (snapshot, context) => {
    const orderId = context.params.orderId;
    const orderData = snapshot.data();
    if (!orderData) return;

    const totalAmount = orderData.totalAmount || 0;
    const title = "New Order Placed 🛍️";
    const body = `Order #${orderId} for ₹${totalAmount} is pending approval.`;
    
    const data = {
      screen: "ADMIN_DASHBOARD",
      orderId: String(orderId),
      type: "NEW_ORDER",
      status: "PENDING"
    };

    const db = admin.firestore();
    try {
      // Look up all admin UIDs securely from the '/admins' collection
      const adminsSnapshot = await db.collection("admins").get();
      const adminPromises = [];
      adminsSnapshot.forEach(doc => {
        const adminUid = doc.id;
        adminPromises.push(sendNotificationToUser(adminUid, title, body, data));
      });
      await Promise.all(adminPromises);
    } catch (err) {
      console.error(`Error processing order creation notification for #${orderId}:`, err);
    }
  });

/**
 * TRIGGER: Triggered when an order document in 'ek_orders' is updated.
 * ACTION: Sends a notification to the customer when status changes.
 */
exports.onOrderUpdated = functions.firestore
  .document('ek_orders/{orderId}')
  .onUpdate(async (change, context) => {
    const orderId = context.params.orderId;
    const beforeData = change.before.data();
    const afterData = change.after.data();
    
    if (!beforeData || !afterData) return;
    
    // Idempotent guard: only trigger if status actually transitions
    if (beforeData.status === afterData.status) {
      return;
    }

    const status = afterData.status;
    const customerUid = afterData.userId;
    if (!customerUid) return;

    let title = "Order Update 🛵";
    let body = `Your Order #${orderId} status is now ${status}.`;
    const screen = "CUSTOMER_DASHBOARD";

    switch (status) {
      case "ACCEPTED":
        title = "Order Accepted! ✅";
        body = `The restaurant has accepted Order #${orderId}. Preparing your delicious food now.`;
        break;
      case "PREPARING":
        title = "Food is Preparing 🍳";
        body = `Your order #${orderId} is being freshly prepared in the kitchen.`;
        break;
      case "READY_FOR_PICKUP":
        title = "Ready for Pick Up 📦";
        body = `Order #${orderId} is ready and waiting for the delivery rider.`;
        break;
      case "OUT_FOR_DELIVERY":
        title = "Out for Delivery! 🛵";
        body = `Your rider has picked up Order #${orderId} and is heading your way.`;
        break;
      case "DELIVERED":
        title = "Order Delivered! 🎉";
        body = `Order #${orderId} has been successfully delivered. Enjoy your meal!`;
        break;
      case "CANCELLED":
        title = "Order Cancelled ❌";
        body = `Order #${orderId} has been cancelled successfully.`;
        break;
    }

    const data = {
      screen: screen,
      orderId: String(orderId),
      type: "ORDER_STATUS_CHANGED",
      status: status
    };

    await sendNotificationToUser(customerUid, title, body, data);
  });

/**
 * TRIGGER: Triggered on write operations inside 'delivery_rides' collection.
 * ACTION: Handles assigning riders and completions safely.
 */
exports.onDeliveryRideCreatedOrUpdated = functions.firestore
  .document('delivery_rides/{rideId}')
  .onWrite(async (change, context) => {
    const rideId = context.params.rideId;
    const beforeExists = change.before.exists;
    const afterExists = change.after.exists;
    
    if (!afterExists) return; // Ignore deletions
    
    const beforeData = beforeExists ? change.before.data() : null;
    const afterData = change.after.data();
    if (!afterData) return;

    const riderUid = afterData.riderUid;
    const orderId = afterData.orderId;
    const status = afterData.status;
    
    if (!riderUid || !orderId) return;

    // Case A: New rider assigned
    if (!beforeData || beforeData.riderUid !== riderUid) {
      const title = "New Delivery Assigned! 🛵";
      const body = `You have been assigned to deliver Order #${orderId}. Check your dashboard.`;
      const data = {
        screen: "DELIVERY_DASHBOARD",
        orderId: String(orderId),
        type: "DELIVERY_ASSIGNED",
        status: status
      };
      await sendNotificationToUser(riderUid, title, body, data);
      return;
    }

    // Case B: Delivery ride status updated
    if (beforeData && beforeData.status !== status) {
      const db = admin.firestore();
      
      if (status === "COMPLETED") {
        const title = "Delivery Completed! 🏡";
        const body = `The rider has completed delivery for Order #${orderId}.`;
        
        const data = {
          screen: "CUSTOMER_DASHBOARD",
          orderId: String(orderId),
          type: "DELIVERY_COMPLETED",
          status: "DELIVERED"
        };
        
        // Notify customer
        const orderDoc = await db.collection("ek_orders").doc(String(orderId)).get();
        if (orderDoc.exists) {
          const orderData = orderDoc.data();
          if (orderData && orderData.userId) {
            await sendNotificationToUser(orderData.userId, title, body, data);
          }
        }
        
        // Notify all admins
        try {
          const adminsSnapshot = await db.collection("admins").get();
          const adminPromises = [];
          adminsSnapshot.forEach(doc => {
            adminPromises.push(sendNotificationToUser(doc.id, title, body, {
              screen: "ADMIN_DASHBOARD",
              orderId: String(orderId),
              type: "DELIVERY_COMPLETED",
              status: "DELIVERED"
            }));
          });
          await Promise.all(adminPromises);
        } catch (err) {
          console.error("Failed to notify admins of completed delivery:", err);
        }
      } else if (status === "OUT_FOR_DELIVERY") {
        const title = "Rider Heading Your Way 🛵";
        const body = `Rider picked up Order #${orderId} and is out for delivery.`;
        const data = {
          screen: "CUSTOMER_DASHBOARD",
          orderId: String(orderId),
          type: "OUT_FOR_DELIVERY",
          status: "OUT_FOR_DELIVERY"
        };
        
        const orderDoc = await db.collection("ek_orders").doc(String(orderId)).get();
        if (orderDoc.exists) {
          const orderData = orderDoc.data();
          if (orderData && orderData.userId) {
            await sendNotificationToUser(orderData.userId, title, body, data);
          }
        }
      }
    }
  });
