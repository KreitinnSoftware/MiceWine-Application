package com.micewine.emu.core;

public class ShellLoader {
    static {
        System.loadLibrary("micewine");
    }

    public interface LogCallback {
        void appendLogs(String text);
    }

    public static native void cleanup();
    public static native void connectOutput(LogCallback callback);
    public static native void runCommand(String command, boolean log);
    public static native String runCommandWithOutput(String command, boolean strErrLog);
}