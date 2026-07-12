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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            // Save to local Room database for in-app history (Notification Center)
            try {
                val db = com.example.data.database.AppDatabase.getInstance(context)
                // Generate a unique ID based on the title and message hash to prevent duplicate notifications
                val hashId = "notif_" + (title + message).hashCode().toString()
                val notificationItem = com.example.data.database.LyoNotification(
                    id = hashId,
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        db.promoBannerDao.insertNotification(notificationItem)
                    } catch (dbEx: Exception) {
                        android.util.Log.e("LyoNotification", "Error saving notification to DB: ${dbEx.message}")
                    }
                }
            } catch (dbEx: Exception) {
                android.util.Log.e("LyoNotification", "Failed to access DB: ${dbEx.message}")
            }

            createNotificationChannel(context)
            
            // Intercept and log test notifications
            try {
                val regex = Regex("#(\\d+)")
                val match = regex.find(title + " " + message)
                if (match != null) {
                    val orderId = match.groupValues[1].toLongOrNull()
                    if (orderId != null) {
                        com.example.data.repository.LyoLiveTestTracker.logNotification(orderId, title, message, "PASS")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LyoNotification", "Error logging test notification: ${e.message}")
            }

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

    fun generateOrderPdfAndShare(
        context: Context,
        order: Order,
        items: List<OrderItem>,
        customerName: String,
        customerPhone: String,
        customerAddress: String
    ) {
        try {
            val canvasWidth = 380f
            val canvasHeight = (500f + (items.size * 25f) + (if (order.couponDiscount > 0) 25f else 0f)).coerceAtLeast(620f)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(canvasWidth.toInt(), canvasHeight.toInt(), 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Slate 900
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val titlePaint = Paint().apply {
                color = Color.rgb(249, 115, 22) // Lyo Orange #F97316
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subTitlePaint = Paint().apply {
                color = Color.rgb(30, 41, 59) // Slate 800
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val linePaint = Paint().apply {
                color = Color.rgb(226, 232, 240) // Slate 200
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            var y = 35f

            // 1. Header
            canvas.drawText("Lyo AI Food Delivery", 20f, y, titlePaint)
            y += 14f
            canvas.drawText("PREMIUM INVOICE RECEIPT", 20f, y, Paint().apply { color = Color.rgb(100, 116, 139); textSize = 8f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            y += 12f
            canvas.drawLine(20f, y, canvasWidth - 20f, y, Paint().apply { color = Color.rgb(249, 115, 22); strokeWidth = 1.5f })
            y += 20f

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val formattedDate = sdf.format(Date(order.timestamp))

            // 2. Order Metadata Info
            canvas.drawText("Bill No: #LYO-ORDER-${order.id}", 20f, y, subTitlePaint)
            y += 16f
            canvas.drawText("Date & Time: $formattedDate", 20f, y, textPaint)
            y += 16f
            canvas.drawText("Customer: $customerName", 20f, y, textPaint)
            y += 16f
            canvas.drawText("Phone: $customerPhone", 20f, y, textPaint)
            y += 16f
            
            // Wrap address to avoid clipping
            canvas.drawText("Delivery Address:", 20f, y, textPaint)
            y += 14f
            val addressLines = customerAddress.chunked(45)
            for (line in addressLines) {
                canvas.drawText(line, 25f, y, Paint().apply { color = Color.rgb(71, 85, 105); textSize = 9f })
                y += 14f
            }
            y += 10f

            // 3. Products Table Headers
            canvas.drawLine(20f, y, canvasWidth - 20f, y, linePaint)
            y += 15f
            canvas.drawText("QTY", 20f, y, headerPaint)
            canvas.drawText("ITEM DESCRIPTION", 60f, y, headerPaint)
            canvas.drawText("PRICE", canvasWidth - 65f, y, headerPaint)
            y += 6f
            canvas.drawLine(20f, y, canvasWidth - 20f, y, linePaint)
            y += 18f

            // 4. Products List
            if (items.isEmpty()) {
                canvas.drawText("1", 20f, y, textPaint)
                canvas.drawText("Standard Culinary Feast Platter", 60f, y, textPaint)
                canvas.drawText("₹${order.subtotal.toInt()}", canvasWidth - 65f, y, textPaint)
                y += 20f
            } else {
                for (item in items) {
                    canvas.drawText(item.quantity.toString(), 20f, y, textPaint)
                    val displayName = item.nameEn
                    if (displayName.length > 25) {
                        canvas.drawText(displayName.substring(0, 23) + "...", 60f, y, textPaint)
                    } else {
                        canvas.drawText(displayName, 60f, y, textPaint)
                    }
                    canvas.drawText("₹${(item.price * item.quantity).toInt()}", canvasWidth - 65f, y, textPaint)
                    y += 20f
                }
            }

            // 5. Subtotals Block
            canvas.drawLine(20f, y, canvasWidth - 20f, y, linePaint)
            y += 18f
            canvas.drawText("Subtotal:", 180f, y, textPaint)
            canvas.drawText("₹${order.subtotal.toInt()}", canvasWidth - 65f, y, textPaint)
            y += 15f
            canvas.drawText("Delivery Fee:", 180f, y, textPaint)
            canvas.drawText("₹${order.deliveryFee.toInt()}", canvasWidth - 65f, y, textPaint)
            y += 15f
            if (order.couponDiscount > 0) {
                canvas.drawText("Discounts:", 180f, y, Paint().apply { color = Color.rgb(22, 163, 74); textSize = 9.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                canvas.drawText("-₹${order.couponDiscount.toInt()}", canvasWidth - 65f, y, Paint().apply { color = Color.rgb(22, 163, 74); textSize = 9.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                y += 15f
            }
            
            canvas.drawLine(180f, y, canvasWidth - 20f, y, linePaint)
            y += 18f

            // 6. Grand Total
            canvas.drawText("Grand Total:", 180f, y, headerPaint)
            canvas.drawText("₹${order.totalAmount.toInt()}", canvasWidth - 65f, y, headerPaint)
            y += 22f

            canvas.drawText("Payment Method: Cash on Delivery", 20f, y, Paint().apply { color = Color.rgb(15, 23, 42); textSize = 9.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            y += 22f
            canvas.drawText("Thank you for ordering with Lyo AI Food Delivery! 🙏", 20f, y, Paint().apply { color = Color.rgb(71, 85, 105); textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) })

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
            val cleanItemsStr = if (items.isEmpty()) {
                "   • 1 × Standard Platter — ₹${order.subtotal.toInt()}"
            } else {
                items.joinToString("\n") { "   • ${it.quantity} × ${it.nameEn} — ₹${(it.price * it.quantity).toInt()}" }
            }
            val discountText = if (order.couponDiscount > 0) "\n🎁 *Discounts:* -₹${order.couponDiscount.toInt()}" else ""
            
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("🛍️  *LYO AI FOOD DELIVERY — INVOICE*\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("🆔 *Order ID:* #${order.id}\n")
            textBuilder.append("📅 *Order Date & Time:* $formattedDate\n")
            textBuilder.append("👤 *Customer Name:* $customerName\n")
            textBuilder.append("📱 *Customer Phone:* $customerPhone\n")
            textBuilder.append("📍 *Delivery Address:* $customerAddress\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("*Ordered Items:*\n")
            textBuilder.append(cleanItemsStr).append("\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n")
            textBuilder.append("💵 *Payment Details:*\n")
            textBuilder.append("   • Subtotal: ₹${order.subtotal.toInt()}\n")
            textBuilder.append("   • Delivery Fee: ₹${order.deliveryFee.toInt()}$discountText\n")
            textBuilder.append("---------------------------------------\n")
            textBuilder.append("💰 *Grand Total:* *₹${order.totalAmount.toInt()}*\n")
            textBuilder.append("💳 *Payment Method:* Cash on Delivery\n")
            textBuilder.append("🔑 *Delivery OTP:* *${order.otpCode}*\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━\n\n")
            textBuilder.append("Thank you for choosing Lyo AI Food Delivery! 🙏\n")
            textBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━")
            val messageText = textBuilder.toString()

            val authority = "com.example.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                putExtra(Intent.EXTRA_SUBJECT, "Lyo AI Food Delivery Invoice #${order.id}")
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

    fun generateKitchenKotPdfAndShare(context: Context, order: Order, items: List<OrderItem>, customerName: String) {
        try {
            val canvasWidth = 380f
            val canvasHeight = (420f + (items.size * 35f)).coerceAtLeast(550f)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(canvasWidth.toInt(), canvasHeight.toInt(), 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val linePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val boldLargePaint = Paint().apply {
                color = Color.BLACK
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val boldTextPaint = Paint().apply {
                color = Color.BLACK
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val normalTextPaint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val italicTextPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }

            var y = 35f

            // 1. Header
            canvas.drawText("Lyo AI Food Delivery", 20f, y, titlePaint)
            y += 14f
            canvas.drawText("KITCHEN ORDER TICKET (KOT)", 20f, y, boldTextPaint)
            y += 10f
            canvas.drawLine(20f, y, canvasWidth - 20f, y, linePaint)
            y += 22f

            // 2. Metadata
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val formattedDate = sdf.format(Date(order.timestamp))

            canvas.drawText("STORE: ${order.vendorName}", 20f, y, boldTextPaint)
            y += 18f
            canvas.drawText("ORDER ID: #LYO-ORDER-${order.id}", 20f, y, boldTextPaint)
            y += 18f
            canvas.drawText("TIME: $formattedDate", 20f, y, normalTextPaint)
            y += 20f

            canvas.drawLine(20f, y, canvasWidth - 20f, y, linePaint)
            y += 22f

            // 3. Products Header
            canvas.drawText("QTY", 20f, y, boldLargePaint)
            canvas.drawText("ITEM NAME", 60f, y, boldLargePaint)
            y += 8f
            canvas.drawLine(20f, y, canvasWidth - 20f, y, linePaint)
            y += 22f

            // 4. Products List
            var totalItemsCount = 0
            if (items.isEmpty()) {
                canvas.drawText("1x", 20f, y, boldLargePaint)
                canvas.drawText("Standard Culinary Platter", 60f, y, boldLargePaint)
                y += 16f
                canvas.drawText("Notes: Fresh Preparation", 60f, y, italicTextPaint)
                y += 22f
                totalItemsCount = 1
            } else {
                for (item in items) {
                    canvas.drawText("${item.quantity}x", 20f, y, boldLargePaint)
                    val displayName = item.nameEn
                    if (displayName.length > 25) {
                        canvas.drawText(displayName.substring(0, 23) + "...", 60f, y, boldLargePaint)
                    } else {
                        canvas.drawText(displayName, 60f, y, boldLargePaint)
                    }
                    y += 16f
                    canvas.drawText("Notes: Fresh Preparation", 60f, y, italicTextPaint)
                    y += 22f
                    totalItemsCount += item.quantity
                }
            }

            canvas.drawLine(20f, y, canvasWidth - 20f, y, linePaint)
            y += 22f

            // 5. Ticket Summary
            canvas.drawText("TOTAL ITEMS: $totalItemsCount", 20f, y, boldLargePaint)
            y += 20f
            canvas.drawText("KITCHEN COPY - DO NOT DELIVER", 20f, y, Paint().apply { color = Color.BLACK; textSize = 9.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })

            pdfDocument.finishPage(page)

            // Save PDF
            val file = File(context.cacheDir, "lyo_kot_${order.id}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val authority = "com.example.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Lyo KOT Ticket #${order.id}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Print/Share Lyo KOT via"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to compile KOT PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateTestReportPdfAndShare(context: Context, reports: List<com.example.data.repository.TestOrderReport>) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val titlePaint = Paint().apply {
                color = Color.rgb(249, 115, 22)
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subTitlePaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 40f

            canvas.drawRect(0f, 0f, 595f, 80f, Paint().apply { color = Color.rgb(241, 245, 249) })
            canvas.drawText("Lyo AI Food Delivery - System Test Report", 40f, 45f, titlePaint)
            canvas.drawText("5-Customer Concurrent Multi-Device Live Order Flow", 40f, 65f, Paint().apply { color = Color.GRAY; textSize = 11f })
            y = 110f

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("GMT+5:30")
            canvas.drawText("Report Timestamp: ${sdf.format(Date())} IST", 40f, y, textPaint)
            y += 20f

            val headers = listOf("Cust", "Order ID", "Shop", "Rider", "Placement Time", "Status", "A B C D E F")
            val cols = listOf(40f, 80f, 150f, 240f, 320f, 440f, 500f)

            canvas.drawLine(40f, y, 555f, y, Paint().apply { color = Color.GRAY; strokeWidth = 1f })
            y += 15f
            for (i in headers.indices) {
                canvas.drawText(headers[i], cols[i], y, headerPaint)
            }
            y += 5f
            canvas.drawLine(40f, y, 555f, y, Paint().apply { color = Color.GRAY; strokeWidth = 1f })
            y += 20f

            for (i in reports.indices) {
                val rep = reports[i]
                canvas.drawText(rep.customerName, cols[0], y, textPaint)
                canvas.drawText(if (rep.orderId > 0L) "#${rep.orderId}" else "Pending", cols[1], y, textPaint)
                canvas.drawText(rep.shopName, cols[2], y, textPaint)
                canvas.drawText(rep.riderName, cols[3], y, textPaint)
                canvas.drawText(rep.placementTimeStr.substringBefore(" IST"), cols[4], y, textPaint)
                
                val statusPaint = Paint().apply {
                    color = when (rep.finalStatus) {
                        "PASS" -> Color.rgb(22, 163, 74)
                        "IN PROGRESS" -> Color.rgb(217, 119, 6)
                        else -> Color.rgb(100, 116, 139)
                    }
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(rep.finalStatus, cols[5], y, statusPaint)

                val chk = rep.checklist
                val chkStr = "${chk["A"]} ${chk["B"]} ${chk["C"]} ${chk["D"]} ${chk["E"]} ${chk["F"]}"
                val processedChkStr = chkStr.replace("NOT TESTED", "N").replace("PASS", "P").replace("IN PROGRESS", "I")
                canvas.drawText(processedChkStr, cols[6], y, textPaint)

                y += 20f
            }

            y += 10f
            canvas.drawLine(40f, y, 555f, y, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
            y += 30f

            for (i in reports.indices) {
                val rep = reports[i]
                if (rep.orderId == 0L) continue

                canvas.drawText("Order Slot #${i + 1}: ${rep.customerName} (${rep.customerPhone})", 40f, y, subTitlePaint)
                y += 15f
                canvas.drawText("Order ID: #${rep.orderId} | Store: ${rep.shopName} | Items: ${rep.itemsText} | Total: ₹${rep.price.toInt()}", 40f, y, textPaint)
                y += 12f
                canvas.drawText("Acceptance: ${rep.adminAcceptanceTimeStr} | Rider Assign: ${rep.riderAssignmentTimeStr} | Completed: ${rep.completionTimeStr}", 40f, y, textPaint)
                y += 12f
                canvas.drawText("GPS Coordinates: ${rep.gpsCoordinatesLog}", 40f, y, textPaint)
                y += 12f
                canvas.drawText("Notifications: ${rep.notificationLogsStr}", 40f, y, textPaint)
                y += 25f

                if (y > 780f) {
                    break
                }
            }

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, "lyo_system_test_report.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val authority = "com.example.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Lyo AI Food Delivery Live Test Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Lyo Test Report PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
