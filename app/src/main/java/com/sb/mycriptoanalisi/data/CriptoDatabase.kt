package com.sb.mycriptoanalisi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CriptoPosseduta::class], version = 1)
abstract class CriptoDatabase : RoomDatabase() {
    abstract fun criptoDao(): CriptoDao

    companion object {
        @Volatile
        private var INSTANCE: CriptoDatabase? = null

        fun getDatabase(context: Context): CriptoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CriptoDatabase::class.java,
                    "crypto_portfolio_db"
                ).fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
