package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone OR email = :phone OR uid = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone OR uid = :phone LIMIT 1")
    fun getUserFlow(phone: String): Flow<User?>

    @Query("SELECT * FROM users WHERE role = 'DELIVERY' OR role = 'RIDER'")
    fun getAllRidersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'DELIVERY' OR role = 'RIDER' LIMIT 1")
    suspend fun getRiderForAssignment(): User?

    @Query("SELECT * FROM users WHERE role = 'CUSTOMER'")
    fun getAllCustomersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE role = 'ADMIN'")
    fun getAllAdminsFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE phone = :phone")
    suspend fun deleteUserByPhone(phone: String)

    @Query("DELETE FROM users WHERE role = 'DELIVERY' OR role = 'RIDER'")
    suspend fun clearAllRiders()

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors ORDER BY sortOrder ASC, rating DESC, id DESC")
    fun getAllVendors(): Flow<List<Vendor>>

    @Query("SELECT * FROM vendors ORDER BY sortOrder ASC, rating DESC, id DESC")
    suspend fun getAllVendorsList(): List<Vendor>

    @Query("SELECT * FROM vendors WHERE id = :id LIMIT 1")
    suspend fun getVendorById(id: Long): Vendor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: Vendor): Long

    @Update
    suspend fun updateVendor(vendor: Vendor)

    @Delete
    suspend fun deleteVendor(vendor: Vendor)

    @Query("DELETE FROM vendors WHERE id = :id")
    suspend fun deleteVendorById(id: Long)

    @Query("DELETE FROM vendors")
    suspend fun clearAllVendors()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE vendorId = :vendorId ORDER BY sortOrder ASC, id ASC")
    fun getCategoriesForVendor(vendorId: Long): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<Category>

    @Query("SELECT * FROM categories WHERE vendorId = :vendorId")
    suspend fun getCategoriesForVendorList(vendorId: Long): List<Category>

    @Query("DELETE FROM categories WHERE vendorId = :vendorId")
    suspend fun deleteCategoriesByVendor(vendorId: Long)

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()
}

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE vendorId = :vendorId")
    fun getMenuItemsForVendor(vendorId: Long): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemById(id: Long): MenuItem?

    @Query("SELECT * FROM menu_items")
    suspend fun getAllMenuItemsList(): List<MenuItem>

    @Query("SELECT * FROM menu_items")
    fun getAllMenuItemsFlow(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId")
    fun getMenuItemsForCategory(categoryId: Long): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE nameEn LIKE :query OR nameTa LIKE :query LIMIT :limit")
    suspend fun searchMenuItems(query: String, limit: Int = 15): List<MenuItem>

    @Query("SELECT * FROM menu_items LIMIT :limit")
    suspend fun getTopMenuItems(limit: Int = 15): List<MenuItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: MenuItem): Long

    @Update
    suspend fun updateMenuItem(menuItem: MenuItem)

    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItem)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Long)

    @Query("SELECT * FROM menu_items WHERE vendorId = :vendorId")
    suspend fun getMenuItemsForVendorList(vendorId: Long): List<MenuItem>

    @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId")
    suspend fun getMenuItemsForCategoryList(categoryId: Long): List<MenuItem>

    @Query("DELETE FROM menu_items WHERE categoryId = :categoryId")
    suspend fun deleteMenuItemsByCategory(categoryId: Long)

    @Query("DELETE FROM menu_items WHERE vendorId = :vendorId")
    suspend fun deleteMenuItemsByVendor(vendorId: Long)

    @Query("DELETE FROM menu_items")
    suspend fun clearAllMenuItems()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId OR userId IN (SELECT uid FROM users WHERE phone = :userId) OR userId IN (SELECT phone FROM users WHERE uid = :userId) ORDER BY timestamp DESC")
    fun getOrdersForUser(userId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE userId = :userId OR userId IN (SELECT uid FROM users WHERE phone = :userId) OR userId IN (SELECT phone FROM users WHERE uid = :userId) ORDER BY timestamp DESC")
    suspend fun getOrdersForUserList(userId: String): List<Order>

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE isPendingSync = 1")
    suspend fun getPendingSyncOrders(): List<Order>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET status = :status, paymentStatus = :paymentStatus, rejectionReason = :rejectionReason WHERE id = :orderId")
    suspend fun verifyPayment(orderId: Long, status: String, paymentStatus: String, rejectionReason: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)
}

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: Long): List<OrderItem>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getItemsForOrderFlow(orderId: Long): Flow<List<OrderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: Long)
}

