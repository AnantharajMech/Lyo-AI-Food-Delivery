package com.example.util

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object LyoMapHelper {

    /**
     * Opens Google Maps (or browser fallback) centered at the given latitude & longitude.
     */
    fun openLocationOnMap(
        context: Context,
        lat: Double,
        lng: Double,
        label: String = "Location"
    ) {
        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(context, "Invalid coordinates (0.0, 0.0)", Toast.LENGTH_SHORT).show()
            return
        }

        val geoUriStr = "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})"
        val geoUri = Uri.parse(geoUriStr)

        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback 1: Any map provider without package restriction
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW, geoUri)
                context.startActivity(genericIntent)
            } catch (ex: Exception) {
                // Fallback 2: Direct Google Maps Web URL in browser
                try {
                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                    val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                    context.startActivity(webIntent)
                } catch (e3: Exception) {
                    Toast.makeText(context, "Could not open map application", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Launches Turn-by-Turn Google Maps Navigation to destination coordinates.
     */
    fun openNavigationOnMap(
        context: Context,
        destLat: Double,
        destLng: Double,
        destName: String = "Destination",
        startLat: Double? = null,
        startLng: Double? = null
    ) {
        if (destLat == 0.0 && destLng == 0.0) {
            Toast.makeText(context, "Invalid destination coordinates", Toast.LENGTH_SHORT).show()
            return
        }

        val navUriStr = "google.navigation:q=$destLat,$destLng"
        val navUri = Uri.parse(navUriStr)

        val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            context.startActivity(navIntent)
        } catch (e: Exception) {
            // Fallback: Google Maps web direction URL
            try {
                val originParam = if (startLat != null && startLng != null && startLat != 0.0 && startLng != 0.0) {
                    "&origin=$startLat,$startLng"
                } else ""
                val dirUrl = "https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng$originParam"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(dirUrl))
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not launch map navigation", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Synchronous / helper reverse geocoding via Android Geocoder or fallback label.
     */
    fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
        if (lat == 0.0 && lng == 0.0) return "Unknown Location"
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: ""
                val city = addr.locality ?: addr.subAdminArea ?: ""
                if (street.isNotEmpty()) "$street, $city".trim(',', ' ') else addr.getAddressLine(0) ?: "GPS: $lat, $lng"
            } else {
                String.format(Locale.US, "GPS: %.5f, %.5f", lat, lng)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "GPS: %.5f, %.5f", lat, lng)
        }
    }
}
