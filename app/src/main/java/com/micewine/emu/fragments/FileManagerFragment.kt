package com.micewine.emu.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.EXPORT_LNK_ACTION
import com.micewine.emu.activities.MainActivity.Companion.fileManagerCwd
import com.micewine.emu.activities.MainActivity.Companion.fileManagerDefaultDir
import com.micewine.emu.activities.MainActivity.Companion.selectedFile
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.activities.MainActivity.Companion.wineDisksFolder
import com.micewine.emu.adapters.AdapterFiles
import com.micewine.emu.core.RatPackageManager
import com.micewine.emu.core.WineWrapper
import com.micewine.emu.databinding.FragmentFileManagerBinding
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.ADTOOLS_DRIVER_PACKAGE
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.MWP_PRESET_PACKAGE
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.RAT_PACKAGE
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.adToolsDriverCandidate
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.mwpPresetCandidate
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.ratCandidate
import com.micewine.emu.fragments.CreatePresetFragment.Companion.BOX64_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.CONTROLLER_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.VIRTUAL_CONTROLLER_PRESET
import com.micewine.emu.fragments.DeleteItemFragment.Companion.DELETE_GAME_ITEM
import com.micewine.emu.fragments.EditGamePreferencesFragment.Companion.FILE_MANAGER_START_PREFERENCES
import com.micewine.emu.fragments.RenameFragment.Companion.RENAME_FILE
import com.micewine.emu.fragments.ShortcutsFragment.Companion.addGameToList
import com.micewine.emu.utils.DriveUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mslinks.ShellLink
import mslinks.ShellLinkException
import java.io.File

class FileManagerFragment : Fragment() {
    private var binding: FragmentFileManagerBinding? = null
    private var currentFolderText: TextView? = null
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFileManagerBinding.inflate(inflater, container, false)
        rootView = binding!!.root

        recyclerView = rootView?.findViewById(R.id.recyclerViewFiles)
        recyclerView?.adapter = AdapterFiles(fileList, requireContext(), false)
        currentFolderText = rootView?.findViewById(R.id.currentFolder)

        if (fileManagerCwd == null) {
            fileManagerCwd = fileManagerDefaultDir
        }

        refreshFiles()
        registerForContextMenu(recyclerView!!)

        currentFolderText?.text = "/"
        currentFolderText?.isSelected = true

        fragmentInstance = this

        return rootView
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val file = File(selectedFile)

