package com.example.data.ai

import android.util.Log
import com.example.data.database.Vendor
import com.example.data.database.MenuItem
import com.example.data.database.Order
import java.util.Calendar

data class AIRecommendation(
    val vendor: Vendor,
    val aiScore: Int, // 0 - 100
    val reasons: List<String>, // Explainable AI reasons in Spoken Tamil & English
    val matchedItem: MenuItem? = null
)

object RecommendationEngine {
    private const val TAG = "RecommendationEngine"

    /**
     * Calculates the dynamic AI Recommendation Score (0-100) for all available vendors
     * based on customer context, order history, time of day, and live platform parameters.
     */
    fun calculateRecommendations(
        vendors: List<Vendor>,
        menuItems: List<MenuItem>,
        pastOrders: List<Order>,
        activeRidersCount: Int,
        currentLat: Double,
        currentLng: Double,
        searchQuery: String = "",
        weatherState: String = "CLEAR" // CLEAR, RAINY, COLD, HOT
    ): List<AIRecommendation> {
        val startTime = System.currentTimeMillis()
        
        // 1. Analyze User History to detect preferences
        val userOrderCount = pastOrders.size
        val vendorOrderCounts = pastOrders.groupBy { it.vendorId }.mapValues { it.value.size }
        val favoriteVendorIds = vendorOrderCounts.filter { it.value >= 2 }.keys
        
        // Extract favorite food keywords/categories from past orders
        val pastOrderedItemsKeywords = mutableSetOf<String>()
        pastOrders.forEach { order ->
            // Try extracting from vendor names or type
            val lowerVendorName = order.vendorName.lowercase()
            if (lowerVendorName.contains("biryani") || lowerVendorName.contains("பிரியாணி")) {
                pastOrderedItemsKeywords.add("biryani")
            }
            if (lowerVendorName.contains("veg") || lowerVendorName.contains("சைவம்")) {
                pastOrderedItemsKeywords.add("veg")
            }
        }
        
        // Analyze customer budget (average order amount)
        val avgOrderAmount = if (userOrderCount > 0) {
            pastOrders.map { it.totalAmount }.average()
        } else {
            250.0 // Default reasonable middle-ground budget
        }

        // 2. Identify current time & day context
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        
        val isBreakfastTime = currentHour in 6..11
        val isLunchTime = currentHour in 12..15
        val isDinnerTime = currentHour in 18..22
        val isLateNight = currentHour in 22..24 || currentHour in 0..3

        val recommendations = vendors.mapNotNull { vendor ->
            // --- STRICT FILTERING FOR HOLIDAY OR INACTIVE STATUS ---
            if (vendor.isOnHoliday || vendor.status == "CLOSED" || vendor.status == "INACTIVE") {
                return@mapNotNull null
            }

            // Check auto open/close times if present
            if (vendor.autoOpenTime.isNotBlank() && vendor.autoCloseTime.isNotBlank()) {
                try {
                    val openParts = vendor.autoOpenTime.split(":")
                    val closeParts = vendor.autoCloseTime.split(":")
                    if (openParts.size == 2 && closeParts.size == 2) {
                        val openMin = openParts[0].toInt() * 60 + openParts[1].toInt()
                        val closeMin = closeParts[0].toInt() * 60 + closeParts[1].toInt()
                        val currentMin = currentHour * 60 + calendar.get(Calendar.MINUTE)
                        if (currentMin < openMin || currentMin > closeMin) {
                            return@mapNotNull null // Closed at this hour
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking auto open/close for ${vendor.name}: ${e.message}")
                }
            }

            // Calculate precise live coordinates distance if available (otherwise fallback to database distance)
            val calculatedDistance = if (currentLat != 0.0 && currentLng != 0.0 && vendor.lat != 0.0 && vendor.lng != 0.0) {
                calculateDistance(currentLat, currentLng, vendor.lat, vendor.lng)
            } else {
                vendor.distance
            }

            // Skip if vendor is beyond its visibility radius
            if (calculatedDistance > vendor.visibilityRadiusKm) {
                return@mapNotNull null
            }

            // Start base score calculation (max 100)
            var score = 50 // Middle point baseline
            val reasons = mutableListOf<String>()

            // A. Restaurant Rating (Max +20 points)
            val ratingScore = (vendor.rating * 4.0).toInt().coerceIn(0, 20)
            score += ratingScore
            if (vendor.rating >= 4.5) {
                reasons.add("⭐ Superb ${vendor.rating} Rating (மிகச்சிறந்த தரம்)")
            } else if (vendor.rating >= 4.0) {
                reasons.add("⭐ Highly Rated (${vendor.rating} ஸ்டார்)")
            }

            // B. Road Distance Penalty (Max -30 points)
            val distancePenalty = (calculatedDistance * 5.0).toInt().coerceAtMost(30)
            score -= distancePenalty
            if (calculatedDistance <= 1.5) {
                score += 15 // Bonus for ultra-local
                reasons.add("📍 Ultra Nearby (${String.format("%.1f", calculatedDistance)} km தொலைவு)")
            } else if (calculatedDistance <= 3.0) {
                reasons.add("📍 Quick delivery (${String.format("%.1f", calculatedDistance)} km)")
            } else {
                reasons.add("📍 Distance: ${String.format("%.1f", calculatedDistance)} km")
            }

            // C. Estimated Delivery Time Penalty (Max -15 points)
            val estimatedEta = (calculatedDistance * 3.0 + 10.0).toInt().coerceIn(15, 60)
            val etaPenalty = ((estimatedEta - 15) * 0.5).toInt().coerceAtMost(15)
            score -= etaPenalty
            if (estimatedEta <= 25) {
                score += 8
                reasons.add("⚡ Super Fast ETA ($estimatedEta mins)")
            } else {
                reasons.add("🕒 ETA: $estimatedEta mins")
            }

            // D. Current Rider Availability (Max +10 points)
            if (activeRidersCount > 0) {
                score += 10
                reasons.add("🛵 Riders Available (டிரைவர்கள் தயார்)")
            } else {
                score -= 10
                reasons.add("⚠️ Peak load: delay possible (டிரைவர்கள் பற்றாக்குறை)")
            }

            // E. Customer Order History & Favorites (Max +25 points)
            val pastOrdersHere = vendorOrderCounts[vendor.id] ?: 0
            if (pastOrdersHere > 0) {
                score += (pastOrdersHere * 8).coerceAtMost(20)
                reasons.add("🍛 You Ordered Here $pastOrdersHere Times (உங்களுக்கு பிடித்த கடை)")
            }
            if (favoriteVendorIds.contains(vendor.id)) {
                score += 5
            }

            // F. Customer Budget Matching (Max +10 points)
            val vendorMinOrder = vendor.minOrderAmount
            if (avgOrderAmount >= vendorMinOrder) {
                score += 8
            } else {
                score -= 12 // Penalty for out of budget
                reasons.add("💰 Min Order is ₹${vendorMinOrder.toInt()} (கொஞ்சம் காஸ்ட்லி)")
            }

            // G. Offer Percentage & Coupon Availability (Max +15 points)
            if (vendor.isCouponEnabled) {
                score += 10
                reasons.add("🎁 Flat ₹${vendor.couponDiscount.toInt()} Off coupon \"${vendor.couponCode}\"")
            }
            if (vendor.deliveryFee == 0.0 || calculatedDistance <= 1.0) {
                score += 8
                reasons.add("🚚 Free Delivery Eligibility (இலவச டெலிவரி)")
            } else if (avgOrderAmount >= vendor.freeDeliveryThreshold) {
                reasons.add("🚚 Free Delivery above ₹${vendor.freeDeliveryThreshold.toInt()}")
            }

            // H. Cuisines and Categories matching Search / Current Hour / Day (Max +20 points)
            val vendorTypeLower = vendor.type.lowercase()
            
            // Time of day matching
            if (isBreakfastTime) {
                if (vendorTypeLower.contains("cafe") || vendorTypeLower.contains("bakery") || vendorTypeLower.contains("hotel")) {
                    score += 15
                    reasons.add("🥞 Fresh Breakfast special (சூடான காலை உணவு)")
                }
            } else if (isLunchTime) {
                if (vendorTypeLower.contains("restaurant") || vendorTypeLower.contains("biryani") || vendorTypeLower.contains("hotel")) {
                    score += 15
                    reasons.add("🍛 Delicious Lunch Option (மதிய பிரியாணி/சாப்பாடு)")
                }
            } else if (isDinnerTime) {
                if (vendorTypeLower.contains("restaurant") || vendorTypeLower.contains("hotel")) {
                    score += 12
                    reasons.add("🍲 Hot Dinner Special (இரவு சுடச்சுட டின்னர்)")
                }
            } else if (isLateNight) {
                score += 10
                reasons.add("🌙 Supports Late Night Cravings (நள்ளிரவு பசிக்கு)")
            }

            // Day of week matching
            if (isWeekend) {
                if (!vendorTypeLower.contains("pure veg") && (vendorTypeLower.contains("biryani") || vendorTypeLower.contains("restaurant") || vendorTypeLower.contains("chicken"))) {
                    score += 10
                    reasons.add("🍗 Weekend Non-Veg Feast (ஞாயிறு ஸ்பெஷல் கறி விருந்து)")
                }
            }

            // Weather conditions matching
            if (weatherState == "RAINY" || weatherState == "COLD") {
                if (vendorTypeLower.contains("cafe") || vendorTypeLower.contains("snack") || vendorTypeLower.contains("bakery")) {
                    score += 12
                    reasons.add("☕ Rainy Day Warm Sips & Snacks (மழைக்கால ஸ்பெஷல் சுடச்சுட டீ, பஜ்ஜி)")
                }
            } else if (weatherState == "HOT") {
                if (vendorTypeLower.contains("juice") || vendorTypeLower.contains("cafe") || vendorTypeLower.contains("shake")) {
                    score += 12
                    reasons.add("🍧 Summer Cooling Refreshments (கோடைகால ஜில்லென்ற ஜூஸ்/மில்க்ஷேக்)")
                }
            }

            // Search query fuzzy boost (Max +30 points)
            var matchedItem: MenuItem? = null
            if (searchQuery.isNotBlank()) {
                val matchingMenu = menuItems.filter { it.vendorId == vendor.id && it.isAvailable }
                val bestMatch = matchingMenu.find { item ->
                    item.nameEn.contains(searchQuery, ignoreCase = true) ||
                    item.nameTa.contains(searchQuery, ignoreCase = true) ||
                    item.descEn.contains(searchQuery, ignoreCase = true) ||
                    item.descTa.contains(searchQuery, ignoreCase = true)
                }
                
                if (bestMatch != null) {
                    score += 35 // Huge match boost
                    matchedItem = bestMatch
                    reasons.add("🎯 Found: ${bestMatch.nameTa.ifEmpty { bestMatch.nameEn }} (₹${bestMatch.price.toInt()})")
                } else if (vendorTypeLower.contains(searchQuery.lowercase()) || vendor.name.contains(searchQuery, ignoreCase = true) || vendor.nameTa.contains(searchQuery, ignoreCase = true)) {
                    score += 20
                    reasons.add("🎯 Matches search query \"$searchQuery\"")
                } else {
                    // No match under explicit search query, penalize heavily so we don't recommend irrelevant shops
                    score -= 40
                }
            }

            val finalScore = score.coerceIn(5, 100)

            AIRecommendation(
                vendor = vendor,
                aiScore = finalScore,
                reasons = reasons.distinct().take(4), // Return top 4 unique explainable reasons
                matchedItem = matchedItem
            )
        }.sortedByDescending { it.aiScore }

        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "Dynamic AI Score calculation completed in $duration ms for ${vendors.size} vendors.")
        return recommendations
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta))
        dist = Math.acos(dist)
        dist = Math.toDegrees(dist)
        dist = dist * 60 * 1.1515 * 1.609344 // Convert to kilometers
        return if (dist.isNaN()) 0.0 else dist
    }
}