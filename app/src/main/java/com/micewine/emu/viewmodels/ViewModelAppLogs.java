package com.micewine.emu.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewModelAppLogs extends ViewModel {
    private MutableLiveData<String> logsText = new MutableLiveData<>();

    public LiveData<String> getTextLiveData() {
        return logsText;
    }

    public void setText(String text) {
        logsText.setValue(text);
    }
}