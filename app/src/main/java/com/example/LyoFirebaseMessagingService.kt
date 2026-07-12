package com.example

import android.util.Log
import com.example.data.repository.LyoFirebaseHelper
import com.example.ui.screens.LyoNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LyoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        // If a user is currently logged in, update token in Firestore
        LyoFirebaseHelper.updateFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received FCM Message from: ${remoteMessage.from}")

        // 1. Check if message contains a notification payload.
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body

        // 2. Check if message contains a data payload (allows rich custom payloads).
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: $data")
            if (title == null) title = data["title"]
            if (body == null) body = data["body"]
        }

        if (title != null && body != null) {
            val targetScreen = data["screen"] ?: "CUSTOMER_DASHBOARD"
            val orderId = data["orderId"] ?: ""
            
            // Show push notification with deep link information
            showNotification(title, body, targetScreen, orderId)
        }
    }

    private fun showNotification(title: String, body: String, screen: String, orderId: String) {
        try {
            val context = applicationContext
            LyoNotificationHelper.createNotificationChannel(context)

            val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("screen", screen)
                if (orderId.isNotEmpty()) {
                    putExtra("order_id", orderId)
                }
            }

            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val largeIconBitmap = try {
                android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.mipmap.ic_launcher)
            } catch (e: Exception) {
                null
            }

            val builder = androidx.core.app.NotificationCompat.Builder(context, "lyo_push_notifications")
                .setSmallIcon(com.example.R.mipmap.ic_launcher)
                .apply {
                    if (largeIconBitmap != null) {
                        setLargeIcon(largeIconBitmap)
                    }
                }
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)

            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display FCM notification: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "LyoFcmService"
    }
}
