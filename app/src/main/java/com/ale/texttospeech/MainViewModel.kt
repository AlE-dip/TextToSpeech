package com.ale.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ale.texttospeech.core.database.Setting
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel : ViewModel() {
    var onExportMp3Listener: OnExportMp3Listener? = null
    var onUpdateTextListener: OnUpdateTextListener? = null

    var setting = MutableLiveData<Setting>().apply {
        value = Setting(
            1,
            null,
            null,
            1f,
            1f,
            null
        )
    }

    var textToSpeech = MutableLiveData<TextToSpeech>()
    var locales = MutableLiveData<Set<Locale>>()
    var choseLocale = MutableLiveData<Locale>()
    var languages = MutableLiveData<ArrayList<String>>()
    var voices = MutableLiveData<ArrayList<Voice>>()
    var choseVoice = MutableLiveData<Voice>()
    var voiceNames = MutableLiveData<ArrayList<String>>()
    var speechRate = MutableLiveData<Float>().apply { value = 1.0f }
    var pitch = MutableLiveData<Float>().apply { value = 1.0f }

    fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        observeForever(object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    fun createSpeech(context: Context, settingViewModel: SettingViewModel) {
        settingViewModel.getAllSetting().observeOnce { settings ->
            textToSpeech.value = TextToSpeech(context, TextToSpeech.OnInitListener {
                if (it == TextToSpeech.SUCCESS) {
                    locales.value = textToSpeech.value?.availableLanguages
                    if (settings == null || settings.size == 0) {
                        setDefaultSpeech(
                            setting.value!!
                        )
                        settingViewModel.insertSetting(setting.value!!)
                    } else if (settings.size == 1) {
                        setting.value = settings.get(0)
                        setDefaultSpeech(
                            setting.value!!
                        )
                    }
                }
            })
        }
    }

    fun setDefaultSpeech(setting: Setting): Setting {
        if (setting.language == null) {
            setting.language = localToString(textToSpeech.value?.language)
        }
        choseDefaultLocale(setting.language)
        setSpeechRate(setting.speechRate)
        setPitch(setting.pitch)
        if (setting.voice == null) {
            setting.voice = choseVoice.value?.name
        }
        choseVoice(setting.voice!!)
        return setting
    }

    fun setSpeechRate(it: Float) {
        speechRate.value = roundDecimal(it)
    }

    fun setPitch(it: Float) {
        pitch.value = roundDecimal(it)
    }

    fun roundDecimal(number: Float): Float {
        val factor = Math.pow(10.0, 1.0).toFloat()
        return (Math.round(number * factor) / factor)
    }

    fun choseLocale(displayName: String) {
        locales.value!!.forEach {
            if (it.displayName.equals(displayName)) {
                choseLocale.value = it
                return
            }
        }
    }

    fun choseDefaultLocale(language: String?) {
        if (choseLocale.value != null || language == null) return
        locales.value!!.forEach {
            if (localToString(it).equals(language)) {
                choseLocale.value = it
                return
            }
        }
    }

    fun localToString(locale: Locale?): String {
        return locale?.isO3Country + locale?.isO3Language
    }

    fun setVoices(setVoices: Set<Voice>?) {
        if (setVoices == null || setVoices.size == 0) return
        if(choseVoice.value?.locale?.toLanguageTag() == choseLocale.value?.toLanguageTag()) return
        var temp: ArrayList<Voice> = ArrayList()
        setVoices?.forEach {
            if (choseLocale.value?.toLanguageTag().equals(it.locale.toLanguageTag())) {
                temp.add(it)
            }
        }
        voices.value = temp
        choseDefaultVoice(temp.get(0))
    }

    fun choseDefaultVoice(voice: Voice) {
        choseVoice.value = voice
    }

    fun choseVoice(voiceName: String) {
        voices.value?.forEach {
            if (voiceName.equals(it.name)) {
                choseVoice.value = it
                return
            }
        }
    }

    interface OnExportMp3Listener {
        fun export()
    }

    interface OnUpdateTextListener {
        fun update(text: String)
    }
}