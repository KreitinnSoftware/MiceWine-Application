package com.micewine.emu.core

import android.os.Build
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.ShellExecutorCmd.ShellLoader
import java.io.File

object WineWrapper {
    private var IS_BOX64 = if (Build.SUPPORTED_ABIS[0] == "x86_64") "" else "$usrDir/bin/box64"
    var wineShell = ShellLoader()

    fun wineServer(args: String) {
        wineShell.runCommand(
            getEnv() + "$IS_BOX64 $appRootDir/wine/bin/wineserver $args"
        )
    }

    fun wine(args: String, winePrefix: File) {
        wineShell.runCommand(
            getEnv() + "WINEPREFIX=$winePrefix $IS_BOX64 $appRootDir/wine/bin/wine $args"
        )
    }

    fun wine(args: String, winePrefix: File, cwd: String) {
        wineShell.runCommand(
            "cd $cwd;" +
                    getEnv() + "WINEPREFIX=$winePrefix $IS_BOX64 $appRootDir/wine/bin/wine $args"
        )
    }

    fun extractIcon(exeFile: File, output: String) {
        if (exeFile.name.endsWith(".exe")) {
            wineShell.runCommand(
                getEnv() + "$usrDir/bin/wrestool -x -t 14 '${exeFile.path}' > '$output'"
            )
        }
    }
}