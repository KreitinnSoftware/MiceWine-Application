package com.micewine.emu.coreutils;

import static com.micewine.emu.activities.MainActivity.homeDir;
import static com.micewine.emu.activities.MainActivity.shellLoader;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.activities.MainActivity.usrDir;

import java.util.LinkedHashMap;

public class EnvVars {
    private static final LinkedHashMap<String, String> vars = new LinkedHashMap<>();

    public static void putVar(String name, Object value) {
        vars.put(name, String.valueOf(value));
    }

    public static String getVar(String key) {
        return vars.get(key);
    }

    public static String exportVariables() {
        String variables = "";
        for (String chave : vars.keySet()) {
            variables = variables.concat(" " + getVar(chave));
        }

        return "export " + variables;
    }

    public static void setVariables() {
        putVar("TMPDIR", "TMPDIR=" + tmpDir);
        putVar("XKB_CONFIG_ROOT", "XKB_CONFIG_ROOT=" + usrDir + "/share/X11/xkb");
        putVar("CLASSPATH", "CLASSPATH=" + shellLoader);
        putVar("HOME", "HOME=" + homeDir);
        putVar("LANG", "LANG=en_US.UTF-8");
        putVar("DISPLAY_SESSION", "DISPLAY=:0");
        putVar("BOX64_LOG", "BOX64_LOG=1");
        putVar("LD_LIBRARY_PATH", "LD_LIBRARY_PATH=" + usrDir + "/lib/");
        putVar("OPENGL_DRIVER", "GALLIUM_DRIVER=softpipe");
        putVar("LIBGL_DRIVERS_PATH", "LIBGL_DRIVERS_PATH=" + usrDir + "/lib/dri/");
        putVar("VULKAN_DRIVER", "VK_ICD_FILENAMES=" + usrDir + "/share/vulkan/icd.d/freedreno_icd.aarch64.json");
        putVar("PATH", "PATH+=:" + usrDir + "/bin");
        putVar("PREFIX", "PREFIX=" + usrDir);
        putVar("DXVK_HUD", "DXVK_HUD=1");
        putVar("FIX_FLICKERING", "MESA_VK_WSI_PRESENT_MODE=mailbox");
    }
}
