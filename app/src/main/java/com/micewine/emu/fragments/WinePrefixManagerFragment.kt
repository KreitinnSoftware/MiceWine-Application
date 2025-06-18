package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_WINE_PREFIX
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_DPI
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_DPI_DEFAULT_VALUE
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.MainActivity.Companion.selectedWine
import com.micewine.emu.activities.MainActivity.Companion.unixUsername
import com.micewine.emu.activities.MainActivity.Companion.winePrefix
import com.micewine.emu.activities.MainActivity.Companion.winePrefixesDir
import com.micewine.emu.adapters.AdapterPreset
import com.micewine.emu.adapters.AdapterPreset.Companion.selectedPresetId
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.core.WineWrapper.wine
import com.micewine.emu.fragments.CreatePresetFragment.Companion.WINEPREFIX_PRESET
import java.io.File

class WinePrefixManagerFragment : Fragment() {
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewGeneralSettings)

        initialize()
        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterPreset(presetListNames, requireContext(), requireActivity().supportFragmentManager))

        presetListNames.clear()
        presetList.forEach {
            addToAdapter(it, WINEPREFIX_PRESET, true)
        }
    }

    private fun addToAdapter(titleSettings: String, type: Int, userPreset: Boolean) {
        presetListNames.add(
            AdapterPreset.Item(titleSettings, type, userPreset, true)
        )
    }

    companion object {
        private var recyclerView: RecyclerView? = null
        private val presetListNames: MutableList<AdapterPreset.Item> = mutableListOf()
        private var presetList: MutableList<String> = mutableListOf()

        fun initialize() {
            presetList = getWinePrefixes().toMutableList()
        }

        fun getWinePrefixes(): MutableList<String> {
            return winePrefixesDir.listFiles()?.map { it.name }?.toMutableList() ?: mutableListOf()
        }

        fun getWinePrefixFile(name: String): File {
            return File("$winePrefixesDir/$name")
        }

        fun getSelectedWinePrefix(): String {
            return preferences?.getString(SELECTED_WINE_PREFIX, "default") ?: "default"
        }

        fun putSelectedWinePrefix(name: String) {
            preferences?.edit()?.apply {
                putString(SELECTED_WINE_PREFIX, name)
                apply()
            }
            winePrefix = name
        }

        fun createWinePrefix(name: String, wineId: String) {
            val winePrefix = File("$winePrefixesDir/$name")
            if (!winePrefix.exists()) {
                val driveC = File("$winePrefix/drive_c")
                val wineUtils = File("$appRootDir/wine-utils")
                val startMenu = File("$driveC/ProgramData/Microsoft/Windows/Start Menu")
                val userSharedFolder = File("/storage/emulated/0/MiceWine")
                val isProton = File("$driveC/users/steamuser").exists()

                val wineUserDir: File = if (isProton) {
                    File("$driveC/users/steamuser")
                } else {
                    File("$driveC/users/$unixUsername")
                }

                val localAppData = File("$wineUserDir/AppData")
                val localSavedGames = File("$wineUserDir/Saved Games")
                val system32 = File("$driveC/windows/system32")
                val syswow64 = File("$driveC/windows/syswow64")
                val winePrefixConfigFile = File("$winePrefix/config")
                val wineFontsDirs = File("$winePrefix/drive_c/windows/Fonts")

                winePrefix.mkdirs()
                winePrefixConfigFile.writeText("$wineId\n$isProton\n")

                selectedWine = wineId

                wine("wineboot -i")

                File("$wineUtils/CoreFonts").copyRecursively(wineFontsDirs, true)

                localAppData.copyRecursively(
                    File("$userSharedFolder/AppData"), true
                )
                localAppData.deleteRecursively()

                localSavedGames.deleteRecursively()

                File("$userSharedFolder/AppData").mkdirs()
                File("$userSharedFolder/Saved Games").mkdirs()

                runCommand("ln -sf '$userSharedFolder/AppData' '$localAppData'")
                runCommand("ln -sf '$userSharedFolder/Saved Games' '$localSavedGames'")

                startMenu.deleteRecursively()

                File("$wineUtils/Start Menu").copyRecursively(File("$startMenu"), true)
                File("$wineUtils/Addons").copyRecursively(File("$driveC/Addons"), true)
                File("$wineUtils/Addons/Windows").copyRecursively(File("$driveC/windows"), true)
                File("$wineUtils/DirectX/x64").copyRecursively(system32, true)
                File("$wineUtils/DirectX/x32").copyRecursively(syswow64, true)
                File("$wineUtils/OpenAL/x64").copyRecursively(system32, true)
                File("$wineUtils/OpenAL/x32").copyRecursively(syswow64, true)

                wine("regedit '$driveC/Addons/DefaultDLLsOverrides.reg'")
                wine("regedit '$driveC/Addons/Themes/DarkBlue/DarkBlue.reg'")
                wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Decorated /d N /f")
                wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Managed /d N /f")
                wine("reg add HKCU\\\\Control\\ Panel\\\\Desktop /t REG_DWORD /v LogPixels /d ${preferences?.getInt(WINE_DPI, WINE_DPI_DEFAULT_VALUE)} /f")

                presetList.add(name)
                presetListNames.add(
                    AdapterPreset.Item(name, WINEPREFIX_PRESET, true, true)
                )

                recyclerView?.post {
                    recyclerView?.adapter?.notifyItemInserted(presetListNames.size)
                }
            }
        }

        fun deleteWinePrefix(name: String) {
            val index = presetList.indexOfFirst { it == name }

            if (getWinePrefixes().count() == 1) {
                return
            }

            runCommand("rm -rf $winePrefixesDir/$name")

            presetList.removeAt(index)
            presetListNames.removeAt(index)

            recyclerView?.adapter?.notifyItemRemoved(index)

            if (index == selectedPresetId) {
                preferences?.edit {
                    putString(SELECTED_WINE_PREFIX, presetListNames.first().titleSettings)
                    apply()
                }
                recyclerView?.adapter?.notifyItemChanged(0)
            }
        }
    }
}
