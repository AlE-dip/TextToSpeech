package com.ale.texttospeech

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ale.texttospeech.core.database.Setting
import com.ale.texttospeech.core.database.SettingRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class SettingViewModel(application: Application): ViewModel() {

    private val settingRepository: SettingRepository = SettingRepository(application)

    fun getAllSetting(): LiveData<List<Setting>> = settingRepository.getAll()

    fun insertSetting(setting: Setting) = viewModelScope.launch {
        settingRepository.insertSetting(setting)
    }

    fun updateSetting(setting: Setting) = viewModelScope.launch {
        settingRepository.updateSetting(setting)
    }

    class SettingViewModelFactory(private val application: Application): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(SettingViewModel::class.java)) {
                return SettingViewModel(application) as T
            }

            throw IllegalArgumentException("Unable construct view model")
        }
    }
}