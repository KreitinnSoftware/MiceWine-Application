package com.micewine.emu.core

import android.os.Build
import com.google.gson.reflect.TypeToken
import com.micewine.emu.activities.MainActivity.Companion.adrenoToolsDriverFile
import com.micewine.emu.activities.MainActivity.Companion.appLang
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.box64Avx
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecAlignedAtomics
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecBigBlock
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecBleedingEdge
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecCallRet
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecDF
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecDirty
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastNan
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastRound
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecForward
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecNativeFlags
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecPause
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecSafeFlags
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecStrongMem
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecWait
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecWeakBarrier
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecX87Double
import com.micewine.emu.activities.MainActivity.Companion.box64LogLevel
import com.micewine.emu.activities.MainActivity.Companion.box64MMap32
import com.micewine.emu.activities.MainActivity.Companion.box64NoSigSegv
import com.micewine.emu.activities.MainActivity.Companion.box64NoSigill
import com.micewine.emu.activities.MainActivity.Companion.box64ShowBt
import com.micewine.emu.activities.MainActivity.Companion.box64ShowSegv
import com.micewine.emu.activities.MainActivity.Companion.box64Sse42
import com.micewine.emu.activities.MainActivity.Companion.enableDRI3
import com.micewine.emu.activities.MainActivity.Companion.getLdPreloadWorkaround
import com.micewine.emu.activities.MainActivity.Companion.gson
import com.micewine.emu.activities.MainActivity.Companion.homeDir
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import com.micewine.emu.activities.MainActivity.Companion.selectedBox64
import com.micewine.emu.activities.MainActivity.Companion.selectedDXVKHud
import com.micewine.emu.activities.MainActivity.Companion.selectedGLProfile
import com.micewine.emu.activities.MainActivity.Companion.selectedMesaVkWsiPresentMode
import com.micewine.emu.activities.MainActivity.Companion.selectedTuDebugPreset
import com.micewine.emu.activities.MainActivity.Companion.selectedWine
import com.micewine.emu.activities.MainActivity.Companion.strBoolToNum
import com.micewine.emu.activities.MainActivity.Companion.tmpDir
import com.micewine.emu.activities.MainActivity.Companion.useAdrenoTools
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.activities.MainActivity.Companion.wineESync
import com.micewine.emu.activities.MainActivity.Companion.wineLogLevel
import com.micewine.emu.fragments.EnvVarsSettingsFragment
import com.micewine.emu.fragments.EnvVarsSettingsFragment.Companion.ENV_VARS_KEY

object EnvVars {
    fun getEnv(): String {
        val vars = mutableListOf<String>()

        setEnv(vars)

        val savedVarsJson = preferences?.getString(ENV_VARS_KEY, null)
        if (savedVarsJson != null) {
            val type = object : TypeToken<List<EnvVarsSettingsFragment.EnvironmentVariable>>() {}.type

            gson.fromJson<List<EnvVarsSettingsFragment.EnvironmentVariable>>(savedVarsJson, type).forEach {
                vars.add("${it.key}=${it.value}")
            }
        }

        return "env ${vars.joinToString(" ")} "
    }

