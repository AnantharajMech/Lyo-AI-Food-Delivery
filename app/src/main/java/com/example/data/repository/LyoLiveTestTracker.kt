package com.example.data.repository

import com.example.data.database.Order
import com.example.data.database.DeliveryRide
import com.example.data.database.OrderItem

object LyoLiveTestTracker {
    val testPhoneNumbers = emptySet<String>()
    val testRiderPhones = emptySet<String>()

    fun isTestOrder(order: Order): Boolean = false
    fun isTestUser(phone: String): Boolean = false

    fun logAdminAcceptance(orderId: Long) {}
    fun logRiderAssignment(orderId: Long) {}
    fun logDeparture(orderId: Long) {}
    fun logCompletion(orderId: Long) {}
    fun logGpsCoordinate(orderId: Long, lat: Double, lng: Double) {}
    fun logNotification(orderId: Long, title: String, body: String, result: String = "PASS") {}

    fun getAdminAcceptanceTime(orderId: Long): Long? = null
    fun getRiderAssignmentTime(orderId: Long): Long? = null
    fun getDepartureTime(orderId: Long): Long? = null
    fun getCompletionTime(orderId: Long): Long? = null
    fun getGpsLog(orderId: Long): List<Pair<Long, Pair<Double, Double>>> = emptyList()
    
    fun getReportForOrder(order: Order, ride: DeliveryRide?, items: List<OrderItem>): TestOrderReport {
        return TestOrderReport(
            orderId = order.id,
            customerPhone = order.userId,
            customerName = "None",
            shopName = "None",
            itemsText = "None",
            price = 0.0,
            riderName = "None",
            riderPhone = "None",
            placementTimeStr = "None",
            adminAcceptanceTimeStr = "None",
            riderAssignmentTimeStr = "None",
            departureTimeStr = "None",
            completionTimeStr = "None",
            durationMinutes = 0,
            gpsCoordinatesLog = "None",
            notificationLogsStr = "None",
            checklist = emptyMap(),
            finalStatus = "None"
        )
    }
}

data class TestOrderReport(
    val orderId: Long,
    val customerPhone: String,
    val customerName: String,
    val shopName: String,
    val itemsText: String,
    val price: Double,
    val riderName: String,
    val riderPhone: String,
    val placementTimeStr: String,
    val adminAcceptanceTimeStr: String,
    val riderAssignmentTimeStr: String,
    val departureTimeStr: String,
    val completionTimeStr: String,
    val durationMinutes: Int,
    val gpsCoordinatesLog: String,
    val notificationLogsStr: String,
    val checklist: Map<String, String>,
    val finalStatus: String
)
