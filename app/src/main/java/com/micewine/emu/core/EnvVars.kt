package com.micewine.emu.core

import android.os.Build
import com.micewine.emu.activities.MainActivity.Companion.appLang
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.box64Avx
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecAlignedAtomics
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecBigblock
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecBleedingEdge
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecCallret
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecDirty
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastnan
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastround
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecNativeflags
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecPause
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecSafeflags
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecStrongmem
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecWait
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecWeakbarrier
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecX87double
import com.micewine.emu.activities.MainActivity.Companion.box64LogLevel
import com.micewine.emu.activities.MainActivity.Companion.box64NoSigSegv
import com.micewine.emu.activities.MainActivity.Companion.box64NoSigill
import com.micewine.emu.activities.MainActivity.Companion.box64ShowBt
import com.micewine.emu.activities.MainActivity.Companion.box64ShowSegv
import com.micewine.emu.activities.MainActivity.Companion.enableDRI3
import com.micewine.emu.activities.MainActivity.Companion.enableMangoHUD
import com.micewine.emu.activities.MainActivity.Companion.homeDir
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import com.micewine.emu.activities.MainActivity.Companion.selectedBox64
import com.micewine.emu.activities.MainActivity.Companion.selectedDXVKHud
import com.micewine.emu.activities.MainActivity.Companion.selectedMesaVkWsiPresentMode
import com.micewine.emu.activities.MainActivity.Companion.selectedTuDebugPreset
import com.micewine.emu.activities.MainActivity.Companion.selectedGLProfile
import com.micewine.emu.activities.MainActivity.Companion.tmpDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.activities.MainActivity.Companion.wineESync
import com.micewine.emu.activities.MainActivity.Companion.wineLogLevel
import java.io.File

object EnvVars {
    private val vars = LinkedHashMap<String, String>()
    private fun putVar(name: String, value: String?) {
        vars[name] = "$name=\"$value\""
    }

    fun getEnv(): String {
        setEnv()

        return "env ${vars.values.joinToString(" ")} "
    }

    private fun setEnv() {
        putVar("LANG", "$appLang.UTF-8")
        putVar("TMPDIR", tmpDir.path)
        putVar("HOME", homeDir.path)
        putVar("XDG_CONFIG_HOME", "$homeDir/.config")
        putVar("DISPLAY", ":0")
        putVar("PULSE_LATENCY_MSEC", "60")
        putVar("LD_LIBRARY_PATH", "/system/lib64:$usrDir/lib")
        putVar("PATH", "\$PATH:$usrDir/bin:$appRootDir/wine/bin:$ratPackagesDir/$selectedBox64/files/usr/bin")
        putVar("PREFIX", usrDir.path)
        putVar("MESA_SHADER_CACHE_DIR", "$homeDir/.cache")
        putVar("MESA_VK_WSI_PRESENT_MODE", selectedMesaVkWsiPresentMode)

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

        putVar("MESA_GL_VERSION_OVERRIDE", glVersionStr)
        putVar("MESA_GLSL_VERSION_OVERRIDE", glslVersion)
        putVar("VK_ICD_FILENAMES", "$appRootDir/vulkan_icd.json")

        putVar("GALLIUM_DRIVER", "zink")
        putVar("TU_DEBUG", "$selectedTuDebugPreset")
        putVar("ZINK_DEBUG", "compact")

        if (!enableDRI3) {
            putVar("MESA_VK_WSI_DEBUG", "sw")
        }

        putVar("DXVK_ASYNC", "1")
        putVar("DXVK_STATE_CACHE_PATH", "$homeDir/.cache/dxvk-shader-cache")
        putVar("DXVK_HUD", selectedDXVKHud)

        if (enableMangoHUD) {
            putVar("MANGOHUD", "1")
            putVar("MANGOHUD_CONFIGFILE", "$usrDir/etc/MangoHud.conf")
        }

        if (Build.SUPPORTED_ABIS[0] != "x86_64") {
            putVar("BOX64_LOG", box64LogLevel)
            putVar("BOX64_CPUNAME", "ARM64 CPU")
            putVar("BOX64_MMAP32", "1")
            putVar("BOX64_AVX", box64Avx)
            putVar("BOX64_RCFILE", "$usrDir/etc/box64.box64rc")
            putVar("BOX64_DYNAREC_BIGBLOCK", box64DynarecBigblock)
            putVar("BOX64_DYNAREC_STRONGMEM", box64DynarecStrongmem)
            putVar("BOX64_DYNAREC_WEAKBARRIER", box64DynarecWeakbarrier)
            putVar("BOX64_DYNAREC_PAUSE", box64DynarecPause)
            putVar("BOX64_DYNAREC_X87DOUBLE", box64DynarecX87double)
            putVar("BOX64_DYNAREC_FASTNAN", box64DynarecFastnan)
            putVar("BOX64_DYNAREC_FASTROUND", box64DynarecFastround)
            putVar("BOX64_DYNAREC_SAFEFLAGS", box64DynarecSafeflags)
            putVar("BOX64_DYNAREC_CALLRET", box64DynarecCallret)
            putVar("BOX64_DYNAREC_ALIGNED_ATOMICS", box64DynarecAlignedAtomics)
            putVar("BOX64_DYNAREC_NATIVEFLAGS", box64DynarecNativeflags)
            putVar("BOX64_DYNAREC_BLEEDING_EDGE", box64DynarecBleedingEdge)
            putVar("BOX64_DYNAREC_WAIT", box64DynarecWait)
            putVar("BOX64_DYNAREC_DIRTY", box64DynarecDirty)
            putVar("BOX64_SHOWSEGV", box64ShowSegv)
            putVar("BOX64_SHOWBT", box64ShowBt)
            putVar("BOX64_NOSIGSEGV", box64NoSigSegv)
            putVar("BOX64_NOSIGILL", box64NoSigill)
        }

        putVar("VKD3D_FEATURE_LEVEL", "12_0")

        if (wineLogLevel == "minimal") {
            putVar("WINEDEBUG", "-all")
        }

        putVar("WINEESYNC", wineESync)
    }
}
