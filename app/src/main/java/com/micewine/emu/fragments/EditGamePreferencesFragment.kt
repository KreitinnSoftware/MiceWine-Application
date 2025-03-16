package com.micewine.emu.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.database.DataSetObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity
import com.micewine.emu.activities.MainActivity.Companion.ACTION_RUN_WINE
import com.micewine.emu.activities.MainActivity.Companion.ACTION_SELECT_ICON
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.selectedCpuAffinity
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Presets
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getControllerPresets
import com.micewine.emu.fragments.DisplaySettingsFragment.Companion.getNativeResolutions
import com.micewine.emu.fragments.DisplaySettingsFragment.Companion.resolutions16_9
import com.micewine.emu.fragments.DisplaySettingsFragment.Companion.resolutions4_3
import com.micewine.emu.fragments.ShortcutsFragment.Companion.editGameFromList
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Preset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getCpuAffinity
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getD3DXRenderer
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDXVKVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDisplaySettings
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getEnableXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getGameExeArguments
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getGameIcon
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVKD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVulkanDriver
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineESync
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineServices
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineVirtualDesktop
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putBox64Preset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putCpuAffinity
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putD3DXRenderer
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putDXVKVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putDisplaySettings
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putEnableXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putVKD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putVulkanDriver
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineESync
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineServices
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineVirtualDesktop
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getVirtualControllerPresets
import com.micewine.emu.fragments.WineSettingsFragment.Companion.availableCPUs
import java.io.File

