package com.micewine.emu.core

import android.os.Build
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.core.ShellLoader.runCommandWithOutput
import java.io.File

object WineWrapper {
    private var IS_BOX64 = if (Build.SUPPORTED_ABIS[0] == "x86_64") "" else "box64"

    fun wineServer(args: String) {
        runCommand(
            getEnv() + "$IS_BOX64 wineserver $args"
        )
    }

    fun wine(args: String, winePrefix: File) {
        runCommand(
            getEnv() + "WINEPREFIX=$winePrefix $IS_BOX64 wine $args"
        )
    }

    fun wine(args: String, winePrefix: File, retLog: Boolean): String {
        if (retLog) {
            return runCommandWithOutput(
                getEnv() + "WINEPREFIX=$winePrefix $IS_BOX64 wine $args"
            )
        }
        return ""
    }

    fun wine(args: String, winePrefix: File, cwd: String) {
        runCommand(
            "cd $cwd;" +
                    getEnv() + "WINEPREFIX=$winePrefix $IS_BOX64 wine $args"
        )
    }

    fun extractIcon(exeFile: File, output: String) {
        if (exeFile.extension.lowercase() == "exe") {
            runCommand(
                getEnv() + "wrestool -x -t 14 '${exeFile.path}' > '$output'"
            )
        }
    }
}