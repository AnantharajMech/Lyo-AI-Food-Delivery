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
    version = 32,
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

        val MIGRATION_30_31 = object : androidx.room.migration.Migration(30, 31) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN iconImageUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_31_32 = object : androidx.room.migration.Migration(31, 32) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN gstAmount REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lyo_food_delivery_db"
                ).addMigrations(MIGRATION_30_31, MIGRATION_31_32)
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
