package com.micewine.emu.core

import com.micewine.emu.activities.MainActivity.Companion.homeDir
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
            variables = variables + " " + getVar(key)
        }
        return "export$variables"
    }

    fun setVariables() {
        putVar("TMPDIR", "TMPDIR=$tmpDir")
        putVar("XKB_CONFIG_ROOT", "XKB_CONFIG_ROOT=$usrDir/share/X11/xkb")
        putVar("HOME", "HOME=$homeDir")
        putVar("LANG", "LANG=en_US.UTF-8")
        putVar("DISPLAY_SESSION", "DISPLAY=:0")
        putVar("BOX64_LOG", "BOX64_LOG=1")
        putVar("LD_LIBRARY_PATH", "LD_LIBRARY_PATH=$usrDir/lib/")
        putVar("OPENGL_DRIVER", "GALLIUM_DRIVER=virpipe")
        putVar("LIBGL_ALWAYS_SOFTWARE", "LIBGL_ALWAYS_SOFTWARE=1")
        putVar("LIBGL_DRIVERS_PATH", "LIBEL_DRIVERS_PATH=$usrDir/lib/dri/")
        putVar("VULKAN_DRIVER", "VK_ICD_FILENAMES=$usrDir/share/vulkan/icd.d/freedreno_icd.aarch64.json")
        putVar("PATH", "PATH+=:$usrDir/bin")
        putVar("PREFIX", "PREFIX=$usrDir")
        putVar("DXVK_HUD", "DXVK_HUD=1")
        putVar("FIX_FLICKERING", "MESA_VK_WSI_PRESENT_MODE=mailbox")
        putVar("MICEWINE_THEME", "MICEWINE_THEME=DarkBlue")
        putVar("IB_VERSION", "IB_VERSION=0.1.8")
        putVar("D3DX_RENDERER", "D3DX_RENDERER=WineD3D")
        putVar("WINED3D_VERSION", "WINED3D_VERSION=\"WineD3D-(9.0)\"")
        putVar("DXVK_VERSION", "DXVK_VERSION=1.10.3-async")
    }
}
