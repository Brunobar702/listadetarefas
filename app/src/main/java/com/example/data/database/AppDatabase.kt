package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AdvertiserDao
import com.example.data.dao.TaskDao
import com.example.data.model.Advertiser
import com.example.data.model.TaskItem
import com.example.data.model.TaskList

@Database(
    entities = [TaskList::class, TaskItem::class, Advertiser::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun advertiserDao(): AdvertiserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_manager_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
