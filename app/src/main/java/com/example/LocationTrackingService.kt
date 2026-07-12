package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.data.repository.LyoFirebaseHelper
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var lastLat = 0.0
    private var lastLng = 0.0
    private var rideId = 0L

    companion object {
        const val EXTRA_RIDE_ID = "ride_id"
        const val MIN_DISTANCE_METERS = 10f  // 10 meter limit
        const val UPDATE_INTERVAL_MS = 15000L // 15 seconds
        private const val NOTIFICATION_ID = 9182
        private const val CHANNEL_ID = "lyo_tracking_channel"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        rideId = intent?.getLongExtra(EXTRA_RIDE_ID, 0L) ?: 0L
        if (rideId == 0L) { stopSelf(); return START_NOT_STICKY }

        // Start Foreground Service with location type for API 34+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }

        startLocationUpdates()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lyo GPS Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Lyo system active GPS runner for live delivery updates"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lyo Delivery Live")
            .setContentText("அருகிலுள்ள வாடிக்கையாளருக்கு உங்களது நேரடி இருப்பிடம் பகிர்கிறது... (Active dynamic GPS updates)")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(UPDATE_INTERVAL_MS)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val lat = location.latitude
                    val lng = location.longitude

                    // Very minor change → skip Firestore push (battery save)
                    val latDiff = Math.abs(lat - lastLat)
                    val lngDiff = Math.abs(lng - lastLng)
                    if (latDiff < 0.00005 && lngDiff < 0.00005 && lastLat != 0.0) return

                    lastLat = lat
                    lastLng = lng

                    pushLocationToFirestore(lat, lng)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun pushLocationToFirestore(lat: Double, lng: Double) {
        if (rideId == 0L) return

        val firestore = LyoFirebaseHelper.firestore ?: return
        val updateMap = mapOf(
            "currentLat" to lat,
            "currentLng" to lng,
            "locationTimestamp" to System.currentTimeMillis()
        )

        firestore.collection("delivery_rides")
            .document(rideId.toString())
            .set(updateMap, SetOptions.merge())
            .addOnFailureListener { e ->
                android.util.Log.w("GPS", "Firestore push failed for rideId $rideId: ${e.message}")
            }
    }

    override fun onDestroy() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
