package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "users", indices = [Index(value = ["role"])])
data class User(
    @PrimaryKey val phone: String, // 10-digit phone number as ID
    val name: String,
    val email: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val isWhatsAppOptIn: Boolean,
    val role: String = "CUSTOMER", // CUSTOMER, ADMIN, DELIVERY
    val vehicleNo: String = "",
    val isActiveRider: Boolean = true,
    val salaryType: String = "MONTHLY", // "MONTHLY" or "PER_KM"
    val salaryRate: Double = 0.0
)

@Entity(tableName = "vendors")
data class Vendor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nameTa: String = "",
    val type: String, // Restaurant, Cafe, Hotel, Bakery, Snack Shop
    val rating: Double,
    val distance: Double, // in km
    val deliveryTime: Int, // in minutes
    val deliveryFee: Double, // in INR
    val address: String,
    val lat: Double,
    val lng: Double,
    val bannerUrl: String,
    val freeDeliveryThreshold: Double = 500.0,
    val minOrderAmount: Double = 100.0,
    val isCouponEnabled: Boolean = true,
    val couponCode: String = "LYOFRESH",
    val couponDiscount: Double = 80.0,
    val couponMinOrder: Double = 300.0,
    val isOnHoliday: Boolean = false,
    val phone: String = "",
    val visibilityRadiusKm: Double = 15.0,
    val sortOrder: Int = 0,
    val isDynamicDelivery: Boolean = false,
    val autoOpenTime: String = "",
    val autoCloseTime: String = ""
)

@Entity(tableName = "categories", indices = [Index(value = ["vendorId"])])
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long,
    val nameEn: String, // e.g. "Main Course"
    val nameTa: String, // e.g. "முக்கிய உணவு"
    val sortOrder: Int = 0,
    val isHidden: Boolean = false,
    val autoOpenTime: String = "",
    val autoCloseTime: String = ""
)

@Entity(tableName = "menu_items", indices = [Index(value = ["vendorId"]), Index(value = ["categoryId"])])
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long,
    val categoryId: Long,
    val nameEn: String, // English (primary visual name) e.g., "Garlic Rotti"
    val nameTa: String, // Tamil (subtext localization name) e.g., "கார்லிக் ரொட்டி"
    val descEn: String,
    val descTa: String,
    val price: Double,
    val isVeg: Boolean,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val autoOpenTime: String = "",
    val autoCloseTime: String = ""
)

@Entity(tableName = "orders", indices = [Index(value = ["userId"]), Index(value = ["vendorId"]), Index(value = ["status"])])
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val vendorId: Long,
    val vendorName: String,
    val status: String, // "PENDING", "ACCEPTED", "PREPARING", "READY_FOR_PICKUP", "OUT_FOR_DELIVERY", "DELIVERED"
    val totalAmount: Double,
    val subtotal: Double,
    val deliveryFee: Double,
    val couponDiscount: Double,
    val tipAmount: Double,
    val itemsCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val otpCode: String = "1234",
    val customerLat: Double,
    val customerLng: Double,
    val redeemedPoints: Int = 0,
    val isPendingSync: Boolean = false
)

@Entity(tableName = "order_items", indices = [Index(value = ["orderId"])])
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val menuItemId: Long,
    val nameEn: String,
    val nameTa: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "delivery_rides", indices = [Index(value = ["orderId"])])
data class DeliveryRide(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val riderName: String,
    val riderPhone: String,
    val status: String, // "ACCEPTED", "PICKING_UP", "DELIVERING", "COMPLETED"
    val currentLat: Double,
    val currentLng: Double,
    val totalDistance: Double,
    val earnings: Double,
    val otpVerified: Boolean = false
)

@Entity(tableName = "promo_banners")
data class PromoBanner(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val description: String,
    val imageUrl: String = ""
)

@Entity(tableName = "saved_addresses", indices = [Index(value = ["userId"])])
data class SavedAddress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String, // e.g., "Home", "Work", "Other"
    val addressLine: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "saved_payment_methods", indices = [Index(value = ["userId"])])
data class SavedPaymentMethod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val cardType: String, // e.g., "Visa", "Mastercard", "Rupay", "UPI"
    val displayName: String, // e.g., "xxxx xxxx xxxx 1234", "muthu@okhdfc"
    val expiryDate: String = "",
    val holderName: String = ""
)

@Entity(tableName = "reviews", indices = [Index(value = ["vendorId"])])
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long,
    val userName: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderMessage(
    val senderId: String = "",
    val senderRole: String = "", // "RIDER" or "CUSTOMER"
    val text: String = "",
    val timestamp: Long = 0L
)

