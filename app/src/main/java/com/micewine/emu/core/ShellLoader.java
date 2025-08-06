package com.micewine.emu.core;

import static com.micewine.emu.activities.EmulationActivity.handler;

import android.util.Log;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.micewine.emu.fragments.InfoDialogFragment;

public class ShellLoader {
    public static class ViewModelAppLogs extends ViewModel {
        FragmentManager supportFragmentManager;
        public MutableLiveData<String> logsTextHead = new MutableLiveData<>();

        public ViewModelAppLogs(FragmentManager supportFragmentManager) {
            this.supportFragmentManager = supportFragmentManager;
        }

        public void appendText(String text) {
            handler.post(() -> {
               logsTextHead.postValue(text);

               if (text.contains("err:module:import_dll")) {
                   String missingDllName = text.split("Library ")[1].split(".dll")[0] + ".dll";

                   Log.e("DLL Import", "Error loading: " + missingDllName);

                   new InfoDialogFragment(
                           "Missing DLL",
                           "Error loading '" + missingDllName + "'"
                   ).show(supportFragmentManager, "");
               } else if (text.contains("VK_ERROR_DEVICE_LOST")) {
                   Log.e("VK Driver", "VK_ERROR_DEVICE_LOST");

                   new InfoDialogFragment(
                           "VK_ERROR_DEVICE_LOST",
                           "Error on Vulkan Graphics Driver 'VK_ERROR_DEVICE_LOST'"
                   ).show(supportFragmentManager, "");
               } else if (text.contains("X_CreateWindow")) {
                   Log.e("X11 Driver", "BadWindow: X_CreateWindow");

                   new InfoDialogFragment(
                           "X_CreateWindow",
                           "Error on Creating X Window 'X_CreateWindow'"
                   ).show(supportFragmentManager, "");
               }
            });
        }
    }

    static {
        System.loadLibrary("micewine");
    }

    public static native void runCommand(String command, boolean log);
    public static native String runCommandWithOutput(String command, boolean strErrLog);
}