package com.micewine.emu.core

import com.micewine.emu.activities.MainActivity.Companion.appLang
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecBigblock
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecCallret
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastnan
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecFastround
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecSafeflags
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecStrongmem
import com.micewine.emu.activities.MainActivity.Companion.box64DynarecX87double
import com.micewine.emu.activities.MainActivity.Companion.d3dxRenderer
import com.micewine.emu.activities.MainActivity.Companion.homeDir
import com.micewine.emu.activities.MainActivity.Companion.selectedDXVK
import com.micewine.emu.activities.MainActivity.Companion.selectedDXVKHud
import com.micewine.emu.activities.MainActivity.Companion.selectedDriver
import com.micewine.emu.activities.MainActivity.Companion.selectedIbVersion
import com.micewine.emu.activities.MainActivity.Companion.selectedTheme
import com.micewine.emu.activities.MainActivity.Companion.selectedVirGLProfile
import com.micewine.emu.activities.MainActivity.Companion.selectedWineD3D
import com.micewine.emu.activities.MainActivity.Companion.tmpDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir

object EnvVars {
    private val vars = LinkedHashMap<String, String>()
    private fun putVar(name: String, value: Any) {
        vars[name] = value.toString()
    }

    private fun getVar(key: String): String? {
        return vars[key]
    }

    fun exportVariables(): String {
        var variables = ""
        for (key in vars.keys) {
            variables += " " + getVar(key)
        }
        return "export$variables"
    }

    fun setVariables() {
        putVar("LANG", "LANG=$appLang")
        putVar("TMPDIR", "TMPDIR=$tmpDir")
        putVar("HOME", "HOME=$homeDir")
        putVar("DISPLAY_SESSION", "DISPLAY=:0")
        putVar("BOX64_LOG", "BOX64_LOG=1")
        putVar("LD_LIBRARY_PATH", "LD_LIBRARY_PATH=$usrDir/lib")
        putVar("PATH", "PATH+=:$usrDir/bin:$appRootDir/wine/x86_64/bin")
        putVar("PREFIX", "PREFIX=$usrDir")
        putVar("IB_VERSION", "IB_VERSION=$selectedIbVersion")
        putVar("MESA_SHADER_CACHE_DIR", "MESA_SHADER_CACHE_DIR=$homeDir/.cache")

        when (selectedDriver) {
            "Turnip/Zink" -> {
                putVar("GALLIUM_DRIVER", "GALLIUM_DRIVER=zink")
                putVar("MESA_VK_WSI_PRESENT_MODE", "MESA_VK_WSI_PRESENT_MODE=mailbox")
                putVar("TU_DEBUG", "TU_DEBUG=noconform")
                putVar("VK_ICD_FILENAMES", "VK_ICD_FILENAMES=$usrDir/share/vulkan/icd.d/freedreno_icd.aarch64.json")
                putVar("MESA_GL_VERSION_OVERRIDE", "MESA_GL_VERSION_OVERRIDE=4.6")
                putVar("MESA_GLSL_VERSION_OVERRIDE", "MESA_GLSL_VERSION_OVERRIDE=460")
            }
            "Android/Zink" -> {
                putVar("GALLIUM_DRIVER", "GALLIUM_DRIVER=zink")
                putVar("LD_LIBRARY_PATH", "LD_LIBRARY_PATH=$usrDir/native-zink/lib:$usrDir/lib")
                putVar("MESA_GL_VERSION_OVERRIDE", "MESA_GL_VERSION_OVERRIDE=4.6")
                putVar("MESA_GLSL_VERSION_OVERRIDE", "MESA_GLSL_VERSION_OVERRIDE=460")
            }
            "VirGL" -> {
                putVar("GALLIUM_DRIVER", "GALLIUM_DRIVER=virpipe")
                putVar("LIBGL_ALWAYS_SOFTWARE", "LIBGL_ALWAYS_SOFTWARE=1")

                if (selectedVirGLProfile == "GL 2.1") {
                    putVar("MESA_GL_VERSION_OVERRIDE", "MESA_GL_VERSION_OVERRIDE=2.1")
                    putVar("MESA_GLSL_VERSION_OVERRIDE", "MESA_GLSL_VERSION_OVERRIDE=120")
                } else if (selectedVirGLProfile == "GL 3.3") {
                    putVar("MESA_GL_VERSION_OVERRIDE", "MESA_GL_VERSION_OVERRIDE=3.3COMPAT")
                    putVar("MESA_GLSL_VERSION_OVERRIDE", "MESA_GLSL_VERSION_OVERRIDE=330")
                }

                putVar("MESA_EXTENSION_OVERRIDE", "MESA_EXTENSION_OVERRIDE='-GL_EXT_texture_sRGB_decode GL_EXT_polygon_offset_clamp'")
            }
        }

        putVar("MICEWINE_THEME", "MICEWINE_THEME=$selectedTheme")
        putVar("D3DX_RENDERER", "D3DX_RENDERER=$d3dxRenderer")
        putVar("WINED3D_VERSION", "WINED3D_VERSION=$selectedWineD3D")
        putVar("DXVK_VERSION", "DXVK_VERSION=$selectedDXVK")
        putVar("DXVK_ASYNC", "DXVK_ASYNC=1")
        putVar("DXVK_STATE_CACHE_PATH", "DXVK_STATE_CACHE_PATH=$homeDir/.cache/dxvk-shader-cache")

        when (selectedDXVKHud) {
            "Off" -> {
                putVar("DXVK_HUD", "DXVK_HUD=0")
            }
            "FPS" -> {
                putVar("DXVK_HUD", "DXVK_HUD=fps")
            }
            "FPS/GPU Load" -> {
                putVar("DXVK_HUD", "DXVK_HUD=fps,gpuload")
            }
        }

        putVar("BOX64_LOG", "BOX64_LOG=1")
        putVar("BOX64_MMAP32", "BOX64_MMAP32=1")
        putVar("BOX64_AVX", "BOX64_AVX=2")
        putVar("BOX64_DYNAREC_BIGBLOCK", "BOX64_DYNAREC_BIGBLOCK=$box64DynarecBigblock")
        putVar("BOX64_DYNAREC_STRONGMEM", "BOX64_DYNAREC_STRONGMEM=$box64DynarecStrongmem")
        putVar("BOX64_DYNAREC_X87DOUBLE", "BOX64_DYNAREC_X87DOUBLE=$box64DynarecX87double")
        putVar("BOX64_DYNAREC_FASTNAN", "BOX64_DYNAREC_FASTNAN=$box64DynarecFastnan")
        putVar("BOX64_DYNAREC_FASTROUND", "BOX64_DYNAREC_FASTROUND=$box64DynarecFastround")
        putVar("BOX64_DYNAREC_SAFEFLAGS", "BOX64_DYNAREC_SAFEFLAGS=$box64DynarecSafeflags")
        putVar("BOX64_DYNAREC_CALLRET", "BOX64_DYNAREC_CALLRET=$box64DynarecCallret")
        putVar("VKD3D_FEATURE_LEVEL", "VKD3D_FEATURE_LEVEL=12_0")
        putVar("WINEDEBUG", "WINEDEBUG=-virtual")
        putVar("WINEPREFIX", "WINEPREFIX=$homeDir/.wine")
    }
}