fun isTimeWithinInterval(start: String, end: String): Boolean {
    val startClean = start.trim().uppercase()
    val endClean = end.trim().uppercase()
    if (startClean.isBlank() || endClean.isBlank()) return true
    
    fun parseToMinutes(timeStr: String): Int? {
        val clean = timeStr.trim().uppercase()
        val pm = clean.contains("PM")
        val am = clean.contains("AM")
        val stripped = clean.replace("AM", "").replace("PM", "").trim()
        val delimiters = listOf(":", ".", " ")
        var hours = -1
        var minutes = -1
        for (d in delimiters) {
            if (stripped.contains(d)) {
                val parts = stripped.split(d)
                if (parts.size >= 2) {
                    hours = parts[0].toIntOrNull() ?: -1
                    minutes = parts[1].toIntOrNull() ?: -1
                    break
                }
            }
        }
        if (hours == -1 && minutes == -1) {
            val rawNum = stripped.toIntOrNull()
            if (rawNum != null && rawNum in 0..24) {
                hours = rawNum
                minutes = 0
            }
        }
        if (hours == -1) return null
        if (minutes == -1) minutes = 0
        
        if (am || pm) {
            if (hours == 12) {
                hours = if (pm) 12 else 0
            } else if (pm) {
                hours += 12
            }
        }
        return hours * 60 + minutes
    }
    
    try {
        val startMin = parseToMinutes(startClean) ?: return true
        val endMin = parseToMinutes(endClean) ?: return true
        val cal = java.util.Calendar.getInstance()
        val nowMin = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        
        return if (endMin > startMin) {
            nowMin >= startMin && nowMin <= endMin
        } else {
            nowMin >= startMin || nowMin <= endMin
        }
    } catch (e: Exception) {
        return true
    }
}

val Vendor.isCurrentlyOpen: Boolean
    get() {
        if (isOnHoliday) return false
        if (autoOpenTime.isNotBlank() && autoCloseTime.isNotBlank()) {
            return isTimeWithinInterval(autoOpenTime, autoCloseTime)
        }
        return true
    }

val Category.isCurrentlyVisible: Boolean
    get() {
        if (isHidden) return false
        if (autoOpenTime.isNotBlank() && autoCloseTime.isNotBlank()) {
            return isTimeWithinInterval(autoOpenTime, autoCloseTime)
        }
        return true
    }

val MenuItem.isCurrentlyAvailable: Boolean
    get() {
        if (!isAvailable) return false
        if (autoOpenTime.isNotBlank() && autoCloseTime.isNotBlank()) {
            return isTimeWithinInterval(autoOpenTime, autoCloseTime)
        }
        return true
    }

val MenuItem.isTrulyVeg: Boolean
    get() {
        val nameLower = (nameEn + " " + nameTa + " " + descEn + " " + descTa).lowercase()
        if (nameLower.contains("chicken") || nameLower.contains("mutton") || nameLower.contains("fish") || 
            nameLower.contains("egg") || nameLower.contains("prawn") || nameLower.contains("beef") || 
            nameLower.contains("meat") || nameLower.contains("non-veg") || nameLower.contains("n-veg") ||
            nameLower.contains("சிக்கன்") || nameLower.contains("முட்டை") || nameLower.contains("மீன்") ||
            nameLower.contains("இறால்") || nameLower.contains("ஆட்டுக்கறி") || nameLower.contains("மட்டன்") ||
            nameLower.contains("கறி") || nameLower.contains("பன்றிக்கறி") || nameLower.contains("non veg") || 
            nameLower.contains("nonveg") || nameLower.contains("boti") || nameLower.contains("குடல்") || 
            nameLower.contains("தலைக்கறி")
        ) {
            return false
        }
        return isVeg
    }

object LyoDeliveryPricingEngine {
    fun calculateDeliveryFee(
        distanceKm: Double,
        subtotal: Double,
        isDynamicDelivery: Boolean,
        baseDeliveryFee: Double,
        baseDistanceKm: Double = 3.0,
        pricePerAdditionalKm: Double = 15.0,
        minDeliveryCharge: Double = 25.0,
        maxDeliveryCharge: Double = 150.0,
        freeDeliveryThreshold: Double = 500.0,
        maxDeliveryRadiusKm: Double = 15.0,
        surgeMultiplier: Double = 1.0
    ): Double {
        if (distanceKm > maxDeliveryRadiusKm) {
            // Cap or filter out of bounds
        }
        // Free delivery condition
        if (subtotal >= freeDeliveryThreshold && distanceKm <= 8.0) {
            return 0.0
        }
        val computedFee = if (isDynamicDelivery) {
            val extraDist = if (distanceKm > baseDistanceKm) distanceKm - baseDistanceKm else 0.0
            baseDeliveryFee + (extraDist * pricePerAdditionalKm)
        } else {
            baseDeliveryFee
        }
        var finalFee = computedFee * surgeMultiplier
        if (finalFee < minDeliveryCharge) finalFee = minDeliveryCharge
        if (finalFee > maxDeliveryCharge) finalFee = maxDeliveryCharge
        
        return Math.round(finalFee).toDouble()
    }
}