        when (file.extension.lowercase()) {
            "rat" -> {
                ratCandidate = RatPackageManager.RatPackage(file.path)

            }
            "mwp" -> {
                val mwpLines = file.readLines()
                if (mwpLines.isNotEmpty()) {
                    when (mwpLines[0]) {
                        "controllerPreset" -> {
                            mwpPresetCandidate = Pair(CONTROLLER_PRESET, file.path)
                        }
                        "virtualControllerPreset" -> {
                            mwpPresetCandidate = Pair(VIRTUAL_CONTROLLER_PRESET, file.path)
                        }
                        "box64Preset" -> {
                            mwpPresetCandidate = Pair(BOX64_PRESET, file.path)
                        }
                    }

                    requireActivity().menuInflater.inflate(R.menu.file_list_context_menu_package, menu)
                } else {
                    requireActivity().menuInflater.inflate(R.menu.file_list_context_menu_default, menu)
                }
            }
            "exe", "msi", "bat", "lnk" -> {
                requireActivity().menuInflater.inflate(R.menu.file_list_context_menu_exe, menu)
            }
            "zip" -> {
                adToolsDriverCandidate = RatPackageManager.AdrenoToolsPackage(file.path)

                if (adToolsDriverCandidate?.name != null) {
                    requireActivity().menuInflater.inflate(R.menu.file_list_context_menu_package, menu)
                } else {
                    requireActivity().menuInflater.inflate(R.menu.file_list_context_menu_default, menu)
                }
            }
            else -> {
                requireActivity().menuInflater.inflate(R.menu.file_list_context_menu_default, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val file = File(selectedFile)

        when (item.itemId) {
            R.id.addToHome -> {
                if (selectedFile.endsWith("exe")) {
                    val output = "$usrDir/icons/${File(selectedFile).nameWithoutExtension}-icon"

                    WineWrapper.extractIcon(file, output)

                    addGameToList(selectedFile, file.nameWithoutExtension, output)
                } else if (selectedFile.endsWith(".bat") || selectedFile.endsWith(".msi")) {
                    addGameToList(selectedFile, file.nameWithoutExtension, "")
                } else {
                    Toast.makeText(requireContext(), getString(R.string.incompatible_selected_file), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.createLnk -> {
                exportLnkAction(selectedFile)
            }
            R.id.executeExe -> {
                val fileExtension = file.extension.lowercase()
                var exeFile: File? = null

                if (fileExtension == "lnk") {
                    try {
                        val drive = DriveUtils.parseWindowsPath(ShellLink(file).resolveTarget())
                        if (drive == null || !File(drive.getUnixPath()).exists()) {
                            Toast.makeText(requireContext(), getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show()
                            return false
                        }

                        exeFile = File(drive.getUnixPath())
                    } catch(_: ShellLinkException) {
                        Toast.makeText(requireContext(), getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    exeFile = file
                }

                EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, exeFile!!).show(requireActivity().supportFragmentManager, "")
            }
            R.id.deleteFile -> {
                DeleteItemFragment(DELETE_GAME_ITEM, requireContext()).show(requireActivity().supportFragmentManager, "")
            }
            R.id.renameFile -> {
                RenameFragment(RENAME_FILE, File(selectedFile).name).show(requireActivity().supportFragmentManager, "")
            }
            R.id.installPackage -> {
                when (file.extension.lowercase()) {
                    "rat" -> {
                        AskInstallPackageFragment(RAT_PACKAGE).show(requireActivity().supportFragmentManager, "")
                    }
                    "mwp" -> {
                        AskInstallPackageFragment(MWP_PRESET_PACKAGE).show(requireActivity().supportFragmentManager, "")
                    }
                    "zip" -> {
                        AskInstallPackageFragment(ADTOOLS_DRIVER_PACKAGE).show(requireActivity().supportFragmentManager, "")
                    }
                }
            }
        }

        return super.onContextItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun exportLnkAction(exePath: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-ms-shortcut"
            putExtra(Intent.EXTRA_TITLE, "${File(exePath).nameWithoutExtension}.lnk")
        }

        startActivityForResult(intent, EXPORT_LNK_ACTION)
    }

    companion object {
        private var recyclerView: RecyclerView? = null
        private val fileList: MutableList<AdapterFiles.FileList> = ArrayList()
        private var _fragmentInstance: FileManagerFragment? = null
        private const val ANIMATION_DURATION = 300L

        var fragmentInstance
            get() = _fragmentInstance
            set(value) {
                _fragmentInstance = value
            }

        @SuppressLint("SetTextI18n")
        fun refreshFiles() {
            var currentWorkingDir = (fileManagerCwd!!.substringAfter("$wineDisksFolder") + "/")
            if (currentWorkingDir.length > 1) currentWorkingDir = currentWorkingDir.substring(1)
            fragmentInstance?.currentFolderText?.text = currentWorkingDir.replaceFirstChar { it.uppercase() }

            if (recyclerView?.tag as? Boolean == true) return

            recyclerView?.tag = true
            recyclerView!!.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION / 2)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onAnimationEnd(animation: Animator) {
                        fragmentInstance?.lifecycleScope?.launch {
                            val newFileList = withContext(Dispatchers.IO) {
                                val filesList = mutableListOf<AdapterFiles.FileList>()
                                if (fileManagerCwd != fileManagerDefaultDir) {
                                    filesList.add(AdapterFiles.FileList(File("..")))
                                }

                                File(fileManagerCwd!!).listFiles()
                                    ?.sorted()
                                    ?.filter { it.isDirectory }
                                    ?.forEach { filesList.add(AdapterFiles.FileList(it)) }
                                File(fileManagerCwd!!).listFiles()
                                    ?.sorted()
                                    ?.filter { it.isFile }
                                    ?.forEach { filesList.add(AdapterFiles.FileList(it)) }
                                filesList
                            }

                            withContext(Dispatchers.Main) {
                                recyclerView?.post {
                                    fileList.clear()
                                    fileList.addAll(newFileList)

                                    recyclerView?.adapter?.notifyDataSetChanged()
                                    recyclerView!!.animate()
                                        .alpha(1f)
                                        .setDuration(ANIMATION_DURATION / 2)
                                        .setInterpolator(AccelerateDecelerateInterpolator())
                                        .setListener(object : AnimatorListenerAdapter() {
                                            override fun onAnimationEnd(animation: Animator) {
                                                recyclerView?.tag = false
                                            }
                                        })
                                }
                            }
                        }
                    }
                })
        }

        fun deleteFile(filePath: String) {
            val index = fileList.indexOfFirst { it.file.path == filePath }

            if (File(filePath).delete()) {
                fileList.removeAt(index)

                recyclerView?.adapter?.notifyItemRemoved(index)
            }
        }

        fun renameFile(filePath: String, newFilePath: String) {
            val file = File(filePath)

            if (File(filePath).exists()) {
                file.renameTo(File(newFilePath))

                refreshFiles()
            }
        }
    }
}