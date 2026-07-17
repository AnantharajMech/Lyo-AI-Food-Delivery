package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Vendor::class,
        Category::class,
        MenuItem::class,
        Order::class,
        OrderItem::class,
        DeliveryRide::class,
        PromoBanner::class,
        SavedAddress::class,
        SavedPaymentMethod::class,
        Review::class,
        MissingDictionaryWord::class,
        LyoNotification::class,
        SmartMenuCorrection::class
    ],
    version = 29,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val vendorDao: VendorDao
    abstract val categoryDao: CategoryDao
    abstract val menuItemDao: MenuItemDao
    abstract val orderDao: OrderDao
    abstract val orderItemDao: OrderItemDao
    abstract val deliveryRideDao: DeliveryRideDao
    abstract val promoBannerDao: PromoBannerDao
    abstract val savedAddressDao: SavedAddressDao
    abstract val savedPaymentMethodDao: SavedPaymentMethodDao
    abstract val reviewDao: ReviewDao
    abstract val missingDictionaryWordDao: MissingDictionaryWordDao
    abstract val smartMenuCorrectionDao: SmartMenuCorrectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lyo_food_delivery_db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