class EditGamePreferencesFragment(private val type: Int, private val exePath: File? = null) : DialogFragment() {
    private var preferences: SharedPreferences? = null
    private var imageView: ImageView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_game_preferences, null)

        val editTextNewName = view.findViewById<EditText>(R.id.editTextNewName)
        val editTextArguments = view.findViewById<EditText>(R.id.appArgumentsEditText)
        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)
        val selectedDisplayModeSpinner = view.findViewById<Spinner>(R.id.selectedDisplayMode)
        val selectedDisplayResolutionSpinner = view.findViewById<Spinner>(R.id.selectedDisplayResolution)
        val selectedDriverSpinner = view.findViewById<Spinner>(R.id.selectedDriver)
        val selectedD3DXRendererSpinner = view.findViewById<Spinner>(R.id.selectedD3DXRenderer)
        val selectedDXVKSpinner = view.findViewById<Spinner>(R.id.selectedDXVK)
        val selectedWineD3DSpinner = view.findViewById<Spinner>(R.id.selectedWineD3D)
        val selectedVKD3DSpinner = view.findViewById<Spinner>(R.id.selectedVKD3D)
        val wineESyncSwitch = view.findViewById<MaterialSwitch>(R.id.wineESync)
        val wineServicesSwitch = view.findViewById<MaterialSwitch>(R.id.wineServices)
        val enableWineVirtualDesktopSwitch = view.findViewById<MaterialSwitch>(R.id.enableWineVirtualDesktop)
        val enableXInputSwitch = view.findViewById<MaterialSwitch>(R.id.enableXInput)
        val cpuAffinitySpinner = view.findViewById<Spinner>(R.id.cpuAffinity)
        val selectedControllerProfileSpinner = view.findViewById<Spinner>(R.id.selectedControllerProfile)
        val selectedVirtualControllerProfileSpinner = view.findViewById<Spinner>(R.id.selectedVirtualControllerProfile)
        val selectedBox64ProfileSpinner = view.findViewById<Spinner>(R.id.selectedBox64Profile)

        imageView = view.findViewById(R.id.imageView)

        if (type == EDIT_GAME_PREFERENCES) {
            val imageBitmap = getGameIcon(selectedGameName)

            if (imageBitmap != null) {
                imageView?.setImageBitmap(
                    resizeBitmap(
                        imageBitmap, imageView?.layoutParams?.width!!, imageView?.layoutParams?.height!!
                    )
                )
            }
        } else if (type == FILE_MANAGER_START_PREFERENCES) {
            val iconFile = File("$usrDir/icons/${exePath?.nameWithoutExtension}-icon")

            if (iconFile.exists() && iconFile.length() > 0) {
                imageView?.setImageBitmap(BitmapFactory.decodeFile(iconFile.path))
            } else {
                imageView?.setImageResource(R.drawable.ic_log)
            }
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (type == EDIT_GAME_PREFERENCES) {
            editTextNewName.setText(selectedGameName)
            editTextArguments.setText(getGameExeArguments(selectedGameName))

            if (selectedGameName == getString(R.string.desktop_mode_init)) {
                editTextNewName.isEnabled = false
                editTextArguments.isEnabled = false
                enableWineVirtualDesktopSwitch.isEnabled = false
                enableWineVirtualDesktopSwitch.isChecked = true

                imageView?.setImageBitmap(
                    resizeBitmap(
                        BitmapFactory.decodeResource(requireActivity().resources, R.drawable.default_icon), imageView?.layoutParams?.width!!, imageView?.layoutParams?.height!!
                    )
                )
            }

            imageView?.setOnClickListener {
                requireActivity().sendBroadcast(
                    Intent(ACTION_SELECT_ICON)
                )
            }
        } else if (type == FILE_MANAGER_START_PREFERENCES) {
            editTextNewName.setText(exePath?.nameWithoutExtension)
            editTextNewName.isEnabled = false
            editTextArguments.setText("")

            selectedGameName = ""
        }

        val controllerProfilesNames: List<String> = getControllerPresets().map { it[0] }
        val virtualControllerProfilesNames: List<String> = getVirtualControllerPresets().map { it.name }
        val box64ProfilesNames: List<String> = getBox64Presets().map { it[0] }

        selectedDisplayModeSpinner.apply {
            val aspectRatios = listOf("16:9", "4:3", "Native")
            val displaySettings = getDisplaySettings(selectedGameName)

            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, aspectRatios)
            setSelection(aspectRatios.indexOf(displaySettings[0]))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    var resolutionList: Array<String>? = null

                    when (selectedItem) {
                        "16:9" -> {
                            resolutionList = resolutions16_9
                        }

                        "4:3" -> {
                            resolutionList = resolutions4_3
                        }

                        "Native" -> {
                            resolutionList = getNativeResolutions(requireActivity()).toTypedArray()
                        }
                    }

                    selectedDisplayResolutionSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, resolutionList!!)
                    selectedDisplayResolutionSpinner.setSelection(resolutionList.indexOf(displaySettings[1]))

                    putDisplaySettings(selectedGameName, selectedItem, getDisplaySettings(selectedGameName)[1])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        selectedDisplayResolutionSpinner.apply {
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()

                    putDisplaySettings(selectedGameName, getDisplaySettings(selectedGameName)[0], selectedItem)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        val vulkanDriversId = File("$appRootDir/packages").listFiles()?.filter { it.name.startsWith("VulkanDriver-") || it.name.startsWith("AdrenoToolsDriver-") }?.mapNotNull { it.name }!!

        selectedDriverSpinner.apply {
            val vulkanDrivers = File("$appRootDir/packages").listFiles()?.filter { it.name.startsWith("VulkanDriver-") || it.name.startsWith("AdrenoToolsDriver-") }?.mapNotNull { "${File(it.path + "/pkg-header").readLines()[0].substringAfter("=")} (${File(it.path + "/pkg-header").readLines()[2].substringAfter("=")}) ${if (it.name.startsWith("AdrenoToolsDriver-")) "(AdrenoTools)" else ""}" }!!
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, vulkanDrivers)
            val index = vulkanDriversId.indexOf(getVulkanDriver(selectedGameName))
            setSelection(if (index == -1) 0 else index)

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    putVulkanDriver(selectedGameName, vulkanDriversId[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        selectedD3DXRendererSpinner.apply {
            val d3dxRenderers = listOf("DXVK", "WineD3D")
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, d3dxRenderers)
            setSelection(d3dxRenderers.indexOf(getD3DXRenderer(selectedGameName)))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = d3dxRenderers[position]

                    when (selectedItem) {
                        "DXVK" -> {
                            selectedWineD3DSpinner.isEnabled = false
                            selectedDXVKSpinner.isEnabled = true
                        }

                        "WineD3D" -> {
                            selectedDXVKSpinner.isEnabled = false
                            selectedWineD3DSpinner.isEnabled = true
                        }
                    }

                    putD3DXRenderer(selectedGameName, selectedItem)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        selectedDXVKSpinner.apply {
            val dxvkVersions = File("$appRootDir/wine-utils/DXVK").listFiles()?.map { it.name }!!
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dxvkVersions)
            setSelection(dxvkVersions.indexOf(getDXVKVersion(selectedGameName)))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    putDXVKVersion(selectedGameName, dxvkVersions[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        selectedWineD3DSpinner.apply {
            val wineD3DVersions = File("$appRootDir/wine-utils/WineD3D").listFiles()?.map { it.name }!!
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, wineD3DVersions)
            setSelection(wineD3DVersions.indexOf(getWineD3DVersion(selectedGameName)))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    putWineD3DVersion(selectedGameName, wineD3DVersions[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        selectedVKD3DSpinner.apply {
            val vkd3dVersions = File("$appRootDir/wine-utils/VKD3D").listFiles()?.map { it.name }!!
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, vkd3dVersions)
            setSelection(vkd3dVersions.indexOf(getVKD3DVersion(selectedGameName)))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    putVKD3DVersion(selectedGameName, vkd3dVersions[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        wineESyncSwitch.apply {
            isChecked = getWineESync(selectedGameName)

            setOnClickListener {
                putWineESync(selectedGameName, isChecked)
            }
        }

        wineServicesSwitch.apply {
            isChecked = getWineServices(selectedGameName)

            setOnClickListener {
                putWineServices(selectedGameName, isChecked)
            }
        }

        if (selectedGameName != getString(R.string.desktop_mode_init)) {
            enableWineVirtualDesktopSwitch.apply {
                isChecked = getWineVirtualDesktop(selectedGameName)

                setOnClickListener {
                    putWineVirtualDesktop(selectedGameName, isChecked)
                }
            }
        }

        enableXInputSwitch.apply {
            isChecked = getEnableXInput(selectedGameName)

            setOnClickListener {
                putEnableXInput(selectedGameName, isChecked)
            }
        }

        cpuAffinitySpinner.apply {
            adapter = CPUAffinityAdapter(requireActivity(), availableCPUs, cpuAffinitySpinner, type)
        }

        selectedControllerProfileSpinner.apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllerProfilesNames)
            setSelection(controllerProfilesNames.indexOf(getControllerPreset(selectedGameName)))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    putControllerPreset(selectedGameName, controllerProfilesNames[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        selectedVirtualControllerProfileSpinner.apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, virtualControllerProfilesNames)
            setSelection(virtualControllerProfilesNames.indexOf(getVirtualControllerPreset(selectedGameName)))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    putVirtualControllerPreset(selectedGameName, virtualControllerProfilesNames[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        selectedBox64ProfileSpinner.apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, box64ProfilesNames)
            setSelection(box64ProfilesNames.indexOf(getBox64Preset(selectedGameName)))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    putBox64Preset(selectedGameName, box64ProfilesNames[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        buttonContinue.setOnClickListener {
            if (type == EDIT_GAME_PREFERENCES) {
                val newName = editTextNewName.text.toString()
                val newArguments = editTextArguments.text.toString()

                if (newName == "") {
                    return@setOnClickListener
                }

                editGameFromList(selectedGameName, newName, newArguments)
            } else if (type == FILE_MANAGER_START_PREFERENCES) {
                val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                    putExtra("exePath", exePath?.path)
                    putExtra("exeArguments", editTextArguments.text.toString())
                    putExtra("driverName", vulkanDriversId[selectedDriverSpinner.selectedItemPosition])
                    putExtra("box64Preset", selectedBox64ProfileSpinner.selectedItem.toString())
                    putExtra("displayResolution", selectedDisplayResolutionSpinner.selectedItem.toString())
                    putExtra("virtualControllerPreset", selectedVirtualControllerProfileSpinner.selectedItem.toString())
                    putExtra("controllerPreset", selectedControllerProfileSpinner.selectedItem.toString())
                    putExtra("d3dxRenderer", selectedD3DXRendererSpinner.selectedItem.toString())
                    putExtra("wineD3D", selectedWineD3DSpinner.selectedItem.toString())
                    putExtra("dxvk", selectedDXVKSpinner.selectedItem.toString())
                    putExtra("vkd3d", selectedVKD3DSpinner.selectedItem.toString())
                    putExtra("esync", wineESyncSwitch.isChecked)
                    putExtra("services", wineServicesSwitch.isChecked)
                    putExtra("virtualDesktop", enableWineVirtualDesktopSwitch.isChecked)
                    putExtra("xinput", enableXInputSwitch.isChecked)
                    putExtra("cpuAffinity", selectedCpuAffinity)
                }

                requireActivity().sendBroadcast(runWineIntent)
                requireActivity().startActivity(
                    Intent(requireActivity(), EmulationActivity::class.java)
                )
            }

            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    override fun onResume() {
        val imageBitmap = getGameIcon(selectedGameName)

        if (imageBitmap != null) {
            imageView?.setImageBitmap(
                resizeBitmap(
                    imageBitmap, imageView?.layoutParams?.width!!, imageView?.layoutParams?.height!!
                )
            )
        }

        super.onResume()
    }

    private fun resizeBitmap(originalBitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }

    companion object {
        const val EDIT_GAME_PREFERENCES = 0
        const val FILE_MANAGER_START_PREFERENCES = 1

        class CPUAffinityAdapter(
            val activity: Activity,
            private val arrayElements: Array<String>,
            private val spinner: Spinner,
            private val type: Int
        ) : SpinnerAdapter {
            val checked = BooleanArray(count)

            override fun registerDataSetObserver(p0: DataSetObserver?) {
            }

            override fun unregisterDataSetObserver(p0: DataSetObserver?) {
            }

            override fun getCount(): Int {
                return arrayElements.count()
            }

            override fun getItem(p0: Int): Any {
                return arrayElements[p0]
            }

            override fun getItemId(p0: Int): Long {
                return p0.toLong()
            }

            override fun hasStableIds(): Boolean {
                return true
            }

            override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
                val inflater = activity.layoutInflater
                val view = p1 ?: inflater.inflate(android.R.layout.simple_spinner_item, p2, false)

                view.findViewById<TextView>(android.R.id.text1).apply {
                    text = getCpuAffinity(selectedGameName)
                }

                return view
            }

            override fun getItemViewType(p0: Int): Int {
                return 0
            }

            override fun getViewTypeCount(): Int {
                return 1
            }

            override fun isEmpty(): Boolean {
                return arrayElements.isEmpty()
            }

            override fun getDropDownView(p0: Int, p1: View?, p2: ViewGroup?): View {
                val inflater = activity.layoutInflater
                val view = inflater.inflate(R.layout.item_checkbox, p2, false)
                val checkBox = view.findViewById<CheckBox>(R.id.checkbox)
                val cpuAffinity = getCpuAffinity(selectedGameName)

                checked[p0] = cpuAffinity.contains(arrayElements[p0]) == true

                checkBox.isChecked = checked[p0]
                checkBox.text = arrayElements[p0]

                checkBox.setOnClickListener {
                    checked[p0] = !checked[p0]

                    val builder: StringBuilder = StringBuilder()

                    for (i in checked.indices) {
                        if (checked[i]) {
                            builder.append(",")
                            builder.append(arrayElements[i])
                        }
                    }

                    if (builder.isNotEmpty()) {
                        builder.deleteCharAt(0)
                    }

                    if (type == EDIT_GAME_PREFERENCES) {
                        putCpuAffinity(selectedGameName, builder.toString())
                    } else if (type == FILE_MANAGER_START_PREFERENCES) {
                        selectedCpuAffinity = builder.toString()
                    }

                    spinner.adapter = this
                }

                return view
            }
        }
    }
}