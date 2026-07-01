package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object LyoFirebaseHelper {
    private const val TAG = "LyoFirebaseHelper"
    
    var db: AppDatabase? = null
    var appContext: Context? = null
    
    var isInitialized = false
        private set

    fun initialize(context: Context) {
        appContext = context.applicationContext
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                isInitialized = true
                Log.d(TAG, "Firebase already initialized")
                // Apply Firestore persistent offline cache settings
                try {
                    val dbInstance = FirebaseFirestore.getInstance()
                    val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                    dbInstance.firestoreSettings = settings
                    Log.d(TAG, "Firestore offline persistence configured for existing app")
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore existing settings config skipped: ${e.message}")
                }
                return
            }

            // Dynamically initialize Firebase so it compiles/runs fine without google-services.json
            /*
             * NOTE: This Android app is registered in the Firebase project "lyo-food-delivery"
             * under App ID: 1:368208047268:android:e67b072862449f8a034a31
             * Package Name: com.lyo.fooddelivery
             *
             * WARNING: This Firebase project may also contain unrelated Web app registrations
             * (used for a separate application, Edappadi Kadai, or leftover from earlier setup).
             * Those Web app API keys and App IDs must NEVER be copied here. Only the Android-type
             * credentials sourced from the BuildConfig fields (FIREBASE_API_KEY, FIREBASE_APP_ID,
             * FIREBASE_PROJECT_ID, FIREBASE_DATABASE_URL, FIREBASE_STORAGE_BUCKET) belong here.
             */
            val options = FirebaseOptions.Builder()
                .setApiKey(com.example.BuildConfig.FIREBASE_API_KEY)
                .setApplicationId(com.example.BuildConfig.FIREBASE_APP_ID)
                .setProjectId(com.example.BuildConfig.FIREBASE_PROJECT_ID)
                .setDatabaseUrl(com.example.BuildConfig.FIREBASE_DATABASE_URL)
                .setStorageBucket(com.example.BuildConfig.FIREBASE_STORAGE_BUCKET)
                .setGcmSenderId("368208047268")
                .build()

            FirebaseApp.initializeApp(context, options)
            isInitialized = true
            Log.d(TAG, "Firebase dynamically initialized successfully")

            // Configure Firestore offline persistence & unlimited cache
            try {
                val dbInstance = FirebaseFirestore.getInstance()
                val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
                dbInstance.firestoreSettings = settings
                Log.d(TAG, "Firestore offline persistence enabled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Firestore offline persistence settings failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase dynamic initialization failed: ${e.message}", e)
            isInitialized = false
        }
    }

    val auth: FirebaseAuth?
        get() = if (isInitialized) FirebaseAuth.getInstance() else null

    val firestore: FirebaseFirestore?
        get() = if (isInitialized) FirebaseFirestore.getInstance() else null

    suspend fun loginWithGoogleInFirebase(
        idToken: String,
        overrideRole: String? = null,
        vehicleNo: String? = null,
        address: String? = null,
        lat: Double? = null,
        lng: Double? = null
    ): User? = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext null
        val dbInstance = firestore ?: return@withContext null
        try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = authInstance.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return@withContext null
            val email = firebaseUser.email ?: ""
            val rawPhone = firebaseUser.phoneNumber ?: email.substringBefore("@").replace(".", "").trim()
            val phone = if (rawPhone.isEmpty()) "9999999999" else rawPhone
            val name = firebaseUser.displayName ?: "Lyo Customer"
            
            val doc = dbInstance.collection("users").document(phone).get().await()
            val user = if (doc.exists()) {
                val existingRole = doc.getString("role") ?: "CUSTOMER"
                val finalRole = overrideRole ?: existingRole
                User(
                    phone = doc.getString("phone") ?: phone,
                    name = doc.getString("name") ?: name,
                    email = doc.getString("email") ?: email,
                    address = doc.getString("address") ?: (address ?: "Idappadi, Salem, Tamil Nadu, 637101"),
                    lat = doc.getDouble("lat") ?: (lat ?: 11.5812),
                    lng = doc.getDouble("lng") ?: (lng ?: 77.8465),
                    isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                    role = finalRole,
                    vehicleNo = doc.getString("vehicleNo") ?: (vehicleNo ?: ""),
                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                    salaryRate = doc.getDouble("salaryRate") ?: 0.0
                )
            } else {
                val finalRole = overrideRole ?: "CUSTOMER"
                val newUser = User(
                    phone = phone,
                    name = name,
                    email = email,
                    address = address ?: "Idappadi, Salem, Tamil Nadu, 637101",
                    lat = lat ?: 11.5812,
                    lng = lng ?: 77.8465,
                    isWhatsAppOptIn = true,
                    role = finalRole,
                    vehicleNo = vehicleNo ?: "",
                    isActiveRider = true,
                    salaryType = "MONTHLY",
                    salaryRate = 0.0
                )
                val userMap = mapOf(
                    "uid" to firebaseUser.uid,
                    "phone" to phone,
                    "name" to name,
                    "email" to email,
                    "address" to (address ?: "Idappadi, Salem, Tamil Nadu, 637101"),
                    "lat" to (lat ?: 11.5812),
                    "lng" to (lng ?: 77.8465),
                    "isWhatsAppOptIn" to true,
                    "role" to finalRole,
                    "vehicleNo" to (vehicleNo ?: ""),
                    "isActiveRider" to true,
                    "salaryType" to "MONTHLY",
                    "salaryRate" to 0.0
                )
                dbInstance.collection("users").document(phone).set(userMap).await()
                newUser
            }
            user
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Google Auth Sign-In failed: ${e.message}", e)
            null
        }
    }

    internal fun hashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password
        }
    }

    // For Auth
    suspend fun registerInFirebase(user: User, plaintextPassword: String? = null): Boolean = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext false
        val dbInstance = firestore ?: return@withContext false
        
        // Core design: we use the SHA-256 hashed password for Firebase Auth so it is secure, robust,
        // and can be auto-authenticated anytime using the password hash stored in the local Room DB.
        val actualAuthPassword = if (!plaintextPassword.isNullOrBlank()) {
            if (plaintextPassword.length == 64 && plaintextPassword.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                plaintextPassword
            } else {
                hashPassword(plaintextPassword)
            }
        } else {
            null
        }

        try {
            val email = "${user.phone.trim()}@lyofoods.in"
            val targetPassword = actualAuthPassword ?: return@withContext false
            
            var uid = user.phone
            try {
                // Create Firebase Auth user
                val result = authInstance.createUserWithEmailAndPassword(email, targetPassword).await()
                uid = result.user?.uid ?: user.phone
                Log.d(TAG, "FirebaseAuth createUserWithEmailAndPassword succeeded for ${user.phone}")
            } catch (authEx: Exception) {
                Log.w(TAG, "FirebaseAuth user creation failed, checking if already in use: ${authEx.message}")
                if (authEx is com.google.firebase.auth.FirebaseAuthUserCollisionException || 
                    authEx.message?.contains("already in use") == true || 
                    authEx.message?.contains("collision") == true) {
                    try {
                        // Sign in with the expected password hash to verify or reuse existing auth account
                        val signInResult = authInstance.signInWithEmailAndPassword(email, targetPassword).await()
                        uid = signInResult.user?.uid ?: user.phone
                        Log.d(TAG, "Successfully signed in existing auth user to synchronize registration")
                    } catch (signInEx: Exception) {
                        Log.e(TAG, "Could not sign in existing auth user: ${signInEx.message}. Proceeding with default uid.")
                        uid = authInstance.currentUser?.uid ?: user.phone
                    }
                } else {
                    throw authEx
                }
            }
            
            // Save details to Firestore
            val userMap = mutableMapOf<String, Any>(
                "uid" to uid,
                "phone" to user.phone,
                "name" to user.name,
                "email" to user.email,
                "address" to user.address,
                "lat" to user.lat,
                "lng" to user.lng,
                "isWhatsAppOptIn" to user.isWhatsAppOptIn,
                "role" to user.role,
                "vehicleNo" to user.vehicleNo,
                "isActiveRider" to user.isActiveRider,
                "salaryType" to user.salaryType,
                "salaryRate" to user.salaryRate
            )
            if (actualAuthPassword != null) {
                userMap["passwordHash"] = actualAuthPassword
            }
            
            dbInstance.collection("users").document(user.phone).set(userMap, SetOptions.merge()).await()
            if (user.role == "ADMIN") {
                dbInstance.collection("admins").document(uid).set(mapOf("phone" to user.phone)).await()
                Log.d(TAG, "Registered admin sync successful for UID: $uid")
            }
            Log.d(TAG, "Registered user ${user.phone} successfully on Firebase and Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase registration failed: ${e.message}", e)
            try {
                // If user already exists in Auth or error, try to save in Firestore anyway
                val userMap = mutableMapOf<String, Any>(
                    "phone" to user.phone,
                    "name" to user.name,
                    "email" to user.email,
                    "address" to user.address,
                    "lat" to user.lat,
                    "lng" to user.lng,
                    "isWhatsAppOptIn" to user.isWhatsAppOptIn,
                    "role" to user.role,
                    "vehicleNo" to user.vehicleNo,
                    "isActiveRider" to user.isActiveRider,
                    "salaryType" to user.salaryType,
                    "salaryRate" to user.salaryRate
                )
                if (actualAuthPassword != null) {
                    userMap["passwordHash"] = actualAuthPassword
                }
                dbInstance.collection("users").document(user.phone).set(userMap, SetOptions.merge()).await()
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    suspend fun loginInFirebase(phone: String, pass: String): User? = withContext(Dispatchers.IO) {
        val authInstance = auth
        val dbInstance = firestore ?: return@withContext null
        try {
            val email = "${phone.trim()}@lyofoods.in"
            
            // Resolve login password - if it's already a SHA-256 hash or is plaintext
            val hashed = if (pass.length == 64 && pass.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                pass
            } else {
                hashPassword(pass)
            }
            
            // Fetch profile details from Firestore
            val doc = dbInstance.collection("users").document(phone.trim()).get().await()
            if (doc.exists()) {
                val dbPasswordHash = doc.getString("passwordHash")
                
                // If password hash is stored in Firestore, verify it directly!
                if (!dbPasswordHash.isNullOrBlank()) {
                    if (dbPasswordHash != hashed) {
                        Log.e(TAG, "Password mismatch in Firestore passwordHash verification!")
                        return@withContext null
                    }
                    Log.d(TAG, "Direct Firestore passwordHash verification successful!")
                } else {
                    // Fallback to standard Firebase Auth if no passwordHash is stored yet
                    if (authInstance != null) {
                        authInstance.signInWithEmailAndPassword(email, hashed).await()
                    } else {
                        Log.e(TAG, "No passwordHash stored and Auth is unavailable!")
                        return@withContext null
                    }
                }

                val user = User(
                    phone = doc.getString("phone") ?: phone,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    address = doc.getString("address") ?: "",
                    lat = doc.getDouble("lat") ?: 0.0,
                    lng = doc.getDouble("lng") ?: 0.0,
                    isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                    role = doc.getString("role") ?: "CUSTOMER",
                    vehicleNo = doc.getString("vehicleNo") ?: "",
                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                    salaryRate = doc.getDouble("salaryRate") ?: 0.0
                )
                
                // Sign into Firebase Auth in the background to set current session if available
                if (authInstance != null) {
                    try {
                        authInstance.signInWithEmailAndPassword(email, hashed).await()
                    } catch (ex: Exception) {
                        Log.w(TAG, "Background Auth sign-in skipped/failed: ${ex.message}")
                    }
                }
                
                Log.d(TAG, "Fetched logged-in Firestore user: ${user.phone}")
                if (user.role == "ADMIN" && authInstance != null) {
                    val adminUid = authInstance.currentUser?.uid ?: user.phone
                    dbInstance.collection("admins").document(adminUid).set(mapOf("phone" to user.phone)).await()
                    Log.d(TAG, "Logged in admin sync successful for UID: $adminUid")
                }
                user
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase login failed: ${e.message}", e)
            null
        }
    }

    suspend fun getUserByPhoneFromFirestore(phone: String): User? = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext null
        try {
            val doc = dbInstance.collection("users").document(phone.trim()).get().await()
            if (doc.exists()) {
                User(
                    phone = doc.getString("phone") ?: phone,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    address = doc.getString("address") ?: "",
                    lat = doc.getDouble("lat") ?: 0.0,
                    lng = doc.getDouble("lng") ?: 0.0,
                    isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                    role = doc.getString("role") ?: "CUSTOMER",
                    vehicleNo = doc.getString("vehicleNo") ?: "",
                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                    salaryRate = doc.getDouble("salaryRate") ?: 0.0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed getting user from Firestore: ${e.message}")
            null
        }
    }

    suspend fun deleteUserFromFirestore(phone: String): Boolean = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext false
        try {
            dbInstance.collection("users").document(phone.trim()).delete().await()
            // Also attempt to delete the Firebase user from Firebase Auth if desired, 
            // but deleting from the "users" collection is the primary source of auth checks in our app
            Log.d(TAG, "Deleted user from Firestore: $phone")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user $phone from Firestore: ${e.message}")
            false
        }
    }

    // Firestore Sync Operations
    suspend fun syncVendorToFirestore(vendor: Vendor) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val idStr = vendor.id.toString()
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
                "freeDeliveryThreshold" to vendor.freeDeliveryThreshold,
                "minOrderAmount" to vendor.minOrderAmount,
                "isCouponEnabled" to vendor.isCouponEnabled,
                "couponCode" to vendor.couponCode,
                "couponDiscount" to vendor.couponDiscount,
                "couponMinOrder" to vendor.couponMinOrder,
                "isOnHoliday" to vendor.isOnHoliday,
                "phone" to vendor.phone,
                "visibilityRadiusKm" to vendor.visibilityRadiusKm,
                "isDynamicDelivery" to vendor.isDynamicDelivery,
                "sortOrder" to vendor.sortOrder,
                "autoOpenTime" to vendor.autoOpenTime,
                "autoCloseTime" to vendor.autoCloseTime
            )
            dbInstance.collection("vendors").document(idStr).set(vendorMap, SetOptions.merge()).await()
            Log.d(TAG, "Synced vendor ${vendor.name} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing vendor ${vendor.name}: ${e.message}")
        }
    }

    suspend fun deleteVendorFromFirestore(vendorId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val idStr = vendorId.toString()
            dbInstance.collection("vendors").document(idStr).delete().await()
            
            // Delete associated menu items and categories
            val itemsDocs = dbInstance.collection("menu_items").whereEqualTo("vendorId", vendorId).get().await()
            for (doc in itemsDocs.documents) {
                dbInstance.collection("menu_items").document(doc.id).delete().await()
            }
            val catDocs = dbInstance.collection("categories").whereEqualTo("vendorId", vendorId).get().await()
            for (doc in catDocs.documents) {
                dbInstance.collection("categories").document(doc.id).delete().await()
            }
            Log.d(TAG, "Deleted vendor $vendorId from Firestore complete")
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting vendor $vendorId: ${e.message}")
        }
    }

    suspend fun clearMenuAndCategoriesFromFirestore(vendorId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val itemsDocs = dbInstance.collection("menu_items").whereEqualTo("vendorId", vendorId).get().await()
            for (doc in itemsDocs.documents) {
                dbInstance.collection("menu_items").document(doc.id).delete().await()
            }
            val catDocs = dbInstance.collection("categories").whereEqualTo("vendorId", vendorId).get().await()
            for (doc in catDocs.documents) {
                dbInstance.collection("categories").document(doc.id).delete().await()
            }
            Log.d(TAG, "Cleared categories & menu items from Firestore for vendor $vendorId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed clearing categories & menu items from Firestore for vendor $vendorId: ${e.message}")
        }
    }

    suspend fun syncCategoryToFirestore(category: Category) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val catMap = mapOf(
                "id" to category.id,
                "vendorId" to category.vendorId,
                "nameEn" to category.nameEn,
                "nameTa" to category.nameTa,
                "sortOrder" to category.sortOrder,
                "isHidden" to category.isHidden,
                "autoOpenTime" to category.autoOpenTime,
                "autoCloseTime" to category.autoCloseTime
            )
            dbInstance.collection("categories").document(category.id.toString()).set(catMap, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing category: ${e.message}")
        }
    }

    suspend fun deleteCategoryFromFirestore(categoryId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            dbInstance.collection("categories").document(categoryId.toString()).delete().await()
            
            // Delete associated menu items
            val itemDocs = dbInstance.collection("menu_items").whereEqualTo("categoryId", categoryId).get().await()
            for (doc in itemDocs.documents) {
                dbInstance.collection("menu_items").document(doc.id).delete().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting category from Firestore: ${e.message}")
        }
    }

    suspend fun syncMenuItemToFirestore(item: MenuItem) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val itemMap = mapOf(
                "id" to item.id,
                "vendorId" to item.vendorId,
                "categoryId" to item.categoryId,
                "nameEn" to item.nameEn,
                "nameTa" to item.nameTa,
                "descEn" to item.descEn,
                "descTa" to item.descTa,
                "price" to item.price,
                "isVeg" to item.isVeg,
                "isAvailable" to item.isAvailable,
                "imageUrl" to item.imageUrl,
                "autoOpenTime" to item.autoOpenTime,
                "autoCloseTime" to item.autoCloseTime
            )
            dbInstance.collection("menu_items").document(item.id.toString()).set(itemMap, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing menu item: ${e.message}")
        }
    }

    suspend fun deleteMenuItemFromFirestore(itemId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            dbInstance.collection("menu_items").document(itemId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting menu item: ${e.message}")
        }
    }

    suspend fun syncPromoBannerToFirestore(banner: PromoBanner) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val docId = if (banner.code.isNotBlank()) banner.code else banner.id.toString()
            val bannerMap = mapOf(
                "id" to banner.id,
                "code" to banner.code,
                "description" to banner.description,
                "imageUrl" to banner.imageUrl
            )
            dbInstance.collection("promo_banners").document(docId).set(bannerMap, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing promo banner: ${e.message}")
        }
    }

    suspend fun deletePromoBannerFromFirestore(banner: PromoBanner) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val docId = if (banner.code.isNotBlank()) banner.code else banner.id.toString()
            dbInstance.collection("promo_banners").document(docId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting promo banner: ${e.message}")
        }
    }

    // Comprehensive push of everything
    suspend fun pushAllLocalToFirestore(db: AppDatabase) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            // Push active categories, vendors, menu_items
            val vendors = db.vendorDao.getAllVendorsList()
            for (v in vendors) {
                syncVendorToFirestore(v)
            }
            Log.d(TAG, "Pushed ${vendors.size} vendors successfully to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed completely local push: ${e.message}")
        }
    }

    suspend fun syncOrderToFirestore(order: Order) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: throw Exception("Firestore instance is null")
        val currentUserId = order.userId
        val idStr = order.id.toString()
        val itemsList = mutableListOf<Map<String, Any>>()
        val localDb = db
        if (localDb != null) {
            try {
                val orderItems = localDb.orderItemDao.getItemsForOrder(order.id)
                for (item in orderItems) {
                    itemsList.add(mapOf(
                        "menuItemId" to item.menuItemId,
                        "nameEn" to item.nameEn,
                        "nameTa" to item.nameTa,
                        "quantity" to item.quantity,
                        "price" to item.price
                    ))
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed loading order items for Firestore: ${ex.message}")
            }
        }
        val orderMap = mapOf(
            "id" to order.id,
            "userId" to order.userId,
            "vendorId" to order.vendorId,
            "vendorName" to order.vendorName,
            "status" to order.status,
            "totalAmount" to order.totalAmount,
            "subtotal" to order.subtotal,
            "deliveryFee" to order.deliveryFee,
            "couponDiscount" to order.couponDiscount,
            "tipAmount" to order.tipAmount,
            "itemsCount" to order.itemsCount,
            "timestamp" to order.timestamp,
            "otpCode" to order.otpCode,
            "customerLat" to order.customerLat,
            "customerLng" to order.customerLng,
            "items" to itemsList
        )
        dbInstance.collection("orders").document(idStr).set(orderMap, SetOptions.merge()).await()
        Log.d(TAG, "Synced order_id $idStr with ${itemsList.size} items successfully to Firestore")
    }

    suspend fun syncDeliveryRideToFirestore(ride: DeliveryRide) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val currentUserId = ride.riderPhone
            val idStr = ride.id.toString()
            val rideMap = mapOf(
                "id" to ride.id,
                "orderId" to ride.orderId,
                "riderName" to ride.riderName,
                "riderPhone" to ride.riderPhone,
                "status" to ride.status,
                "currentLat" to ride.currentLat,
                "currentLng" to ride.currentLng,
                "totalDistance" to ride.totalDistance,
                "earnings" to ride.earnings,
                "otpVerified" to ride.otpVerified
            )
            dbInstance.collection("delivery_rides").document(idStr).set(rideMap, SetOptions.merge()).await()
            Log.d(TAG, "Synced delivery_ride_id $idStr successfully to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing delivery ride: ${e.message}")
        }
    }

    val syncScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
    var repositoryRef: LyoRepository? = null
    private var isPaused = false
    private var userCollectorJob: kotlinx.coroutines.Job? = null

    private var vendorsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var categoriesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var menuItemsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var usersListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var persistentOrdersListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var persistentRidesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var promoBannersListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun startRealtimeSync(db: AppDatabase, repository: LyoRepository) {
        this.db = db
        this.repositoryRef = repository
        val dbInstance = firestore ?: return
        Log.e(TAG, "Starting REAL-TIME Firestore Database Synchronizer on All Collections...")

        // 1, 2, 3: Vendors, categories, and menu_items sync is role-aware and handled inside userCollectorJob

        // 4. Live Sync "users"
        usersListener?.remove()
        usersListener = dbInstance.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Users listener error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    syncScope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val phone = doc.getString("phone") ?: continue
                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    val user = User(
                                        phone = phone,
                                        name = doc.getString("name") ?: "",
                                        email = doc.getString("email") ?: "",
                                        address = doc.getString("address") ?: "",
                                        lat = doc.getDouble("lat") ?: 11.5812,
                                        lng = doc.getDouble("lng") ?: 77.8465,
                                        isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                                        role = doc.getString("role") ?: "CUSTOMER",
                                        vehicleNo = doc.getString("vehicleNo") ?: "",
                                        isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                                        salaryType = doc.getString("salaryType") ?: "MONTHLY",
                                        salaryRate = doc.getDouble("salaryRate") ?: 0.0
                                    )
                                    db.userDao.insertUser(user)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    // Only delete local users that are NOT the currently active user
                                    if (repository.currentUser.value?.phone != phone) {
                                        db.userDao.deleteUserByPhone(phone)
                                        Log.d(TAG, "Successfully processed remote complete deletion of customer profile: $phone")
                                    } else {
                                        Log.d(TAG, "Prevented deletion of local active logged-in user profile during network sync: $phone")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // 5. Live Sync "promo_banners"
        promoBannersListener?.remove()
        promoBannersListener = dbInstance.collection("promo_banners")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Promo banners listener error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    syncScope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val docId = doc.id
                            val bId = doc.getLong("id") ?: 0L
                            val code = doc.getString("code") ?: docId
                            val description = doc.getString("description") ?: ""
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            
                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    val banner = PromoBanner(
                                        id = bId,
                                        code = code,
                                        description = description,
                                        imageUrl = imageUrl
                                    )
                                    val banners = db.promoBannerDao.getAllPromoBannersList()
                                    val existing = banners.find { it.code == code }
                                    val finalBanner = if (existing != null) {
                                        banner.copy(id = existing.id)
                                    } else {
                                        banner
                                    }
                                    db.promoBannerDao.insertPromoBanner(finalBanner)
                                    
                                    if (code == "AI_BROADCAST_PROMO" && description.isNotBlank()) {
                                        if (existing == null || existing.description != description) {
                                            appContext?.let { ctx ->
                                                com.example.ui.screens.LyoNotificationHelper.showPushNotification(
                                                    ctx,
                                                    "Lyo AI Special Offer 📢",
                                                    description
                                                )
                                            }
                                        }
                                    }
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    val banners = db.promoBannerDao.getAllPromoBannersList()
                                    val existing = banners.find { it.code == code }
                                    if (existing != null) {
                                        db.promoBannerDao.deletePromoBanner(existing)
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // Scoped and role-aware sync of "orders" and "delivery_rides" based on currently logged in user
        userCollectorJob?.cancel()
        userCollectorJob = syncScope.launch {
            repository.currentUser.collect { user ->
                persistentOrdersListener?.remove()
                persistentOrdersListener = null
                persistentRidesListener?.remove()
                persistentRidesListener = null
                allOrdersListenerRegistration?.remove()
                allOrdersListenerRegistration = null
                vendorsListener?.remove()
                vendorsListener = null
                categoriesListener?.remove()
                categoriesListener = null
                menuItemsListener?.remove()
                menuItemsListener = null

                if (user == null) {
                    Log.d(TAG, "No user logged in, postponing scoped real-time listeners.")
                    return@collect
                }

                val currentUserId = user.phone
                val role = user.role

                // Dynamic role-aware sync for vendors, categories, menu_items
                if (role == "CUSTOMER") {
                    // 1. One-time fetch "vendors"
                    syncScope.launch {
                        try {
                            val snapshot = dbInstance.collection("vendors").get().await()
                            for (change in snapshot.documentChanges) {
                                val doc = change.document
                                val vId = doc.getLong("id") ?: continue
                                if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val vendor = Vendor(
                                        id = vId,
                                        name = doc.getString("name") ?: "",
                                        nameTa = doc.getString("nameTa") ?: "",
                                        type = doc.getString("type") ?: "Hotel",
                                        rating = doc.getDouble("rating") ?: 4.0,
                                        distance = doc.getDouble("distance") ?: 1.0,
                                        deliveryTime = doc.getLong("deliveryTime")?.toInt() ?: 20,
                                        deliveryFee = doc.getDouble("deliveryFee") ?: 30.0,
                                        address = doc.getString("address") ?: "",
                                        lat = doc.getDouble("lat") ?: 11.5812,
                                        lng = doc.getDouble("lng") ?: 77.8465,
                                        bannerUrl = doc.getString("bannerUrl") ?: "",
                                        freeDeliveryThreshold = doc.getDouble("freeDeliveryThreshold") ?: 400.0,
                                        minOrderAmount = doc.getDouble("minOrderAmount") ?: 100.0,
                                        isCouponEnabled = doc.getBoolean("isCouponEnabled") ?: false,
                                        couponCode = doc.getString("couponCode") ?: "",
                                        couponDiscount = doc.getDouble("couponDiscount") ?: 0.0,
                                        couponMinOrder = doc.getDouble("couponMinOrder") ?: 0.0,
                                        isOnHoliday = doc.getBoolean("isOnHoliday") ?: false,
                                        phone = doc.getString("phone") ?: "",
                                        visibilityRadiusKm = doc.getDouble("visibilityRadiusKm") ?: 15.0,
                                        isDynamicDelivery = doc.getBoolean("isDynamicDelivery") ?: false,
                                        sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0,
                                        autoOpenTime = doc.getString("autoOpenTime") ?: "",
                                        autoCloseTime = doc.getString("autoCloseTime") ?: ""
                                    )
                                    db.vendorDao.insertVendor(vendor)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Vendors one-time fetch error: ${e.message}")
                        }
                    }

                    // 2. One-time fetch "categories"
                    syncScope.launch {
                        try {
                            val snapshot = dbInstance.collection("categories").get().await()
                            for (change in snapshot.documentChanges) {
                                val doc = change.document
                                val cId = doc.getLong("id") ?: continue
                                if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val cat = Category(
                                        id = cId,
                                        vendorId = doc.getLong("vendorId") ?: 0L,
                                        nameEn = doc.getString("nameEn") ?: "",
                                        nameTa = doc.getString("nameTa") ?: "",
                                        sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0,
                                        isHidden = doc.getBoolean("isHidden") ?: false,
                                        autoOpenTime = doc.getString("autoOpenTime") ?: "",
                                        autoCloseTime = doc.getString("autoCloseTime") ?: ""
                                    )
                                    db.categoryDao.insertCategory(cat)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Categories one-time fetch error: ${e.message}")
                        }
                    }

                    // 3. One-time fetch "menu_items"
                    syncScope.launch {
                        try {
                            val snapshot = dbInstance.collection("menu_items").get().await()
                            for (change in snapshot.documentChanges) {
                                val doc = change.document
                                val mId = doc.getLong("id") ?: continue
                                if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val item = MenuItem(
                                        id = mId,
                                        vendorId = doc.getLong("vendorId") ?: 0L,
                                        categoryId = doc.getLong("categoryId") ?: 0L,
                                        nameEn = doc.getString("nameEn") ?: "",
                                        nameTa = doc.getString("nameTa") ?: "",
                                        descEn = doc.getString("descEn") ?: "",
                                        descTa = doc.getString("descTa") ?: "",
                                        price = doc.getDouble("price") ?: 0.0,
                                        isVeg = doc.getBoolean("isVeg") ?: true,
                                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        autoOpenTime = doc.getString("autoOpenTime") ?: "",
                                        autoCloseTime = doc.getString("autoCloseTime") ?: ""
                                    )
                                    db.menuItemDao.insertMenuItem(item)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Menu items one-time fetch error: ${e.message}")
                        }
                    }
                } else {
                    // ADMIN or DELIVERY: Real-time Snapshot Listeners
                    // 1. Live Sync "vendors"
                    vendorsListener = dbInstance.collection("vendors")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w(TAG, "Vendors listener error: ${e.message}")
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val vId = doc.getLong("id") ?: continue
                                        when (change.type) {
                                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                                val vendor = Vendor(
                                                    id = vId,
                                                    name = doc.getString("name") ?: "",
                                                    nameTa = doc.getString("nameTa") ?: "",
                                                    type = doc.getString("type") ?: "Hotel",
                                                    rating = doc.getDouble("rating") ?: 4.0,
                                                    distance = doc.getDouble("distance") ?: 1.0,
                                                    deliveryTime = doc.getLong("deliveryTime")?.toInt() ?: 20,
                                                    deliveryFee = doc.getDouble("deliveryFee") ?: 30.0,
                                                    address = doc.getString("address") ?: "",
                                                    lat = doc.getDouble("lat") ?: 11.5812,
                                                    lng = doc.getDouble("lng") ?: 77.8465,
                                                    bannerUrl = doc.getString("bannerUrl") ?: "",
                                                    freeDeliveryThreshold = doc.getDouble("freeDeliveryThreshold") ?: 400.0,
                                                    minOrderAmount = doc.getDouble("minOrderAmount") ?: 100.0,
                                                    isCouponEnabled = doc.getBoolean("isCouponEnabled") ?: false,
                                                    couponCode = doc.getString("couponCode") ?: "",
                                                    couponDiscount = doc.getDouble("couponDiscount") ?: 0.0,
                                                    couponMinOrder = doc.getDouble("couponMinOrder") ?: 0.0,
                                                    isOnHoliday = doc.getBoolean("isOnHoliday") ?: false,
                                                    phone = doc.getString("phone") ?: "",
                                                    visibilityRadiusKm = doc.getDouble("visibilityRadiusKm") ?: 15.0,
                                                    isDynamicDelivery = doc.getBoolean("isDynamicDelivery") ?: false,
                                                    sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0,
                                                    autoOpenTime = doc.getString("autoOpenTime") ?: "",
                                                    autoCloseTime = doc.getString("autoCloseTime") ?: ""
                                                )
                                                db.vendorDao.insertVendor(vendor)
                                            }
                                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                                db.vendorDao.deleteVendorById(vId)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    // 2. Live Sync "categories"
                    categoriesListener = dbInstance.collection("categories")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w(TAG, "Categories listener error: ${e.message}")
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val cId = doc.getLong("id") ?: continue
                                        when (change.type) {
                                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                                val cat = Category(
                                                    id = cId,
                                                    vendorId = doc.getLong("vendorId") ?: 0L,
                                                    nameEn = doc.getString("nameEn") ?: "",
                                                    nameTa = doc.getString("nameTa") ?: "",
                                                    sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0,
                                                    isHidden = doc.getBoolean("isHidden") ?: false,
                                                    autoOpenTime = doc.getString("autoOpenTime") ?: "",
                                                    autoCloseTime = doc.getString("autoCloseTime") ?: ""
                                                )
                                                db.categoryDao.insertCategory(cat)
                                            }
                                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                                db.categoryDao.deleteCategoryById(cId)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    // 3. Live Sync "menu_items"
                    menuItemsListener = dbInstance.collection("menu_items")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w(TAG, "Menu items listener error: ${e.message}")
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val mId = doc.getLong("id") ?: continue
                                        when (change.type) {
                                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                                val item = MenuItem(
                                                    id = mId,
                                                    vendorId = doc.getLong("vendorId") ?: 0L,
                                                    categoryId = doc.getLong("categoryId") ?: 0L,
                                                    nameEn = doc.getString("nameEn") ?: "",
                                                    nameTa = doc.getString("nameTa") ?: "",
                                                    descEn = doc.getString("descEn") ?: "",
                                                    descTa = doc.getString("descTa") ?: "",
                                                    price = doc.getDouble("price") ?: 0.0,
                                                    isVeg = doc.getBoolean("isVeg") ?: true,
                                                    isAvailable = doc.getBoolean("isAvailable") ?: true,
                                                    imageUrl = doc.getString("imageUrl") ?: "",
                                                    autoOpenTime = doc.getString("autoOpenTime") ?: "",
                                                    autoCloseTime = doc.getString("autoCloseTime") ?: ""
                                                )
                                                db.menuItemDao.insertMenuItem(item)
                                            }
                                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                                db.menuItemDao.deleteMenuItemById(mId)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                }

                Log.d(TAG, "User state updated: $currentUserId ($role). Re-initializing scoped real-time listeners...")

                if (role == "ADMIN") {
                    // Admins listen to all orders
                    persistentOrdersListener = dbInstance.collection("orders")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val oId = doc.getLong("id") ?: continue
                                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                            change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                                        ) {
                                            val order = Order(
                                                id = oId,
                                                userId = doc.getString("userId") ?: "",
                                                vendorId = doc.getLong("vendorId") ?: 0L,
                                                vendorName = doc.getString("vendorName") ?: "",
                                                status = doc.getString("status") ?: "PENDING",
                                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                                subtotal = doc.getDouble("subtotal") ?: 0.0,
                                                deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                                                couponDiscount = doc.getDouble("couponDiscount") ?: 0.0,
                                                tipAmount = doc.getDouble("tipAmount") ?: 0.0,
                                                itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1,
                                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                                otpCode = doc.getString("otpCode") ?: "1234",
                                                customerLat = doc.getDouble("customerLat") ?: 11.5812,
                                                customerLng = doc.getDouble("customerLng") ?: 77.8465
                                            )
                                            db.orderDao.insertOrder(order)
                                            saveOrderItemsFromDoc(oId, doc.get("items"))
                                        }
                                    }
                                }
                            }
                        }

                    persistentRidesListener = dbInstance.collection("delivery_rides")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val rId = doc.getLong("id") ?: continue
                                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                            change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                                        ) {
                                            val ride = DeliveryRide(
                                                id = rId,
                                                orderId = doc.getLong("orderId") ?: 0L,
                                                riderName = doc.getString("riderName") ?: "",
                                                riderPhone = doc.getString("riderPhone") ?: "",
                                                status = doc.getString("status") ?: "ACCEPTED",
                                                currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                                currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                                totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                                earnings = doc.getDouble("earnings") ?: 0.0,
                                                otpVerified = doc.getBoolean("otpVerified") ?: false
                                            )
                                            db.deliveryRideDao.insertDeliveryRide(ride)
                                        }
                                    }
                                }
                            }
                        }
                } else if (role == "DELIVERY") {
                    // Riders only listen to delivery_rides belonging to them
                    persistentRidesListener = dbInstance.collection("delivery_rides")
                        .whereEqualTo("riderPhone", currentUserId)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val rId = doc.getLong("id") ?: continue
                                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                            change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                                        ) {
                                            val ride = DeliveryRide(
                                                id = rId,
                                                orderId = doc.getLong("orderId") ?: 0L,
                                                riderName = doc.getString("riderName") ?: "",
                                                riderPhone = doc.getString("riderPhone") ?: "",
                                                status = doc.getString("status") ?: "ACCEPTED",
                                                currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                                currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                                totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                                earnings = doc.getDouble("earnings") ?: 0.0,
                                                otpVerified = doc.getBoolean("otpVerified") ?: false
                                            )
                                            db.deliveryRideDao.insertDeliveryRide(ride)
                                        }
                                    }
                                }
                            }
                        }

                    // Riders map active/preparing/out-for-delivery orders
                    persistentOrdersListener = dbInstance.collection("orders")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val oId = doc.getLong("id") ?: continue
                                        val statusVal = doc.getString("status") ?: "PENDING"
                                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                            change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                                        ) {
                                            val order = Order(
                                                id = oId,
                                                userId = doc.getString("userId") ?: "",
                                                vendorId = doc.getLong("vendorId") ?: 0L,
                                                vendorName = doc.getString("vendorName") ?: "",
                                                status = statusVal,
                                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                                subtotal = doc.getDouble("subtotal") ?: 0.0,
                                                deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                                                couponDiscount = doc.getDouble("couponDiscount") ?: 0.0,
                                                tipAmount = doc.getDouble("tipAmount") ?: 0.0,
                                                itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1,
                                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                                otpCode = doc.getString("otpCode") ?: "1234",
                                                customerLat = doc.getDouble("customerLat") ?: 11.5812,
                                                customerLng = doc.getDouble("customerLng") ?: 77.8465
                                            )
                                            db.orderDao.insertOrder(order)
                                            saveOrderItemsFromDoc(oId, doc.get("items"))
                                        }
                                    }
                                }
                            }
                        }
                } else {
                    // Customers view ONLY their own orders
                    persistentOrdersListener = dbInstance.collection("orders")
                        .whereEqualTo("userId", currentUserId)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val oId = doc.getLong("id") ?: continue
                                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                            change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                                        ) {
                                            val order = Order(
                                                id = oId,
                                                userId = doc.getString("userId") ?: "",
                                                vendorId = doc.getLong("vendorId") ?: 0L,
                                                vendorName = doc.getString("vendorName") ?: "",
                                                status = doc.getString("status") ?: "PENDING",
                                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                                subtotal = doc.getDouble("subtotal") ?: 0.0,
                                                deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                                                couponDiscount = doc.getDouble("couponDiscount") ?: 0.0,
                                                tipAmount = doc.getDouble("tipAmount") ?: 0.0,
                                                itemsCount = doc.getLong("itemsCount")?.toInt() ?: 1,
                                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                                otpCode = doc.getString("otpCode") ?: "1234",
                                                customerLat = doc.getDouble("customerLat") ?: 11.5812,
                                                customerLng = doc.getDouble("customerLng") ?: 77.8465
                                            )
                                            db.orderDao.insertOrder(order)
                                            saveOrderItemsFromDoc(oId, doc.get("items"))
                                        }
                                    }
                                }
                            }
                        }

                    // Scoped rides listener filtered by their active orders locally
                    persistentRidesListener = dbInstance.collection("delivery_rides")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val doc = change.document
                                        val rId = doc.getLong("id") ?: continue
                                        val orderId = doc.getLong("orderId") ?: 0L
                                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                            change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                                        ) {
                                            val orderFromDb = db.orderDao.getOrderById(orderId)
                                            if (orderFromDb != null && orderFromDb.userId == currentUserId) {
                                                val ride = DeliveryRide(
                                                    id = rId,
                                                    orderId = orderId,
                                                    riderName = doc.getString("riderName") ?: "",
                                                    riderPhone = doc.getString("riderPhone") ?: "",
                                                    status = doc.getString("status") ?: "ACCEPTED",
                                                    currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                                    currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                                    totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                                    earnings = doc.getDouble("earnings") ?: 0.0,
                                                    otpVerified = doc.getBoolean("otpVerified") ?: false
                                                )
                                                db.deliveryRideDao.insertDeliveryRide(ride)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    fun pauseSync() {
        if (!isPaused) {
            isPaused = true
            Log.d(TAG, "Lifecycle PAUSE: detaching Firebase real-time listeners to conserve battery/CPU")
            detachListeners()
        }
    }

    fun resumeSync() {
        if (isPaused) {
            isPaused = false
            Log.d(TAG, "Lifecycle RESUME: re-attaching Firebase real-time listeners")
            val currentDb = db
            val currentRepo = repositoryRef
            if (currentDb != null && currentRepo != null) {
                startRealtimeSync(currentDb, currentRepo)
            }
        }
    }

    fun detachListeners() {
        userCollectorJob?.cancel()
        userCollectorJob = null
        vendorsListener?.remove()
        vendorsListener = null
        categoriesListener?.remove()
        categoriesListener = null
        menuItemsListener?.remove()
        menuItemsListener = null
        usersListener?.remove()
        usersListener = null
        persistentOrdersListener?.remove()
        persistentOrdersListener = null
        persistentRidesListener?.remove()
        persistentRidesListener = null
        promoBannersListener?.remove()
        promoBannersListener = null
        allOrdersListenerRegistration?.remove()
        allOrdersListenerRegistration = null
    }

    private var orderListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var rideListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var allOrdersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private suspend fun saveOrderItemsFromDoc(orderId: Long, itemsField: Any?) {
        val localDb = db ?: return
        if (itemsField is List<*>) {
            val orderItems = mutableListOf<OrderItem>()
            for (obj in itemsField) {
                if (obj is Map<*, *>) {
                    try {
                        val menuItemId = (obj["menuItemId"] as? Number)?.toLong() ?: 0L
                        val nameEn = obj["nameEn"] as? String ?: ""
                        val nameTa = obj["nameTa"] as? String ?: ""
                        val quantity = (obj["quantity"] as? Number)?.toInt() ?: 1
                        val price = (obj["price"] as? Number)?.toDouble() ?: 0.0
                        
                        orderItems.add(OrderItem(
                            orderId = orderId,
                            menuItemId = menuItemId,
                            nameEn = nameEn,
                            nameTa = nameTa,
                            quantity = quantity,
                            price = price
                        ))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing order item map: ${e.message}")
                    }
                }
            }
            if (orderItems.isNotEmpty()) {
                try {
                    localDb.orderItemDao.deleteItemsForOrder(orderId)
                    localDb.orderItemDao.insertOrderItems(orderItems)
                    Log.d(TAG, "Processed and saved ${orderItems.size} order items locally for orderId: $orderId")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed inserting synced order items: ${e.message}")
                }
            }
        }
    }

    fun listenToOrderRealtime(
        orderId: Long,
        onUpdate: (Order) -> Unit
    ) {
        val dbInstance = firestore ?: return
        
        // Clean up previous registration to prevent memory leaks!
        orderListenerRegistration?.remove()
        orderListenerRegistration = null
        
        Log.d(TAG, "Starting real-time Firestore listener for order_id: $orderId")
        orderListenerRegistration = dbInstance.collection("orders")
            .document(orderId.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed for order $orderId", e)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val orderLocalId = snapshot.getLong("id") ?: orderId
                        val order = Order(
                            id = orderLocalId,
                            userId = snapshot.getString("userId") ?: "",
                            vendorId = snapshot.getLong("vendorId") ?: 0L,
                            vendorName = snapshot.getString("vendorName") ?: "",
                            status = snapshot.getString("status") ?: "PENDING",
                            totalAmount = snapshot.getDouble("totalAmount") ?: 0.0,
                            subtotal = snapshot.getDouble("subtotal") ?: 0.0,
                            deliveryFee = snapshot.getDouble("deliveryFee") ?: 0.0,
                            couponDiscount = snapshot.getDouble("couponDiscount") ?: 0.0,
                            tipAmount = snapshot.getDouble("tipAmount") ?: 0.0,
                            itemsCount = snapshot.getLong("itemsCount")?.toInt() ?: 0,
                            timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis(),
                            otpCode = snapshot.getString("otpCode") ?: "1234",
                            customerLat = snapshot.getDouble("customerLat") ?: 11.5812,
                            customerLng = snapshot.getDouble("customerLng") ?: 77.8465
                        )
                        syncScope.launch {
                            saveOrderItemsFromDoc(orderLocalId, snapshot.get("items"))
                        }
                        onUpdate(order)
                    } catch (ex: java.lang.Exception) {
                        Log.e(TAG, "Error parsing order snapshot", ex)
                    }
                }
            }
    }

    fun stopOrderRealtimeListener() {
        orderListenerRegistration?.remove()
        orderListenerRegistration = null
        Log.d(TAG, "Stopped order real-time listener")
    }

    fun listenToDeliveryRideRealtime(
        orderId: Long,
        onUpdate: (DeliveryRide) -> Unit
    ) {
        val dbInstance = firestore ?: return
        
        // Clean up previous registration to prevent memory leaks!
        rideListenerRegistration?.remove()
        rideListenerRegistration = null
        
        Log.d(TAG, "Starting real-time Firestore listener for ride on order_id: $orderId")
        // Find ride in collection "delivery_rides" where orderId == orderId
        rideListenerRegistration = dbInstance.collection("delivery_rides")
            .whereEqualTo("orderId", orderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed for delivery ride on order $orderId", e)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null && doc.exists()) {
                        try {
                            val ride = DeliveryRide(
                                id = doc.getLong("id") ?: 0L,
                                orderId = doc.getLong("orderId") ?: orderId,
                                riderName = doc.getString("riderName") ?: "",
                                riderPhone = doc.getString("riderPhone") ?: "",
                                status = doc.getString("status") ?: "ACCEPTED",
                                currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                earnings = doc.getDouble("earnings") ?: 0.0,
                                otpVerified = doc.getBoolean("otpVerified") ?: false
                            )
                            onUpdate(ride)
                        } catch (ex: java.lang.Exception) {
                            Log.e(TAG, "Error parsing delivery ride snapshot", ex)
                        }
                    }
                }
            }
    }

    fun stopDeliveryRideRealtimeListener() {
        rideListenerRegistration?.remove()
        rideListenerRegistration = null
        Log.d(TAG, "Stopped delivery ride real-time listener")
    }

    fun listenToAllOrdersRealtime(
        onUpdate: (List<Order>) -> Unit
    ) {
        val dbInstance = firestore ?: return
        
        allOrdersListenerRegistration?.remove()
        allOrdersListenerRegistration = null
        
        Log.d(TAG, "Starting all orders real-time listener for Admin Control Tower")
        allOrdersListenerRegistration = dbInstance.collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed for all orders", e)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val list = mutableListOf<Order>()
                    for (doc in snapshot.documents) {
                        try {
                            val orderLocalId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
                            val order = Order(
                                id = orderLocalId,
                                userId = doc.getString("userId") ?: "",
                                vendorId = doc.getLong("vendorId") ?: 0L,
                                vendorName = doc.getString("vendorName") ?: "",
                                status = doc.getString("status") ?: "PENDING",
                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                subtotal = doc.getDouble("subtotal") ?: 0.0,
                                deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                                couponDiscount = doc.getDouble("couponDiscount") ?: 0.0,
                                tipAmount = doc.getDouble("tipAmount") ?: 0.0,
                                itemsCount = doc.getLong("itemsCount")?.toInt() ?: 0,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                otpCode = doc.getString("otpCode") ?: "1234",
                                customerLat = doc.getDouble("customerLat") ?: 11.5812,
                                customerLng = doc.getDouble("customerLng") ?: 77.8465
                            )
                            syncScope.launch {
                                saveOrderItemsFromDoc(orderLocalId, doc.get("items"))
                            }
                            list.add(order)
                        } catch (ex: java.lang.Exception) {}
                    }
                    onUpdate(list)
                }
            }
    }

    fun stopAllOrdersRealtimeListener() {
        allOrdersListenerRegistration?.remove()
        allOrdersListenerRegistration = null
        Log.d(TAG, "Stopped all orders real-time listener")
    }

    suspend fun sendOrderMessage(orderId: Long, senderId: String, senderRole: String, text: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val msg = hashMapOf(
            "senderId" to senderId,
            "senderRole" to senderRole,
            "text" to text,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        try {
            db.collection("order_messages").document(orderId.toString())
                .collection("messages").add(msg).await()
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "sendOrderMessage fails", e)
        }
    }

    suspend fun clearOrderMessages(orderId: Long) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        try {
            val snapshot = db.collection("order_messages").document(orderId.toString())
                .collection("messages").get().await()
            for (doc in snapshot.documents) {
                db.collection("order_messages").document(orderId.toString())
                    .collection("messages").document(doc.id).delete().await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "clearOrderMessages fails", e)
        }
    }

    fun listenToOrderMessagesRealtime(
        orderId: Long,
        onMessagesChanged: (List<com.example.data.database.OrderMessage>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("order_messages").document(orderId.toString())
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("LyoFirebaseHelper", "Listen messages failed", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            val senderId = doc.getString("senderId") ?: ""
                            val senderRole = doc.getString("senderRole") ?: ""
                            val text = doc.getString("text") ?: ""
                            com.example.data.database.OrderMessage(senderId, senderRole, text)
                        }
                        onMessagesChanged(messages)
                    }
                }
        } catch (e: Exception) {
            Log.e("LyoFirebaseHelper", "listenToOrderMessagesRealtime failed", e)
            null
        }
    }
}
