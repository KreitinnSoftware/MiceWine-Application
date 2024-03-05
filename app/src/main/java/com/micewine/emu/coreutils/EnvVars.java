package com.micewine.emu.coreutils;

import static com.micewine.emu.activities.MainActivity.homeDir;
import static com.micewine.emu.activities.MainActivity.shellLoader;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.activities.MainActivity.usrDir;

import java.util.LinkedHashMap;

public class EnvVars {
    private static final LinkedHashMap<String , String> vars = new LinkedHashMap<>();

    public static void putVar(String name, Object value) {
        vars.put(name, String.valueOf(value));
    }

    public static String getVar(String key) {
        return vars.get(key);
    }

    public static String exportVariables() {
        String variables = "";
        for (String key : vars.keySet()) {
            variables = variables.concat(" " + getVar(key));
        }

        return "export " + variables;
    }

    public static void setVariables() {
        putVar("TMPDIR", "TMPDIR=" + tmpDir);
        putVar("XKB_CONFIG_ROOT", "XKB_CONFIG_ROOT=" + "/sdcard/x11/X11/xkb");
        putVar("CLASSPATH", "CLASSPATH=/data/data/com.micewine.emu/files/loader.apk");
        putVar("HOME", "HOME=" + homeDir);
        putVar("LANG", "LANG=en_US.UTF-8");
        putVar("BOX64_LOG", "BOX64_LOG=1");
        putVar("LD_LIBRARY_PATH", "LD_LIBRARY_PATH=" + usrDir + "/lib");
    }
}