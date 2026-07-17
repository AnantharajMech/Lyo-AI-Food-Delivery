package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.example.BuildConfig
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

object LyoFirebaseHelper {
    private const val TAG = "LyoFirebaseHelper"
    
    var db: AppDatabase? = null
    var appContext: Context? = null
    var transientPaymentMethod: String = "COD"
    var transientOrderAddress: String = ""
    var transientUpiTransactionId: String = ""
    
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

            /*
             * NOTE: This Android app is registered in the Firebase project "lyo-ai-food-delivery"
             * under Package Name: com.lyo.fooddelivery
             *
             * INSTRUCTIONS:
             * To connect to Firebase, the real google-services.json file must be downloaded
             * from the Firebase Console and placed inside the 'app' module at 'app/google-services.json'.
             * The Google Services Gradle plugin (if applied) or Android resources will then supply
             * the required Firebase configuration at runtime.
             */
            try {
                FirebaseApp.initializeApp(context)
                isInitialized = true
                Log.d(TAG, "Firebase initialized successfully from resources")

                // Initialize Firebase App Check immediately upon app startup
                try {
                    val firebaseAppCheck = FirebaseAppCheck.getInstance()
                    if (BuildConfig.DEBUG) {
                        firebaseAppCheck.installAppCheckProviderFactory(
                            DebugAppCheckProviderFactory.getInstance()
                        )
                        Log.d(TAG, "Firebase App Check initialized with Debug provider")
                    } else {
                        firebaseAppCheck.installAppCheckProviderFactory(
                            PlayIntegrityAppCheckProviderFactory.getInstance()
                        )
                        Log.d(TAG, "Firebase App Check initialized with Play Integrity provider")
                    }
                } catch (appCheckEx: Exception) {
                    Log.e(TAG, "Failed to initialize Firebase App Check: ${appCheckEx.message}", appCheckEx)
                }
            } catch (e: Exception) {
                try {
                    Log.w(TAG, "Resource-based initialization failed, attempting programmatic fallback: ${e.message}")
                    val options = FirebaseOptions.Builder()
                        .setApiKey(BuildConfig.FIREBASE_API_KEY)
                        .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                        .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                        .setDatabaseUrl(BuildConfig.FIREBASE_DATABASE_URL)
                        .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    isInitialized = true
                    Log.d(TAG, "Firebase initialized successfully from programmatic options fallback")

                    // Initialize Firebase App Check immediately upon app startup
                    try {
                        val firebaseAppCheck = FirebaseAppCheck.getInstance()
                        if (BuildConfig.DEBUG) {
                            firebaseAppCheck.installAppCheckProviderFactory(
                                DebugAppCheckProviderFactory.getInstance()
                            )
                            Log.d(TAG, "Firebase App Check initialized with Debug provider (fallback)")
                        } else {
                            firebaseAppCheck.installAppCheckProviderFactory(
                                PlayIntegrityAppCheckProviderFactory.getInstance()
                            )
                            Log.d(TAG, "Firebase App Check initialized with Play Integrity provider (fallback)")
                        }
                    } catch (appCheckEx: Exception) {
                        Log.e(TAG, "Failed to initialize Firebase App Check (fallback): ${appCheckEx.message}", appCheckEx)
                    }
                } catch (fallbackEx: Exception) {
                    throw IllegalStateException(
                        "Firebase Android configuration is missing or invalid. Download google-services.json for com.lyo.fooddelivery from Firebase Console and place it in app/google-services.json or configure environment variables.",
                        fallbackEx
                    )
                }
            }

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
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}", e)
            throw e
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
            val cachedUser = if (phone.isNotBlank()) db?.userDao?.getUserByPhone(phone) else null
            val cachedName = cachedUser?.name?.takeIf { it.isNotBlank() }
            val name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: cachedName ?: "Lyo Customer"
            
            val doc = dbInstance.collection("users").document(firebaseUser.uid).get().await()
            val user = if (doc.exists()) {
                val existingRole = doc.getString("role") ?: "CUSTOMER"
                val rawRole = overrideRole ?: existingRole
                val finalRole = if (rawRole == "DELIVERY" || rawRole == "RIDER") "RIDER" else rawRole
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
                    salaryRate = doc.getDouble("salaryRate") ?: 0.0,
                    uid = firebaseUser.uid
                )
            } else {
                val rawRole = overrideRole ?: "CUSTOMER"
                val finalRole = if (rawRole == "DELIVERY" || rawRole == "RIDER") "RIDER" else rawRole
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
                    salaryRate = 0.0,
                    uid = firebaseUser.uid
                )
                val userMap = mutableMapOf<String, Any>(
                    "uid" to firebaseUser.uid,
                    "phone" to phone,
                    "name" to name,
                    "email" to email,
                    "address" to (address ?: "Idappadi, Salem, Tamil Nadu, 637101"),
                    "lat" to (lat ?: 11.5812),
                    "lng" to (lng ?: 77.8465),
                    "isWhatsAppOptIn" to true,
                    "role" to finalRole
                )
                if (finalRole == "RIDER" || finalRole == "DELIVERY") {
                    userMap["vehicleNo"] = vehicleNo ?: ""
                    userMap["isActiveRider"] = true
                    userMap["salaryType"] = "MONTHLY"
                    userMap["salaryRate"] = 0.0
                }
                dbInstance.collection("users").document(firebaseUser.uid).set(userMap).await()
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

    fun normalizePhone(phone: String): String {
        val trimmed = phone.trim().replace(" ", "").replace("-", "")
        val digits = trimmed.filter { it.isDigit() }
        return if (digits.length >= 10) {
            digits.takeLast(10)
        } else {
            trimmed
        }
    }

    // For Auth
    suspend fun getUidByPhone(phone: String): String? = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext null
        try {
            val normalized = normalizePhone(phone)
            val variants = listOf(normalized, "+91$normalized", "91$normalized", phone.trim()).filter { it.isNotEmpty() }.distinct()
            val querySnapshot = dbInstance.collection("users")
                .whereIn("phone", variants)
                .limit(1)
                .get()
                .await()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.firstOrNull()?.id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting UID by phone: ${e.message}")
            null
        }
    }

    suspend fun registerInFirebase(user: User, plaintextPassword: String? = null): Boolean = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext false
        val dbInstance = firestore ?: return@withContext false
        
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
            val normalizedPhone = normalizePhone(user.phone)
            val email = "${normalizedPhone}@lyofoods.in"
            var uid = authInstance.currentUser?.let {
                if (it.email?.lowercase() == email.lowercase()) it.uid else null
            }
            if (uid == null) {
                uid = getUidByPhone(normalizedPhone) ?: getUidByPhone(user.phone)
            }

            if (uid == null && actualAuthPassword != null) {
                try {
                    // Create Firebase Auth user
                    val result = authInstance.createUserWithEmailAndPassword(email, actualAuthPassword).await()
                    uid = result.user?.uid
                    Log.d(TAG, "FirebaseAuth createUserWithEmailAndPassword succeeded for ${normalizedPhone}")
                } catch (authEx: Exception) {
                    Log.w(TAG, "FirebaseAuth user creation failed, checking if already in use: ${authEx.message}")
                    if (authEx is com.google.firebase.auth.FirebaseAuthUserCollisionException || 
                        authEx.message?.contains("already in use") == true || 
                        authEx.message?.contains("collision") == true) {
                        try {
                            // Sign in with the expected password to verify or reuse existing auth account
                            val signInResult = authInstance.signInWithEmailAndPassword(email, actualAuthPassword).await()
                            uid = signInResult.user?.uid
                            Log.d(TAG, "Successfully signed in existing auth user to synchronize registration")
                        } catch (signInEx: Exception) {
                            Log.e(TAG, "Could not sign in existing auth user: ${signInEx.message}")
                            throw signInEx
                        }
                    } else {
                        throw authEx
                    }
                }
            }

            if (uid == null) {
                uid = "uid_${normalizedPhone}"
                Log.w(TAG, "Using deterministic local fallback UID: $uid")
            }

            // Sync the updated UID to local SQLite database immediately
            val localDb = db
            if (localDb != null) {
                val localUser = localDb.userDao.getUserByPhone(user.phone)
                if (localUser != null && (localUser.uid != uid)) {
                    localDb.userDao.insertUser(localUser.copy(uid = uid))
                    Log.d(TAG, "Synchronized generated Firebase UID ($uid) to local SQLite database user: ${user.phone}")
                }
            }

            // Save details to Firestore
            val mappedRole = if (user.role == "DELIVERY" || user.role == "RIDER") "RIDER" else user.role
            val userMap = mutableMapOf<String, Any>(
                "uid" to uid,
                "phone" to normalizedPhone,
                "name" to user.name,
                "username" to normalizedPhone, // Default username
                "email" to user.email,
                "address" to user.address,
                "lat" to user.lat,
                "lng" to user.lng,
                "isWhatsAppOptIn" to user.isWhatsAppOptIn,
                "role" to mappedRole,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            if (mappedRole == "CUSTOMER") {
                // To comply with strict firestore.rules, do not include wallet, balance, approved, salaryType, salaryRate, isActiveRider, vehicleNo, or isActive fields on customer creation
            } else {
                userMap["isActive"] = true
                if (mappedRole == "RIDER" || mappedRole == "DELIVERY") {
                    userMap["vehicleNo"] = user.vehicleNo
                    userMap["isActiveRider"] = user.isActiveRider
                    userMap["salaryType"] = user.salaryType
                    userMap["salaryRate"] = user.salaryRate
                }
            }
            if (actualAuthPassword != null) {
                userMap["passwordHash"] = actualAuthPassword
            }
            // Only add createdAt if it's a new registration or not existing in doc
            val docRef = dbInstance.collection("users").document(uid)
            val docExists = docRef.get().await().exists()
            if (!docExists) {
                userMap["createdAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
            }
            
            docRef.set(userMap, SetOptions.merge()).await()
            if (mappedRole == "RIDER" || mappedRole == "DELIVERY") {
                val riderMap = userMap.toMutableMap()
                dbInstance.collection("riders").document(uid).set(riderMap, SetOptions.merge()).await()
                Log.d(TAG, "Rider document successfully updated in riders collection.")
            }
            
            val docVerify = docRef.get().await()
            if (!docVerify.exists()) {
                throw Exception("Firestore verification failed: user document was not found after creation!")
            }
            
            if (user.role == "ADMIN") {
                dbInstance.collection("admins").document(uid).set(mapOf("phone" to user.phone)).await()
                Log.d(TAG, "Registered admin sync successful for UID: $uid")
            }
            Log.d(TAG, "Registered/Updated user ${user.phone} successfully on Firebase and Firestore with UID: $uid")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase registration/update failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun loginInFirebase(phone: String, pass: String): User? = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext null
        val dbInstance = firestore ?: return@withContext null
        try {
            val trimmedPhone = phone.trim()
            val normalizedPhone = normalizePhone(trimmedPhone)
            val phoneVariants = listOf(normalizedPhone).filter { it.isNotEmpty() }

            val emails = if (trimmedPhone.contains("@")) {
                listOf(trimmedPhone)
            } else {
                listOf("${normalizedPhone}@lyofoods.in", "${normalizedPhone}@lyofresh.in")
            }

            // Resolve login password - if it's already a SHA-256 hash or is plaintext
            val hashed = if (pass.length == 64 && pass.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                pass
            } else {
                hashPassword(pass)
            }
            val passwords = listOf(hashed, pass.trim())

            var authRes: com.google.firebase.auth.AuthResult? = null
            var lastAuthEx: Exception? = null

            // Try candidates in a loop to sign in
            for (emailCandidate in emails) {
                for (passwordCandidate in passwords) {
                    try {
                        authRes = authInstance.signInWithEmailAndPassword(emailCandidate, passwordCandidate).await()
                        if (authRes != null) break
                    } catch (e: Exception) {
                        lastAuthEx = e
                    }
                }
                if (authRes != null) break
            }

            if (authRes == null) {
                Log.e(TAG, "All FirebaseAuth attempts failed: ${lastAuthEx?.message}")
                if (lastAuthEx is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                    lastAuthEx is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    throw lastAuthEx
                }
                return@withContext null
            }

            val uid = authRes.user?.uid ?: return@withContext null

            // Successfully authenticated! Now we have permissions to read our Firestore profile!
            var doc = dbInstance.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                // Check if old-format document exists under the phone number
                val oldDoc = dbInstance.collection("users").document(trimmedPhone).get().await()
                if (oldDoc.exists()) {
                    Log.d(TAG, "Dynamically migrating old-format phone-ID document to uid-ID document: $uid")
                    val newProfileMap = oldDoc.data?.toMutableMap() ?: mutableMapOf()
                    newProfileMap["uid"] = uid
                    newProfileMap["phone"] = trimmedPhone
                    newProfileMap["isActive"] = oldDoc.getBoolean("isActive") ?: oldDoc.getBoolean("isActiveRider") ?: true
                    val safeCreatedAt = try {
                        oldDoc.getTimestamp("createdAt")
                    } catch (e: Exception) {
                        try {
                            oldDoc.getLong("createdAt")?.let { com.google.firebase.Timestamp(it / 1000L, 0) }
                        } catch (e2: Exception) {
                            null
                        }
                    } ?: com.google.firebase.firestore.FieldValue.serverTimestamp()
                    newProfileMap["createdAt"] = safeCreatedAt
                    newProfileMap["updatedAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                    
                    newProfileMap.remove("password")
                    
                    val currentRole = oldDoc.getString("role") ?: "CUSTOMER"
                    val normalizedRole = when (currentRole.uppercase()) {
                        "ADMIN" -> "ADMIN"
                        "CUSTOMER_CARE" -> "CUSTOMER_CARE"
                        "RIDER", "DELIVERY" -> "RIDER"
                        else -> "CUSTOMER"
                    }
                    newProfileMap["role"] = normalizedRole
                    
                    dbInstance.collection("users").document(uid).set(newProfileMap, SetOptions.merge()).await()
                    
                    val verifiedDoc = dbInstance.collection("users").document(uid).get().await()
                    if (verifiedDoc.exists()) {
                        dbInstance.collection("users").document(trimmedPhone).delete().await()
                        Log.d(TAG, "Dynamic migration successful! Deleted old phone-ID document.")
                        doc = verifiedDoc
                    }
                }
            }
            
            if (doc.exists()) {
                val rawRole = doc.getString("role") ?: "CUSTOMER"
                
                if (rawRole.isNullOrBlank() || rawRole.trim() !in listOf("CUSTOMER", "ADMIN", "RIDER", "DELIVERY")) {
                    Log.e(TAG, "User profile is incomplete or has invalid role: $rawRole")
                    authInstance.signOut()
                    return@withContext null
                }
                
                val isActiveVal = doc.getBoolean("isActive") ?: doc.getBoolean("isActiveRider") ?: true
                if (!isActiveVal) {
                    Log.e(TAG, "User account is inactive.")
                    return@withContext null
                }
                
                val finalRole = if (rawRole == "DELIVERY" || rawRole == "RIDER") "RIDER" else rawRole
                val user = User(
                    phone = doc.getString("phone") ?: trimmedPhone,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    address = doc.getString("address") ?: "",
                    lat = doc.getDouble("lat") ?: 0.0,
                    lng = doc.getDouble("lng") ?: 0.0,
                    isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                    role = finalRole,
                    vehicleNo = doc.getString("vehicleNo") ?: "",
                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                    salaryRate = doc.getDouble("salaryRate") ?: 0.0,
                    uid = doc.getString("uid") ?: uid
                )
                
                Log.d(TAG, "Fetched logged-in Firestore user: ${user.phone}")
                if (user.role == "ADMIN") {
                    dbInstance.collection("admins").document(uid).set(mapOf("phone" to user.phone)).await()
                    Log.d(TAG, "Logged in admin sync successful for UID: $uid")
                }
                user
            } else {
                Log.d(TAG, "Authenticated but profile missing in LyoFirebaseHelper. Auto-creating customer profile for UID: $uid")
                val finalRole = "CUSTOMER"
                val cachedUser = if (trimmedPhone.isNotBlank()) db?.userDao?.getUserByPhone(trimmedPhone) else null
                val hasRealLocalName = cachedUser != null && cachedUser.name.isNotBlank() && cachedUser.name != "Lyo Customer"
                
                val finalName = if (hasRealLocalName) cachedUser!!.name else "Lyo Customer"
                val finalEmail = if (hasRealLocalName && cachedUser!!.email.isNotBlank()) cachedUser.email else "${normalizedPhone}@lyofoods.in"
                val finalAddress = if (hasRealLocalName) cachedUser!!.address else ""
                val finalLat = if (hasRealLocalName) cachedUser!!.lat else 11.5812
                val finalLng = if (hasRealLocalName) cachedUser!!.lng else 77.8465
                val finalWhatsApp = if (hasRealLocalName) cachedUser!!.isWhatsAppOptIn else true
                
                val userMap = mapOf(
                    "uid" to uid,
                    "phone" to trimmedPhone,
                    "name" to finalName,
                    "email" to finalEmail,
                    "address" to finalAddress,
                    "lat" to finalLat,
                    "lng" to finalLng,
                    "isWhatsAppOptIn" to finalWhatsApp,
                    "role" to finalRole,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                dbInstance.collection("users").document(uid).set(userMap).await()
                val user = User(
                    phone = trimmedPhone,
                    name = finalName,
                    email = finalEmail,
                    address = finalAddress,
                    lat = finalLat,
                    lng = finalLng,
                    isWhatsAppOptIn = finalWhatsApp,
                    role = finalRole,
                    uid = uid
                )
                user
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase login failed: ${e.message}", e)
            if (e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                e is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                throw e
            }
            null
        }
    }

    suspend fun fetchAndSyncRidersFromFirestore(userDao: com.example.data.database.UserDao): Unit = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            val snapshot = dbInstance.collection("users")
                .whereIn("role", listOf("DELIVERY", "RIDER"))
                .get()
                .await()
            userDao.clearAllRiders()
            for (doc in snapshot.documents) {
                val phone = doc.getString("phone") ?: continue
                if (phone.startsWith("999991") || phone == "9000000002" || phone == "9000000003") {
                    continue
                }
                val user = User(
                    phone = phone,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    address = doc.getString("address") ?: "",
                    lat = doc.getDouble("lat") ?: 11.5812,
                    lng = doc.getDouble("lng") ?: 77.8465,
                    isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                    role = "RIDER", // Force-normalize to RIDER locally so Room filters it correctly
                    vehicleNo = doc.getString("vehicleNo") ?: "",
                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                    salaryRate = doc.getDouble("salaryRate") ?: 0.0,
                    uid = doc.getString("uid") ?: ""
                )
                userDao.insertUser(user)
            }
            Log.d(TAG, "Force pre-fetched and synchronized ${snapshot.size()} riders from Firestore successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed force pre-fetching riders: ${e.message}")
        }
    }

    suspend fun getUserByPhoneFromFirestore(phone: String): User? = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext null
        try {
            val querySnapshot = dbInstance.collection("users")
                .whereEqualTo("phone", phone.trim())
                .limit(1)
                .get()
                .await()
            val doc = querySnapshot.documents.firstOrNull()
            if (doc != null && doc.exists()) {
                val rawRole = doc.getString("role")
                if (rawRole.isNullOrBlank() || rawRole.trim() !in listOf("CUSTOMER", "ADMIN", "RIDER", "DELIVERY")) {
                    null
                } else {
                    val finalRole = if (rawRole == "DELIVERY" || rawRole == "RIDER") "RIDER" else rawRole
                    User(
                        phone = doc.getString("phone") ?: phone,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        address = doc.getString("address") ?: "",
                        lat = doc.getDouble("lat") ?: 0.0,
                        lng = doc.getDouble("lng") ?: 0.0,
                        isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                        role = finalRole,
                        vehicleNo = doc.getString("vehicleNo") ?: "",
                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                    salaryRate = doc.getDouble("salaryRate") ?: 0.0,
                    uid = doc.getString("uid") ?: ""
                )
               }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed getting user from Firestore by phone: ${e.message}")
            null
        }
    }

    suspend fun deleteUserFromFirestore(phone: String): Boolean = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext false
        try {
            val uid = getUidByPhone(phone)
            if (uid != null) {
                dbInstance.collection("users").document(uid).delete().await()
                Log.d(TAG, "Deleted user from Firestore with UID: $uid (Phone: $phone)")
                true
            } else {
                Log.w(TAG, "Could not find UID for user with phone $phone to delete")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user $phone from Firestore: ${e.message}")
            false
        }
    }

    suspend fun migrateExistingProfiles() = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        val authInstance = auth ?: return@withContext
        try {
            Log.d(TAG, "Starting safe one-time user profiles migration...")
            val usersSnapshot = dbInstance.collection("users").get().await()
            for (doc in usersSnapshot.documents) {
                val docId = doc.id
                // A standard Firebase UID is 28 characters. If docId length is less than 20 or consists only of digits (like phone number), it's an old format document.
                val isOldFormat = docId.length < 20 || docId.all { it.isDigit() }
                if (isOldFormat) {
                    val phone = doc.getString("phone") ?: docId
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: "${phone.trim()}@lyofoods.in"
                    val passwordHash = doc.getString("passwordHash") ?: doc.getString("password") ?: ""
                    
                    if (phone.isNotBlank()) {
                        Log.d(TAG, "Migrating old user profile for phone: $phone")
                        // Determine the correct UID by trying to create/authenticate Auth user
                        var correctUid: String? = null
                        if (passwordHash.isNotBlank()) {
                            try {
                                val createResult = authInstance.createUserWithEmailAndPassword(email, passwordHash).await()
                                correctUid = createResult.user?.uid
                            } catch (e: Exception) {
                                // Already exists, try to sign in to retrieve UID
                                try {
                                    val signResult = authInstance.signInWithEmailAndPassword(email, passwordHash).await()
                                    correctUid = signResult.user?.uid
                                } catch (signInEx: Exception) {
                                    Log.e(TAG, "Could not sign in during migration: ${signInEx.message}")
                                }
                            }
                        }
                        
                        // If passwordHash was blank or auth login failed, but we have a currently logged in user that matches this phone
                        if (correctUid == null && authInstance.currentUser != null) {
                            val curUser = authInstance.currentUser
                            if (curUser?.email?.lowercase() == email.lowercase()) {
                                correctUid = curUser.uid
                            }
                        }
                        
                        // Fallback: If we couldn't resolve the UID because we don't have the password, we shouldn't migrate or delete yet.
                        if (correctUid != null) {
                            // Copy all valid existing data
                            val newProfileMap = doc.data?.toMutableMap() ?: mutableMapOf()
                            newProfileMap["uid"] = correctUid
                            newProfileMap["phone"] = phone
                            newProfileMap["isActive"] = doc.getBoolean("isActive") ?: doc.getBoolean("isActiveRider") ?: true
                            val safeCreatedAt = try {
                                doc.getTimestamp("createdAt")
                            } catch (e: Exception) {
                                try {
                                    doc.getLong("createdAt")?.let { com.google.firebase.Timestamp(it / 1000L, 0) }
                                } catch (e2: Exception) {
                                    null
                                }
                            } ?: com.google.firebase.firestore.FieldValue.serverTimestamp()
                            newProfileMap["createdAt"] = safeCreatedAt
                            newProfileMap["updatedAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                            
                            // Remove insecure password field if present
                            newProfileMap.remove("password")
                            
                            // Ensure role is normalized
                            val currentRole = doc.getString("role") ?: "CUSTOMER"
                            val normalizedRole = when (currentRole.uppercase()) {
                                "ADMIN" -> "ADMIN"
                                "CUSTOMER_CARE" -> "CUSTOMER_CARE"
                                "RIDER", "DELIVERY" -> "RIDER"
                                else -> "CUSTOMER"
                            }
                            newProfileMap["role"] = normalizedRole
                            
                            // Write to the new document ID
                            dbInstance.collection("users").document(correctUid).set(newProfileMap, SetOptions.merge()).await()
                            Log.d(TAG, "Successfully copied profile to users/$correctUid")
                            
                            // Verify the new document exists before deleting the old one
                            val verifiedDoc = dbInstance.collection("users").document(correctUid).get().await()
                            if (verifiedDoc.exists()) {
                                dbInstance.collection("users").document(docId).delete().await()
                                Log.d(TAG, "Successfully deleted old profile document: $docId")
                            }
                        } else {
                            Log.w(TAG, "Could not resolve correct UID for user $phone. Skipping migration for now.")
                        }
                    }
                }
            }
            Log.d(TAG, "Safe user profiles migration completed successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Migration error: ${e.message}", e)
        }
    }

    suspend fun migrateCatalogFields() = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            Log.d(TAG, "Running safe catalog fields default value migration...")
            
            // 1. Vendors status migration
            val vendors = dbInstance.collection("vendors").get().await()
            for (doc in vendors.documents) {
                if (!doc.contains("status")) {
                    try {
                        dbInstance.collection("vendors").document(doc.id).update("status", "ACTIVE").await()
                        Log.d(TAG, "Migrated vendor ${doc.id} with status = ACTIVE")
                    } catch (e: Exception) {
                        // Ignore permission/read/write issues
                    }
                }
            }

            // 2. Promo Banners status migration
            val banners = dbInstance.collection("promo_banners").get().await()
            for (doc in banners.documents) {
                if (!doc.contains("status")) {
                    try {
                        dbInstance.collection("promo_banners").document(doc.id).update("status", "ACTIVE").await()
                        Log.d(TAG, "Migrated banner ${doc.id} with status = ACTIVE")
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }

            // 3. Categories isHidden migration
            val categories = dbInstance.collection("categories").get().await()
            for (doc in categories.documents) {
                if (!doc.contains("isHidden")) {
                    try {
                        dbInstance.collection("categories").document(doc.id).update("isHidden", false).await()
                        Log.d(TAG, "Migrated category ${doc.id} with isHidden = false")
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }

            // 4. Menu Items isAvailable migration
            val items = dbInstance.collection("menu_items").get().await()
            for (doc in items.documents) {
                if (!doc.contains("isAvailable")) {
                    try {
                        dbInstance.collection("menu_items").document(doc.id).update("isAvailable", true).await()
                        Log.d(TAG, "Migrated menu item ${doc.id} with isAvailable = true")
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Catalog fields migration skipped or failed (likely no write permission): ${e.message}")
        }
    }

    // Firestore Sync Operations
    suspend fun syncVendorToFirestore(vendor: Vendor) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
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
                "autoCloseTime" to vendor.autoCloseTime,
                "status" to vendor.status,
                "isOfferEnabled" to vendor.isOfferEnabled,
                "offerType" to vendor.offerType,
                "offerValue" to vendor.offerValue,
                "offerText" to vendor.offerText,
                "offerStartDate" to vendor.offerStartDate,
                "offerEndDate" to vendor.offerEndDate,
                "offerPriority" to vendor.offerPriority
            )
            runSafeFirestoreWrite {
                dbInstance.collection("vendors").document(idStr).set(vendorMap, SetOptions.merge()).await()
                dbInstance.collection("stores").document(idStr).set(vendorMap, SetOptions.merge()).await()
            }
            Log.d(TAG, "Synced vendor ${vendor.name} to Firestore (vendors & stores)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing vendor ${vendor.name}: ${e.message}")
            throw e
        }
    }

    suspend fun deleteVendorFromFirestore(vendorId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
            val idStr = vendorId.toString()
            runSafeFirestoreWrite {
                dbInstance.collection("vendors").document(idStr).delete().await()
                dbInstance.collection("stores").document(idStr).delete().await()
                
                // Delete associated menu items and categories
                val itemsDocs = dbInstance.collection("menu_items").whereEqualTo("vendorId", vendorId).get().await()
                for (doc in itemsDocs.documents) {
                    dbInstance.collection("menu_items").document(doc.id).delete().await()
                }
                val catDocs = dbInstance.collection("categories").whereEqualTo("vendorId", vendorId).get().await()
                for (doc in catDocs.documents) {
                    dbInstance.collection("categories").document(doc.id).delete().await()
                }
            }
            Log.d(TAG, "Deleted vendor $vendorId from Firestore complete")
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting vendor $vendorId: ${e.message}")
            throw e
        }
    }

    suspend fun clearMenuAndCategoriesFromFirestore(vendorId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
            runSafeFirestoreWrite {
                val itemsDocs = dbInstance.collection("menu_items").whereEqualTo("vendorId", vendorId).get().await()
                for (doc in itemsDocs.documents) {
                    dbInstance.collection("menu_items").document(doc.id).delete().await()
                }
                val catDocs = dbInstance.collection("categories").whereEqualTo("vendorId", vendorId).get().await()
                for (doc in catDocs.documents) {
                    dbInstance.collection("categories").document(doc.id).delete().await()
                }
            }
            Log.d(TAG, "Cleared categories & menu items from Firestore for vendor $vendorId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed clearing categories & menu items from Firestore for vendor $vendorId: ${e.message}")
            throw e
        }
    }

    suspend fun syncCategoryToFirestore(category: Category) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
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
            runSafeFirestoreWrite {
                dbInstance.collection("categories").document(category.id.toString()).set(catMap, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing category: ${e.message}")
            throw e
        }
    }

    suspend fun deleteCategoryFromFirestore(categoryId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
            runSafeFirestoreWrite {
                dbInstance.collection("categories").document(categoryId.toString()).delete().await()
                
                // Delete associated menu items
                val itemDocs = dbInstance.collection("menu_items").whereEqualTo("categoryId", categoryId).get().await()
                for (doc in itemDocs.documents) {
                    dbInstance.collection("menu_items").document(doc.id).delete().await()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting category from Firestore: ${e.message}")
            throw e
        }
    }

    suspend fun syncMenuItemToFirestore(item: MenuItem) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
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
            runSafeFirestoreWrite {
                dbInstance.collection("menu_items").document(item.id.toString()).set(itemMap, SetOptions.merge()).await()
                dbInstance.collection("vendors").document(item.vendorId.toString()).collection("products").document(item.id.toString()).set(itemMap, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing menu item: ${e.message}")
            throw e
        }
    }

    suspend fun deleteMenuItemFromFirestore(itemId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
            runSafeFirestoreWrite {
                dbInstance.collection("menu_items").document(itemId.toString()).delete().await()
                val localDb = db
                val menuItem = localDb?.menuItemDao?.getMenuItemById(itemId)
                if (menuItem != null) {
                    dbInstance.collection("vendors").document(menuItem.vendorId.toString()).collection("products").document(itemId.toString()).delete().await()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting menu item: ${e.message}")
            throw e
        }
    }

    suspend fun syncPromoBannerToFirestore(banner: PromoBanner) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
            val docId = if (banner.code.isNotBlank()) banner.code else banner.id.toString()
            val bannerMap = mapOf(
                "id" to banner.id,
                "code" to banner.code,
                "description" to banner.description,
                "imageUrl" to banner.imageUrl,
                "status" to banner.status
            )
            runSafeFirestoreWrite {
                dbInstance.collection("promo_banners").document(docId).set(bannerMap, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing promo banner: ${e.message}")
            throw e
        }
    }

    suspend fun deletePromoBannerFromFirestore(banner: PromoBanner) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            ensureFirebaseAdminAuth()
            val docId = if (banner.code.isNotBlank()) banner.code else banner.id.toString()
            runSafeFirestoreWrite {
                dbInstance.collection("promo_banners").document(docId).delete().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting promo banner: ${e.message}")
            throw e
        }
    }

    suspend fun createMenuBackup(vendorId: Long, vendorName: String): Boolean = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext false
        try {
            val categoriesSnapshot = dbInstance.collection("categories").whereEqualTo("vendorId", vendorId).get().await()
            val itemsSnapshot = dbInstance.collection("menu_items").whereEqualTo("vendorId", vendorId).get().await()

            val categoriesList = categoriesSnapshot.documents.map { doc ->
                mapOf(
                    "id" to doc.getLong("id"),
                    "vendorId" to doc.getLong("vendorId"),
                    "nameEn" to doc.getString("nameEn"),
                    "nameTa" to doc.getString("nameTa"),
                    "sortOrder" to doc.getLong("sortOrder"),
                    "isHidden" to doc.getBoolean("isHidden")
                )
            }

            val itemsList = itemsSnapshot.documents.map { doc ->
                mapOf(
                    "id" to doc.getLong("id"),
                    "vendorId" to doc.getLong("vendorId"),
                    "categoryId" to doc.getLong("categoryId"),
                    "nameEn" to doc.getString("nameEn"),
                    "nameTa" to doc.getString("nameTa"),
                    "descEn" to doc.getString("descEn"),
                    "descTa" to doc.getString("descTa"),
                    "price" to doc.getDouble("price"),
                    "isVeg" to doc.getBoolean("isVeg"),
                    "isAvailable" to doc.getBoolean("isAvailable"),
                    "imageUrl" to doc.getString("imageUrl")
                )
            }

            val timestamp = System.currentTimeMillis()
            val backupData = mapOf(
                "vendorId" to vendorId,
                "vendorName" to vendorName,
                "timestamp" to timestamp,
                "categories" to categoriesList,
                "menuItems" to itemsList
            )

            dbInstance.collection("menu_backups")
                .document("${vendorId}_$timestamp")
                .set(backupData)
                .await()

            Log.d(TAG, "Created backup for vendor $vendorId at $timestamp")

            try {
                val backupsSnapshot = dbInstance.collection("menu_backups")
                    .whereEqualTo("vendorId", vendorId)
                    .get()
                    .await()
                
                val sortedBackups = backupsSnapshot.documents
                    .sortedByDescending { it.getLong("timestamp") ?: 0L }

                if (sortedBackups.size > 5) {
                    for (i in 5 until sortedBackups.size) {
                        dbInstance.collection("menu_backups")
                            .document(sortedBackups[i].id)
                            .delete()
                            .await()
                        Log.d(TAG, "Deleted old backup: ${sortedBackups[i].id}")
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed pruning old backups: ${ex.message}")
            }

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Failed creating backup for vendor $vendorId: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun getLatestBackup(vendorId: Long): Map<String, Any>? = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext null
        try {
            val snapshot = dbInstance.collection("menu_backups")
                .whereEqualTo("vendorId", vendorId)
                .get()
                .await()
            val latestDoc = snapshot.documents.maxByOrNull { it.getLong("timestamp") ?: 0L }
            return@withContext latestDoc?.data
        } catch (e: Exception) {
            Log.e(TAG, "Failed getting latest backup: ${e.message}")
            return@withContext null
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
                
                // Sync categories of this vendor
                try {
                    val categories = db.categoryDao.getCategoriesForVendorList(v.id)
                    for (cat in categories) {
                        syncCategoryToFirestore(cat)
                    }
                } catch (ce: Exception) {
                    Log.e(TAG, "Failed syncing categories for vendor ${v.id}: ${ce.message}")
                }
                
                // Sync menu items of this vendor
                try {
                    val menuItems = db.menuItemDao.getMenuItemsForVendorList(v.id)
                    for (item in menuItems) {
                        syncMenuItemToFirestore(item)
                    }
                } catch (me: Exception) {
                    Log.e(TAG, "Failed syncing menu items for vendor ${v.id}: ${me.message}")
                }
            }
            Log.d(TAG, "Pushed ${vendors.size} vendors with their categories & menus successfully to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed completely local push: ${e.message}")
        }
    }

    suspend fun cancelOrderCustomerTransaction(orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext Result.failure(Exception("Firestore is not initialized"))
        try {
            dbInstance.runTransaction { transaction ->
                val docRef = dbInstance.collection("ek_orders").document(orderId.toString())
                val snapshot = transaction.get(docRef)
                
                if (!snapshot.exists()) {
                    throw Exception("Order does not exist in Firestore / ஆர்டர் இல்லை")
                }
                
                val currentStatus = (snapshot.getString("status") ?: "PENDING").uppercase()
                val cancellableStatuses = listOf("PENDING", "PLACED", "NEW", "READY_FOR_ACCEPTANCE")
                if (currentStatus !in cancellableStatuses) {
                    throw Exception("ACCEPTED_BLOCKED")
                }
                
                val updates = mapOf(
                    "status" to "CANCELLED",
                    "cancelledBy" to "customer",
                    "cancelledAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                transaction.update(docRef, updates)
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminAcceptOrderTransaction(orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext Result.failure(Exception("Firestore is not initialized"))
        try {
            dbInstance.runTransaction { transaction ->
                val docRef = dbInstance.collection("ek_orders").document(orderId.toString())
                val snapshot = transaction.get(docRef)
                
                if (!snapshot.exists()) {
                    throw Exception("Order does not exist in Firestore / ஆர்டர் இல்லை")
                }
                
                val currentStatus = (snapshot.getString("status") ?: "PENDING").uppercase()
                if (currentStatus == "CANCELLED") {
                    throw Exception("BLOCKED: Order was already cancelled by the customer / ஆர்டர் வாடிக்கையாளரால் ரத்து செய்யப்பட்டது")
                }
                
                val alreadyAcceptedStatuses = listOf("ACCEPTED", "PREPARING", "READY_FOR_PICKUP", "OUT_FOR_DELIVERY")
                if (currentStatus in alreadyAcceptedStatuses) {
                    return@runTransaction null
                }
                
                val cancellableStatuses = listOf("PENDING", "PLACED", "NEW", "READY_FOR_ACCEPTANCE")
                if (currentStatus !in cancellableStatuses) {
                    throw Exception("BLOCKED: Order is already accepted/processed / ஆர்டர் ஏற்கனவே ஏற்கப்பட்டது")
                }
                
                val updates = mapOf(
                    "status" to "ACCEPTED",
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                transaction.update(docRef, updates)
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun riderAcceptOrderTransaction(
        rideId: Long,
        orderId: Long,
        riderUid: String,
        riderName: String,
        riderPhone: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext Result.failure(Exception("Firestore is not initialized"))
        try {
            dbInstance.runTransaction { transaction ->
                val orderRef = dbInstance.collection("ek_orders").document(orderId.toString())
                val rideRef = dbInstance.collection("delivery_rides").document(rideId.toString())
                
                val orderSnapshot = transaction.get(orderRef)
                val rideSnapshot = transaction.get(rideRef)
                
                if (orderSnapshot.exists()) {
                    val existingRiderUid = orderSnapshot.getString("riderUid")
                    val existingStatus = orderSnapshot.getString("status") ?: "PENDING"
                    if (!existingRiderUid.isNullOrBlank() && existingRiderUid != riderUid) {
                        throw Exception("Order already accepted by another rider! / ஆர்டர் ஏற்கனவே வேறொரு நபரால் ஏற்கப்பட்டது!")
                    }
                    if (existingStatus == "CANCELLED") {
                        throw Exception("Order cancelled by customer. / ஆர்டர் வாடிக்கையாளரால் ரத்து செய்யப்பட்டது.")
                    }
                }
                
                if (rideSnapshot.exists()) {
                    val existingRiderUid = rideSnapshot.getString("riderUid")
                    val existingStatus = rideSnapshot.getString("status") ?: "ACCEPTED"
                    if (!existingRiderUid.isNullOrBlank() && existingRiderUid != riderUid) {
                        throw Exception("Ride already accepted by another rider!")
                    }
                    if (existingStatus == "COMPLETED" || existingStatus == "DELIVERED") {
                        throw Exception("Ride is already completed!")
                    }
                }
                
                // Update Order document in Firestore
                val orderUpdates = mapOf(
                    "status" to "ACCEPTED",
                    "riderUid" to riderUid,
                    "riderName" to riderName,
                    "riderPhone" to riderPhone,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                transaction.update(orderRef, orderUpdates)
                
                // Update Ride document in Firestore
                val rideUpdates = mapOf(
                    "status" to "PICKING_UP",
                    "riderUid" to riderUid,
                    "riderName" to riderName,
                    "riderPhone" to riderPhone,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                transaction.update(rideRef, rideUpdates)
                
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkOrCreateIdempotencyKey(
        userId: String,
        vendorId: Long,
        itemsSignature: String,
        newOrderId: Long
    ): Long = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext 0L
        try {
            // 3-minute window to prevent duplicate orders
            val timeWindow = System.currentTimeMillis() / 180000L
            val idempotencyKey = "order_idemp_${userId}_${vendorId}_${timeWindow}_$itemsSignature"
            val idempRef = dbInstance.collection("idempotency_keys").document(idempotencyKey)
            
            val resultOrderId = dbInstance.runTransaction { transaction ->
                val snapshot = transaction.get(idempRef)
                if (snapshot.exists()) {
                    val existingId = snapshot.getLong("orderId") ?: 0L
                    existingId
                } else {
                    val data = mapOf(
                        "orderId" to newOrderId,
                        "userId" to userId,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    transaction.set(idempRef, data)
                    0L
                }
            }.await()
            resultOrderId
        } catch (e: Exception) {
            Log.e(TAG, "Idempotency key check failed: ${e.message}", e)
            0L
        }
    }

    suspend fun syncOrderToFirestore(order: Order) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: throw Exception("Firestore instance is null")
        val currentUserId = order.userId
        val idStr = order.id.toString()
        val itemsList = mutableListOf<Map<String, Any>>()
        val localDb = db
        var riderUidVal: String? = null
        var orderItemsList = emptyList<OrderItem>()
        if (localDb != null) {
            try {
                val orderItems = localDb.orderItemDao.getItemsForOrder(order.id)
                orderItemsList = orderItems
                for (item in orderItems) {
                    itemsList.add(mapOf(
                        "menuItemId" to item.menuItemId,
                        "nameEn" to item.nameEn,
                        "nameTa" to item.nameTa,
                        "quantity" to item.quantity,
                        "price" to item.price
                    ))
                }
                
                val ride = localDb.deliveryRideDao.getRideForOrder(order.id)
                if (ride != null) {
                    riderUidVal = ride.riderUid
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed loading order items/ride for Firestore: ${ex.message}")
            }
        }

        // Load customer address and phone
        var addressVal = transientOrderAddress
        var phoneVal = ""
        if (localDb != null) {
            try {
                val localUser = localDb.userDao.getUserByPhone(order.userId)
                if (localUser != null) {
                    if (addressVal.isEmpty()) addressVal = localUser.address
                    phoneVal = localUser.phone
                }
            } catch (ex: Exception) {
                Log.w(TAG, "Failed fetching local user profile for order: ${ex.message}")
            }
        }
        if (addressVal.isEmpty() || phoneVal.isEmpty()) {
            try {
                val userDoc = dbInstance.collection("users").document(order.userId).get().await()
                if (userDoc.exists()) {
                    if (addressVal.isEmpty()) addressVal = userDoc.getString("address") ?: ""
                    if (phoneVal.isEmpty()) phoneVal = userDoc.getString("phone") ?: ""
                }
            } catch (ex: Exception) {
                Log.w(TAG, "Failed fetching Firestore user profile for order: ${ex.message}")
            }
        }

        val orderMap = mutableMapOf<String, Any>(
            "id" to order.id,
            "orderId" to order.id,
            "userId" to order.userId,
            "customerId" to order.userId,
            "vendorId" to order.vendorId,
            "vendorName" to order.vendorName,
            "status" to order.status,
            "orderStatus" to order.status,
            "totalAmount" to order.totalAmount,
            "grandTotal" to order.totalAmount,
            "subtotal" to order.subtotal,
            "deliveryFee" to order.deliveryFee,
            "deliveryCharge" to order.deliveryFee,
            "couponDiscount" to order.couponDiscount,
            "tipAmount" to order.tipAmount,
            "itemsCount" to order.itemsCount,
            "timestamp" to order.timestamp,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "otpCode" to order.otpCode,
            "customerLat" to order.customerLat,
            "customerLng" to order.customerLng,
            "items" to itemsList,
            "quantities" to orderItemsList.map { it.quantity },
            "prices" to orderItemsList.map { it.price },
            "address" to addressVal,
            "phone" to phoneVal,
            "paymentMethod" to transientPaymentMethod,
            "paymentStatus" to if (transientPaymentMethod == "UPI") "PAID_PENDING_VERIFICATION" else "PENDING",
            "upiTransactionId" to if (transientPaymentMethod == "UPI") transientUpiTransactionId else ""
        )
        if (riderUidVal != null && riderUidVal.isNotEmpty()) {
            orderMap["riderUid"] = riderUidVal
            orderMap["assignedPartnerId"] = riderUidVal
        }
        dbInstance.collection("ek_orders").document(idStr).set(orderMap, SetOptions.merge()).await()
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
                "riderUid" to ride.riderUid,
                "status" to ride.status,
                "currentLat" to ride.currentLat,
                "currentLng" to ride.currentLng,
                "totalDistance" to ride.totalDistance,
                "earnings" to ride.earnings,
                "otpVerified" to ride.otpVerified,
                "locationTimestamp" to System.currentTimeMillis()
            )
            dbInstance.collection("delivery_rides").document(idStr).set(rideMap, SetOptions.merge()).await()
            Log.d(TAG, "Synced delivery_ride_id $idStr successfully to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing delivery ride: ${e.message}")
            appContext?.let { ctx ->
                withContext(Dispatchers.Main) {
                    val friendlyMsg = getFriendlyPermissionErrorMessage(e)
                    android.widget.Toast.makeText(ctx, "Failed syncing delivery ride: $friendlyMsg", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            throw e
        }
    }

    val syncScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
    var repositoryRef: LyoRepository? = null
    private var isPaused = false
    private var userCollectorJob: kotlinx.coroutines.Job? = null
    private var scheduleCheckJob: kotlinx.coroutines.Job? = null

    private var vendorsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var categoriesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var menuItemsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var usersListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var persistentOrdersListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var persistentRidesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var promoBannersListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var savedAddressesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var appSettingsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var deviceSessionsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val orderRideListeners = java.util.concurrent.ConcurrentHashMap<Long, com.google.firebase.firestore.ListenerRegistration>()

    fun startRealtimeSync(db: AppDatabase, repository: LyoRepository) {
        this.db = db
        this.repositoryRef = repository
        val dbInstance = firestore ?: return
        Log.e(TAG, "Starting REAL-TIME Firestore Database Synchronizer on All Collections...")

        // Auto Store & Item Schedule Sync Loop
        scheduleCheckJob?.cancel()
        scheduleCheckJob = syncScope.launch {
            kotlinx.coroutines.delay(5000) // Give the system 5 seconds to load from network first
            while (isActive) {
                try {
                    val currentDb = LyoFirebaseHelper.db
                    if (currentDb != null) {
                        // 1. Check automatic store scheduling
                        val localVendors = currentDb.vendorDao.getAllVendorsList()
                        for (v in localVendors) {
                            if (v.autoOpenTime.isNotBlank() && v.autoCloseTime.isNotBlank()) {
                                val computedOpen = isTimeWithinInterval(v.autoOpenTime, v.autoCloseTime)
                                val expectedStatus = if (computedOpen) "ACTIVE" else "CLOSED"
                                if (v.status != expectedStatus) {
                                    val updatedVendor = v.copy(status = expectedStatus)
                                    currentDb.vendorDao.updateVendor(updatedVendor)
                                    syncVendorToFirestore(updatedVendor)
                                    Log.d("StoreScheduler", "Automatically updated store ${v.name} status to $expectedStatus based on schedule (${v.autoOpenTime} - ${v.autoCloseTime})")
                                }
                            }
                        }

                        // 2. Check automatic menu item scheduling
                        val localMenuItems = currentDb.menuItemDao.getAllMenuItemsList()
                        for (item in localMenuItems) {
                            if (item.autoOpenTime.isNotBlank() && item.autoCloseTime.isNotBlank()) {
                                val computedAvailable = isTimeWithinInterval(item.autoOpenTime, item.autoCloseTime)
                                if (item.isAvailable != computedAvailable) {
                                    val updatedItem = item.copy(isAvailable = computedAvailable)
                                    currentDb.menuItemDao.updateMenuItem(updatedItem)
                                    syncMenuItemToFirestore(updatedItem)
                                    Log.d("ItemScheduler", "Automatically updated menu item ${item.nameEn} availability to $computedAvailable based on schedule (${item.autoOpenTime} - ${item.autoCloseTime})")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Scheduler", "Error in automatic schedule checker: ${e.message}")
                }
                kotlinx.coroutines.delay(10000) // Check every 10 seconds
            }
        }

        // Trigger safe user profile and catalog fields migration
        syncScope.launch {
            try {
                migrateExistingProfiles()
                migrateCatalogFields()
            } catch (e: Exception) {
                Log.e(TAG, "Failed profiles or catalog migration in background: ${e.message}")
            }
        }

        // 1, 2, 3: Vendors, categories, and menu_items sync is role-aware and handled inside userCollectorJob

        // 4. Live Sync "users"
        usersListener?.remove()
        usersListener = null

        // 5. Live Sync "promo_banners" and "vendors" are role-aware and initialized dynamically inside userCollectorJob below.

        // 7, 8. Live Sync "categories" and "menu_items" are role-aware and initialized dynamically inside userCollectorJob below.

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
                deviceSessionsListener?.remove()
                deviceSessionsListener = null
                vendorsListener?.remove()
                vendorsListener = null
                promoBannersListener?.remove()
                promoBannersListener = null
                savedAddressesListener?.remove()
                savedAddressesListener = null
                categoriesListener?.remove()
                categoriesListener = null
                menuItemsListener?.remove()
                menuItemsListener = null
                orderRideListeners.values.forEach { it.remove() }
                orderRideListeners.clear()

                val role = user?.role ?: "GUEST"
                val currentUserId = auth?.currentUser?.uid ?: user?.uid?.ifBlank { null }

                // Initialize role-aware vendors, promo banners, categories, and menu_items queries:
                val vendorsQuery = if (role == "ADMIN") {
                    dbInstance.collection("vendors")
                } else {
                    dbInstance.collection("vendors").whereEqualTo("status", "ACTIVE")
                }
                
                val bannersQuery = if (role == "ADMIN") {
                    dbInstance.collection("promo_banners")
                } else {
                    dbInstance.collection("promo_banners").whereEqualTo("status", "ACTIVE")
                }

                val categoriesQuery = if (role == "ADMIN") {
                    dbInstance.collection("categories")
                } else {
                    dbInstance.collection("categories").whereEqualTo("isHidden", false)
                }

                val menuItemsQuery = if (role == "ADMIN") {
                    dbInstance.collection("menu_items")
                } else {
                    dbInstance.collection("menu_items").whereEqualTo("isAvailable", true)
                }

                // Start snapshot listener for categories
                categoriesListener = categoriesQuery.addSnapshotListener { snapshot, e ->
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

                // Start snapshot listener for menu items
                menuItemsListener = menuItemsQuery.addSnapshotListener { snapshot, e ->
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

                // Start snapshot listener for vendors
                vendorsListener = vendorsQuery.addSnapshotListener { snapshot, e ->
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
                                            autoCloseTime = doc.getString("autoCloseTime") ?: "",
                                            status = doc.getString("status") ?: "ACTIVE",
                                            isOfferEnabled = doc.getBoolean("isOfferEnabled") ?: false,
                                            offerType = doc.getString("offerType") ?: "Percentage",
                                            offerValue = doc.getDouble("offerValue") ?: 0.0,
                                            offerText = doc.getString("offerText") ?: "",
                                            offerStartDate = doc.getString("offerStartDate") ?: "",
                                            offerEndDate = doc.getString("offerEndDate") ?: "",
                                            offerPriority = doc.getLong("offerPriority")?.toInt() ?: 0
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

                // Start snapshot listener for promo banners
                promoBannersListener = bannersQuery.addSnapshotListener { snapshot, e ->
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
                                val statusVal = doc.getString("status") ?: "ACTIVE"
                                
                                when (change.type) {
                                    com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                        val banner = PromoBanner(
                                            id = bId,
                                            code = code,
                                            description = description,
                                            imageUrl = imageUrl,
                                            status = statusVal
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
                                        } else if (description.isNotBlank()) {
                                            if (existing == null || existing.description != description) {
                                                appContext?.let { ctx ->
                                                    com.example.ui.screens.LyoNotificationHelper.showPushNotification(
                                                        ctx,
                                                        "Lyo New Special Promo! 🎁✨",
                                                        "புதிய ஆஃபர்: $description"
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

                if (currentUserId == null) {
                    Log.d(TAG, "Guest Catalog mode. Scoped authenticated real-time listeners setup bypassed.")
                    return@collect
                }

                // Register session for current device
                registerDeviceSession(currentUserId)

                // Start snapshot listener to monitor multi-device active sessions and remote logouts
                deviceSessionsListener = dbInstance.collection("users")
                    .document(currentUserId)
                    .collection("sessions")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Device sessions listener error: ${e.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(com.example.data.database.DeviceSession::class.java)?.copy(deviceId = doc.id)
                            }
                            repository.activeSessions.value = list

                            // Detect remote session revocation/deletion
                            appContext?.let { ctx ->
                                val myDeviceId = getDeviceId(ctx)
                                val stillExists = list.any { it.deviceId == myDeviceId }
                                if (!stillExists && list.isNotEmpty()) {
                                    Log.w(TAG, "Current session ($myDeviceId) has been revoked remotely. Triggering logout.")
                                    repository.triggerRemoteLogout()
                                }
                            }
                        }
                    }

                Log.d(TAG, "User state updated: $currentUserId ($role). Re-initializing scoped real-time listeners...")

                // Dynamic role-aware users listener
                usersListener?.remove()
                if (role == "ADMIN") {
                    usersListener = dbInstance.collection("users")
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w(TAG, "Admins users listener error: ${e.message}")
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
                                                val u = User(
                                                    phone = phone,
                                                    name = doc.getString("name") ?: "",
                                                    email = doc.getString("email") ?: "",
                                                    address = doc.getString("address") ?: "",
                                                    lat = doc.getDouble("lat") ?: 11.5812,
                                                    lng = doc.getDouble("lng") ?: 77.8465,
                                                    isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                                                    role = doc.getString("role")?.let { if (it == "DELIVERY" || it == "RIDER") "RIDER" else it } ?: "CUSTOMER",
                                                    uid = doc.getString("uid") ?: "",
                                                    vehicleNo = doc.getString("vehicleNo") ?: "",
                                                    isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                                                    salaryType = doc.getString("salaryType") ?: "MONTHLY",
                                                    salaryRate = doc.getDouble("salaryRate") ?: 0.0
                                                )
                                                db.userDao.insertUser(u)
                                            }
                                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                                val localUser = db.userDao.getUserByPhone(phone)
                                                if (localUser?.role != "DELIVERY" && localUser?.role != "ADMIN" && repository.currentUser.value?.phone != phone) {
                                                    db.userDao.deleteUserByPhone(phone)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                } else {
                    savedAddressesListener?.remove()
                    savedAddressesListener = dbInstance.collection("users")
                        .document(currentUserId)
                        .collection("saved_addresses")
                        .addSnapshotListener { snapshot, err ->
                            if (err != null) {
                                Log.w(TAG, "Saved addresses listener error: ${err.message}")
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                syncScope.launch {
                                    for (change in snapshot.documentChanges) {
                                        val addressDoc = change.document
                                        val addrId = addressDoc.getLong("id") ?: addressDoc.id.toLongOrNull() ?: continue
                                        val userId = addressDoc.getString("userId") ?: ""
                                        val name = addressDoc.getString("name") ?: ""
                                        val addressLine = addressDoc.getString("addressLine") ?: ""
                                        val isDefault = addressDoc.getBoolean("isDefault") ?: false
                                        val latitude = addressDoc.getDouble("latitude") ?: 0.0
                                        val longitude = addressDoc.getDouble("longitude") ?: 0.0

                                        val savedAddr = SavedAddress(
                                            id = addrId,
                                            userId = userId,
                                            name = name,
                                            addressLine = addressLine,
                                            isDefault = isDefault,
                                            latitude = latitude,
                                            longitude = longitude
                                        )

                                        when (change.type) {
                                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                                db.savedAddressDao.insertAddress(savedAddr)
                                            }
                                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                                db.savedAddressDao.deleteAddress(savedAddr)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    usersListener = dbInstance.collection("users")
                        .document(currentUserId)
                        .addSnapshotListener { doc, e ->
                            if (e != null) {
                                Log.w(TAG, "Customer/Rider own user listener error: ${e.message}")
                                return@addSnapshotListener
                            }
                            if (doc != null && doc.exists()) {
                                syncScope.launch {
                                    val phone = doc.getString("phone") ?: return@launch
                                    val u = User(
                                        phone = phone,
                                        name = doc.getString("name") ?: "",
                                        email = doc.getString("email") ?: "",
                                        address = doc.getString("address") ?: "",
                                        lat = doc.getDouble("lat") ?: 11.5812,
                                        lng = doc.getDouble("lng") ?: 77.8465,
                                        isWhatsAppOptIn = doc.getBoolean("isWhatsAppOptIn") ?: true,
                                        role = doc.getString("role")?.let { if (it == "DELIVERY" || it == "RIDER") "RIDER" else it } ?: "CUSTOMER",
                                        uid = doc.getString("uid") ?: "",
                                        vehicleNo = doc.getString("vehicleNo") ?: "",
                                        isActiveRider = doc.getBoolean("isActiveRider") ?: true,
                                        salaryType = doc.getString("salaryType") ?: "MONTHLY",
                                        salaryRate = doc.getDouble("salaryRate") ?: 0.0
                                    )
                                    db.userDao.insertUser(u)
                                    if (phone == repository.currentUser.value?.phone) {
                                        repository.currentUser.value = u
                                    }
                                }
                            }
                        }
                }

                if (role == "ADMIN") {
                    // Admins listen to all orders
                    persistentOrdersListener = dbInstance.collection("ek_orders")
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
                                            val localDb = db
                                            if (localDb != null) {
                                                val existingLocal = localDb.orderDao.getOrderById(oId)
                                                if (existingLocal == null || existingLocal.status != "CANCELLED" || order.status == "CANCELLED") {
                                                    localDb.orderDao.insertOrder(order)
                                                    saveOrderItemsFromDoc(oId, doc.get("items"))
                                                }
                                            }
                                        } else if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                            db?.orderDao?.deleteOrderById(oId)
                                            db?.orderItemDao?.deleteItemsForOrder(oId)
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
                                                riderUid = doc.getString("riderUid") ?: "",
                                                status = doc.getString("status") ?: "ACCEPTED",
                                                currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                                currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                                totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                                earnings = doc.getDouble("earnings") ?: 0.0,
                                                otpVerified = doc.getBoolean("otpVerified") ?: false,
                                                locationTimestamp = doc.getLong("locationTimestamp") ?: 0L
                                            )
                                            db.deliveryRideDao.insertDeliveryRide(ride)
                                        }
                                    }
                                }
                            }
                        }
                } else if (role == "RIDER" || role == "DELIVERY") {
                    // Riders only listen to delivery_rides belonging to them
                    val myUid = auth?.currentUser?.uid ?: ""
                    persistentRidesListener = dbInstance.collection("delivery_rides")
                        .whereEqualTo("riderUid", myUid)
                        .addSnapshotListener { snapshot, e -> // Rider rides listener
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
                                                riderUid = doc.getString("riderUid") ?: "",
                                                status = doc.getString("status") ?: "ACCEPTED",
                                                currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                                currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                                totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                                earnings = doc.getDouble("earnings") ?: 0.0,
                                                otpVerified = doc.getBoolean("otpVerified") ?: false,
                                                locationTimestamp = doc.getLong("locationTimestamp") ?: 0L
                                            )
                                            db.deliveryRideDao.insertDeliveryRide(ride)
                                        }
                                    }
                                }
                            }
                        }

                    // Riders map active/preparing/out-for-delivery orders
                    persistentOrdersListener = dbInstance.collection("ek_orders")
                        .whereEqualTo("riderUid", myUid)
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
                                            val localDb = db
                                            if (localDb != null) {
                                                val existingLocal = localDb.orderDao.getOrderById(oId)
                                                if (existingLocal == null || existingLocal.status != "CANCELLED" || order.status == "CANCELLED") {
                                                    localDb.orderDao.insertOrder(order)
                                                    saveOrderItemsFromDoc(oId, doc.get("items"))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                } else {
                    // Customers view ONLY their own orders
                    persistentOrdersListener = dbInstance.collection("ek_orders")
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
                                            // Securely register dynamic snapshot listener for this order's delivery ride
                                            setupOrderRideListener(dbInstance, db, oId)

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
                                            val localDb = db
                                            if (localDb != null) {
                                                val existingLocal = localDb.orderDao.getOrderById(oId)
                                                if (existingLocal == null || existingLocal.status != "CANCELLED" || order.status == "CANCELLED") {
                                                    localDb.orderDao.insertOrder(order)
                                                    saveOrderItemsFromDoc(oId, doc.get("items"))
                                                }
                                            }
                                        } else if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                            db?.orderDao?.deleteOrderById(oId)
                                            db?.orderItemDao?.deleteItemsForOrder(oId)
                                        }
                                    }
                                }
                            }
                        }

                    // Scoped rides listener has been replaced with the secure dynamic order ride listener above.
                }
            }
        }

        // 8. Live Sync "app_settings"
        appSettingsListener?.remove()
        appSettingsListener = dbInstance.collection("app_settings").document("global")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "App settings listener error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val isPaused = snapshot.getBoolean("isAppPaused") ?: false
                    val msgEn = snapshot.getString("appPauseMessageEn") ?: "We are currently closed for a short break. Please check back soon!"
                    val msgTa = snapshot.getString("appPauseMessageTa") ?: "நாங்கள் தற்காலிக விடுப்பில் உள்ளோம். விரைவில் மீண்டும் வருகிறோம்!"
                    
                    val gstEnabled = snapshot.getBoolean("gstEnabled") ?: false
                    val gstRate = snapshot.getDouble("gstRate") ?: 5.0

                    val upiId = snapshot.getString("upiId") ?: "8778148899@ptyes"
                    val upiName = snapshot.getString("upiName") ?: "Anantharaj R"

                    val repo = repositoryRef
                    if (repo != null) {
                        repo.setGstSettingsLocally(appContext, gstEnabled, gstRate)
                        repo.setAppPauseSettingsLocally(appContext, isPaused, msgEn, msgTa)
                        repo.setUpiSettingsLocally(appContext, upiId, upiName)
                    }
                    Log.d(TAG, "Live synced app settings from Firestore: isPaused=$isPaused, gstEnabled=$gstEnabled, gstRate=$gstRate, upiId=$upiId")
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
        savedAddressesListener?.remove()
        savedAddressesListener = null
        appSettingsListener?.remove()
        appSettingsListener = null
        allOrdersListenerRegistration?.remove()
        allOrdersListenerRegistration = null
    }

    private var orderListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var rideListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var allOrdersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private fun setupOrderRideListener(
        dbInstance: com.google.firebase.firestore.FirebaseFirestore,
        db: AppDatabase,
        orderId: Long
    ) {
        if (orderRideListeners.containsKey(orderId)) return
        val listener = dbInstance.collection("delivery_rides")
            .whereEqualTo("orderId", orderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Dynamic ride listener error for order $orderId: ${e.message}")
                    return@addSnapshotListener
                }
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
                                    orderId = orderId,
                                    riderName = doc.getString("riderName") ?: "",
                                    riderPhone = doc.getString("riderPhone") ?: "",
                                    riderUid = doc.getString("riderUid") ?: "",
                                    status = doc.getString("status") ?: "ACCEPTED",
                                    currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                    currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                    totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                    earnings = doc.getDouble("earnings") ?: 0.0,
                                    otpVerified = doc.getBoolean("otpVerified") ?: false,
                                    locationTimestamp = doc.getLong("locationTimestamp") ?: 0L
                                )
                                db.deliveryRideDao.insertDeliveryRide(ride)
                            }
                        }
                    }
                }
            }
        orderRideListeners[orderId] = listener
    }

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
        orderListenerRegistration = dbInstance.collection("ek_orders")
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
                                orderId = doc.getLong("orderId") ?: orderId, riderUid = doc.getString("riderUid") ?: "",
                                riderName = doc.getString("riderName") ?: "",
                                riderPhone = doc.getString("riderPhone") ?: "",
                                status = doc.getString("status") ?: "ACCEPTED",
                                currentLat = doc.getDouble("currentLat") ?: 11.5850,
                                currentLng = doc.getDouble("currentLng") ?: 77.8420,
                                totalDistance = doc.getDouble("totalDistance") ?: 0.0,
                                earnings = doc.getDouble("earnings") ?: 0.0,
                                otpVerified = doc.getBoolean("otpVerified") ?: false,
                                locationTimestamp = doc.getLong("locationTimestamp") ?: 0L
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
        allOrdersListenerRegistration = dbInstance.collection("ek_orders")
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

    fun getDeviceId(context: android.content.Context): String {
        val prefs = context.getSharedPreferences("lyo_session_prefs", android.content.Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }

    fun getDeviceName(): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }

    fun registerDeviceSession(userUid: String) {
        if (!isInitialized) return
        val context = appContext ?: return
        val db = firestore ?: return
        val deviceId = getDeviceId(context)
        val deviceName = getDeviceName()
        val osVersion = "Android ${android.os.Build.VERSION.RELEASE}"

        val sessionMap = mapOf(
            "deviceId" to deviceId,
            "deviceName" to deviceName,
            "osVersion" to osVersion,
            "loginTime" to System.currentTimeMillis(),
            "lastActive" to System.currentTimeMillis()
        )

        syncScope.launch {
            try {
                db.collection("users")
                    .document(userUid)
                    .collection("sessions")
                    .document(deviceId)
                    .set(sessionMap)
                    .await()
                Log.d(TAG, "Registered session for device: $deviceName ($deviceId)")
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Failed registering device session: ${e.message}")
            }

            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val token = task.result
                        updateFcmToken(token)
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Failed retrieving and registering FCM token on session login: ${e.message}")
            }
        }
    }

    fun removeDeviceSession(userUid: String) {
        if (!isInitialized) return
        val context = appContext ?: return
        val db = firestore ?: return
        val deviceId = getDeviceId(context)

        syncScope.launch {
            try {
                // Delete device token first before deleting the session
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            val token = task.result
                            deleteFcmToken(token)
                        }
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "Failed retrieving and deleting FCM token on logout: ${e.message}")
                }

                db.collection("users")
                    .document(userUid)
                    .collection("sessions")
                    .document(deviceId)
                    .delete()
                    .await()
                Log.d(TAG, "Removed device session: $deviceId")
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Failed removing device session: ${e.message}")
            }
        }
    }

    fun updateFcmToken(token: String) {
        if (!isInitialized) return
        val authInstance = auth ?: return
        val dbInstance = firestore ?: return
        val uid = authInstance.currentUser?.uid ?: return

        val tokenMap = mapOf(
            "token" to token,
            "platform" to "android",
            "updatedAt" to System.currentTimeMillis()
        )

        syncScope.launch {
            try {
                dbInstance.collection("users")
                    .document(uid)
                    .collection("deviceTokens")
                    .document(token)
                    .set(tokenMap)
                    .await()
                Log.d(TAG, "Successfully saved FCM token to Firestore for UID: $uid")
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Failed to save FCM token to Firestore: ${e.message}")
            }
        }
    }

    fun deleteFcmToken(token: String) {
        if (!isInitialized) return
        val authInstance = auth ?: return
        val dbInstance = firestore ?: return
        val uid = authInstance.currentUser?.uid ?: return

        syncScope.launch {
            try {
                dbInstance.collection("users")
                    .document(uid)
                    .collection("deviceTokens")
                    .document(token)
                    .delete()
                    .await()
                Log.d(TAG, "Successfully deleted FCM token from Firestore for UID: $uid")
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Failed to delete FCM token from Firestore: ${e.message}")
            }
        }
    }

    fun removeAllOtherDeviceSessions(userUid: String) {
        if (!isInitialized) return
        val context = appContext ?: return
        val db = firestore ?: return
        val deviceId = getDeviceId(context)

        syncScope.launch {
            try {
                val sessionsRef = db.collection("users").document(userUid).collection("sessions")
                val snapshot = sessionsRef.get().await()
                for (doc in snapshot.documents) {
                    val docId = doc.id
                    if (docId != deviceId) {
                        sessionsRef.document(docId).delete().await()
                    }
                }
                Log.d(TAG, "Removed all other device sessions for UID: $userUid")
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Failed removing other device sessions: ${e.message}")
            }
        }
    }

    suspend fun deleteOrderFromFirestore(orderId: Long) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            dbInstance.collection("ek_orders").document(orderId.toString()).delete().await()
            Log.d(TAG, "Deleted order $orderId from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete order $orderId: ${e.message}")
        }
    }

    suspend fun addRiderPointsAndRating(riderPhone: String, rating: Int, points: Int) = withContext(Dispatchers.IO) {
        val dbInstance = firestore ?: return@withContext
        try {
            var userRef = dbInstance.collection("users").document(riderPhone)
            val directSnap = userRef.get().await()
            if (!directSnap.exists()) {
                val querySnapshot = dbInstance.collection("users")
                    .whereEqualTo("phone", riderPhone.trim())
                    .limit(1)
                    .get()
                    .await()
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null && doc.exists()) {
                    userRef = doc.reference
                } else {
                    val querySnapshotUid = dbInstance.collection("users")
                        .whereEqualTo("uid", riderPhone.trim())
                        .limit(1)
                        .get()
                        .await()
                    val docUid = querySnapshotUid.documents.firstOrNull()
                    if (docUid != null && docUid.exists()) {
                        userRef = docUid.reference
                    } else {
                        Log.w(TAG, "No rider found for ID/Phone: $riderPhone")
                        return@withContext
                    }
                }
            }

            dbInstance.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                if (snapshot.exists()) {
                    val currentPoints = snapshot.getLong("riderPoints") ?: 0L
                    val currentRatingSum = snapshot.getDouble("riderRatingSum") ?: 0.0
                    val currentRatingCount = snapshot.getLong("riderRatingCount") ?: 0L

                    val newPoints = currentPoints + points
                    val newRatingCount = currentRatingCount + 1
                    val newRatingSum = currentRatingSum + rating
                    val newRating = if (newRatingCount > 0) newRatingSum / newRatingCount else 0.0

                    val updates = mapOf(
                        "riderPoints" to newPoints,
                        "riderRating" to newRating,
                        "riderRatingSum" to newRatingSum,
                        "riderRatingCount" to newRatingCount
                    )
                    transaction.update(userRef, updates)
                }
            }.await()
            Log.d(TAG, "Successfully updated points and rating for rider $riderPhone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed updating rider points/rating: ${e.message}")
        }
    }

    fun getFriendlyPermissionErrorMessage(e: Throwable): String {
        val message = e.message ?: ""
        val lower = message.lowercase()
        
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (auth.currentUser == null || lower.contains("unauthenticated") || lower.contains("not authenticated") || lower.contains("authentication required") || lower.contains("auth required")) {
            return "You are not authenticated. (நீங்கள் இன்னும் லாகின் செய்யவில்லை!)"
        }
        
        if (lower.contains("session") && (lower.contains("expired") || lower.contains("invalid") || lower.contains("token"))) {
            return "Your Admin session has expired. (உங்களது அட்மின் செஷன் காலாவதியாகிவிட்டது! தயவுசெய்து மீண்டும் லாகின் செய்யவும்.)"
        }
        
        if (lower.contains("approve") || lower.contains("approved") || lower.contains("pending approval") || lower.contains("not approved")) {
            return "Vendor account not approved. (உணவக கணக்கு இன்னும் அங்கீகரிக்கப்படவில்லை!)"
        }
        
        if (lower.contains("permission_denied") || lower.contains("permission denied") || lower.contains("insufficient permissions") || lower.contains("forbidden") || lower.contains("access denied")) {
            if (lower.contains("rules") || lower.contains("security rules")) {
                return "Permission denied by Firestore Rules. (Firestore பாதுகாப்பு விதிகளின்படி அனுமதி மறுக்கப்பட்டுள்ளது.)"
            }
            if (lower.contains("role") || lower.contains("invalid role") || lower.contains("missing role") || lower.contains("unauthorized")) {
                return "Missing Firestore role. (வழங்கப்பட்ட கணக்கில் உரிய அனுமதி அல்லது ரோல் இல்லை!)"
            }
            return "Permission denied by Firestore Rules. (வழங்கப்பட்ட கணக்கில் உரிய அனுமதி இல்லை!)"
        }
        
        if (lower.contains("not found") || lower.contains("does not exist") || lower.contains("document not found")) {
            return "Document does not exist. (தேடப்படும் ஆவணம் இல்லை!)"
        }
        
        return "Permission denied: $message"
    }

    private suspend fun runSafeFirestoreWrite(block: suspend () -> Unit) {
        val startTime = System.currentTimeMillis()
        try {
            withTimeout(5000L) {
                block()
            }
            Log.i("SmartMenuPublishTrace", "[Stage 8: Firestore write] Executed: Yes. Output: Successfully committed transaction/write to Firestore. Execution time: ${System.currentTimeMillis() - startTime}ms. File: LyoFirebaseHelper.kt, Function: runSafeFirestoreWrite")
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Firestore write timed out (will be synchronized offline automatically)")
            Log.e("SmartMenuPublishTrace", "[Stage 8: Firestore write] TIMEOUT: timed out after 5000ms. Will automatically synchronize offline via Firestore persistent cache.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore write failed: ${e.message}", e)
            Log.e("SmartMenuPublishTrace", "[Stage 8: Firestore write] FAILED: Exception: ${e.message}. File: LyoFirebaseHelper.kt, Function: runSafeFirestoreWrite", e)
            throw e
        }
    }

    suspend fun ensureFirebaseAdminAuth(): Boolean = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext false
        val dbInstance = firestore ?: return@withContext false
        val startTime = System.currentTimeMillis()
        try {
            withTimeout(5000L) {
                var currentUser = authInstance.currentUser
                if (currentUser == null) {
                    Log.d(TAG, "No Firebase user found. Attempting anonymous sign-in...")
                    val result = authInstance.signInAnonymously().await()
                    currentUser = result.user
                }
                val uid = currentUser?.uid ?: return@withTimeout
                
                val appUser = repositoryRef?.currentUser?.value
                val phone = appUser?.phone ?: "8778148899"
                val name = appUser?.name ?: "Anantharaj R (CEO)"
                val email = appUser?.email ?: "AnantharajEinstein@gmail.com"
                
                val adminProfile = mapOf(
                    "uid" to uid,
                    "phone" to phone,
                    "name" to name,
                    "email" to email,
                    "role" to "ADMIN",
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                
                Log.d(TAG, "Syncing admin profile under /users/$uid to Firestore...")
                dbInstance.collection("users").document(uid).set(adminProfile, SetOptions.merge()).await()
                Log.d(TAG, "Admin profile successfully synced for UID: $uid")
                Log.i("SmartMenuPublishTrace", "[Stage 7: Firestore authentication] Executed: Yes. User UID: $uid. Admin role successfully established and verified. Execution time: ${System.currentTimeMillis() - startTime}ms. File: LyoFirebaseHelper.kt, Function: ensureFirebaseAdminAuth")
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "ensureFirebaseAdminAuth failed or timed out: ${e.message}", e)
            Log.e("SmartMenuPublishTrace", "[Stage 7: Firestore authentication] FAILED/TIMED OUT: ${e.message}. File: LyoFirebaseHelper.kt, Function: ensureFirebaseAdminAuth", e)
            return@withContext false
        }
    }
}

