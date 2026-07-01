package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.database.Order
import com.example.data.database.OrderItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LyoNotificationHelper {

    private const val CHANNEL_ID = "lyo_push_notifications"
    private const val CHANNEL_NAME = "Lyo Order Alerts & Promotions"
    private const val CHANNEL_DESC = "Real-time alerts for orders, riders, and promo updates"
    private var notificationSeed = 2000

    fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF6B00")
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPushNotification(context: Context, title: String, message: String) {
        try {
            createNotificationChannel(context)
            
            val intent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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

            val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(com.example.R.mipmap.ic_launcher) // Use app launcher icon as status bar icon
                .apply {
                    if (largeIconBitmap != null) {
                        setLargeIcon(largeIconBitmap) // Full-color beautiful app icon on the side
                    }
                }
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationSeed += 1

            // Check if POST_NOTIFICATIONS permission is granted (Android 13+)
            val canNotify = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (!canNotify) return

            try {
                notificationManager.notify(notificationSeed, builder.build())
            } catch (e: SecurityException) {
                android.util.Log.w("LyoNotification", "Notification permission denied: ${e.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateOrderPdfAndShare(context: Context, order: Order, items: List<OrderItem>) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val titlePaint = Paint().apply {
                color = Color.rgb(249, 115, 22) // Lyo Orange #F97316
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subTitlePaint = Paint().apply {
                color = Color.rgb(30, 41, 59) // Slate 800
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 50f

            // 1. Header Banner
            canvas.drawRect(0f, 0f, 595f, 100f, Paint().apply { color = Color.rgb(241, 245, 249) })
            canvas.drawText("LYO FOODS - DIGITAL INVOICE RECEIPT", 40f, 60f, titlePaint)
            y = 130f

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val formattedDate = sdf.format(Date(order.timestamp))

            // 2. Order Metadata Info
            canvas.drawText("Order ID: #LYO-ORDER-${order.id}", 40f, y, subTitlePaint)
            y += 20f
            canvas.drawText("Date: $formattedDate", 40f, y, textPaint)
            y += 20f
            canvas.drawText("Status: ${order.status}", 40f, y, textPaint)
            y += 30f

            // 3. Customer & Address Box
            canvas.drawRect(40f, y, 555f, y + 80f, Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL })
            canvas.drawRect(40f, y, 555f, y + 80f, Paint().apply { color = Color.rgb(226, 232, 240); style = Paint.Style.STROKE; strokeWidth = 1f })
            
            val boxY = y + 20f
            canvas.drawText("CUSTOMER BILLING INFO:", 50f, boxY, Paint().apply { color = Color.BLACK; textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            canvas.drawText("Name: Lyo Customer", 50f, boxY + 16f, textPaint)
            canvas.drawText("Phone: ${order.userId}", 50f, boxY + 32f, textPaint)
            canvas.drawText("Address: Coordinates (${order.customerLat}, ${order.customerLng})", 50f, boxY + 48f, textPaint)
            y += 100f

            // 4. Products Table Headers
            canvas.drawText("ITEM DESCRIPTION", 40f, y, Paint().apply { textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            canvas.drawText("QTY", 400f, y, Paint().apply { textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            canvas.drawText("PRICE", 480f, y, Paint().apply { textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            
            y += 5f
            canvas.drawLine(40f, y, 555f, y, Paint().apply { color = Color.GRAY; strokeWidth = 1f })
            y += 20f

            // 5. Products List
            if (items.isEmpty()) {
                canvas.drawText("Custom Lyo Foods Delicacy Platter", 40f, y, textPaint)
                canvas.drawText("1", 400f, y, textPaint)
                canvas.drawText("₹${order.subtotal.toInt()}", 480f, y, textPaint)
                y += 20f
            } else {
                for (item in items) {
                    canvas.drawText(item.nameEn, 40f, y, textPaint)
                    canvas.drawText(item.quantity.toString(), 400f, y, textPaint)
                    canvas.drawText("₹${(item.price * item.quantity).toInt()}", 480f, y, textPaint)
                    y += 20f
                }
            }

            y += 10f
            canvas.drawLine(40f, y, 555f, y, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
            y += 20f

            // 6. Subtotals Block
            canvas.drawText("Subtotal:", 380f, y, textPaint)
            canvas.drawText("₹${order.subtotal.toInt()}", 480f, y, textPaint)
            y += 18f
            canvas.drawText("Delivery Fee:", 380f, y, textPaint)
            canvas.drawText("₹${order.deliveryFee.toInt()}", 480f, y, textPaint)
            y += 18f
            canvas.drawText("Tip added:", 380f, y, textPaint)
            canvas.drawText("₹${order.tipAmount.toInt()}", 480f, y, textPaint)
            y += 18f
            if (order.couponDiscount > 0) {
                canvas.drawText("Discount Code applied:", 380f, y, Paint().apply { color = Color.rgb(34, 197, 94); textSize = 11f })
                canvas.drawText("-₹${order.couponDiscount.toInt()}", 480f, y, Paint().apply { color = Color.rgb(34, 197, 94); strokeWidth = 1f })
                y += 18f
            }
            
            canvas.drawLine(380f, y, 555f, y, Paint().apply { color = Color.GRAY; strokeWidth = 1f })
            y += 20f

            // 7. Grande Total Summary
            val grandPaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Total Payable:", 380f, y, grandPaint)
            canvas.drawText("₹${order.totalAmount.toInt()}", 480f, y, grandPaint)

            y += 40f
            canvas.drawText("Secure Delivery OTP Authorization Code: ${order.otpCode}", 40f, y, grandPaint)

            pdfDocument.finishPage(page)

            // Save PDF
            val file = File(context.cacheDir, "lyo_invoice_${order.id}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Build Message Text
            val textBuilder = StringBuilder()
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("📝 *LYO FOODS — DIGITAL INVOICE RECEIPT*\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("🧾 *ஆர்டர் ஐடி (Order ID):* #LYO-ORDER-${order.id}\n")
            textBuilder.append("📅 *தேதி/நேரம் (Date):* $formattedDate\n")
            textBuilder.append("👤 *வாடிக்கையாளர் (Customer):* Lyo Customer\n")
            textBuilder.append("📍 *இருப்பிடம் (Coordinates):* (${order.customerLat}, ${order.customerLng})\n")
            textBuilder.append("📞 *தொடர்புக்கு (Contact):* ${order.userId}\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("🛍️ *உணவு விவரங்கள் (Items Ordered):*\n")
            if (items.isEmpty()) {
                textBuilder.append("   • Standard Lyo Deluxe Platter x1\n")
            } else {
                for (item in items) {
                    textBuilder.append("   • ${item.nameEn} (x${item.quantity}) — ₹${(item.price * item.quantity).toInt()}\n")
                }
            }
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("💵 *கட்டண விவரங்கள் (Bill Summary):*\n")
            textBuilder.append("   • உணவுத் தொகை (Subtotal): ₹${order.subtotal.toInt()}\n")
            textBuilder.append("   • விநியோகக் கட்டணம் (Delivery): ₹${order.deliveryFee.toInt()}\n")
            textBuilder.append("   • கூடுதல் டிப் (Driver Tip): ₹${order.tipAmount.toInt()}\n")
            if (order.couponDiscount > 0) {
                textBuilder.append("   • தள்ளுபடி (Coupon Savings): -₹${order.couponDiscount.toInt()}\n")
            }
            textBuilder.append("---------------------------------------\n")
            textBuilder.append("💰 *மொத்தத் தொகை (Grand Total):* *₹${order.totalAmount.toInt()}*\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("🛵 *விநியோக நபர் (Rider):* Live Tracker Rider\n")
            textBuilder.append("🔑 *டெலிவரி ஓடிபி (Delivery OTP):* *${order.otpCode}*\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n\n")
            textBuilder.append("எங்களது சேவையைப் பயன்படுத்தியதற்கு நன்றி! 🙏 — *Lyo Foods*\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━")

            val messageText = textBuilder.toString()

            val authority = "com.example.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                putExtra(Intent.EXTRA_SUBJECT, "Lyo Order Invoice #${order.id}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                `package` = "com.whatsapp"
            }

            try {
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                shareIntent.`package` = null
                context.startActivity(Intent.createChooser(shareIntent, "Share Lyo Invoice PDF via"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to compile Invoice PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
