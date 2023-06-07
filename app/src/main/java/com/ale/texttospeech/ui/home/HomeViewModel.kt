package com.ale.texttospeech.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class HomeViewModel : ViewModel() {

    var isFabRun = MutableLiveData<Boolean>().apply { value = false }
    var text = MutableLiveData<String>()
    var indexCursor = MutableLiveData<Int>().apply { value = 0 }
}