    private fun setEnv(vars: MutableList<String>) {
        vars.add("LANG=$appLang.UTF-8")
        vars.add("TMPDIR=$tmpDir")
        vars.add("HOME=$homeDir")
        vars.add("XDG_CONFIG_HOME=$homeDir/.config")
        vars.add("DISPLAY=:0")
        vars.add("PULSE_LATENCY_MSEC=60")
        vars.add("LD_LIBRARY_PATH=/system/lib64:$usrDir/lib")
        vars.add("PATH=\$PATH:$usrDir/bin:$ratPackagesDir/$selectedWine/files/wine/bin:$ratPackagesDir/$selectedBox64/files/usr/bin")
        vars.add("PREFIX=$usrDir")
        vars.add("MESA_SHADER_CACHE_DIR=$homeDir/.cache")
        vars.add("MESA_VK_WSI_PRESENT_MODE=$selectedMesaVkWsiPresentMode")

        val glVersionStr = selectedGLProfile!!.split(" ")[1]
        val glslVersion =
            when (val glVersionInt = glVersionStr.replace(".", "").toInt()) {
                in 33..46 -> "$glVersionInt" + "0"
                32 -> "150"
                31 -> "140"
                30 -> "130"
                21 -> "120"
                else -> null
            }

        vars.add("MESA_GL_VERSION_OVERRIDE=$glVersionStr")
        vars.add("MESA_GLSL_VERSION_OVERRIDE=$glslVersion")
        vars.add("VK_ICD_FILENAMES=$appRootDir/vulkan_icd.json")

        vars.add("GALLIUM_DRIVER=zink")
        vars.add("TU_DEBUG=$selectedTuDebugPreset")
        vars.add("ZINK_DEBUG=compact")
        vars.add("ZINK_DESCRIPTORS=lazy")

        if (!enableDRI3) {
            vars.add("MESA_VK_WSI_DEBUG=sw")
        }

        vars.add("DXVK_ASYNC=1")
        vars.add("DXVK_STATE_CACHE_PATH=$homeDir/.cache/dxvk-shader-cache")
        vars.add("DXVK_HUD=$selectedDXVKHud")
        vars.add("MANGOHUD=1")
        vars.add("MANGOHUD_CONFIGFILE=$usrDir/etc/MangoHud.conf")

        if (Build.SUPPORTED_ABIS[0] != "x86_64") {
            vars.add("BOX64_LOG=$box64LogLevel")
            vars.add("BOX64_CPUNAME=\"ARM64 CPU\"")
            vars.add("BOX64_MMAP32=$box64MMap32")
            vars.add("BOX64_AVX=$box64Avx")
            vars.add("BOX64_SSE42=$box64Sse42")
            vars.add("BOX64_RCFILE=$usrDir/etc/box64.box64rc")
            vars.add("BOX64_DYNAREC_BIGBLOCK=$box64DynarecBigBlock")
            vars.add("BOX64_DYNAREC_STRONGMEM=$box64DynarecStrongMem")
            vars.add("BOX64_DYNAREC_WEAKBARRIER=$box64DynarecWeakBarrier")
            vars.add("BOX64_DYNAREC_PAUSE=$box64DynarecPause")
            vars.add("BOX64_DYNAREC_X87DOUBLE=$box64DynarecX87Double")
            vars.add("BOX64_DYNAREC_FASTNAN=$box64DynarecFastNan")
            vars.add("BOX64_DYNAREC_FASTROUND=$box64DynarecFastRound")
            vars.add("BOX64_DYNAREC_SAFEFLAGS=$box64DynarecSafeFlags")
            vars.add("BOX64_DYNAREC_CALLRET=$box64DynarecCallRet")
            vars.add("BOX64_DYNAREC_ALIGNED_ATOMICS=$box64DynarecAlignedAtomics")
            vars.add("BOX64_DYNAREC_NATIVEFLAGS=$box64DynarecNativeFlags")
            vars.add("BOX64_DYNAREC_BLEEDING_EDGE=$box64DynarecBleedingEdge")
            vars.add("BOX64_DYNAREC_WAIT=$box64DynarecWait")
            vars.add("BOX64_DYNAREC_DIRTY=$box64DynarecDirty")
            vars.add("BOX64_DYNAREC_FORWARD=$box64DynarecForward")
            vars.add("BOX64_DYNAREC_DF=$box64DynarecDF")
            vars.add("BOX64_SHOWSEGV=$box64ShowSegv")
            vars.add("BOX64_SHOWBT=$box64ShowBt")
            vars.add("BOX64_NOSIGSEGV=$box64NoSigSegv")
            vars.add("BOX64_NOSIGILL=$box64NoSigill")
        }

        vars.add("VKD3D_FEATURE_LEVEL=12_0")

        if (wineLogLevel == "disabled") {
            vars.add("WINEDEBUG=-all")
        }

        vars.add("WINE_Z_DISK=$appRootDir")
        vars.add("WINEESYNC=${strBoolToNum(wineESync)}")

        if (useAdrenoTools) {
            vars.add("USE_ADRENOTOOLS=1")
            vars.add("ADRENOTOOLS_CUSTOM_DRIVER_DIR=${adrenoToolsDriverFile?.parent}/")
            vars.add("ADRENOTOOLS_CUSTOM_DRIVER_NAME=${adrenoToolsDriverFile?.name}")
            // Workaround for dlopen error (at least on my device)
            vars.add(getLdPreloadWorkaround())
        }

        // Force SDL Games to use DInput/XInput (RawInput and WGI don't works)
        vars.add("SDL_JOYSTICK_WGI=0")
        vars.add("SDL_JOYSTICK_RAWINPUT=0")
    }
}
