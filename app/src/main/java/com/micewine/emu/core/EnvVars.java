package com.micewine.emu.core;

import static com.micewine.emu.activities.MainActivity.adrenoToolsDriverFile;
import static com.micewine.emu.activities.MainActivity.appLang;
import static com.micewine.emu.activities.MainActivity.appRootDir;
import static com.micewine.emu.activities.MainActivity.box64Avx;
import static com.micewine.emu.activities.MainActivity.box64DynarecAlignedAtomics;
import static com.micewine.emu.activities.MainActivity.box64DynarecBigBlock;
import static com.micewine.emu.activities.MainActivity.box64DynarecBleedingEdge;
import static com.micewine.emu.activities.MainActivity.box64DynarecCallRet;
import static com.micewine.emu.activities.MainActivity.box64DynarecDF;
import static com.micewine.emu.activities.MainActivity.box64DynarecDirty;
import static com.micewine.emu.activities.MainActivity.box64DynarecFastNan;
import static com.micewine.emu.activities.MainActivity.box64DynarecFastRound;
import static com.micewine.emu.activities.MainActivity.box64DynarecForward;
import static com.micewine.emu.activities.MainActivity.box64DynarecNativeFlags;
import static com.micewine.emu.activities.MainActivity.box64DynarecPause;
import static com.micewine.emu.activities.MainActivity.box64DynarecSafeFlags;
import static com.micewine.emu.activities.MainActivity.box64DynarecStrongMem;
import static com.micewine.emu.activities.MainActivity.box64DynarecWait;
import static com.micewine.emu.activities.MainActivity.box64DynarecWeakBarrier;
import static com.micewine.emu.activities.MainActivity.box64DynarecX87Double;
import static com.micewine.emu.activities.MainActivity.box64LogLevel;
import static com.micewine.emu.activities.MainActivity.box64MMap32;
import static com.micewine.emu.activities.MainActivity.box64NoSigSegv;
import static com.micewine.emu.activities.MainActivity.box64NoSigill;
import static com.micewine.emu.activities.MainActivity.box64ShowBt;
import static com.micewine.emu.activities.MainActivity.box64ShowSegv;
import static com.micewine.emu.activities.MainActivity.box64Sse42;
import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.activities.MainActivity.enableDRI3;
import static com.micewine.emu.activities.MainActivity.homeDir;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.ratPackagesDir;
import static com.micewine.emu.activities.MainActivity.selectedBox64;
import static com.micewine.emu.activities.MainActivity.selectedDXVKHud;
import static com.micewine.emu.activities.MainActivity.selectedGLProfile;
import static com.micewine.emu.activities.MainActivity.selectedMesaVkWsiPresentMode;
import static com.micewine.emu.activities.MainActivity.selectedTuDebugPreset;
import static com.micewine.emu.activities.MainActivity.selectedWine;
import static com.micewine.emu.activities.MainActivity.strBoolToNum;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.activities.MainActivity.useAdrenoTools;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.activities.MainActivity.wineESync;
import static com.micewine.emu.activities.MainActivity.wineLogLevel;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.fragments.EnvVarsSettingsFragment.getCustomEnvVars;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnvVars;

