package com.hritikbhat.bellweather.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hritikbhat.bellweather.data.db.tables.AppSettingTable
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.data.db.tables.KeywordTable

@Database(entities = [AppTable::class, AppSettingTable::class, KeywordTable::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){

    abstract fun getAppDao() : AppDao

    companion object {

        @Volatile private var instance : AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }
        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, AppDatabase :: class.java,
            "appDB") .build()
    }}
