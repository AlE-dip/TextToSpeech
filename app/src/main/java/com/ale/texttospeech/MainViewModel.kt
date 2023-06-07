package com.ale.texttospeech

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale

class MainViewModel: ViewModel() {

    companion object {
        lateinit var textToSpeech: TextToSpeech
    }
    var onExportMp3Listener: OnExportMp3Listener? = null

    var locales = MutableLiveData<Set<Locale>>()
    var choseLocale = MutableLiveData<Locale>()
    var languages = MutableLiveData<ArrayList<String>>()
    var voices = MutableLiveData<ArrayList<Voice>>()
    var choseVoice = MutableLiveData<Voice>()
    var voiceNames = MutableLiveData<ArrayList<String>>()
    var speechRate = MutableLiveData<Float>().apply { value = 1.0f }
    var pitch = MutableLiveData<Float>().apply { value = 1.0f }

    fun setSpeechRate(it: Float){
        speechRate.value = roundDecimal(it)
    }

    fun setPitch(it: Float){
        pitch.value = roundDecimal(it)
    }

    fun roundDecimal(number: Float,): Float {
        val factor = Math.pow(10.0, 1.0).toFloat()
        return (Math.round(number * factor) / factor)
    }

    fun choseLocale(displayName: String){
        locales.value!!.forEach {
            if(it.displayName.equals(displayName)){
                choseLocale.value = it
                return
            }
        }
    }

    fun choseDefaultLocale(isO3Country: String?, isO3Language: String?){
        if(choseLocale.value != null || isO3Country == null || isO3Language == null) return
        locales.value!!.forEach {
            if(it.isO3Country.equals(isO3Country) && it.isO3Language.equals(isO3Language)){
                choseLocale.value = it
                return
            }
        }
    }

    fun setVoices(setVoices: Set<Voice>?){
        if(setVoices == null) return
        var temp: ArrayList<Voice> = ArrayList()
        setVoices?.forEach {
            if(choseLocale.value?.toLanguageTag().equals(it.locale.toLanguageTag())){
                temp.add(it)
            }
        }
        voices.value = temp
        choseDefaultVoice(temp.get(0))
    }

    fun choseDefaultVoice(voice: Voice){
        choseVoice.value = voice
    }

    fun choseVoice(voiceName: String){
        voices.value?.forEach {
            if(voiceName.equals(it.name)){
                choseVoice.value = it
                return
            }
        }
    }

    interface OnExportMp3Listener {
        fun export()
    }
}