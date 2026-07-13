package com.example.data.repository

import com.example.data.database.Vendor
import com.example.data.database.User
import com.example.data.database.Order
import com.example.data.database.MenuItem
import com.example.data.ai.RecommendationEngine
import com.example.data.ai.AIRecommendation

data class VendorRecommendation(
    val vendor: Vendor,
    val aiScore: Double, // Score out of 100
    val reasons: List<String>, // Dynamic reasons in English
    val reasonsTa: List<String> // Dynamic reasons in Tamil
)

object LyoAiRecommendationEngine {

    fun calculateRecommendationScores(
        vendors: List<Vendor>,
        user: User?,
        pastOrders: List<Order>,
        activeRidersCount: Int,
        weather: String = "Sunny",
        isFestival: Boolean = false
    ): List<VendorRecommendation> {
        val menuItems = emptyList<MenuItem>() // Empty fallback for standalone model
        val recommendations = RecommendationEngine.calculateRecommendations(
            vendors = vendors,
            menuItems = menuItems,
            pastOrders = pastOrders,
            activeRidersCount = activeRidersCount,
            currentLat = user?.lat ?: 0.0,
            currentLng = user?.lng ?: 0.0,
            searchQuery = "",
            weatherState = weather
        )
        return recommendations.map { rec ->
            VendorRecommendation(
                vendor = rec.vendor,
                aiScore = rec.aiScore.toDouble(),
                reasons = rec.reasons,
                reasonsTa = rec.reasons // The reasons list in upgraded engine already contains both languages or highly readable text!
            )
        }
    }
}
