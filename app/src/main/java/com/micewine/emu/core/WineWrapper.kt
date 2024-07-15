package com.micewine.emu.core

import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.ShellExecutorCmd.executeShell
import com.micewine.emu.core.ShellExecutorCmd.executeShellWithOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object WineWrapper {
    const val LINKER_PATH = "/system/bin/linker64"

    fun wineServer(args: String) {
        executeShell(
            getEnv() + "$LINKER_PATH $usrDir/bin/box64 $appRootDir/wine/x86_64/bin/wineserver $args", "WineServer"
        )
    }

    suspend fun wineServerSuspend(args: String) {
        withContext(Dispatchers.Default) {
            executeShell(
                getEnv() + "$LINKER_PATH $usrDir/bin/box64 $appRootDir/wine/x86_64/bin/wineserver $args", "WineServer"
            )
        }
    }

    fun wine(args: String, winePrefix: File) {
        executeShell(
            getEnv() + "WINEPREFIX=$winePrefix $LINKER_PATH $usrDir/bin/box64 $appRootDir/wine/x86_64/bin/wine $args", "WineProcess"
        )
    }

    fun wine(args: String, winePrefix: File, cwd: String) {
        executeShell(
            "cd $cwd;" +
                    getEnv() + "WINEPREFIX=$winePrefix $LINKER_PATH $usrDir/bin/box64 $appRootDir/wine/x86_64/bin/wine $args", "WineProcess"
        )
    }

    fun extractIcon(exeFile: File, output: String) {
        if (exeFile.name.endsWith(".exe")) {
            executeShellWithOutput(
                getEnv() + "$LINKER_PATH $usrDir/bin/wrestool -x -t 14 '${exeFile.path}' > '$output'"
            )
        }
    }
}