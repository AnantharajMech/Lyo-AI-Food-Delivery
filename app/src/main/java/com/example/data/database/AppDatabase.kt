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
        Review::class
    ],
    version = 20,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lyo_food_delivery_db"
                ).fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
