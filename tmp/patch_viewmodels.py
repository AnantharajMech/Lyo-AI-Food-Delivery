# -*- coding: utf-8 -*-
import os

file_path = "/app/src/main/java/com/example/ui/viewmodels/LyoViewModels.kt"

if not os.path.exists(file_path):
    # Try relative path
    file_path = "app/src/main/java/com/example/ui/viewmodels/LyoViewModels.kt"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. State/Methods Injection Target
target_injection = "    val saveDraftChecked = MutableStateFlow(false)\n    val closeReopenChecked = MutableStateFlow(false)"

new_code = """    val saveDraftChecked = MutableStateFlow(false)
    val closeReopenChecked = MutableStateFlow(false)

    // --- Bulk Store Import State ---
    val bulkImportRawText = MutableStateFlow("")
    val bulkImportParsedRows = MutableStateFlow<List<BulkImportRow>>(emptyList())
    val isBulkImportLoading = MutableStateFlow(false)

    fun parseBulkImport(text: String) {
        viewModelScope.launch {
            val lines = text.split("\\n")
            val rows = mutableListOf<BulkImportRow>()
            val existingVendors = repository.vendorDao.getAllVendorsList()
            val seenPhonesInImport = mutableSetOf<String>()

            var index = 0
            for (line in lines) {
                if (line.trim().isEmpty()) continue
                
                var parts = line.split(",")
                if (parts.size < 2 && line.contains("\\t")) {
                    parts = line.split("\\t")
                }
                
                if (parts.isEmpty()) continue
                index++

                val name = parts.getOrNull(0)?.trim() ?: ""
                val category = parts.getOrNull(1)?.trim() ?: "Restaurant"
                val address = parts.getOrNull(2)?.trim() ?: ""
                val phone = parts.getOrNull(3)?.trim() ?: ""
                val openTime = parts.getOrNull(4)?.trim() ?: "09:00 AM"
                val closeTime = parts.getOrNull(5)?.trim() ?: "10:00 PM"

                var status = "READY"
                var msg = "Valid"

                if (name.isEmpty()) {
                    status = "ERROR"
                    msg = "கடையின் பெயர் தேவை (Store Name is required)"
                } else if (address.isEmpty()) {
                    status = "ERROR"
                    msg = "முகவரி தேவை (Address is required)"
                } else if (phone.length != 10 or not phone.isdigit()):
                    status = "ERROR"
                    msg = "தவறான தொலைபேசி எண் (Must be 10 digits)"
                } else {
                    val normalizedPhone = "".join([c for c in phone if c.isdigit()])
                    val isDuplicateInDb = any("".join([c for c in v.phone if c.isdigit()]) == normalizedPhone for v in existingVendors)
                    if normalizedPhone in seenPhonesInImport:
                        status = "WARNING"
                        msg = "பட்டியலில் ஏற்கனவே உள்ள எண் (Duplicate in upload)"
                    elif isDuplicateInDb:
                        status = "WARNING"
                        msg = "ஆப்பில் ஏற்கனவே உள்ள கடை (Store already exists in App)"
                    seenPhonesInImport.add(normalizedPhone)
                }

                rows.add(
                    BulkImportRow(
                        rowIndex = index,
                        name = name,
                        category = category,
                        address = address,
                        phone = phone,
                        openTime = openTime,
                        closeTime = closeTime,
                        status = status,
                        message = msg
                    )
                )
            }
            bulkImportParsedRows.value = rows
        }
    }

    fun executeBulkPublish(onSuccess: (Int, Int) -> Unit, onError: (String) -> Unit) {
        val rows = bulkImportParsedRows.value
        val validRows = rows.filter { it.status == "READY" || it.status == "WARNING" }
        if (validRows.isEmpty()) {
            onError("சேமிக்க தகுதியான கடைகள் எதுவும் இல்லை! (No valid rows to publish)")
            return
        }

        isBulkImportLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val dbInstance = com.example.data.repository.LyoFirebaseHelper.firestore
            val authInstance = com.example.data.repository.LyoFirebaseHelper.auth
            if (dbInstance == null) {
                withContext(Dispatchers.Main) {
                    isBulkImportLoading.value = false
                    onError("Firebase database is not initialized.")
                }
                return@launch
            }

            val adminUid = authInstance?.currentUser?.uid ?: "unknown_admin"
            val adminPhone = repository.currentUser.value?.phone ?: ""
            var successCount = 0

            try {
                val chunks = validRows.chunked(400)
                for (chunk in chunks) {
                    val batch = dbInstance.batch()
                    val batchVendors = mutableListOf<Vendor>()

                    for (row in chunk) {
                        val vId = generateUniqueLongId()
                        val vendor = Vendor(
                            id = vId,
                            name = row.name,
                            nameTa = row.name,
                            type = row.category,
                            rating = 4.5,
                            distance = 1.0,
                            deliveryTime = 20,
                            deliveryFee = 30.0,
                            address = row.address,
                            lat = 11.5812,
                            lng = 77.8465,
                            bannerUrl = row.category.lowercase(),
                            phone = row.phone,
                            visibilityRadiusKm = 99999.0,
                            autoOpenTime = row.openTime,
                            autoCloseTime = row.closeTime,
                            status = "ACTIVE"
                        )
                        
                        val idStr = vId.toString()
                        val vendorMap = mapOf(
                            "id" to vendor.id,
                            "name" to vendor.name,
                            "nameTa" to vendor.nameTa,
                            "type" to vendor.type,
                            "rating" to vendor.rating,
                            "distance" to vendor.distance,
                            "deliveryTime" to vendor.deliveryTime,
                            "deliveryFee" to vendor.deliveryFee,
                            "address" to vendor.address,
                            "lat" to vendor.lat,
                            "lng" to vendor.lng,
                            "bannerUrl" to vendor.bannerUrl,
                            "phone" to vendor.phone,
                            "visibilityRadiusKm" to vendor.visibilityRadiusKm,
                            "autoOpenTime" to vendor.autoOpenTime,
                            "autoCloseTime" to vendor.autoCloseTime,
                            "status" to vendor.status
                        )

                        val vendorRef = dbInstance.collection("vendors").document(idStr)
                        val storeRef = dbInstance.collection("stores").document(idStr)
                        
                        batch.set(vendorRef, vendorMap, com.google.firebase.firestore.SetOptions.merge())
                        batch.set(storeRef, vendorMap, com.google.firebase.firestore.SetOptions.merge())
                        
                        batchVendors.add(vendor)
                    }

                    batch.commit().await()

                    for (v in batchVendors) {
                        repository.vendorDao.insertVendor(v)
                    }
                    successCount += chunk.size
                }

                val logId = "log_${System.currentTimeMillis()}"
                val logMap = mapOf(
                    "id" to logId,
                    "adminUid" to adminUid,
                    "adminPhone" to adminPhone,
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "rowCount" to rows.size,
                    "successCount" to successCount,
                    "failureCount" to (rows.size - successCount)
                )
                dbInstance.collection("bulk_import_logs").document(logId).set(logMap).await()

                withContext(Dispatchers.Main) {
                    isBulkImportLoading.value = false
                    bulkImportRawText.value = ""
                    bulkImportParsedRows.value = emptyList()
                    onSuccess(successCount, rows.size - successCount)
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Bulk store import batch commit failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isBulkImportLoading.value = false
                    onError("Bulk import failed: ${e.message}")
                }
            }
        }
    }"""

if target_injection in content:
    content = content.replace(target_injection, new_code)
    print("Injected state and methods successfully.")
else:
    print("Injection target not found in file!")

# 2. Append BulkImportRow data class at end
bulk_import_row_class = """

data class BulkImportRow(
    val rowIndex: Int,
    val name: String,
    val category: String,
    val address: String,
    val phone: String,
    val openTime: String,
    val closeTime: String,
    val status: String, // "READY", "ERROR", "WARNING"
    val message: String
)
"""

if "data class BulkImportRow" not in content:
    content += bulk_import_row_class
    print("Appended BulkImportRow class successfully.")
else:
    print("BulkImportRow class already exists.")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("File LyoViewModels.kt written successfully.")
