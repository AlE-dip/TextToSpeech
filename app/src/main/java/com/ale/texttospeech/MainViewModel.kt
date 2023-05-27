package com.ale.texttospeech

import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale

class MainViewModel: ViewModel() {

    companion object {
        lateinit var textToSpeech : TextToSpeech
    }

    var locale = MutableLiveData<Set<Locale>>()
    var speechRate = MutableLiveData<Float>().apply { value = 1.0f }
    var choseLocale = MutableLiveData<Locale>()

    fun setSpeechRate(it: Float){
        speechRate.value = roundDecimal(it)
    }

    fun roundDecimal(number: Float,): Float {
        val factor = Math.pow(10.0, 1.0).toFloat()
        return (Math.round(number * factor) / factor)
    }
}