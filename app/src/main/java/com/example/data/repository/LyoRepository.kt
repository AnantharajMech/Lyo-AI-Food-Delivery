package com.example.data.repository

import android.util.Log
import com.example.data.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class LyoRepository(val db: AppDatabase) {

    // Global GST Config
    var gstEnabled: Boolean = false
    var gstRate: Double = 5.0

    // Global Dynamic Surcharge Config
    var rainSurchargeEnabled: Boolean = false
    var peakHourSurchargeEnabled: Boolean = false
    var deliveryZoneMultiplier: Double = 1.0

    // Global UPI Config
    val activeUpiId = MutableStateFlow("8778148899@ptyes")
    val activeUpiName = MutableStateFlow("Anantharaj R")

    fun initGstSettings(context: android.content.Context) {
        val prefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
        gstEnabled = prefs.getBoolean("gst_enabled", false)
        gstRate = prefs.getFloat("gst_rate", 5.0f).toDouble()
        rainSurchargeEnabled = prefs.getBoolean("rain_surcharge_enabled", false)
        peakHourSurchargeEnabled = prefs.getBoolean("peak_hour_surcharge_enabled", false)
        deliveryZoneMultiplier = prefs.getFloat("delivery_zone_multiplier", 1.0f).toDouble()
    }

    fun initUpiSettings(context: android.content.Context) {
        val prefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
        val id = prefs.getString("upi_id", "8778148899@ptyes") ?: "8778148899@ptyes"
        val name = prefs.getString("upi_name", "Anantharaj R") ?: "Anantharaj R"
        activeUpiId.value = id
        activeUpiName.value = name
    }

    fun setUpiSettingsLocally(context: android.content.Context?, id: String, name: String) {
        activeUpiId.value = id
        activeUpiName.value = name
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putString("upi_id", id)
                .putString("upi_name", name)
                .apply()
        }
    }

    fun updateUpiSettings(context: android.content.Context, id: String, name: String) {
        setUpiSettingsLocally(context, id, name)
        // Sync to Firestore live config!
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val upiMap = mapOf(
                    "upiId" to id,
                    "upiName" to name
                )
                dbInstance.collection("app_settings").document("global").set(upiMap, com.google.firebase.firestore.SetOptions.merge())
                Log.d("LyoRepository", "Synced UPI settings to Firestore: upiId=$id, upiName=$name")
            } catch (e: java.lang.Exception) {
                Log.w("LyoRepository", "Error syncing UPI settings to Firestore: ${e.message}")
            }
        }
    }

    fun setGstSettingsLocally(context: android.content.Context?, enabled: Boolean, rate: Double) {
        gstEnabled = enabled
        gstRate = rate
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("gst_enabled", enabled)
                .putFloat("gst_rate", rate.toFloat())
                .apply()
        }
    }

    fun updateGstSettings(context: android.content.Context, enabled: Boolean, rate: Double) {
        setGstSettingsLocally(context, enabled, rate)
        // Also sync to Firestore live config!
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val gstMap = mapOf(
                    "gstEnabled" to enabled,
                    "gstRate" to rate
                )
                dbInstance.collection("app_settings").document("global").set(gstMap, com.google.firebase.firestore.SetOptions.merge())
                Log.d("LyoRepository", "Synced GST settings to Firestore: enabled=$enabled, rate=$rate")
            } catch (e: Exception) {
                Log.w("LyoRepository", "Error syncing GST settings to Firestore: ${e.message}")
            }
        }
    }

    fun updateSurchargeSettings(context: android.content.Context, rain: Boolean, peak: Boolean, zoneMultiplier: Double) {
        rainSurchargeEnabled = rain
        peakHourSurchargeEnabled = peak
        deliveryZoneMultiplier = zoneMultiplier
        val prefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("rain_surcharge_enabled", rain)
            .putBoolean("peak_hour_surcharge_enabled", peak)
            .putFloat("delivery_zone_multiplier", zoneMultiplier.toFloat())
            .apply()

        // Sync to Firestore live config!
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val surchargeMap = mapOf(
                    "rainSurchargeEnabled" to rain,
                    "peakHourSurchargeEnabled" to peak,
                    "deliveryZoneMultiplier" to zoneMultiplier
                )
                dbInstance.collection("app_settings").document("global").set(surchargeMap, com.google.firebase.firestore.SetOptions.merge())
                Log.d("LyoRepository", "Synced Surcharge settings to Firestore")
            } catch (e: Exception) {
                Log.w("LyoRepository", "Error syncing Surcharges to Firestore: ${e.message}")
            }
        }
    }

    // Live In-Memory States
    val currentUser = MutableStateFlow<User?>(null)
    var adminLoginCredentials: Pair<String, String>? = null
    val isAuthRestoring = MutableStateFlow(true)
    val activeSessions = MutableStateFlow<List<DeviceSession>>(emptyList())
    val remoteLogoutTriggered = MutableStateFlow<Boolean>(false)
    val currentVendor = MutableStateFlow<Vendor?>(null)
    val cart = MutableStateFlow<Map<MenuItem, Int>>(emptyMap()) // MenuItem -> Quantity
    val activeLiveOrder = MutableStateFlow<Order?>(null)
    val maxStoreDistanceRadius = MutableStateFlow<Double>(15.0)

    // App Suspension Config
    val isAppPaused = MutableStateFlow(false)
    val appPauseMessageEn = MutableStateFlow("")
    val appPauseMessageTa = MutableStateFlow("")

    // Global Success Confirmation State
    val globalSuccessMessage = MutableStateFlow<String?>(null)

    fun showSuccess(msg: String) {
        globalSuccessMessage.value = msg
    }

    fun initAppPauseSettings(context: android.content.Context) {
        val prefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
        isAppPaused.value = prefs.getBoolean("is_app_paused", false)
        appPauseMessageEn.value = prefs.getString("app_pause_message_en", "We are currently closed for a short break. Please check back soon!") ?: "We are currently closed for a short break. Please check back soon!"
        appPauseMessageTa.value = prefs.getString("app_pause_message_ta", "நாங்கள் தற்போது தற்காலிக விடுப்பில் உள்ளோம். விரைவில் மீண்டும் வருகிறோம்!") ?: "நாங்கள் தற்போது தற்காலிக விடுப்பில் உள்ளோம். விரைவில் மீண்டும் வருகிறோம்!"
    }

    fun setAppPauseSettingsLocally(context: android.content.Context?, paused: Boolean, msgEn: String, msgTa: String) {
        isAppPaused.value = paused
        appPauseMessageEn.value = msgEn
        appPauseMessageTa.value = msgTa
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_app_paused", paused)
                .putString("app_pause_message_en", msgEn)
                .putString("app_pause_message_ta", msgTa)
                .apply()
        }
    }

    fun updateAppPauseSettings(context: android.content.Context, paused: Boolean, msgEn: String, msgTa: String) {
        setAppPauseSettingsLocally(context, paused, msgEn, msgTa)

        // Also sync to Firestore live config!
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val settingsMap = mapOf(
                    "isAppPaused" to paused,
                    "appPauseMessageEn" to msgEn,
                    "appPauseMessageTa" to msgTa
                )
                dbInstance.collection("app_settings").document("global").set(settingsMap, com.google.firebase.firestore.SetOptions.merge())
                Log.d("LyoRepository", "Synced app suspension to Firestore: paused=$paused")
            } catch (e: Exception) {
                Log.w("LyoRepository", "Error syncing app suspension to Firestore: ${e.message}")
            }
        }
    }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            var waitCount = 0
            while (!com.example.data.repository.LyoFirebaseHelper.isInitialized && waitCount < 30) {
                delay(100)
                waitCount++
            }
            if (com.example.data.repository.LyoFirebaseHelper.isInitialized) {
                try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener { auth ->
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val uid = firebaseUser.uid
                        val current = currentUser.value
                        if (current == null || current.uid != uid) {
                            CoroutineScope(Dispatchers.IO).launch {
                                Log.d("LyoRepository", "AuthStateListener: Firebase user connected (UID: $uid). Restoring/aligning session...")
                                var recoveredUser = db.userDao.getUserByPhone(uid)
                                if (recoveredUser == null) {
                                    val phone = firebaseUser.phoneNumber ?: firebaseUser.email ?: ""
                                    if (phone.isNotEmpty()) {
                                        recoveredUser = db.userDao.getUserByPhone(phone)
                                    }
                                }
                                if (recoveredUser == null && firebaseUser.email?.contains("Anantharaj", ignoreCase = true) == true) {
                                    recoveredUser = db.userDao.getUserByPhone("Anantharajmech")
                                }
                                if (recoveredUser == null) {
                                    try {
                                        val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            .collection("users").document(uid).get().await()
                                        if (doc.exists()) {
                                            val rawRole = doc.getString("role")
                                            if (rawRole.isNullOrBlank() || rawRole.trim() !in listOf("CUSTOMER", "ADMIN", "RIDER", "DELIVERY")) {
                                                Log.e("LyoRepository", "AuthStateListener: Missing, empty, or invalid role.")
                                            } else {
                                                val phone = doc.getString("phone") ?: ""
                                                val role = if (rawRole == "DELIVERY" || rawRole == "RIDER") "RIDER" else rawRole
                                                recoveredUser = User(
                                                    phone = phone,
                                                    name = doc.getString("name") ?: "",
                                                    email = doc.getString("email") ?: "",
                                                    address = doc.getString("address") ?: "",
                                                    lat = doc.getDouble("lat") ?: 11.5812,
                                                    lng = doc.getDouble("lng") ?: 77.8465,
                                                    isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                                                    role = role,
                                                    vehicleNo = doc.getString("vehicleNo") ?: "",
                                                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                                                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                                                    salaryRate = doc.getDouble("salaryRate") ?: 0.0,
                                                    uid = uid
                                                )
                                                db.userDao.insertUser(recoveredUser)
                                            }
                                        } else {
                                            Log.e("LyoRepository", "AuthStateListener: Profile document missing in Firestore.")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("LyoRepository", "AuthStateListener: Error fetching profile from Firestore: ${e.message}")
                                    }
                                } else {
                                    val cachedRole = recoveredUser.role
                                    if (cachedRole.isEmpty() || cachedRole !in listOf("CUSTOMER", "ADMIN", "RIDER", "DELIVERY")) {
                                        Log.e("LyoRepository", "AuthStateListener: Cached user role invalid or missing.")
                                        recoveredUser = null
                                    }
                                }
                                if (recoveredUser != null) {
                                    val alignedUser = recoveredUser.copy(uid = uid)
                                    db.userDao.insertUser(alignedUser)
                                    currentUser.value = alignedUser
                                    Log.d("LyoRepository", "AuthStateListener: Session restored & UID aligned successfully ($uid) for: ${alignedUser.phone}")
                                } else if (current != null) {
                                    val alignedUser = current.copy(uid = uid)
                                    db.userDao.insertUser(alignedUser)
                                    currentUser.value = alignedUser
                                    Log.d("LyoRepository", "AuthStateListener: Updated current user UID to $uid")
                                } else {
                                    Log.e("LyoRepository", "AuthStateListener: Session recovery skipped/deferred.")
                                }
                            }
                        }
                    } else {
                        Log.d("LyoRepository", "AuthStateListener: Firebase user is null. Keeping local/cached session intact.")
                    }
                }
            } catch (e: Exception) {
                Log.e("LyoRepository", "AuthStateListener: Failed to setup AuthStateListener: ${e.message}")
            }
        }
    }

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            currentUser.map { it?.phone }.distinctUntilChanged().collectLatest { phone ->
                if (phone != null) {
                    // Start child coroutine to automatically monitor and restore active orders for this user
                    launch {
                        db.orderDao.getOrdersForUser(phone).collect { ordersList ->
                            val currentActive = activeLiveOrder.value
                            if (currentActive == null || currentActive.status == "DELIVERED" || currentActive.status == "CANCELLED") {
                                // Find any active, uncompleted order
                                val activeOrder = ordersList.firstOrNull { 
                                    it.status != "DELIVERED" && it.status != "CANCELLED" 
                                }
                                if (activeOrder != null) {
                                    activeLiveOrder.value = activeOrder
                                    Log.d("LyoRepository", "Auto-restored active order for customer: #${activeOrder.id} status=${activeOrder.status}")
                                }
                            } else {
                                val updated = ordersList.firstOrNull { it.id == currentActive.id }
                                if (updated != null) {
                                    if (updated.status != currentActive.status || updated != currentActive) {
                                        activeLiveOrder.value = updated
                                    }
                                }
                            }
                        }
                    }

                    db.userDao.getUserFlow(phone).collect { updatedUser ->
                        if (updatedUser != null) {
                            if (updatedUser != currentUser.value) {
                                currentUser.value = updatedUser
                                Log.d("LyoRepository", "Auto-synced current user from DB: is_active=${updatedUser.isActiveRider}")
                            }
                        } else {
                            // Safer: do not set currentUser.value = null spontaneously to prevent accidental logouts during DB sync / migrations
                            Log.w("LyoRepository", "Current user $phone was not found in local DB. Keeping in-memory session active to prevent accidental logout.")
                        }
                    }
                } else {
                    activeLiveOrder.value = null
                }
            }
        }
    }

    fun triggerRemoteLogout() {
        remoteLogoutTriggered.value = true
    }

    // DAOs
    val userDao = db.userDao
    val vendorDao = db.vendorDao
    val categoryDao = db.categoryDao
    val menuItemDao = db.menuItemDao
    val orderDao = db.orderDao
    val orderItemDao = db.orderItemDao
    val deliveryRideDao = db.deliveryRideDao
    val promoBannerDao = db.promoBannerDao
    val savedAddressDao = db.savedAddressDao
    val savedPaymentMethodDao = db.savedPaymentMethodDao
    val smartMenuCorrectionDao = db.smartMenuCorrectionDao

    // Saved Addresses & Payment Methods Operations
    fun getSavedAddressesForUser(userId: String): Flow<List<SavedAddress>> =
        db.savedAddressDao.getAddressesForUserFlow(userId)

    suspend fun saveAddress(address: SavedAddress) = withContext(Dispatchers.IO) {
        if (address.isDefault) {
            db.savedAddressDao.clearDefaultsForUser(address.userId)
            try {
                val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                dbInstance.collection("users").document(address.userId).collection("saved_addresses")
                    .whereEqualTo("isDefault", true)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (doc in querySnapshot.documents) {
                            doc.reference.update("isDefault", false)
                        }
                    }
            } catch (e: Exception) {
                Log.w("LyoRepository", "Error clearing defaults in Firestore: ${e.message}")
            }
        }
        val newId = db.savedAddressDao.insertAddress(address)
        val finalAddress = if (address.id == 0L) address.copy(id = newId) else address
        
        try {
            val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val addrMap = mapOf(
                "id" to finalAddress.id,
                "userId" to finalAddress.userId,
                "name" to finalAddress.name,
                "addressLine" to finalAddress.addressLine,
                "isDefault" to finalAddress.isDefault,
                "latitude" to finalAddress.latitude,
                "longitude" to finalAddress.longitude
            )
            dbInstance.collection("users").document(finalAddress.userId)
                .collection("saved_addresses").document(finalAddress.id.toString())
                .set(addrMap)
            Log.d("LyoRepository", "Synced saved address ${finalAddress.id} to Firestore")
        } catch (e: Exception) {
            Log.w("LyoRepository", "Error syncing saved address to Firestore: ${e.message}")
        }
    }

    suspend fun deleteAddress(address: SavedAddress) = withContext(Dispatchers.IO) {
        db.savedAddressDao.deleteAddress(address)
        try {
            val dbInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            dbInstance.collection("users").document(address.userId)
                .collection("saved_addresses").document(address.id.toString())
                .delete()
            Log.d("LyoRepository", "Deleted saved address ${address.id} from Firestore")
        } catch (e: Exception) {
            Log.w("LyoRepository", "Error deleting saved address from Firestore: ${e.message}")
        }
    }

    fun getSavedPaymentMethodsForUser(userId: String): Flow<List<SavedPaymentMethod>> =
        db.savedPaymentMethodDao.getPaymentMethodsForUserFlow(userId)

    suspend fun savePaymentMethod(paymentMethod: SavedPaymentMethod) = withContext(Dispatchers.IO) {
        db.savedPaymentMethodDao.insertPaymentMethod(paymentMethod)
    }

    suspend fun deletePaymentMethod(paymentMethod: SavedPaymentMethod) = withContext(Dispatchers.IO) {
        db.savedPaymentMethodDao.deletePaymentMethod(paymentMethod)
    }

    // Flow listings from DB
    val allVendors: Flow<List<Vendor>> = db.vendorDao.getAllVendors()
    val allPromoBanners: Flow<List<PromoBanner>> = db.promoBannerDao.getAllPromoBanners()
    val activeDeliveryRides: Flow<List<DeliveryRide>> = db.deliveryRideDao.getActiveRides()
    val allDeliveryRides: Flow<List<DeliveryRide>> = db.deliveryRideDao.getAllRidesFlow()
    val allOrdersAdmin: Flow<List<Order>> = db.orderDao.getAllOrders()
    val allRiders: Flow<List<User>> = db.userDao.getAllRidersFlow()
    val allCustomers: Flow<List<User>> = db.userDao.getAllCustomersFlow()
    val allAdmins: Flow<List<User>> = db.userDao.getAllAdminsFlow()
    val allMenuItems: Flow<List<MenuItem>> = db.menuItemDao.getAllMenuItemsFlow()

    fun getCategoriesForVendor(vendorId: Long): Flow<List<Category>> =
        db.categoryDao.getCategoriesForVendor(vendorId)

    fun getMenuItemsForVendor(vendorId: Long): Flow<List<MenuItem>> =
        db.menuItemDao.getMenuItemsForVendor(vendorId)

    fun getOrdersForUser(userId: String): Flow<List<Order>> =
        db.orderDao.getOrdersForUser(userId)

    fun getReviewsForVendor(vendorId: Long): Flow<List<Review>> =
        db.reviewDao.getReviewsForVendor(vendorId)

    suspend fun addReview(review: Review) = withContext(Dispatchers.IO) {
        db.reviewDao.insertReview(review)
        val reviews = db.reviewDao.getReviewsForVendorList(review.vendorId)
        if (reviews.isNotEmpty()) {
            val avg = reviews.map { it.rating }.average()
            val formattedAvg = String.format(java.util.Locale.US, "%.1f", avg).toDoubleOrNull() ?: avg
            val vendor = db.vendorDao.getVendorById(review.vendorId)
            if (vendor != null) {
                db.vendorDao.insertVendor(vendor.copy(rating = formattedAvg))
            }
        }
    }

    suspend fun getOrderWithItems(orderId: Long): Pair<Order?, List<OrderItem>> {
        val order = db.orderDao.getOrderById(orderId)
        val items = if (order != null) db.orderItemDao.getItemsForOrder(orderId) else emptyList()
        return Pair(order, items)
    }

    // Auth & Accounts
    suspend fun findUserLocallyOnly(phone: String): User? = withContext(Dispatchers.IO) {
        db.userDao.getUserByPhone(phone)
    }

    suspend fun findUser(phone: String): User? = withContext(Dispatchers.IO) {
        val normalized = LyoFirebaseHelper.normalizePhone(phone)
        val variants = listOf(phone.trim(), normalized, "+91$normalized", "91$normalized").distinct()
        var user: User? = null
        for (v in variants) {
            user = db.userDao.getUserByPhone(v)
            if (user != null) break
        }
        if ((user == null || user.uid.isBlank() || user.uid.startsWith("uid_")) && LyoFirebaseHelper.isInitialized) {
            val firestoreUser = LyoFirebaseHelper.getUserByPhoneFromFirestore(phone)
            if (firestoreUser != null && !firestoreUser.uid.startsWith("uid_") && firestoreUser.uid.isNotBlank()) {
                user = firestoreUser
                db.userDao.insertUser(firestoreUser)
            }
        }
        user
    }

    suspend fun registerUser(user: User, plaintextPassword: String? = null) = withContext(Dispatchers.IO) {
        var firebaseRegistered = false
        var finalUser = user
        if (LyoFirebaseHelper.isInitialized) {
            try {
                val authInstance = LyoFirebaseHelper.auth
                val email = "${LyoFirebaseHelper.normalizePhone(user.phone)}@lyofoods.in"
                var uid = authInstance?.currentUser?.let {
                    if (it.email?.lowercase() == email.lowercase()) it.uid else null
                }
                if (uid == null) {
                    uid = LyoFirebaseHelper.getUidByPhone(LyoFirebaseHelper.normalizePhone(user.phone)) ?: LyoFirebaseHelper.getUidByPhone(user.phone)
                }
                if (uid != null) {
                    finalUser = user.copy(uid = uid)
                }
                
                val success = LyoFirebaseHelper.registerInFirebase(finalUser, plaintextPassword)
                if (!success) {
                    throw Exception("Firebase write returned false. Registration aborted.")
                }
                firebaseRegistered = true
                
                val dbUser = db.userDao.getUserByPhone(user.phone)
                val resolvedUid = if (dbUser != null && dbUser.uid.isNotBlank() && !dbUser.uid.startsWith("uid_")) dbUser.uid else {
                    LyoFirebaseHelper.getUidByPhone(LyoFirebaseHelper.normalizePhone(user.phone)) ?: finalUser.uid
                }
                val finalResolvedUid = if (resolvedUid.isBlank()) {
                    "uid_${LyoFirebaseHelper.normalizePhone(user.phone)}"
                } else {
                    resolvedUid
                }
                finalUser = finalUser.copy(uid = finalResolvedUid)
            } catch (e: Exception) {
                Log.e("LyoRepository", "Firebase registration/sync failed, rejecting registration: ${e.message}")
                throw e
            }
        } else {
            throw IllegalStateException("Firebase is not initialized. Cannot register/update account without Firebase connection.")
        }
        
        if (firebaseRegistered) {
            finalUser = finalUser.copy(updatedAt = System.currentTimeMillis())
            db.userDao.insertUser(finalUser)
            
            LyoFirebaseHelper.appContext?.let { context ->
                try {
                    val allUsers = db.userDao.getAllUsers()
                    LyoLocalBackupHelper.backupUsers(allUsers, context)
                } catch (e: Exception) {
                    Log.e("LyoRepository", "Local background user backup failed: ${e.message}")
                }

                if (!plaintextPassword.isNullOrBlank()) {
                    try {
                        val hash = LyoFirebaseHelper.hashPassword(plaintextPassword)
                        val sharedPrefs = context.getSharedPreferences("lyo_offline_passwords", android.content.Context.MODE_PRIVATE)
                        sharedPrefs.edit().putString("pass_hash_${finalUser.phone}", hash).apply()
                        Log.d("LyoRepository", "Saved offline password hash for phone: ${finalUser.phone}")
                    } catch (e: Exception) {
                        Log.e("LyoRepository", "Failed to save offline password hash: ${e.message}")
                    }
                }
            }
        }
    }

    suspend fun deleteCustomer(user: User) = withContext(Dispatchers.IO) {
        try {
            if (!LyoFirebaseHelper.ensureFirebaseAdminAuth()) {
                throw IllegalStateException("Your admin session has expired. Please log out and log in again.")
            }
            val success = LyoFirebaseHelper.deleteUserFromFirestore(user.phone)
            if (!success) {
                throw Exception("Failed to delete customer profile from Firestore.")
            }
            db.userDao.deleteUserByPhone(user.phone)
            Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [deleteCustomer] SUCCESS for customer: ${user.phone}")
        } catch (e: Exception) {
            Log.e("LyoRepository", "Firebase Firestore delete customer failed: ${e.message}")
            Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [deleteCustomer] FAILED for customer: ${user.phone}: ${e.message}")
            throw e
        }
    }

    suspend fun deleteRiderWithAuthCleanup(rider: User) = withContext(Dispatchers.IO) {
        val authInstance = com.google.firebase.auth.FirebaseAuth.getInstance()
        val firestoreInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        try {
            if (!LyoFirebaseHelper.ensureFirebaseAdminAuth()) {
                throw IllegalStateException("Your admin session has expired. Please log out and log in again.")
            }
            val riderUid = LyoFirebaseHelper.getUidByPhone(rider.phone) ?: rider.uid
            if (riderUid.isNotBlank()) {
                // Fetch rider's doc to get passwordHash before deleting
                val doc = firestoreInstance.collection("users").document(riderUid).get().await()
                val passwordHash = doc.getString("passwordHash") ?: ""
                val email = doc.getString("email") ?: "${rider.phone.trim()}@lyofoods.in"
                
                if (passwordHash.isNotBlank()) {
                    val currentAdmin = currentUser.value
                    val adminCreds = adminLoginCredentials
                    
                    Log.d("LyoRepository", "Attempting Auth cleanup for rider: $email")
                    try {
                        // Sign in as rider temporarily
                        authInstance.signInWithEmailAndPassword(email, passwordHash).await()
                        // Delete rider's Firebase Auth account
                        authInstance.currentUser?.delete()?.await()
                        Log.d("LyoRepository", "Successfully deleted rider Firebase Auth account.")
                    } catch (authEx: Exception) {
                        Log.e("LyoRepository", "Failed deleting rider Firebase Auth account: ${authEx.message}")
                        throw Exception("Failed to delete delivery partner Auth account: ${authEx.message}", authEx)
                    } finally {
                        // ALWAYS re-authenticate Admin
                        if (adminCreds != null && currentAdmin != null) {
                            try {
                                val rawInput = adminCreds.first.trim()
                                val adminEmail = if (rawInput.equals("Anantharajmech", ignoreCase = true) || 
                                                    rawInput.equals("8778148899", ignoreCase = true) || 
                                                    rawInput.equals("AnanthEinstein", ignoreCase = true) || 
                                                    rawInput.equals("AnantharajEinstein@gmail.com", ignoreCase = true) ||
                                                    rawInput.equals("anantharajeinstein@gmail.com", ignoreCase = true)) {
                                    "AnantharajEinstein@gmail.com"
                                } else if (rawInput.contains("@")) {
                                    rawInput
                                } else {
                                    "${rawInput}@lyofoods.in"
                                }
                                val adminPass = adminCreds.second
                                val adminHash = LyoFirebaseHelper.hashPassword(adminPass)
                                try {
                                    authInstance.signInWithEmailAndPassword(adminEmail, adminPass).await()
                                } catch (e: Exception) {
                                    authInstance.signInWithEmailAndPassword(adminEmail, adminHash).await()
                                }
                                currentUser.value = currentAdmin
                                Log.d("LyoRepository", "Successfully re-authenticated Admin session for $adminEmail.")
                            } catch (reauthEx: Exception) {
                                Log.e("LyoRepository", "Failed to re-authenticate Admin session: ${reauthEx.message}")
                            }
                        }
                    }
                }
                
                // Delete Firestore documents
                firestoreInstance.collection("users").document(riderUid).delete().await()
                firestoreInstance.collection("admins").document(riderUid).delete().await()
                firestoreInstance.collection("riders").document(riderUid).delete().await()
                Log.d("LyoRepository", "Deleted rider documents from Firestore.")
            } else {
                throw Exception("Could not resolve UID for delivery partner to delete from Firestore.")
            }
            
            // Delete from local Room database ONLY on success
            db.userDao.deleteUserByPhone(rider.phone)
            Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [deleteRiderWithAuthCleanup] SUCCESS for rider: ${rider.phone}")
        } catch (e: Exception) {
            Log.e("LyoRepository", "Error in deleteRiderWithAuthCleanup: ${e.message}")
            Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [deleteRiderWithAuthCleanup] FAILED for rider: ${rider.phone}: ${e.message}")
            throw e
        }
    }

    suspend fun deleteAdmin(user: User) = withContext(Dispatchers.IO) {
        try {
            if (!LyoFirebaseHelper.ensureFirebaseAdminAuth()) {
                throw IllegalStateException("Your admin session has expired. Please log out and log in again.")
            }
            val success = LyoFirebaseHelper.deleteUserFromFirestore(user.phone)
            if (!success) {
                throw Exception("Failed to delete admin profile from Firestore.")
            }
            db.userDao.deleteUserByPhone(user.phone)
            Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [deleteAdmin] SUCCESS for admin: ${user.phone}")
        } catch (e: Exception) {
            Log.e("LyoRepository", "Firebase Firestore delete admin failed: ${e.message}")
            Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [deleteAdmin] FAILED for admin: ${user.phone}: ${e.message}")
            throw e
        }
    }

    // Shopping Cart Operations
    fun addToCart(item: MenuItem, supplier: Vendor) {
        val currentCart = cart.value.toMutableMap()
        val currentVendorValue = currentVendor.value
        
        // Reset cart if user switches vendors
        if (currentVendorValue != null && currentVendorValue.id != supplier.id) {
            currentCart.clear()
        }
        currentVendor.value = supplier
        currentCart[item] = (currentCart[item] ?: 0) + 1
        cart.value = currentCart
    }

    fun addToCartWithQuantity(item: MenuItem, supplier: Vendor, quantity: Int) {
        val currentCart = cart.value.toMutableMap()
        val currentVendorValue = currentVendor.value
        if (currentVendorValue != null && currentVendorValue.id != supplier.id) {
            currentCart.clear()
        }
        currentVendor.value = supplier
        currentCart[item] = (currentCart[item] ?: 0) + quantity
        cart.value = currentCart
    }

    fun removeFromCart(item: MenuItem) {
        val currentCart = cart.value.toMutableMap()
        val count = currentCart[item] ?: 0
        if (count > 1) {
            currentCart[item] = count - 1
        } else {
            currentCart.remove(item)
        }
        if (currentCart.isEmpty()) {
            currentVendor.value = null
        }
        cart.value = currentCart
    }

    fun removeItemCompletely(item: MenuItem) {
        val currentCart = cart.value.toMutableMap()
        currentCart.remove(item)
        if (currentCart.isEmpty()) {
            currentVendor.value = null
        }
        cart.value = currentCart
    }

    fun clearCart() {
        cart.value = emptyMap()
        currentVendor.value = null
    }

    // Checkout processing
    suspend fun placeOrder(
        userId: String,
        vendor: Vendor,
        subtotal: Double,
        deliveryFee: Double,
        couponDiscount: Double,
        tipAmount: Double,
        itemsList: List<Pair<MenuItem, Int>>,
        customerLat: Double,
        customerLng: Double,
        redeemedPoints: Int = 0
    ): Order = withContext(Dispatchers.IO) {
        val gst = if (gstEnabled) subtotal * (gstRate / 100.0) else 0.0
        val total = subtotal + gst + deliveryFee + tipAmount - couponDiscount
        val otp = (1000..9999).random().toString() // Randomized delivery security OTP
        
        val sortedItems = itemsList.sortedBy { it.first.id }.joinToString("-") { "${it.first.id}:${it.second}" }
        val tempUniqueOrderId = (System.currentTimeMillis() / 1000L) * 1000000L + (100000..999999).random()
        
        // 1. Check idempotency on Firestore first
        val checkedOrderId = LyoFirebaseHelper.checkOrCreateIdempotencyKey(
            userId = userId,
            vendorId = vendor.id,
            itemsSignature = sortedItems,
            newOrderId = tempUniqueOrderId
        )
        
        val uniqueOrderId: Long
        val isDuplicate: Boolean
        if (checkedOrderId != 0L && checkedOrderId != tempUniqueOrderId) {
            uniqueOrderId = checkedOrderId
            isDuplicate = true
            Log.d("LyoRepository", "Idempotency key match: Reusing existing order ID $uniqueOrderId")
        } else {
            uniqueOrderId = tempUniqueOrderId
            isDuplicate = false
        }

        if (isDuplicate) {
            val existingLocalOrder = db.orderDao.getOrderById(uniqueOrderId)
            if (existingLocalOrder != null) {
                activeLiveOrder.value = existingLocalOrder
                clearCart()
                return@withContext existingLocalOrder
            }
        }
 
        val isUpi = LyoFirebaseHelper.transientPaymentMethod == "UPI"
        val initialStatus = if (isUpi) "PAID_PENDING_VERIFICATION" else "PENDING"
        val initialPaymentStatus = if (isUpi) "PAID_PENDING_VERIFICATION" else "PENDING"
        val initialUpiTxId = if (isUpi) LyoFirebaseHelper.transientUpiTransactionId else ""

        val newOrder = Order(
            id = uniqueOrderId, // Pass directly so Room uses this unique ID instead of autoincrementing from 1
            userId = userId,
            vendorId = vendor.id,
            vendorName = vendor.name,
            status = initialStatus,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            couponDiscount = couponDiscount,
            tipAmount = tipAmount,
            totalAmount = if (total < 0.0) 0.0 else total,
            itemsCount = itemsList.sumOf { it.second },
            otpCode = otp,
            customerLat = customerLat,
            customerLng = customerLng,
            redeemedPoints = redeemedPoints,
            paymentMethod = LyoFirebaseHelper.transientPaymentMethod,
            paymentStatus = initialPaymentStatus,
            upiTransactionId = initialUpiTxId,
            gstAmount = gst
        )
        
        db.orderDao.insertOrder(newOrder)
        val savedOrder = newOrder
        
        // Save items
        val orderItems = itemsList.map { (menuItem, qty) ->
            OrderItem(
                orderId = uniqueOrderId,
                menuItemId = menuItem.id,
                nameEn = menuItem.nameEn,
                nameTa = menuItem.nameTa,
                quantity = qty,
                price = menuItem.price
            )
        }
        db.orderItemDao.insertOrderItems(orderItems)
        
        activeLiveOrder.value = savedOrder
        clearCart()
        
        // Direct suspended order synchronization to Firestore with 3 retries and exponential backoff
        var syncSuccess = false
        var attempts = 0
        var delayMs = 1000L
        var lastException: Exception? = null

        while (attempts < 3 && !syncSuccess) {
            attempts++
            try {
                if (com.example.BuildConfig.DEBUG) {
                    android.util.Log.d("LyoAuthDebug", "--- WRITING ORDER TO FIRESTORE (ek_orders) ---")
                    android.util.Log.d("LyoAuthDebug", "• Document/Order ID: ${savedOrder.id}")
                    android.util.Log.d("LyoAuthDebug", "• Order userId field: ${savedOrder.userId}")
                    android.util.Log.d("LyoAuthDebug", "• Current FirebaseAuth user UID: ${com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "NULL"}")
                    android.util.Log.d("LyoAuthDebug", "• Total Payable Amount: ₹${savedOrder.totalAmount}")
                }
                LyoFirebaseHelper.syncOrderToFirestore(savedOrder)
                syncSuccess = true
            } catch (e: Exception) {
                lastException = e
                Log.w("LyoRepository", "Firestore order sync attempt $attempts failed: ${e.message}")
                if (attempts < 3) {
                    delay(delayMs)
                    delayMs *= 2
                }
            }
        }

        val finalOrder = if (!syncSuccess && lastException != null) {
            val failureReason = when {
                lastException.message?.contains("permission", ignoreCase = true) == true ||
                lastException.message?.contains("denied", ignoreCase = true) == true -> {
                    LyoFirebaseHelper.getFriendlyPermissionErrorMessage(lastException)
                }
                lastException.message?.contains("network", ignoreCase = true) == true ||
                lastException.message?.contains("unavailable", ignoreCase = true) == true ||
                lastException.message?.contains("timeout", ignoreCase = true) == true -> "NETWORK_ERROR"
                else -> "OTHER_ERROR (${lastException.message})"
            }
            Log.e("LyoRepository", "Firestore sync failed completely after 3 attempts. Reason: $failureReason", lastException)

            val pendingOrder = savedOrder.copy(isPendingSync = true)
            db.orderDao.updateOrder(pendingOrder)
            activeLiveOrder.value = pendingOrder
            pendingOrder
        } else {
            savedOrder
        }

        // Trigger Instant Order Placed System Notification
        LyoFirebaseHelper.appContext?.let { ctx ->
            com.example.ui.screens.LyoNotificationHelper.showPushNotification(
                ctx,
                "Lyo AI • ஆர்டர் செய்யப்பட்டது 🎉",
                "ஆர்டர் #${finalOrder.id} எடப்பாடியில் வெற்றிகரமாக பதிவுபெற்றுள்ளது! விரைவில் உங்கள் இல்லம் வந்தடையும். 🛵"
            )
        }

        finalOrder
    }

    suspend fun retryPendingSyncs() = withContext(Dispatchers.IO) {
        try {
            val pending = db.orderDao.getPendingSyncOrders()
            if (pending.isNotEmpty()) {
                Log.d("LyoRepository", "Found ${pending.size} pending sync orders, retrying...")
                for (order in pending) {
                    try {
                        LyoFirebaseHelper.syncOrderToFirestore(order)
                        db.orderDao.updateOrder(order.copy(isPendingSync = false))
                        Log.d("LyoRepository", "Successfully synced pending order #${order.id}")
                    } catch (e: Exception) {
                        Log.w("LyoRepository", "Failed retrying sync for order #${order.id}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LyoRepository", "Error retrying pending syncs: ${e.message}")
        }
    }

    suspend fun saveOrderFromFirestore(order: Order) = withContext(Dispatchers.IO) {
        val existing = db.orderDao.getOrderById(order.id)
        if (existing != null) {
            // Rule 6: Realtime listeners must never overwrite a local CANCELLED order with an older Firestore status.
            if (existing.status == "CANCELLED" && order.status != "CANCELLED") {
                Log.d("LyoRepository", "Rule 6: Prevented overwriting local CANCELLED order #${order.id} with status ${order.status}")
                return@withContext
            }
        }
        db.orderDao.insertOrder(order)
        
        // Log status change if it's a test order
        if (LyoLiveTestTracker.isTestOrder(order)) {
            when (order.status) {
                "ACCEPTED" -> LyoLiveTestTracker.logAdminAcceptance(order.id)
                "DELIVERING" -> LyoLiveTestTracker.logDeparture(order.id)
                "DELIVERED" -> LyoLiveTestTracker.logCompletion(order.id)
            }
        }

        // If it is the activeLiveOrder, update its state flow too!
        val live = activeLiveOrder.value
        if (live != null && live.id == order.id) {
            activeLiveOrder.value = order
        }

        // Real-time Cloud updates -> System Push Notification
        if (existing != null && existing.status != order.status) {
            LyoFirebaseHelper.appContext?.let { ctx ->
                val tamilStatus = when (order.status) {
                    "PENDING" -> "ஆர்டர் சமர்ப்பிக்கப்பட்டு காத்திருப்பில் உள்ளது ⏳"
                    "PREPARING" -> "சமையலறையில் உங்கள் உணவு வேகமாய் தயாராகி வருகிறது! 🍳"
                    "READY_FOR_PICKUP" -> "உங்கள் உணவு பேக் செய்யபட்டு தயாராக உள்ளது! 🛍️"
                    "DELIVERING" -> "மகிழ்ச்சியான செய்தி! டெலிவரி தம்பி உங்கள் உணவை எடுத்துக்கொண்டு புறப்பட்டுவிட்டார்! 🏍️💨"
                    "DELIVERED" -> "உங்கள் உணவு வெற்றிகரமாக டெலிவரி செய்யப்பட்டுவிட்டது! மகிழ்ந்து சுவையுங்கள்! 🥳"
                    "CANCELLED" -> "உங்கள் ஆர்டர் ரத்து செய்யப்பட்டுள்ளது ❌"
                    else -> order.status
                }
                com.example.ui.screens.LyoNotificationHelper.showPushNotification(
                    ctx,
                    "Lyo Order Track • ஆர்டர் நிலை",
                    "ஆர்டர் #${order.id}: $tamilStatus"
                )
            }
        }
    }

    suspend fun saveDeliveryRideFromFirestore(ride: DeliveryRide) = withContext(Dispatchers.IO) {
        db.deliveryRideDao.insertDeliveryRide(ride)
        val order = db.orderDao.getOrderById(ride.orderId)
        if (order != null && LyoLiveTestTracker.isTestOrder(order)) {
            LyoLiveTestTracker.logGpsCoordinate(ride.orderId, ride.currentLat, ride.currentLng)
            if (ride.status == "DELIVERING") {
                LyoLiveTestTracker.logDeparture(ride.orderId)
            } else if (ride.status == "DELIVERED") {
                LyoLiveTestTracker.logCompletion(ride.orderId)
            }
        }
    }

    suspend fun cancelOrderCustomer(orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val result = LyoFirebaseHelper.cancelOrderCustomerTransaction(orderId)
        if (result.isSuccess) {
            db.orderDao.updateOrderStatus(orderId, "CANCELLED")
            val live = activeLiveOrder.value
            if (live != null && live.id == orderId) {
                activeLiveOrder.value = live.copy(status = "CANCELLED")
            }
            Result.success(Unit)
        } else {
            val ex = result.exceptionOrNull() ?: Exception("Unknown error")
            val friendlyMsg = LyoFirebaseHelper.getFriendlyPermissionErrorMessage(ex)
            Result.failure(Exception(friendlyMsg))
        }
    }

    suspend fun updateOrderStatus(orderId: Long, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        val oldOrder = db.orderDao.getOrderById(orderId)
        if (oldOrder != null) {
            if (oldOrder.status == "CANCELLED" || oldOrder.status == "DELIVERED") {
                Log.w("LyoRepository", "Cannot update order status: Order #${orderId} is already ${oldOrder.status}")
                return@withContext Result.failure(Exception("Cannot modify an order that is already ${oldOrder.status}."))
            }
        }
        
        // Write to Firestore first inside a transaction
        val txResult = if (status == "ACCEPTED") {
            LyoFirebaseHelper.adminAcceptOrderTransaction(orderId)
        } else {
            LyoFirebaseHelper.updateOrderStatusTransaction(orderId, status)
        }

        if (txResult.isFailure) {
            val ex = txResult.exceptionOrNull() ?: Exception("Unknown error")
            val errorMsg = LyoFirebaseHelper.getFriendlyPermissionErrorMessage(ex)
            Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [updateOrderStatus] FAILED for [order id=$orderId]: $errorMsg")
            Log.e("LyoRepository", "Order status transition to $status failed: $errorMsg")
            withContext(Dispatchers.Main) {
                LyoFirebaseHelper.appContext?.let { ctx ->
                    android.widget.Toast.makeText(ctx, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
            return@withContext Result.failure(Exception(errorMsg))
        }

        Log.i("FirestoreSyncAudit", "FirestoreSyncAudit: [updateOrderStatus] SUCCESS for [order id=$orderId] at ${System.currentTimeMillis()}")
        // Only update local Room after Firestore confirms success
        db.orderDao.updateOrderStatus(orderId, status)
        
        val testOrder = db.orderDao.getOrderById(orderId)
        if (testOrder != null && LyoLiveTestTracker.isTestOrder(testOrder)) {
            when (status) {
                "ACCEPTED" -> LyoLiveTestTracker.logAdminAcceptance(orderId)
                "DELIVERING" -> LyoLiveTestTracker.logDeparture(orderId)
                "DELIVERED" -> LyoLiveTestTracker.logCompletion(orderId)
            }
        }
        
        // Update live order cache if matched
        val live = activeLiveOrder.value
        if (live != null && live.id == orderId) {
            activeLiveOrder.value = live.copy(status = status)
        }

        if (status == "DELIVERED") {
            LyoFirebaseHelper.clearOrderMessages(orderId)
        }

        // Local State updates -> System Push Notification
        if (oldOrder != null && oldOrder.status != status) {
            LyoFirebaseHelper.appContext?.let { ctx ->
                val tamilStatus = when (status) {
                    "PENDING" -> "ஆர்டர் பெற்று காத்திருப்பில் உள்ளது ⏳"
                    "PREPARING" -> "சமையலறையில் உணவு சமைக்கப்பட்டு கொண்டிருக்கிறது! 🍳"
                    "READY_FOR_PICKUP" -> "உணவு சூடாய் பேக் செய்யபட்டு தயாராக உள்ளது! 🛍️"
                    "DELIVERING" -> "டெலிவரி தம்பி உங்கள் உணவை எடுத்துக்கொண்டு புறப்பட்டுவிட்டார்! 🏍️💨"
                    "DELIVERED" -> "ஆர்டர் வெற்றிகரமாக டெலிவரி செய்யப்பட்டது! 🥳"
                    "CANCELLED" -> "ஆற்டர் ரத்து செய்யப்பட்டுள்ளது ❌"
                    else -> status
                }
                com.example.ui.screens.LyoNotificationHelper.showPushNotification(
                    ctx,
                    "Lyo Order Track • ஆர்டர் நிலை",
                    "ஆர்டர் #${orderId}: $tamilStatus"
                )
            }
        }
        
        // Auto-create delivery rides on READY_FOR_PICKUP for proximity dispatch simulation
        if (status == "READY_FOR_PICKUP") {
            val order = db.orderDao.getOrderById(orderId)
            val existingRide = db.deliveryRideDao.getRideForOrder(orderId)
            if (order != null && existingRide == null) {
                val rideTs = System.currentTimeMillis() / 1000L
                val rideRand = (100000..999999).random()
                val uniqueRideId = rideTs * 1000000L + rideRand

                // Dynamically fetch any registered active delivery partner to assign this order's ride
                val assignedRider = db.userDao.getRiderForAssignment()
                val assignedPhone = assignedRider?.phone ?: "Unassigned"
                val assignedName = assignedRider?.name ?: "Lyo Express Rider"

                val ride = DeliveryRide(
                    id = uniqueRideId,
                    orderId = orderId,
                    riderName = assignedName,
                    riderPhone = assignedPhone,
                    status = "ACCEPTED",
                    currentLat = assignedRider?.lat ?: 11.5850, // Point near Salem Road, Idappadi
                    currentLng = assignedRider?.lng ?: 77.8420,
                    totalDistance = order.deliveryFee / 10.0, // mock calculation
                    earnings = (order.deliveryFee * 0.8) + order.tipAmount + 15.0, // earnings base: 80% delivery fee + tips + local allowance
                    riderUid = assignedRider?.uid ?: ""
                )
                db.deliveryRideDao.insertDeliveryRide(ride)
                if (LyoLiveTestTracker.isTestOrder(order)) {
                    LyoLiveTestTracker.logRiderAssignment(orderId)
                }
                val finalRide = ride
                LyoFirebaseHelper.syncDeliveryRideToFirestore(finalRide)
                try {
                    LyoFirebaseHelper.syncOrderToFirestore(order)
                } catch (e: Exception) {
                    Log.e("LyoRepository", "Failed syncing order rider uid on ready for pickup: ${e.message}")
                }

                // Dispatch assignment notification
                LyoFirebaseHelper.appContext?.let { ctx ->
                    com.example.ui.screens.LyoNotificationHelper.showPushNotification(
                        ctx,
                        "🛵 New Delivery Assignment • புதிய சவாரி",
                        "ஆர்டர் #${orderId} சவாரி ${assignedName}விற்கு அசைன் செய்யப்பட்டுள்ளது!"
                    )
                }
            }
        }
        Result.success(Unit)
    }

    suspend fun verifyPayment(orderId: Long, status: String, paymentStatus: String, rejectionReason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val oldOrder = db.orderDao.getOrderById(orderId)
        if (oldOrder != null) {
            if (oldOrder.status == "CANCELLED" || oldOrder.status == "DELIVERED") {
                Log.w("LyoRepository", "Cannot update payment: Order #${orderId} is already ${oldOrder.status}")
                return@withContext Result.failure(Exception("Cannot modify an order that is already ${oldOrder.status}."))
            }
        }

        db.orderDao.verifyPayment(orderId, status, paymentStatus, rejectionReason)

        val updatedOrder = db.orderDao.getOrderById(orderId)
        if (updatedOrder != null) {
            try {
                LyoFirebaseHelper.syncOrderToFirestore(updatedOrder)
            } catch (e: Exception) {
                Log.e("LyoRepository", "Payment verification Firestore sync failed: ${e.message}")
            }
        }
        
        // Update live order cache if matched
        val live = activeLiveOrder.value
        if (live != null && live.id == orderId) {
            activeLiveOrder.value = live.copy(status = status, paymentStatus = paymentStatus, rejectionReason = rejectionReason)
        }

        // Local State updates -> System Push Notification
        if (oldOrder != null && oldOrder.status != status) {
            LyoFirebaseHelper.appContext?.let { ctx ->
                val title = if (status == "CONFIRMED") "Payment Verified! 🎉" else "Payment Rejected ❌"
                val body = if (status == "CONFIRMED") "Your UPI payment for order #${orderId} is verified! We are preparing your food." else "Your payment was rejected: $rejectionReason"
                com.example.ui.screens.LyoNotificationHelper.showPushNotification(ctx, title, body)
            }
        }

        Result.success(Unit)
    }

    // Driver Operations
    suspend fun getRideById(id: Long): DeliveryRide? = withContext(Dispatchers.IO) {
        db.deliveryRideDao.getRideById(id)
    }

    suspend fun getRideForOrder(orderId: Long): DeliveryRide? = withContext(Dispatchers.IO) {
        db.deliveryRideDao.getRideForOrder(orderId)
    }

    fun getRideForOrderFlow(orderId: Long): Flow<DeliveryRide?> {
        return db.deliveryRideDao.getRideForOrderFlow(orderId)
    }

    suspend fun updateRide(ride: DeliveryRide) = withContext(Dispatchers.IO) {
        val oldRide = db.deliveryRideDao.getRideById(ride.id)
        db.deliveryRideDao.updateDeliveryRide(ride)
        LyoFirebaseHelper.syncDeliveryRideToFirestore(ride)
        
        val order = db.orderDao.getOrderById(ride.orderId)
        if (order != null && LyoLiveTestTracker.isTestOrder(order)) {
            LyoLiveTestTracker.logGpsCoordinate(ride.orderId, ride.currentLat, ride.currentLng)
            if (ride.status == "DELIVERING") {
                LyoLiveTestTracker.logDeparture(ride.orderId)
            } else if (ride.status == "DELIVERED") {
                LyoLiveTestTracker.logCompletion(ride.orderId)
            }
        }
        
        if (oldRide != null && oldRide.status != ride.status) {
            LyoFirebaseHelper.appContext?.let { ctx ->
                val statusMessage = when (ride.status) {
                    "ACCEPTED" -> "டெலிவரி நபர் உங்கள் ஆர்டரை ஏற்றுக்கொண்டார்! 🛵"
                    "DELIVERING" -> "டெலிவரி நபர் உங்கள் உணவை பெற்றுக்கொண்டு புறப்பட்டுவிட்டார்! 🏍️💨"
                    "DELIVERED" -> "ஆர்டர் வெற்றிகரமாக உங்களிடம் ஒப்படைக்கப்பட்டது! 🥳"
                    "CANCELLED" -> "டெலிவரி சவாரி ரத்து செய்யப்பட்டுள்ளது ❌"
                    else -> "டெலிவரி நிலை: ${ride.status}"
                }
                com.example.ui.screens.LyoNotificationHelper.showPushNotification(
                    ctx,
                    "Lyo Delivery • டெலிவரி நிலை",
                    "ஆர்டர் #${ride.orderId}: $statusMessage"
                )
            }
        }
    }

    private fun hashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password
        }
    }

    // Seeding Default High-Fidelity Data
    suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        try {
            // Hard-delete dummy customer and rider immediately to keep the database fresh and authentic
            try {
            userDao.deleteUserByPhone("9000000002")
            userDao.deleteUserByPhone("9000000003")
            val dummyRiderPhones = listOf("9999910001", "9999910002", "9999910003", "9999910004", "9999910005")
            for (p in dummyRiderPhones) {
                userDao.deleteUserByPhone(p)
            }
            if (LyoFirebaseHelper.isInitialized) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val fs = LyoFirebaseHelper.firestore
                        if (fs != null) {
                            val dummyPhones = listOf("9000000002", "9000000003") + dummyRiderPhones
                            for (phone in dummyPhones) {
                                val query = fs.collection("users").whereEqualTo("phone", phone).get().await()
                                for (doc in query.documents) {
                                    fs.collection("users").document(doc.id).delete().await()
                                    fs.collection("admins").document(doc.id).delete().await()
                                    Log.d("LyoRepository", "Deleted dummy user with phone $phone and uid ${doc.id} from Firestore")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LyoRepository", "Firebase deletion of dummy users failed: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LyoRepository", "Local deletion of dummy users failed: ${e.message}")
        }

        val existingGlobalCategories = db.categoryDao.getCategoriesForVendorList(-1L)
        if (existingGlobalCategories.isEmpty()) {
            val defaultGlobals = listOf(
                Category(vendorId = -1L, nameEn = "Restaurant", nameTa = "உணவகம்", sortOrder = 0, iconKey = "Restaurant", accentColor = "#16C7E8", isActive = true, iconImageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=300&q=80"),
                Category(vendorId = -1L, nameEn = "Cafe", nameTa = "காபி கடை", sortOrder = 1, iconKey = "Coffee", accentColor = "#FF7A1A", isActive = true, iconImageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=300&q=80"),
                Category(vendorId = -1L, nameEn = "Hotel", nameTa = "ஹோட்டல்", sortOrder = 2, iconKey = "LocalDining", accentColor = "#16A56B", isActive = true, iconImageUrl = "https://images.unsplash.com/photo-1610192244261-3f33de3f55e4?w=300&q=80"),
                Category(vendorId = -1L, nameEn = "Bakery", nameTa = "பேக்கரி", sortOrder = 3, iconKey = "Cake", accentColor = "#D94A52", isActive = true, iconImageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=300&q=80"),
                Category(vendorId = -1L, nameEn = "Snack Shop", nameTa = "சிற்றுண்டி", sortOrder = 4, iconKey = "LocalPizza", accentColor = "#A855F7", isActive = true, iconImageUrl = "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=300&q=80"),
                Category(vendorId = -1L, nameEn = "Dhaba", nameTa = "தாபா", sortOrder = 5, iconKey = "Store", accentColor = "#EC4899", isActive = true, iconImageUrl = "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=300&q=80")
            )
            for (cat in defaultGlobals) {
                db.categoryDao.insertCategory(cat)
            }
        }

        var existingVendors = db.vendorDao.getAllVendorsList()
        if (existingVendors.isNotEmpty()) {
            // Already seeded previously. DO NOT re-seed or overwrite anything!
            return@withContext
        }

        // Firestore is the SINGLE SOURCE OF TRUTH.
        // Before seeding, check if Firestore already contains any vendors.
        // If Firestore is not empty, we MUST skip seeding to avoid overwriting or seeding duplicate data.
        var waitCount = 0
        while (!LyoFirebaseHelper.isInitialized && waitCount < 50) {
            delay(100)
            waitCount++
        }

        var isFirestoreEmpty = true
        if (LyoFirebaseHelper.isInitialized) {
            try {
                // Wait up to 3000ms for response
                kotlinx.coroutines.withTimeoutOrNull(3000) {
                    val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("vendors")
                        .limit(1)
                        .get()
                        .await()
                    if (!snapshot.isEmpty) {
                        isFirestoreEmpty = false
                        Log.d("LyoRepository", "seedDatabaseIfNeeded: Firestore contains vendors. Skipping local seeding to respect Firestore single-source-of-truth.")
                    }
                }
            } catch (e: Exception) {
                Log.e("LyoRepository", "seedDatabaseIfNeeded: Error checking Firestore vendors: ${e.message}")
            }
        }

        if (!isFirestoreEmpty) {
            return@withContext
        }

        // 1. Seed Support, Customer, and Rider Users
        val seedUsers = emptyList<User>()

        for (u in seedUsers) {
            userDao.insertUser(u)
            val seedPass = "LyoTest123"
            
            // Save password hash locally for offline login verification during seeding
            if (seedPass != null) {
                LyoFirebaseHelper.appContext?.let { context ->
                    try {
                        val hash = LyoFirebaseHelper.hashPassword(seedPass)
                        val sharedPrefs = context.getSharedPreferences("lyo_offline_passwords", android.content.Context.MODE_PRIVATE)
                        sharedPrefs.edit().putString("pass_hash_${u.phone}", hash).apply()
                    } catch (e: Exception) {
                        Log.e("LyoRepository", "Failed to save offline password hash during seed: ${e.message}")
                    }
                }
            }
            
            if (LyoFirebaseHelper.isInitialized) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        LyoFirebaseHelper.registerInFirebase(u, seedPass)
                    } catch (e: Exception) {
                        Log.e("LyoRepository", "Field seeding registration for ${u.phone} failed: ${e.message}")
                    }
                }
            }
        }

        // 1.5. Seed Default Promo Banners
        db.promoBannerDao.insertPromoBanner(PromoBanner(code = "LYOFRESH", description = "₹80 OFF on orders above ₹300", imageUrl = ""))
        db.promoBannerDao.insertPromoBanner(PromoBanner(code = "CHENNADI70", description = "₹50 OFF on traditional South Indian tiffins", imageUrl = ""))

        // 2. Seed Default Vendors, Categories, and MenuItems with Tamil bilingual pair
        
        // A. Saravana Bhavan (Hotel)
        val sbId = db.vendorDao.insertVendor(Vendor(
            name = "Saravana Bhavan",
            nameTa = "சரவண பவன்",
            type = "Hotel",
            rating = 4.6,
            distance = 1.2,
            deliveryTime = 25,
            deliveryFee = 45.00,
            address = "Salem Bypass Road, Idappadi, Salem",
            lat = 11.5835,
            lng = 77.8442,
            bannerUrl = "https://images.unsplash.com/photo-1541832676-9b763b0239ab?auto=format&fit=crop&w=800&q=80",
            freeDeliveryThreshold = 400.0,
            minOrderAmount = 100.0,
            isCouponEnabled = true,
            couponCode = "CHENNADI70",
            couponDiscount = 50.0,
            couponMinOrder = 100.0,
            phone = "+919444012345"
        ))
        
        val sbCatId1 = db.categoryDao.insertCategory(Category(vendorId = sbId, nameEn = "Traditional Tiffin", nameTa = "பாரம்பரிய சிற்றுண்டி"))
        val sbCatId2 = db.categoryDao.insertCategory(Category(vendorId = sbId, nameEn = "Lunch Specials", nameTa = "மதிய உணவு"))

        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Ghee Roast Dosa", nameTa = "நெய் ரோஸ்ட் தோசை", descEn = "Crisp golden crepe smeared with pure aromatic Desi ghee.", descTa = "தூய வாசனையுள்ள நெய் தடவிய பொன்னிற மொறுமொறு தோசை.", price = 110.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Idli Sambar Combo", nameTa = "இட்லி சாம்பார் காம்போ", descEn = "Two steamed rice cakes floating in delicious spicy lentil soup.", descTa = "சுவையான கமகமக்கும் சாம்பாரில் மிதக்கும் இரண்டு இட்லி.", price = 60.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Medhu Vada", nameTa = "மெது வடை", descEn = "Crisp deep-fried savory doughnut infused with peppercorns.", descTa = "மிளகு வாசனை வீசும் மொறுமொறுப்பான உளுந்து வடை.", price = 45.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Special Rava Khichdi", nameTa = "ரவா கிச்சடி", descEn = "Roasted semolina cooked dry with loaded garden vegetables.", descTa = "வறுத்த ரவையுடன் காய்கறிகள் சேர்த்து செய்யப்பட்ட கிச்சடி.", price = 85.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Poori Masala Potato", nameTa = "பூரி மசாலா", descEn = "Fluffy deep fried soft flour poori served with potato masala.", descTa = "உருளைக்கிழங்கு மசாலாவுடன் பரிமாறப்படும் பஞ்சு போன்ற பூரி.", price = 90.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Sambar Vada Glow", nameTa = "வடா சாம்பார்", descEn = "Two deep fried lentil donuts completely submerged in hotel sambar.", descTa = "வடிகட்டப்பட்ட சுவையான சாம்பாரில் மிதக்கும் உளுந்து வடை.", price = 50.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Traditional Pongal Ghee", nameTa = "நெய் பொங்கல்", descEn = "Steamed rice & lentils tempered with peppercorns, cashews and rich ghee.", descTa = "நெய், மிளகு, முந்திரி சேர்த்து சமைக்கப்பட்ட கமகமக்கும் பொங்கல்.", price = 85.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Onion Podi Uttapam", nameTa = "பொடி ஊத்தப்பம்", descEn = "Thick rice pancake loaded with chopped shallots and gun powder.", descTa = "பொடி மற்றும் வெங்காயம் தூவிய தடிமனான அரிசி மாவு ஊத்தப்பம்.", price = 120.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId1, nameEn = "Mini Idli Sambar (14 Pcs)", nameTa = "மினி இட்லி", descEn = "Teeny small rice idli buttons doused in hotel spiced sambar.", descTa = "மினி இட்லி குண்டுகள் மிதக்கும் சாம்பார் குவளை.", price = 95.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
 
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId2, nameEn = "South Indian Meals", nameTa = "தென்னிந்திய சாப்பாடு", descEn = "Authentic platter of rice, sambar, rasam, kootu, poriyal, and payasam.", descTa = "அன்னம், சாம்பார், ரசம், கூட்டு, பொரியல் மற்றும் பாயாசம் அடங்கிய சாப்பாடு.", price = 180.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1610192244261-3f33de3f55e4?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = sbId, categoryId = sbCatId2, nameEn = "Pineapple Kesari Sweet", nameTa = "பைனாப்பிள் கேசரி", descEn = "Classic roasted semolina dessert infused with pineapple bits.", descTa = "பைனாப்பிள் சுவையுடன் செய்யப்பட்ட பாரம்பரிய நெய் கேசரி.", price = 75.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1589301973394-b2b1fa8ae0c1?auto=format&fit=crop&w=400&q=80"))
 
        // B. Anjappar Chettinad (Restaurant)
        val ajId = db.vendorDao.insertVendor(Vendor(
            name = "Anjappar Chettinad Restaurant",
            nameTa = "அஞ்சப்பர் செட்டிநாடு உணவகம்",
            type = "Restaurant",
            rating = 4.4,
            distance = 2.4,
            deliveryTime = 35,
            deliveryFee = 60.00,
            address = "New Bus Stand Junction, Salem",
            lat = 11.5910,
            lng = 77.8505,
            bannerUrl = "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?auto=format&fit=crop&w=800&q=80",
            freeDeliveryThreshold = 600.0,
            minOrderAmount = 150.0,
            isCouponEnabled = true,
            couponCode = "LYOFRESH",
            couponDiscount = 80.0,
            couponMinOrder = 300.0,
            phone = "+919444054321"
        ))
        
        val ajCatId1 = db.categoryDao.insertCategory(Category(vendorId = ajId, nameEn = "Chettinad Starters", nameTa = "செட்டிநாடு துவகங்கள்"))
        val ajCatId2 = db.categoryDao.insertCategory(Category(vendorId = ajId, nameEn = "Breads and Curries", nameTa = "ரொட்டி மற்றும் குழம்புகள்"))
 
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId1, nameEn = "Chettinad Pepper Chicken", nameTa = "செட்டிநாடு மிளகு கோழி", descEn = "Boneless chicken cubes tossed with fiery freshly-ground pepper.", descTa = "புதிதாக அரைத்த மிளகால் சமைக்கப்பட்ட வறுத்த காரசாரமான கோழிக்கறி.", price = 280.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId1, nameEn = "Mutton Seeraga Samba Biryani", nameTa = "ஆட்டுக்கறி பிரியாணி", descEn = "Rich biryani layered with Seeraga Samba rice and tender mutton pieces.", descTa = "சீரக சம்பா அரிசி மற்றும் மென்மையான ஆட்டுக்கறியுடன் கூடிய பிரியாணி.", price = 350.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId1, nameEn = "Spicy Chicken 65 (Dry)", nameTa = "சிக்கன் 65", descEn = "Crunchy deep fried boneless chicken breast cubes with chilies.", descTa = "காரசாரமாக வறுக்கப்பட்ட மொறுமொறுப்பான எலும்பில்லா சிக்கன் துண்டுகள்.", price = 190.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId1, nameEn = "Anjappar Mutton Chukka", nameTa = "ஆட்டுக்கறி சுக்கா", descEn = "Spicy slow roasted bone mutton pieces cooked in regional masala.", descTa = "செட்டிநாடு மசாலாப் பொடிகளுடன் வறுத்தெடுக்கப்பட்ட ஆட்டுக்கறி சுக்கா.", price = 340.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId1, nameEn = "Tandoori Chicken (Half)", nameTa = "தந்தூரி சிக்கன்", descEn = "Spicy skewered chicken leg pieces baked in clay-tandoor oven.", descTa = "தணலில் சுடப்பட்ட சுவையான செட்டிநாடு தந்தூரி கோழி.", price = 250.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?auto=format&fit=crop&w=400&q=80"))
 
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId2, nameEn = "Garlic Rotti", nameTa = "கார்லிக் ரொட்டி", descEn = "Clay-oven flatbread flavored with chopped garlic and butter.", descTa = "பூண்டு மற்றும் வெண்ணெய் பூசப்பட்ட தணல் ரொட்டி.", price = 90.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId2, nameEn = "Paneer Butter Masala", nameTa = "பனீர் பட்டர் மசாலா", descEn = "Indian cottage cheese cooked in creamy rich cottage tomato sauce.", descTa = "கிரீமி தக்காளி சாஸில் சமைக்கப்பட்ட பனீர் துண்டுகள்.", price = 220.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId2, nameEn = "Chettinad Chicken Masala", nameTa = "சிக்கன் மசாலா", descEn = "Famous hot chicken gravy cooked in authentic coconut-poppy sauce.", descTa = "செட்டிநாடு தேங்காய் மசாலா சாறில் சமைக்கப்பட்ட சிறந்த நாட்டுக்கோழி கிரேவி.", price = 260.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId2, nameEn = "Butter Naan Flatbread", nameTa = "வெண்ணெய் நான்", descEn = "Leavened oven-baked flatbread glazed with butter.", descTa = "தணல் அடுப்பில் சுடப்பட்டு வெண்ணெய் பூசப்பட்ட மென்மையான நான் ரொட்டி.", price = 70.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId2, nameEn = "Kadai Paneer Gravy", nameTa = "கடாய் பனீர்", descEn = "Cottage cheese pieces wok-fried with bell peppers in spicy gravy.", descTa = "குடைமிளகாய் மற்றும் பனீர் சேர்த்து வதக்கி சமைக்கப்பட்ட காரசாரமான கிரேவி.", price = 210.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId2, nameEn = "Gobi Manchurian Crisp", nameTa = "கோபி மஞ்சூரியன்", descEn = "Spicy Indo-Chinese cauliflower florets tossed in tangy ginger soy sauce.", descTa = "இஞ்சி மற்றும் சோயா சாஸில் வதக்கப்பட்ட மொறுமொறு காலிபிளவர் மஞ்சூரியன்.", price = 180.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ajId, categoryId = ajCatId2, nameEn = "Chicken Fried Rice", nameTa = "கோழி வறுத்த சாதம்", descEn = "Premium Basmati rice stir-fried with eggs, vegetables, and chicken.", descTa = "முட்டை, காயறி மற்றும் கோழிக் கறியுடன் வதக்கப்பட்ட பாசுமதி சாதம்.", price = 220.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?auto=format&fit=crop&w=400&q=80"))
 
        // C. Madras Coffee House (Cafe)
        val mchId = db.vendorDao.insertVendor(Vendor(
            name = "Madras Coffee House",
            nameTa = "மெட்ராஸ் காபி ஹவுஸ்",
            type = "Cafe",
            rating = 4.8,
            distance = 0.5,
            deliveryTime = 15,
            deliveryFee = 25.00,
            address = "High Road, Idappadi, Salem",
            lat = 11.5801,
            lng = 77.8490,
            bannerUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=800&q=80",
            freeDeliveryThreshold = 250.0,
            minOrderAmount = 50.0,
            isCouponEnabled = true,
            couponCode = "CHENNADI70",
            couponDiscount = 50.0,
            couponMinOrder = 100.0,
            phone = "+919840011223"
        ))
        
        val mchCatId = db.categoryDao.insertCategory(Category(vendorId = mchId, nameEn = "Hot Brews", nameTa = "சூடான பானங்கள்"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Filter Coffee", nameTa = "பில்டர் காபி", descEn = "Strong traditional South Indian filter coffee brewed with chicory.", descTa = "பாரம்பரிய முறையில் வடிகட்டப்பட்ட திக்கான பில்டர் காபி.", price = 40.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Irani Masala Chai", nameTa = "மசாலா டீ", descEn = "Spicy milky ginger and cardamom tea brewed slowly.", descTa = "இஞ்சி மற்றும் ஏலக்காய் மணத்துடன் கூடிய பால் டீ.", price = 50.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Lemon Ginger Honey Tea", nameTa = "லெமன் டீ", descEn = "Zesty light hot tea brewed with lemon juice and wild honey.", descTa = "எலுமிச்சம்பழ சாறு மற்றும் தூய தேனுடன் கூடிய புத்துணர்ச்சி டீ.", price = 45.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Madras Bread Omelette", nameTa = "பிரெட் ஆம்லெட்", descEn = "Popular street styled toasted butter bread sandwich with masala egg.", descTa = "வெண்ணெய்யில் டோஸ்ட் செய்த பிரெட் மற்றும் கார முட்டை ஆம்லெட்.", price = 75.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Crunchy Onion Samosa (2 Pcs)", nameTa = "வெங்காய சமோசா", descEn = "Crispy triangle puffs loaded with fried onions and peas.", descTa = "காரசாரமான வெங்காய மசாலா நிரப்பப்பட்ட மொறுமொறு சமோசா ஜோடி.", price = 30.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Paneer Tikka Roll", nameTa = "பனீர் ரோல்", descEn = "Whole wheat wrap rolled with spiced paneer tikka and mint sauce.", descTa = "பனீர் டிக்கா மற்றும் புதினா சட்னி கொண்டு சுருட்டப்பட்ட கோதுமை ரோல்.", price = 120.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Classic Cold Coffee", nameTa = "கோல்ட் காபி", descEn = "Rich whipped espresso milk shaken with ice cream blocks.", descTa = "ஐஸ்கிரீம் மற்றும் எஸ்பிரெசோ பால் கலக்கப்பட்ட குளுமையான கோல்ட் காபி.", price = 90.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = mchId, categoryId = mchCatId, nameEn = "Hot Bournvita Milk", nameTa = "போர்ன்விட்டா பால்", descEn = "Creamy steamed milk fortified with sweet chocolate malted Bournvita.", descTa = "போர்ன்விட்டா சாக்லேட் மால்ட் கலக்கப்பட்ட சுடச்சுட பால்.", price = 60.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=400&q=80"))
 
        // D. Iyengar Bakery (Bakery)
        val ibId = db.vendorDao.insertVendor(Vendor(
            name = "Iyengar Bakery",
            nameTa = "ஐயங்கார் பேக்கரி",
            type = "Bakery",
            rating = 4.5,
            distance = 1.8,
            deliveryTime = 20,
            deliveryFee = 30.00,
            address = "Sangagiri Main Road, Idappadi",
            lat = 11.5714,
            lng = 77.8395,
            bannerUrl = "https://images.unsplash.com/photo-1608686207856-001b95cf60ca?auto=format&fit=crop&w=800&q=80",
            freeDeliveryThreshold = 300.0,
            minOrderAmount = 60.0,
            isCouponEnabled = true,
            couponCode = "LYOFRESH",
            couponDiscount = 80.0,
            couponMinOrder = 300.0,
            phone = "+919840033445"
        ))
        
        val ibCatId = db.categoryDao.insertCategory(Category(vendorId = ibId, nameEn = "Hot Puffs & Cakes", nameTa = "பஃப் மற்றும் கேக் வகைகள்"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Crisp Veg Puff", nameTa = "வெஜிடபிள் பஃப்", descEn = "Layered crispy pastry stuffed with spicy potato and green peas.", descTa = "காரசாரமான உருளைக்கிழங்கு மற்றும் பட்டாணி நிரப்பப்பட்ட பஃப்.", price = 30.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Old School Honey Cake", nameTa = "தேன் கேக்", descEn = "Classic sponge cake infused with honey syrup and coconut shavings.", descTa = "தேனில் நனைந்த ஜாம் மற்றும் தேங்காய் துருவல் தூவிய ஸ்பாஞ்ச் கேக்.", price = 60.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1587314168485-3236d6710814?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Sweet Coconut Bun", nameTa = "தேங்காய் பன்", descEn = "Fluffy sweet bakery bun filled with sweetened coconut and cherries.", descTa = "தேங்காய் துருவல் மற்றும் செர்ரி நிரப்பப்பட்ட இனிப்பு பன்.", price = 45.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Premium Egg Puff Single", nameTa = "முட்டை பஃப்", descEn = "Multi layered baked crispy pastry containing spicy boiled egg half.", descTa = "அவித்த முட்டை மற்றும் மசாலா பாதியுடன் பேக் செய்யப்பட்ட மொறுமொறு பஃப்.", price = 35.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Baked Dilpasand Cake", nameTa = "தில்பசந்த்", descEn = "Stuffed circular pastry filled with sweet visual tutti-frutti and nuts.", descTa = "நட்ஸ் மன்றும் டூட்டி-ப்ரூட்டி இனிப்புகள் நிரப்பப்பட்ட பாரம்பரிய தில்பசந்த்.", price = 80.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Rich Plum Cake Slice", nameTa = "பிளம் கேக்", descEn = "Aromatic traditional baked dark cake packed with dry winter plums.", descTa = "விண்டர் உலர் திராட்சை மற்றும் பழங்களுடன் பேக் செய்யப்பட்ட பிளம் கேக்.", price = 70.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1587314168485-3236d6710814?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Cream Bun Original", nameTa = "கிரீம் பன்", descEn = "Buttery soft bun sliced and loaded with sweet vanilla cream icing.", descTa = "வெண்ணிலா இனிப்பு கிரீமால் நிரப்பப்பட்ட மென்மையான பன் ரொட்டி.", price = 40.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Bakers Chocolate Truffle Ring", nameTa = "சாக்லேட் கேக்", descEn = "Decadent whole circular soft chocolate truffle mousse ring cake.", descTa = "டபுள் சாக்லேட் லேயரால் செய்யப்பட்ட சிறந்த சாக்லேட் ரிங் கேக்.", price = 150.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = ibId, categoryId = ibCatId, nameEn = "Crisp Butter Biscuits (Box)", nameTa = "வெண்ணெய் பிஸ்கட்", descEn = "Crispy buttery handmade oven salted cookies that melt in your mouth.", descTa = "வாயில் கரையும் கையால் செய்யப்பட்ட மொறுமொறு வெண்ணெய் பிஸ்கட் பெட்டி.", price = 95.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1608686207856-001b95cf60ca?auto=format&fit=crop&w=400&q=80"))
 
        // E. Murugan Snacks (Snack Shop)
        val msId = db.vendorDao.insertVendor(Vendor(
            name = "Murugan Snacks & Sweets",
            nameTa = "முருகன் ஸ்நாக்ஸ் & ஸ்வீட்ஸ்",
            type = "Snack Shop",
            rating = 4.2,
            distance = 0.8,
            deliveryTime = 22,
            deliveryFee = 25.00,
            address = "Salem Road, Idappadi, Salem",
            lat = 11.5828,
            lng = 77.8488,
            bannerUrl = "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=800&q=80",
            freeDeliveryThreshold = 200.0,
            minOrderAmount = 50.0,
            isCouponEnabled = true,
            couponCode = "LYOFRESH",
            couponDiscount = 80.0,
            couponMinOrder = 300.0,
            phone = "+919840055667"
        ))
        
        val msCatId = db.categoryDao.insertCategory(Category(vendorId = msId, nameEn = "Crisp Starters", nameTa = "மொறுமொறு நொறுக்குத்தீனி"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = msId, categoryId = msCatId, nameEn = "Onion Pakoda", nameTa = "வெங்காய பகோடா", descEn = "Crisp fried gram-flour fritters loaded with sliced onions and chilies.", descTa = "வெங்காயம் மற்றும் கடலை மாவுடன் வறுத்த மொறுமொறு பகோடா.", price = 75.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = msId, categoryId = msCatId, nameEn = "Sweet Kozhukattai", nameTa = "கொழுகட்டை", descEn = "Steamed rice flour dumplings stuffed with sweet jaggery and coconut.", descTa = "வெல்லம் மற்றும் தேங்காய் பூரணம் நிரப்பப்பட்டு அவிக்கப்பட்ட கொழுகட்டை.", price = 45.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = msId, categoryId = msCatId, nameEn = "Traditional Ghee Mysore Pak", nameTa = "மைசூர் பாக்", descEn = "Authentic soft premium sweet made of gram flour, sugar and lots of ghee.", descTa = "வாயில் இட்டதும் கரையும் நெய் மற்றும் கடலைம்மாவில் சமைத்த மைசூர் பாக்.", price = 160.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = msId, categoryId = msCatId, nameEn = "Crunchy Kai Murukku (Pack)", nameTa = "கை முறுக்கு", descEn = "Crisp and coiled salted rice flour handmade murukku munchies.", descTa = "பாரம்பரிய அரிசி மாவில் கையால் செய்த மொறுமொறு கை முறுக்கு.", price = 105.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = msId, categoryId = msCatId, nameEn = "Spicy Kara Boondhi (150g)", nameTa = "கார பூந்தி", descEn = "Fired gram flour pearls seasoned with peanuts, curry leaves, and chili.", descTa = "முந்திரி மற்றும் கருவேப்பிலை வறுத்த சுவையான காரசார கார பூந்தி.", price = 85.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = msId, categoryId = msCatId, nameEn = "Hot Jalebi (Circle)", nameTa = "ஜிலேபி", descEn = "Freshly fried spiral dessert completely soaked in boiling sugar syrup.", descTa = "சர்க்கரை பாகில் ஊறிய கிரிஸ்பி இனிப்பான சூடான ஜிலேபி வளையங்கள்.", price = 120.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = msId, categoryId = msCatId, nameEn = "Motichoor Laddoo (4 Pcs)", nameTa = "லட்டு", descEn = "Delicious round orange dessert structured with fine gram pearls.", descTa = "கடலை மாவு முந்துகளாக செய்யப்பட்ட சுவையான சாப்ட் மோதிசூர் லட்டு.", price = 130.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=400&q=80"))
 
        // F. Sher-e-Punjab Highway Dhaba (Dhaba)
        val dhabaId = db.vendorDao.insertVendor(Vendor(
            name = "Sher-e-Punjab Highway Dhaba",
            nameTa = "ஷேர்-இ-பஞ்சாப் ஹைவே தாபா",
            type = "Dhaba",
            rating = 4.7,
            distance = 3.5,
            deliveryTime = 40,
            deliveryFee = 70.00,
            address = "Salem-Cochin Highway Bypass, Salem",
            lat = 11.6050,
            lng = 77.8630,
            bannerUrl = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=800&q=80",
            freeDeliveryThreshold = 700.0,
            minOrderAmount = 150.0,
            isCouponEnabled = true,
            couponCode = "LYOFRESH",
            couponDiscount = 80.0,
            couponMinOrder = 300.0,
            phone = "+919840077889"
        ))
 
        val dhabaCatId1 = db.categoryDao.insertCategory(Category(vendorId = dhabaId, nameEn = "Dhaba Tandoori Specialties", nameTa = "தாபா தந்தூரி உணவுகள்"))
        val dhabaCatId2 = db.categoryDao.insertCategory(Category(vendorId = dhabaId, nameEn = "Rich Gravies & Rice", nameTa = "பஞ்சாபி கிரேவி மற்றும் சாதம்"))
        val dhabaCatId3 = db.categoryDao.insertCategory(Category(vendorId = dhabaId, nameEn = "Dhaba Refreshers", nameTa = "பானங்கள்"))
 
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId1, nameEn = "Tandoori Roti Butter", nameTa = "தந்தூரி ரொட்டி", descEn = "Crisp whole wheat flatbread baked directly in charcoal clay oven.", descTa = "தணல் மண் அடுப்பில் சுடப்பட்ட மொறுமொறுப்பான கோதுமை ரொட்டி.", price = 55.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId1, nameEn = "Butter Naan Giant", nameTa = "பட்டர் நான்", descEn = "Mammoth soft naans glazed with local churned white butter.", descTa = "நாடன் வெண்ணெய் தடவப்பட்ட மென்மையான பஞ்சாபி நான் ரொட்டி.", price = 105.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId2, nameEn = "Dal Makhani Slow-cooked", nameTa = "தால் மக்கானி", descEn = "Black lentils simmered overnight on slow fire with butter and cream.", descTa = "குளிர்கால தணலில் வெண்ணெய் சேர்த்து இரவோடு இரவாக சமைத்த தால் மக்கானி.", price = 250.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId2, nameEn = "Paneer Tikka Masala Dhaba Style", nameTa = "பனீர் டிக்கா மசாலா", descEn = "Skewered spicy paneer cubes cooked in rich onion tomato bell pepper gravy.", descTa = "தணலில் வறுத்த பனீர் மற்றும் குடைமிளகாய் சேர்த்து செய்த மசாலா கிரேவி.", price = 315.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId2, nameEn = "Jeera Basmati Rice", nameTa = "சீரக சாதம்", descEn = "Fluffy aromatic long seed Basmati rice tempered with cumin.", descTa = "குமின் சேர்த்து சமைக்கப்பட்ட நீண்ட கமகமக்கும் பாசுமதி சீரக சாதம்.", price = 185.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId2, nameEn = "Dhaba Butter Chicken", nameTa = "தாபா பட்டர் சிக்கன்", descEn = "Rich, smoky tandoori chicken shredded and cooked in butter cashew curry.", descTa = "தந்தூரியில் வறுக்கப்பட்டு முந்திரி கிரேவியில் சமைக்கப்பட்ட பட்டர் சிக்கன்.", price = 380.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId2, nameEn = "Spicy Egg Bhurji", nameTa = "முட்டை புர்ஜி", descEn = "Rustic scrambled farm eggs with chopped onions, green chilies, and coriander.", descTa = "வெங்காயம், பச்சை மிளகாய், மல்லி தூவி வதக்கிய முட்டை புர்ஜி.", price = 145.0, isVeg = false, imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=400&q=80"))
        db.menuItemDao.insertMenuItem(MenuItem(vendorId = dhabaId, categoryId = dhabaCatId3, nameEn = "Grand Punjabi Lassi Sweet", nameTa = "பஞ்சாபி லஸ்ஸி", descEn = "Churned thick local yogurt beverage flavored with cardamoms and malai.", descTa = "தயிர் மற்றும் மலாய் கொண்டு தயாரிக்கப்பட்ட குளுமையான பஞ்சாபி லஸ்ஸி.", price = 105.0, isVeg = true, imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=400&q=80"))
        
        // Seed Merry World Vendor as well
        try {
            seedMerryWorldVendor(db)
        } catch (e: Exception) {
            Log.e("LyoRepository", "Error seeding Merry World statically: ${e.message}")
        }

        // Seed default reviews for other vendors
        try {
            db.reviewDao.insertReview(Review(
                vendorId = sbId,
                userName = "Muthu Kumar",
                rating = 5,
                comment = "Best Ghee Roast Dosa in Idappadi! Super crispy and served hot. சாம்பார் மிக அருமை!"
            ))
            db.reviewDao.insertReview(Review(
                vendorId = sbId,
                userName = "Anantharaj",
                rating = 4,
                comment = "Idli is incredibly soft. Sambar is authentic. A bit crowded during weekends."
            ))
            db.reviewDao.insertReview(Review(
                vendorId = sbId,
                userName = "Priya S",
                rating = 5,
                comment = "Traditional taste maintained perfectly. Prompt and clean packaging."
            ))

            db.reviewDao.insertReview(Review(
                vendorId = ajId,
                userName = "Karthik R",
                rating = 5,
                comment = "Spicy Chettinad Pepper Chicken was outstanding. Authentic spices."
            ))
            db.reviewDao.insertReview(Review(
                vendorId = ajId,
                userName = "Meenakshi",
                rating = 4,
                comment = "Biryani is flavorful and mutton is tender. Excellent packing."
            ))

            db.reviewDao.insertReview(Review(
                vendorId = mchId,
                userName = "Vijay",
                rating = 5,
                comment = "Perfect Filter Coffee! It wakes you right up. ஏலக்காய் டீ மிக நன்று."
            ))
            db.reviewDao.insertReview(Review(
                vendorId = mchId,
                userName = "Divya",
                rating = 4,
                comment = "Quick cup of coffee on the go. High standards maintained."
            ))

            db.reviewDao.insertReview(Review(
                vendorId = dhabaId,
                userName = "Ranveer Singh",
                rating = 5,
                comment = "Absolute best butter chicken and garlic naan. Authentic Punjabi dhaba taste."
            ))
        } catch (e: Exception) {
            Log.e("LyoRepository", "Error seeding default reviews: ${e.message}")
        }

        // Push the newly seeded data to Firestore so Firestore is fully populated
        if (LyoFirebaseHelper.isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("LyoRepository", "Uploading seeded database to Firestore...")
                    syncAllLocalToFirestore()
                } catch (e: Exception) {
                    Log.e("LyoRepository", "Failed to upload seeded database to Firestore: ${e.message}")
                }
            }
        }
        } catch (e: Exception) {
            Log.w("LyoRepository", "Database access exception during seeding (possibly closed during test teardown): ${e.message}")
        }
    }

    private suspend fun seedMerryWorldVendor(db: AppDatabase) = withContext(Dispatchers.IO) {
        val mwId = db.vendorDao.insertVendor(Vendor(
            name = "Merry World",
            nameTa = "மெர்ரி வேர்ல்டு",
            type = "Cafe",
            rating = 4.8,
            distance = 1.1,
            deliveryTime = 20,
            deliveryFee = 35.00,
            address = "Salem Bypass Road, Idappadi",
            lat = 11.5815,
            lng = 77.8468,
            bannerUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=800&q=80",
            freeDeliveryThreshold = 350.0,
            minOrderAmount = 100.0,
            isCouponEnabled = true,
            couponCode = "LYOFRESH",
            couponDiscount = 80.0,
            couponMinOrder = 300.0,
            phone = "+919444099999"
        ))

        suspend fun add(catId: Long, name: String, price: Double, isVeg: Boolean, imgUrl: String = "") {
            db.menuItemDao.insertMenuItem(MenuItem(
                vendorId = mwId,
                categoryId = catId,
                nameEn = name,
                nameTa = name,
                descEn = "Freshly prepared delicious $name.",
                descTa = "சுவையான தயாரிப்பு: $name.",
                price = price,
                isVeg = isVeg,
                imageUrl = imgUrl
            ))
        }

        // 1. Fried Chicken
        val c1 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Fried Chicken", nameTa = "Fried Chicken"))
        add(c1, "1 Pc Hot & Crispy", 110.0, false, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=400&q=80")
        add(c1, "2 Pc Hot & Crispy", 208.0, false, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=400&q=80")
        add(c1, "2 Pc Hot & Crispy (Drumstick)", 221.0, false, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=400&q=80")
        add(c1, "4 Pc Hot & Crispy", 404.0, false, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=400&q=80")
        add(c1, "6 Pc Hot & Crispy", 576.0, false, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=400&q=80")
        add(c1, "8 Pc Hot & Crispy", 760.0, false, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=400&q=80")

        // 2. Veg Burger
        val c2 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Veg Burger", nameTa = "Veg Burger"))
        add(c2, "Classic Veg Burger", 123.0, true, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80")
        add(c2, "Panner Burger", 184.0, true, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80")

        // 3. Veg Sandwich
        val c3 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Veg Sandwich", nameTa = "Veg Sandwich"))
        add(c3, "Chocolate Sandwich", 121.0, true, "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=400&q=80")
        add(c3, "Merry World Spcl Sandwich", 135.0, true, "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=400&q=80")
        add(c3, "Mayo Corn Sandwich", 135.0, true, "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=400&q=80")
        add(c3, "Veg Sandwich", 135.0, true, "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=400&q=80")
        add(c3, "Paneer Sandwich", 172.0, true, "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=400&q=80")

        // 4. Finger Foods
        val c4 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Finger Foods", nameTa = "Finger Foods"))
        add(c4, "Chicken Popcorn Small", 147.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")
        add(c4, "Chicken Popcorn Regular", 184.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")
        add(c4, "Chicken Strips 4 Pc", 184.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")
        add(c4, "Chicken Wings 4 Pc", 184.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")
        add(c4, "Chicken Lollipop 4 Pc", 184.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")
        add(c4, "Chicken Strips 7 Pc", 282.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")
        add(c4, "Chicken Wings 7 Pc", 282.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")
        add(c4, "Chicken Lollipop 7 Pc", 282.0, false, "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=400&q=80")

        // 5. Non Veg Burger
        val c5 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Non Veg Burger", nameTa = "Non Veg Burger"))
        add(c5, "Classic Chicken Burger", 147.0, false, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80")
        add(c5, "Fried Chicken Burger", 190.0, false, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80")
        add(c5, "King Burger", 245.0, false, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80")

        // 6. Chicken Sandwich
        val c6 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Chicken Sandwich", nameTa = "Chicken Sandwich"))
        add(c6, "Crispy Chicken Sandwich", 172.0, false, "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=400&q=80")
        add(c6, "Super Monster Sandwich", 208.0, false, "https://images.unsplash.com/photo-1528698827591-e19ccd7bc23d?auto=format&fit=crop&w=400&q=80")

        // 7. Veg Momos
        val c7 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Veg Momos", nameTa = "Veg Momos"))
        add(c7, "Veg Steamed Momos", 135.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c7, "Veg Fried Momos", 147.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c7, "Veg Hot and Crispy Momos", 172.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")

        // 8. Mushroom Momos
        val c8 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Mushroom Momos", nameTa = "Mushroom Momos"))
        add(c8, "Mushroom Steamed Momos", 159.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c8, "Mushroom Fried Momos", 172.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c8, "Mushroom Hot and Crispy Momos", 184.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")

        // 9. Paneer Momos
        val c9 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Paneer Momos", nameTa = "Paneer Momos"))
        add(c9, "Paneer Steamed Momos", 159.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c9, "Paneer Fried Momos", 172.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c9, "Paneer Hot and Crispy Momos", 184.0, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")

        // 10. Chicken Momos
        val c10 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Chicken Momos", nameTa = "Chicken Momos"))
        add(c10, "Chicken Steamed Momos", 159.0, false, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c10, "Chicken Fried Momos", 172.0, false, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")
        add(c10, "Chicken Hot and Crispy Momos", 184.0, false, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=400&q=80")

        // 11. Mojito
        val c11 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Mojito", nameTa = "Mojito"))
        add(c11, "Coke", 37.0, true, "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=400&q=80")
        add(c11, "Mint Mojito", 98.0, true, "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=400&q=80")
        add(c11, "Green Mojito", 98.0, true, "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=400&q=80")
        add(c11, "Blue Mojito", 98.0, true, "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=400&q=80")

        // 12. Misc
        val c12 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Misc", nameTa = "Misc"))
        add(c12, "Brownie", 74.0, true)
        add(c12, "Extra Cheese", 37.0, true)
        add(c12, "Extra Ice Cream", 49.0, true)
        add(c12, "Each Topping", 6.0, true)
        add(c12, "Extra Tandoori Mayo", 12.0, true)

        // 13. Fries
        val c13 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Fries", nameTa = "Fries"))
        add(c13, "French Fries Small", 98.0, true)
        add(c13, "French Fries Regular", 135.0, true)
        add(c13, "Garlic Potato pops", 123.0, true)
        add(c13, "Smiley", 123.0, true)
        add(c13, "Perri Perri French Fries", 147.0, true)
        add(c13, "Cheese French Fries", 159.0, true)
        add(c13, "Cheese Finger Fries (4 pc)", 184.0, true)
        add(c13, "Cheese Finger Fries (7 pc)", 282.0, true)

        // 14. Waffles
        val c14 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Waffles", nameTa = "Waffles"))
        add(c14, "Plain Waffle", 121.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Honey Butter Waffle", 146.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Kitkat Waffle", 170.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Oreo Choco Waffle", 170.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Butterscotch Crunch Waffle", 170.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Belgian Waffle", 183.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Dark & White Waffle", 183.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Red Velvet Waffle", 183.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Triple Chocolate Waffle", 195.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Dark Choco Waffle", 195.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Strawberry Creamy Waffle", 195.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")
        add(c14, "Blueberry Creamy Waffle", 195.0, true, "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=400&q=80")

        // 15. Wonder Sundaes
        val c15 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Wonder Sundaes", nameTa = "Wonder Sundaes"))
        add(c15, "Vanilla Wonder", 97.0, true, "https://images.unsplash.com/photo-1563805042-7684c019e1cb?auto=format&fit=crop&w=400&q=80")
        add(c15, "Hot Chocolate Wonder", 97.0, true, "https://images.unsplash.com/photo-1563805042-7684c019e1cb?auto=format&fit=crop&w=400&q=80")
        add(c15, "Oreo Wonder", 109.0, true, "https://images.unsplash.com/photo-1563805042-7684c019e1cb?auto=format&fit=crop&w=400&q=80")
        add(c15, "Kitkat Wonder", 121.0, true, "https://images.unsplash.com/photo-1563805042-7684c019e1cb?auto=format&fit=crop&w=400&q=80")
        add(c15, "Cookies & Cream Wonder", 134.0, true, "https://images.unsplash.com/photo-1563805042-7684c019e1cb?auto=format&fit=crop&w=400&q=80")

        // 16. Cake Sundaes
        val c16 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Cake Sundaes", nameTa = "Cake Sundaes"))
        add(c16, "Death By Chocolate (Small)", 178.0, true)
        add(c16, "Death By Chocolate (Large)", 239.0, true)
        add(c16, "Redvelvet Choco Delight (Small)", 178.0, true)
        add(c16, "Redvelvet Choco Delight (Large)", 239.0, true)
        add(c16, "Choco Delight (Small)", 178.0, true)
        add(c16, "Choco Delight (Large)", 239.0, true)
        add(c16, "Oreo Choco Delight (Small)", 178.0, true)
        add(c16, "Oreo Choco Delight (Large)", 239.0, true)
        add(c16, "Tiramisu", 239.0, true)

        // 17. Fruit Sundaes
        val c17 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Fruit Sundaes", nameTa = "Fruit Sundaes"))
        add(c17, "Strawberry Sundae", 178.0, true)
        add(c17, "Mango Sundae", 190.0, true)
        add(c17, "Pineapple Sundae", 190.0, true)
        add(c17, "Love Lyichii", 190.0, true)
        add(c17, "Dry Frit Spcl Sundae", 202.0, true)
        add(c17, "Fig & Honey Sundae", 202.0, true)

        // 18. Brownie Sundaes
        val c18 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Brownie Sundaes", nameTa = "Brownie Sundaes"))
        add(c18, "Brownie Fudge", 202.0, true)
        add(c18, "Caramel Brownie Fudge", 214.0, true)
        add(c18, "Choco Brownie Fudge", 227.0, true)
        add(c18, "Choco lava", 239.0, true)
        add(c18, "Nutella Brownie Fudge", 239.0, true)
        add(c18, "Sizzling brownie vanilla", 244.0, true)
        add(c18, "Sizzling brownie chocolate", 270.0, true)

        // 19. Signature Sundaes
        val c19 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Signature Sundaes", nameTa = "Signature Sundaes"))
        add(c19, "Banana Split", 244.0, true)
        add(c19, "Gudbud", 244.0, true)
        add(c19, "Tall Beauty", 244.0, true)
        add(c19, "Black Pearl", 244.0, true)
        add(c19, "Fruit Zest", 244.0, true)
        add(c19, "Nuttu Crispy Tower", 281.0, true)
        add(c19, "Miracle Four", 281.0, true)

        // 20. Classic Sundaes
        val c20 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Classic Sundaes", nameTa = "Classic Sundaes"))
        add(c20, "Chikki Crunch", 159.0, true)
        add(c20, "Jelly Supreme", 184.0, true)
        add(c20, "Black & White", 190.0, true)
        add(c20, "Merry World Killer", 190.0, true)

        // 21. Sundaes Paradise
        val c21 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Sundaes Paradise", nameTa = "Sundaes Paradise"))
        add(c21, "Butter Scotch Sundae", 159.0, true)
        add(c21, "Caramel Sundae", 159.0, true)
        add(c21, "Nutty Crunch", 172.0, true)
        add(c21, "Chocolate Merry World", 172.0, true)
        add(c21, "Cookies & Crumbles", 178.0, true)

        // 22. Falooda
        val c22 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Falooda", nameTa = "Falooda"))
        add(c22, "Vanilla", 172.0, true, "https://images.unsplash.com/photo-1517093157648-f9d2718a9020?auto=format&fit=crop&w=400&q=80")
        add(c22, "Chocolate", 184.0, true, "https://images.unsplash.com/photo-1517093157648-f9d2718a9020?auto=format&fit=crop&w=400&q=80")
        add(c22, "Alphonse Mango", 196.0, true, "https://images.unsplash.com/photo-1517093157648-f9d2718a9020?auto=format&fit=crop&w=400&q=80")
        add(c22, "Kesar Pista", 208.0, true, "https://images.unsplash.com/photo-1517093157648-f9d2718a9020?auto=format&fit=crop&w=400&q=80")
        add(c22, "Arabian Fantasy", 208.0, true, "https://images.unsplash.com/photo-1517093157648-f9d2718a9020?auto=format&fit=crop&w=400&q=80")
        add(c22, "Kulfi", 221.0, true, "https://images.unsplash.com/photo-1517093157648-f9d2718a9020?auto=format&fit=crop&w=400&q=80")

        // 23. Milk Shake
        val c23 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Milk Shake", nameTa = "Milk Shake"))
        add(c23, "Rose Milkshake", 97.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Vanilla", 146.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Strawberry", 146.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Chocolate", 158.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Mango", 158.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Black Currant", 170.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Butterscotch", 170.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Dry Fruit Milk Shake", 183.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Chickko", 195.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c23, "Kulfi", 195.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")

        // 24. Thick Shake
        val c24 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Thick Shake", nameTa = "Thick Shake"))
        add(c24, "Kitkat Thick Shake", 172.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c24, "Red Banana Thick Shake", 172.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c24, "Oreo Thick Shake", 184.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c24, "Red Velvet Thick Shake", 184.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c24, "Cold Coffee", 184.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c24, "Dbc Thick Shake", 184.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c24, "Mango Banana Thick Shake", 196.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")
        add(c24, "Brownie Thick Shake", 196.0, true, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=400&q=80")

        // 25. Flavours
        val c25 = db.categoryDao.insertCategory(Category(vendorId = mwId, nameEn = "Flavours", nameTa = "Flavours"))
        add(c25, "Vanilla", 61.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Strawberry", 61.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Mango", 74.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Chocolate", 74.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Butterscotch", 86.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Fresh Pineapple", 86.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Chocolate Chocochip", 98.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Black Currant", 98.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Kaju Kismiss", 98.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Chocolate Almond Fudge", 110.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Coffee Brazilla", 116.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Cookie's and Cream", 116.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Fig N Honey", 116.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Lyichica", 116.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Arabian Fantasy", 134.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Kesar Pista", 134.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Pistachionut", 134.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Chico", 134.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")
        add(c25, "Kulfi", 146.0, true, "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?auto=format&fit=crop&w=400&q=80")

        // Seed reviews for Merry World
        try {
            db.reviewDao.insertReview(Review(
                vendorId = mwId,
                userName = "Suresh K",
                rating = 5,
                comment = "Classic Veg Burger and Kitkat Thick Shake are my absolute favorites here. Kids love it!"
            ))
            db.reviewDao.insertReview(Review(
                vendorId = mwId,
                userName = "Deepa",
                rating = 4,
                comment = "Great cafe in Idappadi for sandwiches and ice cream sundae. Clean and quick."
            ))
        } catch (e: Exception) {
            Log.e("LyoRepository", "Error seeding Merry World reviews: ${e.message}")
        }
    }

    suspend fun deleteVendor(vendor: Vendor) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val dbInstance = LyoFirebaseHelper.firestore ?: throw Exception("Firebase Firestore not initialized")
        if (!LyoFirebaseHelper.ensureFirebaseAdminAuth()) {
            throw IllegalStateException("Your admin session has expired. Please log out and log in again.")
        }

        // 1. Fetch vendor's categories and menu items
        val vendorItems = db.menuItemDao.getMenuItemsForVendorList(vendor.id)
        val vendorCategories = db.categoryDao.getCategoriesForVendorList(vendor.id)

        // 2. Perform Firestore write batch deletions in safe chunked batches
        val batchOps = mutableListOf<(com.google.firebase.firestore.WriteBatch) -> Unit>()
        
        // Vendor deletions
        val vendorIdStr = vendor.id.toString()
        batchOps.add { b -> b.delete(dbInstance.collection("vendors").document(vendorIdStr)) }
        batchOps.add { b -> b.delete(dbInstance.collection("stores").document(vendorIdStr)) }
        
        // Category deletions
        vendorCategories.forEach { cat ->
            batchOps.add { b -> b.delete(dbInstance.collection("categories").document(cat.id.toString())) }
        }
        
        // Menu item deletions
        vendorItems.forEach { item ->
            val itemIdStr = item.id.toString()
            batchOps.add { b -> b.delete(dbInstance.collection("menu_items").document(itemIdStr)) }
            batchOps.add { b -> b.delete(dbInstance.collection("vendors").document(vendorIdStr).collection("products").document(itemIdStr)) }
        }

        batchOps.chunked(400).forEach { chunk ->
            val currentBatch = dbInstance.batch()
            chunk.forEach { op -> op(currentBatch) }
            currentBatch.commit().await()
        }

        // 3. Delete from local Room database ONLY on success
        db.menuItemDao.deleteMenuItemsByVendor(vendor.id)
        db.categoryDao.deleteCategoriesByVendor(vendor.id)
        db.vendorDao.deleteVendor(vendor)
    }

    suspend fun seedMenuForVendor(vendorId: Long, type: String) {
        LyoMenuSeeder.seedForVendor(db, vendorId, type)
    }

    suspend fun syncAllLocalToFirestore() {
        LyoFirebaseHelper.pushAllLocalToFirestore(db)
    }
}
