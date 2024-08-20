package com.micewine.emu.core

import android.os.Build
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.ShellExecutorCmd.executeShell
import com.micewine.emu.core.ShellExecutorCmd.executeShellWithOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object WineWrapper {
    private var IS_BOX64 = if (Build.SUPPORTED_ABIS[0] == "x86_64") "" else "$usrDir/bin/box64"

    fun wineServer(args: String) {
        executeShellWithOutput(
            getEnv() + "$IS_BOX64 $appRootDir/wine/bin/wineserver $args"
        )
    }

    suspend fun wineServerSuspend(args: String) {
        withContext(Dispatchers.Default) {
            executeShell(
                getEnv() + "$IS_BOX64 $appRootDir/wine/bin/wineserver $args", "WineServer"
            )
        }
    }

    fun wine(args: String, winePrefix: File) {
        executeShell(
            getEnv() + "WINEPREFIX=$winePrefix $IS_BOX64 $appRootDir/wine/bin/wine $args", "WineProcess"
        )
    }

    fun wine(args: String, winePrefix: File, cwd: String) {
        executeShell(
            "cd $cwd;" +
                    getEnv() + "WINEPREFIX=$winePrefix $IS_BOX64 $appRootDir/wine/bin/wine $args", "WineProcess"
        )
    }

    fun extractIcon(exeFile: File, output: String) {
        if (exeFile.name.endsWith(".exe")) {
            executeShellWithOutput(
                getEnv() + "$usrDir/bin/wrestool -x -t 14 '${exeFile.path}' > '$output'"
            )
        }
    }
}