import com.micewine.emu.adapters.AdapterEnvVar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnvVars {
    public static String getEnv() {
        final ArrayList<String> vars = new ArrayList<>();

        setEnv(vars);

        if (preferences != null) {
            List<AdapterEnvVar.EnvVar> customEnvVars = getCustomEnvVars();

            customEnvVars.forEach((v) -> vars.add(v.key + "=" + v.value));
            getEnvVars(selectedGameName).forEach((v) -> vars.add(v.key + "=" + v.value));
        }

        return "env " + String.join(" ", vars) + " ";
    }

    private static void setEnv(ArrayList<String> vars) {
        vars.add("LANG=" + appLang + ".UTF-8");
        vars.add("TMPDIR=" + tmpDir);
        vars.add("HOME=" + homeDir);
        vars.add("XDG_CONFIG_HOME=" + homeDir + "/.config");
        vars.add("DISPLAY=:0");
        vars.add("PULSE_LATENCY_MSEC=60");
        vars.add("LD_LIBRARY_PATH=/system/lib64:" + usrDir + "/lib");
        vars.add("PATH=$PATH:" + usrDir + "/bin:" + ratPackagesDir + "/" + selectedWine + "/files/wine/bin:" + ratPackagesDir + "/" + selectedWine + "/files/wine/lib/wine/x86_64-unix:" + ratPackagesDir + "/" + selectedBox64 + "/files/usr/bin");
        vars.add("PREFIX=" + usrDir);
        vars.add("MESA_SHADER_CACHE_DIR=" + homeDir + "/.cache");
        vars.add("MESA_VK_WSI_PRESENT_MODE=" + selectedMesaVkWsiPresentMode);

        if (selectedGLProfile != null) {
            String glVersionStr = selectedGLProfile.split(" ")[1];
            int glVersionInt = Integer.parseInt(glVersionStr.replace(".", ""));
            String glslVersion = switch (glVersionInt) {
                case 32 -> "150";
                case 31 -> "140";
                case 30 -> "130";
                case 21 -> "120";
                default -> String.valueOf(glVersionInt * 10);
            };

            vars.add("MESA_GL_VERSION_OVERRIDE=" + glVersionStr);
            vars.add("MESA_GLSL_VERSION_OVERRIDE=" + glslVersion);
        }

        vars.add("VK_ICD_FILENAMES=" + appRootDir + "/vulkan_icd.json");

        vars.add("GALLIUM_DRIVER=zink");
        vars.add("TU_DEBUG=" + selectedTuDebugPreset);
        vars.add("ZINK_DEBUG=compact");
        vars.add("ZINK_DESCRIPTORS=lazy");

        if (!enableDRI3) {
            vars.add("MESA_VK_WSI_DEBUG=sw");
        }

        vars.add("DXVK_ASYNC=1");
        vars.add("DXVK_STATE_CACHE_PATH=" + homeDir + "/.cache/dxvk-shader-cache");
        vars.add("DXVK_HUD=" + selectedDXVKHud);
        vars.add("MANGOHUD=1");
        vars.add("MANGOHUD_CONFIGFILE=" + usrDir + "/etc/MangoHud.conf");

        if (!deviceArch.equals("x86_64")) {
            vars.add("BOX64_LOG=" + box64LogLevel);
            vars.add("BOX64_CPUNAME=\"ARM64 CPU\"");
            vars.add("BOX64_MMAP32=" + box64MMap32);
            vars.add("BOX64_AVX=" + box64Avx);
            vars.add("BOX64_SSE42=" + box64Sse42);
            vars.add("BOX64_RCFILE=" + usrDir + "/etc/box64.box64rc");
            vars.add("BOX64_DYNAREC_BIGBLOCK=" + box64DynarecBigBlock);
            vars.add("BOX64_DYNAREC_STRONGMEM=" + box64DynarecStrongMem);
            vars.add("BOX64_DYNAREC_WEAKBARRIER=" + box64DynarecWeakBarrier);
            vars.add("BOX64_DYNAREC_PAUSE=" + box64DynarecPause);
            vars.add("BOX64_DYNAREC_X87DOUBLE=" + box64DynarecX87Double);
            vars.add("BOX64_DYNAREC_FASTNAN=" + box64DynarecFastNan);
            vars.add("BOX64_DYNAREC_FASTROUND=" + box64DynarecFastRound);
            vars.add("BOX64_DYNAREC_SAFEFLAGS=" + box64DynarecSafeFlags);
            vars.add("BOX64_DYNAREC_CALLRET=" + box64DynarecCallRet);
            vars.add("BOX64_DYNAREC_ALIGNED_ATOMICS=" + box64DynarecAlignedAtomics);
            vars.add("BOX64_DYNAREC_NATIVEFLAGS=" + box64DynarecNativeFlags);
            vars.add("BOX64_DYNAREC_BLEEDING_EDGE=" + box64DynarecBleedingEdge);
            vars.add("BOX64_DYNAREC_WAIT=" + box64DynarecWait);
            vars.add("BOX64_DYNAREC_DIRTY=" + box64DynarecDirty);
            vars.add("BOX64_DYNAREC_FORWARD=" + box64DynarecForward);
            vars.add("BOX64_DYNAREC_DF=" + box64DynarecDF);
            vars.add("BOX64_SHOWSEGV=" + box64ShowSegv);
            vars.add("BOX64_SHOWBT=" + box64ShowBt);
            vars.add("BOX64_NOSIGSEGV=" + box64NoSigSegv);
            vars.add("BOX64_NOSIGILL=" + box64NoSigill);
        }

        vars.add("VKD3D_FEATURE_LEVEL=12_0");

        if (Objects.equals(wineLogLevel, "disabled")) {
            vars.add("WINEDEBUG=-all");
        }

        vars.add("WINE_Z_DISK=" + appRootDir);
        vars.add("WINEESYNC=" + strBoolToNum(wineESync));

        if (useAdrenoTools && adrenoToolsDriverFile != null) {
            vars.add("USE_ADRENOTOOLS=1");
            vars.add("ADRENOTOOLS_CUSTOM_DRIVER_DIR=" + adrenoToolsDriverFile.getParent() + "/");
            vars.add("ADRENOTOOLS_CUSTOM_DRIVER_NAME=" + adrenoToolsDriverFile.getName());
            // Fix Segfault (at least on my device)
            vars.add("LD_PRELOAD=/system/lib64/libEGL.so");
        }

        // Force SDL Games to use DInput/XInput (RawInput and WGI don't works)
        vars.add("SDL_JOYSTICK_WGI=0");
        vars.add("SDL_JOYSTICK_RAWINPUT=0");
    }
}