@Dao
interface DeliveryRideDao {
    @Query("SELECT * FROM delivery_rides WHERE status != 'COMPLETED'")
    fun getActiveRides(): Flow<List<DeliveryRide>>

    @Query("SELECT * FROM delivery_rides")
    fun getAllRidesFlow(): Flow<List<DeliveryRide>>

    @Query("SELECT * FROM delivery_rides WHERE id = :id LIMIT 1")
    suspend fun getRideById(id: Long): DeliveryRide?

    @Query("SELECT * FROM delivery_rides WHERE orderId = :orderId LIMIT 1")
    suspend fun getRideForOrder(orderId: Long): DeliveryRide?

    @Query("SELECT * FROM delivery_rides WHERE orderId = :orderId LIMIT 1")
    fun getRideForOrderFlow(orderId: Long): Flow<DeliveryRide?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryRide(ride: DeliveryRide): Long

    @Update
    suspend fun updateDeliveryRide(ride: DeliveryRide)
}

@Dao
interface PromoBannerDao {
    @Query("SELECT * FROM promo_banners ORDER BY id ASC")
    fun getAllPromoBanners(): Flow<List<PromoBanner>>

    @Query("SELECT * FROM promo_banners ORDER BY id ASC")
    suspend fun getAllPromoBannersList(): List<PromoBanner>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoBanner(banner: PromoBanner): Long

    @Update
    suspend fun updatePromoBanner(banner: PromoBanner)

    @Delete
    suspend fun deletePromoBanner(banner: PromoBanner)

    // Lyo Notifications Queries
    @Query("SELECT * FROM lyo_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<LyoNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: LyoNotification)

    @Query("UPDATE lyo_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE lyo_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM lyo_notifications")
    suspend fun clearAllNotifications()
}

@Dao
interface SavedAddressDao {
    @Query("SELECT * FROM saved_addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    fun getAddressesForUserFlow(userId: String): Flow<List<SavedAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: SavedAddress): Long

    @Delete
    suspend fun deleteAddress(address: SavedAddress)

    @Query("UPDATE saved_addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultsForUser(userId: String)
}

@Dao
interface SavedPaymentMethodDao {
    @Query("SELECT * FROM saved_payment_methods WHERE userId = :userId ORDER BY id DESC")
    fun getPaymentMethodsForUserFlow(userId: String): Flow<List<SavedPaymentMethod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: SavedPaymentMethod): Long

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: SavedPaymentMethod)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId ORDER BY timestamp DESC")
    fun getReviewsForVendor(vendorId: Long): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId")
    suspend fun getReviewsForVendorList(vendorId: Long): List<Review>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long
}

@Dao
interface MissingDictionaryWordDao {
    @Query("SELECT * FROM missing_dictionary_words ORDER BY firstSeenAt DESC")
    fun getAllMissingWordsFlow(): Flow<List<MissingDictionaryWord>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissingWord(word: MissingDictionaryWord)

    @Query("DELETE FROM missing_dictionary_words")
    suspend fun clearAllMissingWords()
}

@Dao
interface SmartMenuCorrectionDao {
    @Query("SELECT * FROM smart_menu_corrections")
    suspend fun getAllCorrections(): List<SmartMenuCorrection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrection(correction: SmartMenuCorrection)

    @Query("DELETE FROM smart_menu_corrections WHERE originalName = :originalName")
    suspend fun deleteCorrection(originalName: String)

    @Query("DELETE FROM smart_menu_corrections")
    suspend fun clearAllCorrections()
}



