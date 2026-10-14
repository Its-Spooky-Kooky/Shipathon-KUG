package com.eta.assistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eta.assistant.data.local.dao.CustomerDao
import com.eta.assistant.data.local.dao.ExpenseDao
import com.eta.assistant.data.local.dao.PaymentDao
import com.eta.assistant.data.local.dao.ProductDao
import com.eta.assistant.data.local.dao.SaleDao
import com.eta.assistant.data.local.dao.StockMovementDao
import com.eta.assistant.data.local.entity.CustomerEntity
import com.eta.assistant.data.local.entity.ExpenseEntity
import com.eta.assistant.data.local.entity.PaymentEntity
import com.eta.assistant.data.local.entity.ProductEntity
import com.eta.assistant.data.local.entity.SaleEntity
import com.eta.assistant.data.local.entity.SaleItemEntity
import com.eta.assistant.data.local.entity.StockMovementEntity

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EtaDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: EtaDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Future schema changes will be safely applied here without data loss
            }
        }

        fun getDatabase(context: Context): EtaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EtaDatabase::class.java,
                    "eta_business.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

