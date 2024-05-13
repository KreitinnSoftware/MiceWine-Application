package com.micewine.emu.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelAppLogs : ViewModel() {
    private val logsText = MutableLiveData<String>()
    val textLiveData: LiveData<String>
        get() = logsText

    fun setText(text: String) {
        logsText.value = text
    }
}