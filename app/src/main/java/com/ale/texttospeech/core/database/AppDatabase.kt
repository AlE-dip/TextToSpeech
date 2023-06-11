package com.ale.texttospeech.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Setting::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingDao

    companion object {
        private val DATABASE_NAME: String = "text_to_speech_lite"

        @Volatile
        var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            if(instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
            }
            return instance!!
        }
    }
}