package com.ale.texttospeech.core.database

import android.app.Application
import androidx.lifecycle.LiveData

class SettingRepository(application: Application) {
    private val settingDao: SettingDao

    init {
        val appDatabase: AppDatabase = AppDatabase.getInstance(application)
        settingDao = appDatabase.settingDao()
    }

    fun getAll(): LiveData<List<Setting>> = settingDao.getAll()
    suspend fun insertSetting(setting: Setting) = settingDao.insert(setting)
    suspend fun updateSetting(setting: Setting) = settingDao.update(setting)
}