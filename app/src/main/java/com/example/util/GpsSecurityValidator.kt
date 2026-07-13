package com.example.util

import android.location.Location
import android.os.Build
import android.util.Log

object GpsSecurityValidator {
    private const val TAG = "GpsSecurityValidator"
    private const val MAX_SPEED_MPS = 42.0 // Approx 150 km/h - impossible speed for a delivery vehicle in local streets
    private const val MAX_TIME_DIFF_MS = 60000L // 1 minute max age of GPS location

    // Keep state of the last validated coordinates per ride to check for impossible jumps
    private val lastPositions = java.util.concurrent.ConcurrentHashMap<Long, PositionState>()

    data class PositionState(
        val lat: Double,
        val lng: Double,
        val timestamp: Long
    )

    /**
     * Validates a GPS location update thoroughly.
     * Returns true if the location is safe and valid, false if spoofed or invalid.
     */
    fun validateLocation(rideId: Long, location: Location): Boolean {
        val now = System.currentTimeMillis()

        // 1. Invalid Coordinate Range Rejection
        val lat = location.latitude
        val lng = location.longitude
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            Log.e(TAG, "Rejected location: Coordinate out of bounds (Lat: $lat, Lng: $lng)")
            return false
        }
        if (lat == 0.0 && lng == 0.0) {
            Log.e(TAG, "Rejected location: Invalid default coordinate (0.0, 0.0)")
            return false
        }

        // 2. Fake GPS / Spoofing / Mock Location Detection
        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
        if (isMock) {
            Log.e(TAG, "Rejected spoofed location: Fake/Mock GPS detected on ride $rideId! (Lat: $lat, Lng: $lng)")
            return false
        }

        // Check mock location provider flags in extras
        val extras = location.extras
        if (extras != null && extras.getBoolean("mockLocation", false)) {
            Log.e(TAG, "Rejected spoofed location: Mock location provider extra detected on ride $rideId!")
            return false
        }

        // 3. Timestamp Validation
        val locationTime = location.time
        val ageMs = Math.abs(now - locationTime)
        if (ageMs > MAX_TIME_DIFF_MS) {
            Log.w(TAG, "Rejected location: Stale location coordinate ($ageMs ms old)")
            return false
        }
        if (locationTime > now + 5000L) { // Reject coordinates from the future
            Log.e(TAG, "Rejected location: GPS timestamp is in the future relative to system clock")
            return false
        }

        // 4. Sudden Impossible Jumps (Velocity Filter)
        val lastState = lastPositions[rideId]
        if (lastState != null) {
            val timeElapsedSec = (now - lastState.timestamp) / 1000.0
            if (timeElapsedSec > 0.1) {
                val results = FloatArray(1)
                Location.distanceBetween(lastState.lat, lastState.lng, lat, lng, results)
                val distanceMeters = results[0]
                val speedMps = distanceMeters / timeElapsedSec

                if (speedMps > MAX_SPEED_MPS && distanceMeters > 50.0) {
                    Log.e(TAG, "Rejected impossible jump on ride $rideId: Jump of $distanceMeters meters in $timeElapsedSec seconds! (Speed: ${speedMps * 3.6} km/h)")
                    return false
                }
            }
        }

        // Update the last known valid position state
        lastPositions[rideId] = PositionState(lat, lng, now)
        return true
    }

    /**
     * Clears tracked GPS positions for a specific ride (e.g. when completed or logged out)
     */
    fun clearRideState(rideId: Long) {
        lastPositions.remove(rideId)
    }

    fun clearAll() {
        lastPositions.clear()
    }
}
