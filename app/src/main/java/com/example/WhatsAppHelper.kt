package com.example

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.data.database.Order
import com.example.data.database.OrderItem

data class LyoSettings(
    val restaurantOwnerPhone: String = "",
    val restaurantName: String = "Lyo Restaurant"
)

object WhatsAppHelper {

    fun getSettings(context: Context): LyoSettings {
        val sharedPrefs = context.getSharedPreferences("lyo_session_prefs", Context.MODE_PRIVATE)
        val phone = sharedPrefs.getString("restaurant_owner_phone", "") ?: ""
        val name = sharedPrefs.getString("restaurant_name", "Lyo Restaurant") ?: "Lyo Restaurant"
        return LyoSettings(phone, name)
    }

    fun saveSettings(context: Context, settings: LyoSettings) {
        val sharedPrefs = context.getSharedPreferences("lyo_session_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("restaurant_owner_phone", settings.restaurantOwnerPhone)
            .putString("restaurant_name", settings.restaurantName)
            .apply()
    }

    fun sendMessage(context: Context, rawPhone: String, message: String) {
        val clean = rawPhone.replace(Regex("[^0-9]"), "")
        val phone = if (clean.startsWith("91") && clean.length == 12) clean
                    else if (clean.length == 10) "91$clean"
                    else clean

        // Scheme 1: whatsapp:// (very direct, bypasses web redirection completely)
        val nativeUri = Uri.parse("whatsapp://send?phone=$phone&text=${Uri.encode(message)}")
        val nativeIntent = Intent(Intent.ACTION_VIEW, nativeUri)

        try {
            context.startActivity(nativeIntent)
        } catch (e: Exception) {
            // Fallback: Using api.whatsapp.com with WhatsApp package restriction
            val webUri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(webIntent)
            } catch (ex2: Exception) {
                // Absolute fallback (opens browser if WhatsApp is uninstalled completely)
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}")))
                } catch (ex3: Exception) {
                    ex3.printStackTrace()
                }
            }
        }
    }

    // Delivery assign: customer + owner KOT (800ms gap)
    fun sendOrderAssignedMessages(
        context: Context,
        order: Order,
        items: List<OrderItem>,
        settings: LyoSettings,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        riderName: String
    ) {
        // Message 1: Customer
        val customerMsg = buildCustomerConfirmationMessage(order, items, customerName, deliveryAddress, riderName)
        sendMessage(context, customerPhone, customerMsg)

        // Message 2: Restaurant KOT (with delay to avoid WA double-open glitch)
        if (settings.restaurantOwnerPhone.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                val kotMsg = buildKOTMessage(order, items, customerName, customerPhone, deliveryAddress)
                sendMessage(context, settings.restaurantOwnerPhone, kotMsg)
            }, 800)
        }
    }

    fun sendInvoiceMessage(
        context: Context, 
        order: Order,
        items: List<OrderItem>,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String
    ) {
        val msg = buildInvoiceMessage(order, items, customerName, deliveryAddress)
        sendMessage(context, customerPhone, msg)
    }

    // ── Message Builders ─────────────────────────────────────────────────

    private fun buildCustomerConfirmationMessage(
        order: Order, 
        items: List<OrderItem>,
        customerName: String,
        deliveryAddress: String,
        riderName: String
    ): String {
        val itemsStr = items.joinToString("\n") { "   • ${it.nameEn} × ${it.quantity} — ₹${(it.price * it.quantity).toInt()}" }
        return """
━━━━━━━━━━━━━━━━━━━━━━━
🛍️  *LYO FOOD DELIVERY — ORDER CONFIRMED*
━━━━━━━━━━━━━━━━━━━━━━━
வணக்கம் $customerName! 🙏

✅ *உங்களது ஆர்டர் வெற்றிகரமாக உறுதி செய்யப்பட்டது!*
🍽️ உணவு தற்போது தயாரிப்பில் உள்ளது.

📦 *ஆர்டர் விவரங்கள் (Order Details):*
🆔 ஆர்டர் ஐடி (Order ID): *#${order.id}*
---------------------------------------
$itemsStr
---------------------------------------
💰 *மொத்தத் தொகை (Total Amount):* ₹${order.totalAmount.toInt()}
📍 *டெலிவரி முகவரி (Delivery Address):* $deliveryAddress
🏍️ *டெலிவரி நபர் (Delivery Partner):* $riderName

⏱️ *30–45 நிமிடங்களில் தங்களது இல்லம் வந்தடைவோம்!*

நன்றி! 🙏 — *Lyo AI*
━━━━━━━━━━━━━━━━━━━━━━━
        """.trimIndent()
    }

    private fun buildKOTMessage(
        order: Order, 
        items: List<OrderItem>,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String
    ): String {
        val time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                       .format(java.util.Date())
        val itemsStr = items.joinToString("\n") { "   • ${it.quantity}x ${it.nameEn}" }
        return """
━━━━━━━━━━━━━━━━━━━━━━━
🔔 *புதிய KOT — NEW KITCHEN ORDER*
━━━━━━━━━━━━━━━━━━━━━━━
🏪 *உணவகம்:* Lyo Partner

🆔 *ஆர்டர் ஐடி (Order ID):* *#${order.id}*
🕐 *நேரம் (Time):* $time
👤 *வாடிக்கையாளர் (Customer):* $customerName
📱 *கைபேசி எண் (Phone):* $customerPhone

🍽️ *தயாரிக்க வேண்டிய உணவுகள் (Items):*
---------------------------------------
$itemsStr
---------------------------------------
💰 *மொத்த மதிப்பு (Total Value):* ₹${order.totalAmount.toInt()} | Cash on Delivery
📍 *முகவரி (Address):* $deliveryAddress

⚡ *உணவினை உடனே தயார் செய்யுமாறு கேட்டுக்கொள்கிறோம்!*

நன்றி! 🙏 — *Lyo AI*
━━━━━━━━━━━━━━━━━━━━━━━
        """.trimIndent()
    }

    private fun buildInvoiceMessage(
        order: Order, 
        items: List<OrderItem>,
        customerName: String,
        deliveryAddress: String
    ): String {
        val date = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                       .format(java.util.Date(order.timestamp))
        val itemsStr = items.mapIndexed { i, it ->
            "   ${i+1}. ${it.nameEn} × ${it.quantity}  —  ₹${(it.price * it.quantity).toInt()}"
        }.joinToString("\n")
        val discount = if (order.couponDiscount > 0) "\n🎁 தள்ளுபடி (Discount): -₹${order.couponDiscount.toInt()}" else ""
        return """
━━━━━━━━━━━━━━━━━━━━━━━
🧾 *LYO FOOD DELIVERY — INVOICE / பில்*
━━━━━━━━━━━━━━━━━━━━━━━
Bill No: *#${order.id}*
Date: $date

👤 *வாடிக்கையாளர் (Customer):* $customerName
📍 *முகவரி (Delivery Address):* $deliveryAddress
━━━━━━━━━━━━━━━━━━━━━━━
*உணவு விவரங்கள் (Ordered Items):*
$itemsStr
━━━━━━━━━━━━━━━━━━━━━━━
💵 *கட்டண விவரங்கள் (Payment Summary):*
   • உணவுத் தொகை (Subtotal): ₹${order.subtotal.toInt()}
   • விநியோகக் கட்டணம் (Delivery Fee): ₹${order.deliveryFee.toInt()}$discount

💰 *மொத்தத் தொகை (GRAND TOTAL):* *₹${order.totalAmount.toInt()}*
💳 *கட்டண முறை (Payment Mode):* Cash on Delivery
━━━━━━━━━━━━━━━━━━━━━━━

எங்களது சேவையைப் பயன்படுத்தியதற்கு நன்றி! 🙏 — *Lyo AI*
━━━━━━━━━━━━━━━━━━━━━━━
        """.trimIndent()
    }
}
