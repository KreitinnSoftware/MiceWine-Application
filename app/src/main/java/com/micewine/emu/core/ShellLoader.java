package com.micewine.emu.core;

import static com.micewine.emu.activities.EmulationActivity.handler;
import static com.micewine.emu.activities.EmulationActivity.sharedLogs;

import android.util.Log;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.micewine.emu.fragments.InfoDialogFragment;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellLoader {
    public static String runCommandWithOutput(String cmd, boolean enableStdErr) {
        StringBuilder output = new StringBuilder();

        try {
            Process shell = Runtime.getRuntime().exec("/system/bin/sh");

            try (DataOutputStream os = new DataOutputStream(shell.getOutputStream())) {
                os.writeBytes(cmd + "\nexit\n");
                os.flush();
            }

            BufferedReader stdout = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            Thread stdoutReader = new Thread(() -> {
                String line;
                try {
                    while ((line = stdout.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException ignored) {
                } finally {
                    try {
                        stdout.close();
                    } catch (IOException ignored) {}
                }
            });

            Thread stderrReader = null;
            if (enableStdErr) {
                stderrReader = new Thread(() -> {
                    String line;
                    try (BufferedReader stderr = new BufferedReader(new InputStreamReader(shell.getErrorStream()))) {
                        while ((line = stderr.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                    } catch (IOException ignored) {
                    }
                });
            }

            stdoutReader.start();
            if (stderrReader != null) stderrReader.start();

            stdoutReader.join();
            if (stderrReader != null) stderrReader.join();

            shell.waitFor();

        } catch (IOException | InterruptedException ignored) {
        }

        return output.toString();
    }



    public static void runCommand(String cmd, boolean log) {
        Process shell = null;
        DataOutputStream os = null;

        if (log) {
            Log.d("ShellLoader", "Trying to exec: '" + cmd + "'");
        }

        try {
            shell = Runtime.getRuntime().exec("/system/bin/sh");
            os = new DataOutputStream(shell.getOutputStream());

            os.writeBytes(cmd + "\nexit\n");
            os.flush();

            BufferedReader stdout = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(shell.getErrorStream()));

            Thread stdoutReader = new Thread(() -> {
                String stdOut;
                try {
                    while ((stdOut = stdout.readLine()) != null) {
                        if (sharedLogs != null) {
                            Log.d("ShellLoader", stdOut + "\n");
                            sharedLogs.appendText(stdOut);
                        }
                    }
                } catch (IOException ignored) {
                } finally {
                    try {
                        stdout.close();
                    } catch (IOException ignored) {}
                }
            });

            Thread stderrReader = new Thread(() -> {
                String stdErr;
                try {
                    while ((stdErr = stderr.readLine()) != null) {
                        if (sharedLogs != null) {
                            Log.d("ShellLoader", stdErr + "\n");
                            sharedLogs.appendText(stdErr);
                        }
                    }
                } catch (IOException ignored) {
                } finally {
                    try {
                        stderr.close();
                    } catch (IOException ignored) {}
                }
            });

            if (log) {
                stdoutReader.start();
                stderrReader.start();

                stdoutReader.join();
                stderrReader.join();
            } else {
                stdout.close();
                stderr.close();
            }

            os.close();

            shell.waitFor();
            shell.destroy();

        } catch (IOException | InterruptedException ignored) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ignored) {}

            if (shell != null) {
                shell.destroy();
            }
        }
    }

    public static class ViewModelAppLogs extends ViewModel {
        FragmentManager supportFragmentManager;
        public MutableLiveData<String> logsTextHead = new MutableLiveData<>();

        public ViewModelAppLogs(FragmentManager supportFragmentManager) {
            this.supportFragmentManager = supportFragmentManager;
        }

        public void appendText(String text) {
            handler.post(() -> {
               logsTextHead.postValue(text + "\n");

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
}