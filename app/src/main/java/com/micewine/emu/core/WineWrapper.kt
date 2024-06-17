package com.micewine.emu.core

import com.micewine.emu.core.EnvVars.exportVariables
import com.micewine.emu.core.EnvVars.setVariables
import com.micewine.emu.core.ShellExecutorCmd.executeShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object WineWrapper {
    private const val LINKER_PATH = "/system/bin/linker64"

    fun wineServer(args: String) {
        setVariables()

        executeShell(
            exportVariables() + ";" +
                "$LINKER_PATH $(which box64) $(which wineserver) $args", "WineServer"
        )
    }

    suspend fun wineServerSuspend(args: String) {
        withContext(Dispatchers.Default) {
            setVariables()

            executeShell(
                exportVariables() + ";" +
                        "$LINKER_PATH $(which box64) $(which wineserver) $args", "WineServer"
            )
        }
    }

    fun wine(args: String, winePrefix: File) {
        setVariables()

        executeShell(
            exportVariables() + " WINEPREFIX=$winePrefix;" +
                    "$LINKER_PATH $(which box64) $(which wine) $args", "WineProcess"
        )
    }

    fun wine(args: String, winePrefix: File, cwd: String) {
        setVariables()

        executeShell(
            exportVariables() + " WINEPREFIX=$winePrefix;" +
                    "cd $cwd;" +
                    "$LINKER_PATH $(which box64) $(which wine) $args", "WineProcess"
        )
    }

    fun waitXServer() {
        setVariables()

        executeShell(
            exportVariables() + ";" +
                    "$LINKER_PATH $(which wait-xserver)", "WaitingXServer"
        )
    }
}