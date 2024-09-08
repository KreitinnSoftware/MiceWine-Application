package com.micewine.emu.core

import com.micewine.emu.activities.MainActivity.Companion.appLang
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecAlignedAtomics
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecBigblock
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecBleedingEdge
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecCallret
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastnan
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastround
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecSafeflags
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecStrongmem
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecWait
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecX87double
import com.micewine.emu.activities.MainActivity.Companion.homeDir
import com.micewine.emu.activities.MainActivity.Companion.selectedDXVKHud
import com.micewine.emu.activities.MainActivity.Companion.selectedDriver
import com.micewine.emu.activities.MainActivity.Companion.selectedMesaVkWsiPresentMode
import com.micewine.emu.activities.MainActivity.Companion.selectedTuDebugPreset
import com.micewine.emu.activities.MainActivity.Companion.selectedGLProfile
import com.micewine.emu.activities.MainActivity.Companion.tmpDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir

object EnvVars {
    private val vars = LinkedHashMap<String, String>()
    private fun putVar(name: String, value: String?) {
        vars[name] = "$name=$value"
    }

    fun getEnv(): String {
        setEnv()

        return "env ${vars.values.joinToString(" ")} "
    }

    private fun setEnv() {
        putVar("LANG", "$appLang.UTF-8")
        putVar("TMPDIR", tmpDir.path)
        putVar("HOME", homeDir.path)
        putVar("DISPLAY", ":0")
        putVar("BOX64_LOG", "1")
        putVar("LD_LIBRARY_PATH", "$usrDir/lib")
        putVar("PATH", "\$PATH:$usrDir/bin:$appRootDir/wine/bin")
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

        when (selectedDriver) {
            "Turnip/Zink" -> {
                putVar("GALLIUM_DRIVER", "zink")
                putVar("MESA_LOADER_DRIVER_OVERRIDE", "zink")
                putVar("TU_DEBUG", "$selectedTuDebugPreset")
                putVar("VK_ICD_FILENAMES", "$usrDir/share/vulkan/icd.d/freedreno_icd.aarch64.json")
                putVar("ZINK_DEBUG", "compact")
            }
            "Android/Zink" -> {
                putVar("GALLIUM_DRIVER", "zink")
                putVar("MESA_LOADER_DRIVER_OVERRIDE", "zink")
                putVar("LD_LIBRARY_PATH", "/system/lib64:$usrDir/lib")
                putVar("VK_ICD_FILENAMES", "$usrDir/share/vulkan/icd.d/sysvk_icd.json")
                putVar("ZINK_DEBUG", "compact")
            }
        }

        putVar("DXVK_ASYNC", "1")
        putVar("DXVK_STATE_CACHE_PATH", "$homeDir/.cache/dxvk-shader-cache")
        putVar("DXVK_HUD", selectedDXVKHud)

        putVar("GALLIUM_HUD", "simple,fps")
        putVar("BOX64_LOG", "1")
        putVar("BOX64_MMAP32", "1")
        putVar("BOX64_AVX", "2")
        putVar("BOX64_RCFILE", "$usrDir/etc/box64.box64rc")
        putVar("BOX64_DYNAREC_BIGBLOCK", box64DynarecBigblock)
        putVar("BOX64_DYNAREC_STRONGMEM", box64DynarecStrongmem)
        putVar("BOX64_DYNAREC_X87DOUBLE", box64DynarecX87double)
        putVar("BOX64_DYNAREC_FASTNAN", box64DynarecFastnan)
        putVar("BOX64_DYNAREC_FASTROUND", box64DynarecFastround)
        putVar("BOX64_DYNAREC_SAFEFLAGS", box64DynarecSafeflags)
        putVar("BOX64_DYNAREC_CALLRET", box64DynarecCallret)
        putVar("BOX64_DYNAREC_ALIGNED_ATOMICS", box64DynarecAlignedAtomics)
        putVar("BOX64_DYNAREC_BLEEDING_EDGE", box64DynarecBleedingEdge)
        putVar("BOX64_DYNAREC_WAIT", box64DynarecWait)
        putVar("VKD3D_FEATURE_LEVEL", "12_0")
        putVar("WINEESYNC", "1")
    }
}
