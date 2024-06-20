package com.example.ichiba.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AuthToken::class], version = 1)
@TypeConverters(Converters::class)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun authTokenDao(): AuthTokenDao

    companion object {
        @Volatile
        private var INSTANCE: AuthDatabase? = null

        fun getDatabase(context: Context): AuthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuthDatabase::class.java,
                    "auth_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}