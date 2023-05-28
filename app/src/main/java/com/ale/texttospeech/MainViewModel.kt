package com.ale.texttospeech

import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale

class MainViewModel: ViewModel() {

    companion object {
        lateinit var textToSpeech: TextToSpeech
    }


    var locales = MutableLiveData<Set<Locale>>()
    var speechRate = MutableLiveData<Float>().apply { value = 1.0f }
    var choseLocale = MutableLiveData<Locale>()
    var languages = MutableLiveData<ArrayList<String>>()

    fun setSpeechRate(it: Float){
        speechRate.value = roundDecimal(it)
    }

    fun roundDecimal(number: Float,): Float {
        val factor = Math.pow(10.0, 1.0).toFloat()
        return (Math.round(number * factor) / factor)
    }

    fun choseLocale(displayName: String){
        locales.value!!.forEach {
            if(it.displayName.equals(displayName)){
                choseLocale.value = it
            }
        }
    }

    fun choseLocale(isO3Country: String, isO3Language: String){
        locales.value!!.forEach {
            if(it.isO3Country.equals(isO3Country) && it.isO3Language.equals(isO3Language)){
                choseLocale.value = it
            }
        }
    }
}