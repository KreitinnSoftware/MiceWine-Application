package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_BOX64
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_VULKAN_DRIVER
import com.micewine.emu.activities.MainActivity.Companion.ACTION_RUN_WINE
import com.micewine.emu.activities.MainActivity.Companion.ACTION_SELECT_EXE_PATH
import com.micewine.emu.activities.MainActivity.Companion.ACTION_SELECT_ICON
import com.micewine.emu.activities.MainActivity.Companion.getNativeResolutions
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.MainActivity.Companion.resolutions16_9
import com.micewine.emu.activities.MainActivity.Companion.resolutions4_3
import com.micewine.emu.activities.MainActivity.Companion.selectedCpuAffinity
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.activities.MainActivity.Companion.wineDisksFolder
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers
import com.micewine.emu.core.RatPackageManager.getPackageNameVersionById
import com.micewine.emu.core.RatPackageManager.listRatPackages
import com.micewine.emu.core.RatPackageManager.listRatPackagesId
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Presets
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getControllerPresets
import com.micewine.emu.fragments.ControllerSettingsFragment.Companion.ACTION_UPDATE_CONTROLLERS_STATUS
import com.micewine.emu.fragments.DebugSettingsFragment.Companion.availableCPUs
import com.micewine.emu.fragments.ShortcutsFragment.Companion.ADRENO_TOOLS_DRIVER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.MESA_DRIVER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Preset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Version
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerXInputSwapAnalogs
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getCpuAffinity
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getD3DXRenderer
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDXVKVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDisplaySettings
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getExePath
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getGameExeArguments
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getGameIcon
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getSelectedVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVKD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVirtualControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVulkanDriver
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineESync
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineServices
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineVirtualDesktop
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putBox64Preset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putBox64Version
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putCpuAffinity
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putD3DXRenderer
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putDXVKVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putDisplaySettings
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putExeArguments
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putSelectedVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putVKD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putVirtualControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putVulkanDriver
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineESync
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineServices
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putWineVirtualDesktop
import com.micewine.emu.fragments.ShortcutsFragment.Companion.setGameName
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getVirtualControllerPresets
import java.io.File

class EditGamePreferencesFragment(private val type: Int, private val exePath: File? = null) : DialogFragment() {
    private lateinit var imageView: ImageView
    private lateinit var exePathText: TextView
    private lateinit var selectExePath: ImageButton
    private lateinit var editTextNewName: EditText
    private lateinit var editTextArguments: EditText
    private lateinit var buttonContinue: Button
    private lateinit var buttonCancel: Button
    private lateinit var selectedDisplayModeSpinner: Spinner
    private lateinit var selectedDisplayResolutionSpinner: Spinner
    private lateinit var selectedDriverSpinner: Spinner
    private lateinit var selectedD3DXRendererSpinner: Spinner
    private lateinit var selectedDXVKSpinner: Spinner
    private lateinit var selectedWineD3DSpinner: Spinner
    private lateinit var selectedVKD3DSpinner: Spinner
    private lateinit var wineESyncSwitch: MaterialSwitch
    private lateinit var wineServicesSwitch: MaterialSwitch
    private lateinit var enableWineVirtualDesktopSwitch: MaterialSwitch
    private lateinit var cpuAffinitySpinner: Spinner
    private lateinit var selectedBox64Spinner: Spinner
    private lateinit var selectedBox64ProfileSpinner: Spinner
    private lateinit var controllersMappingTypeTexts: List<TextView>
    private lateinit var controllersMappingTypeSpinners: List<Spinner>
    private lateinit var controllersSwapAnalogsTexts: List<TextView>
    private lateinit var controllersSwapAnalogsSwitches: List<MaterialSwitch>
    private lateinit var controllersKeyboardPresetSpinners: List<Spinner>
    private lateinit var controllersKeyboardPresetTexts: List<TextView>
    private lateinit var controllersNamesTexts: List<TextView>
    private lateinit var onScreenControllerMappingTypeText: TextView
    private lateinit var onScreenControllerMappingTypeSpinner: Spinner
    private lateinit var onScreenControllerKeyboardPresetText: TextView
    private lateinit var onScreenControllerKeyboardPresetSpinner: Spinner
    private val mappingTypes = listOf("MiceWine Controller", "Keyboard/Mouse")
    private val controllerProfilesNames: List<String> = getControllerPresets().map { it.name }
    private val virtualControllerProfilesNames: List<String> = getVirtualControllerPresets().map { it.name }
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_UPDATE_CONTROLLERS_STATUS -> {
                    requireActivity().runOnUiThread {
                        updateControllersStatus()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateControllersStatus() {
        controllersNamesTexts.forEach { it.visibility = View.VISIBLE }
        controllersMappingTypeTexts.forEach { it.visibility = View.VISIBLE }
        controllersMappingTypeSpinners.forEach { it.visibility = View.VISIBLE }
        controllersKeyboardPresetSpinners.forEach { it.visibility = View.VISIBLE }
        controllersKeyboardPresetTexts.forEach { it.visibility = View.VISIBLE }
        controllersSwapAnalogsTexts.forEach { it.visibility = View.VISIBLE }
        controllersSwapAnalogsSwitches.forEach { it.visibility = View.VISIBLE }

        for (i in connectedPhysicalControllers.size..3) {
            controllersNamesTexts[i].visibility = View.GONE
            controllersMappingTypeTexts[i].visibility = View.GONE
            controllersMappingTypeSpinners[i].visibility = View.GONE
            controllersKeyboardPresetSpinners[i].visibility = View.GONE
            controllersKeyboardPresetTexts[i].visibility = View.GONE
            controllersSwapAnalogsTexts[i].visibility = View.GONE
            controllersSwapAnalogsSwitches[i].visibility = View.GONE
        }

        if (connectedPhysicalControllers.size == 0) {
            controllersMappingTypeTexts[0].visibility = View.VISIBLE
            controllersMappingTypeTexts[0].text = requireContext().getString(R.string.no_controllers_connected)
        }

        controllersNamesTexts.forEachIndexed { index, it ->
            if (index < connectedPhysicalControllers.size) {
                it.text = "$index: ${connectedPhysicalControllers[index].name}"
            }
        }
        controllersMappingTypeSpinners.forEachIndexed { index, it ->
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes)
            it.setSelection(
                if (getControllerXInput(selectedGameName, index)) 0 else 1
            )
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val isXInput = p2 == 0
                    controllersKeyboardPresetSpinners[index].visibility = if (isXInput) View.GONE else View.VISIBLE
                    controllersKeyboardPresetTexts[index].visibility = if (isXInput) View.GONE else View.VISIBLE

                    controllersSwapAnalogsSwitches[index].visibility = if (isXInput) View.VISIBLE else View.GONE
                    controllersSwapAnalogsTexts[index].visibility = if (isXInput) View.VISIBLE else View.GONE

                    temporarySettings.controllerXInput[index] = isXInput
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
        controllersSwapAnalogsSwitches.forEachIndexed { index, sw ->
            sw.isChecked = getControllerXInputSwapAnalogs(selectedGameName, index)
            sw.setOnClickListener {
                temporarySettings.controllerSwapAnalogs[index] = sw.isChecked
            }
        }
        controllersKeyboardPresetSpinners.forEachIndexed { index, it ->
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllerProfilesNames)
            it.setSelection(controllerProfilesNames.indexOf(getControllerPreset(selectedGameName, index)))
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    temporarySettings.controllerPreset[index] = p0?.selectedItem.toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_edit_game_preferences, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        selectExePath = view.findViewById(R.id.selectExePath)
        editTextNewName = view.findViewById(R.id.editTextNewName)
        editTextArguments = view.findViewById(R.id.appArgumentsEditText)
        buttonContinue = view.findViewById(R.id.buttonContinue)
        buttonCancel = view.findViewById(R.id.buttonCancel)
        selectedDisplayModeSpinner = view.findViewById(R.id.selectedDisplayMode)
        selectedDisplayResolutionSpinner = view.findViewById(R.id.selectedDisplayResolution)
        selectedDriverSpinner = view.findViewById(R.id.selectedDriver)
        selectedD3DXRendererSpinner = view.findViewById(R.id.selectedD3DXRenderer)
        selectedDXVKSpinner = view.findViewById(R.id.selectedDXVK)
        selectedWineD3DSpinner = view.findViewById(R.id.selectedWineD3D)
        selectedVKD3DSpinner = view.findViewById(R.id.selectedVKD3D)
        wineESyncSwitch = view.findViewById(R.id.wineESync)
        wineServicesSwitch = view.findViewById(R.id.wineServices)
        enableWineVirtualDesktopSwitch = view.findViewById(R.id.enableWineVirtualDesktop)
        cpuAffinitySpinner = view.findViewById(R.id.cpuAffinity)

        controllersMappingTypeTexts = listOf(
            view.findViewById(R.id.controller0MappingTypeText),
            view.findViewById(R.id.controller1MappingTypeText),
            view.findViewById(R.id.controller2MappingTypeText),
            view.findViewById(R.id.controller3MappingTypeText)
        )
        controllersMappingTypeSpinners = listOf(
            view.findViewById(R.id.controller0MappingTypeSpinner),
            view.findViewById(R.id.controller1MappingTypeSpinner),
            view.findViewById(R.id.controller2MappingTypeSpinner),
            view.findViewById(R.id.controller3MappingTypeSpinner)
        )
        controllersSwapAnalogsTexts = listOf(
            view.findViewById(R.id.controller0SwapAnalogsText),
            view.findViewById(R.id.controller1SwapAnalogsText),
            view.findViewById(R.id.controller2SwapAnalogsText),
            view.findViewById(R.id.controller3SwapAnalogsText)
        )
        controllersSwapAnalogsSwitches = listOf(
            view.findViewById(R.id.controller0SwapAnalogsSwitch),
            view.findViewById(R.id.controller1SwapAnalogsSwitch),
            view.findViewById(R.id.controller2SwapAnalogsSwitch),
            view.findViewById(R.id.controller3SwapAnalogsSwitch)
        )
        controllersKeyboardPresetSpinners = listOf(
            view.findViewById(R.id.controller0KeyboardPresetSpinner),
            view.findViewById(R.id.controller1KeyboardPresetSpinner),
            view.findViewById(R.id.controller2KeyboardPresetSpinner),
            view.findViewById(R.id.controller3KeyboardPresetSpinner)
        )
        controllersKeyboardPresetTexts = listOf(
            view.findViewById(R.id.controller0KeyboardPresetText),
            view.findViewById(R.id.controller1KeyboardPresetText),
            view.findViewById(R.id.controller2KeyboardPresetText),
            view.findViewById(R.id.controller3KeyboardPresetText)
        )
        controllersNamesTexts = listOf(
            view.findViewById(R.id.controller0Name),
            view.findViewById(R.id.controller1Name),
            view.findViewById(R.id.controller2Name),
            view.findViewById(R.id.controller3Name)
        )

        updateControllersStatus()

        onScreenControllerMappingTypeText = view.findViewById(R.id.onScreenControllerMappingTypeText)
        onScreenControllerMappingTypeSpinner = view.findViewById(R.id.onScreenControllerMappingTypeSpinner)
        onScreenControllerKeyboardPresetText = view.findViewById(R.id.onScreenControllerKeyboardPresetText)
        onScreenControllerKeyboardPresetSpinner = view.findViewById(R.id.onScreenControllerKeyboardPresetSpinner)

        onScreenControllerMappingTypeSpinner.let {
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes)
            it.setSelection(
                if (getVirtualControllerXInput(selectedGameName)) 0 else 1
            )
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val isXInput = p2 == 0
                    onScreenControllerKeyboardPresetSpinner.visibility = if (isXInput) View.GONE else View.VISIBLE
                    onScreenControllerKeyboardPresetText.visibility = if (isXInput) View.GONE else View.VISIBLE

                    temporarySettings.virtualXInputController = isXInput
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
        onScreenControllerKeyboardPresetSpinner.let {
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, virtualControllerProfilesNames)
            it.setSelection(virtualControllerProfilesNames.indexOf(getSelectedVirtualControllerPreset(selectedGameName)))
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    temporarySettings.virtualControllerPreset = virtualControllerProfilesNames[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }

        selectedBox64Spinner = view.findViewById(R.id.selectedBox64)
        selectedBox64ProfileSpinner = view.findViewById(R.id.selectedBox64Profile)

        imageView = view.findViewById(R.id.imageView)
        exePathText = view.findViewById(R.id.appExePath)

        when (type) {
            EDIT_GAME_PREFERENCES -> {
                getGameIcon(selectedGameName)?.let {
                    imageView.setImageBitmap(
                        resizeBitmap(
                            it, imageView.layoutParams.width, imageView.layoutParams.height
                        )
                    )
                }

                editTextNewName.setText(selectedGameName)
                editTextArguments.setText(getGameExeArguments(selectedGameName))
                exePathText.text = getExePath(selectedGameName).substringAfter("$wineDisksFolder/").replaceFirstChar { it.uppercase() }
                exePathText.isSelected = true

                if (selectedGameName == getString(R.string.desktop_mode_init)) {
                    editTextNewName.isEnabled = false
                    editTextArguments.isEnabled = false
                    enableWineVirtualDesktopSwitch.isEnabled = false
                    enableWineVirtualDesktopSwitch.isChecked = true

                    imageView.setImageBitmap(
                        resizeBitmap(
                            BitmapFactory.decodeResource(requireActivity().resources, R.drawable.default_icon), imageView.layoutParams.width, imageView.layoutParams.height
                        )
                    )
                }

                imageView.setOnClickListener {
                    requireActivity().sendBroadcast(
                        Intent(ACTION_SELECT_ICON)
                    )
                }

                selectExePath.setOnClickListener {
                    requireActivity().sendBroadcast(
                        Intent(ACTION_SELECT_EXE_PATH)
                    )
                }
            }
            FILE_MANAGER_START_PREFERENCES -> {
                val iconFile = File("$usrDir/icons/${exePath?.nameWithoutExtension}-icon")
                if (iconFile.exists() && iconFile.length() > 0) {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(iconFile.path))
                } else {
                    imageView.setImageResource(R.drawable.ic_log)
                }
                editTextNewName.setText(exePath?.nameWithoutExtension)
                selectExePath.visibility = View.GONE
                editTextNewName.isEnabled = false
                editTextArguments.setText("")
                exePathText.text = exePath!!.path.substringAfter("$wineDisksFolder/").replaceFirstChar { it.uppercase() }
                exePathText.isSelected = true

                selectedGameName = ""
            }
        }

        val box64Versions: List<String> = listRatPackages("Box64-").map { it.name + " " + it.version }.toMutableList().apply { add(0, "Global: ${getPackageNameVersionById(preferences?.getString(SELECTED_BOX64, ""))}") }
        val box64VersionsId: List<String> = listRatPackagesId("Box64-").toMutableList().apply { add(0, "Global") }
        val box64ProfilesNames: List<String> = getBox64Presets().map { it.name }

        selectedDisplayModeSpinner.let {
            val displaySettings = getDisplaySettings(selectedGameName)

            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, aspectRatios)
            it.setSelection(aspectRatios.indexOf(displaySettings[0]))
            it.onItemSelectedListener = object : OnItemSelectedListener {
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

                    temporarySettings.displayMode = selectedItem
                    temporarySettings.displayResolution = resolutionList.first()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        selectedDisplayResolutionSpinner.let {
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()

                    temporarySettings.displayResolution = selectedItem
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        val vulkanDriversId = listRatPackagesId("VulkanDriver", "AdrenoToolsDriver").toMutableList().apply { add(0, "Global") }
        val vulkanDrivers = listRatPackages("VulkanDriver", "AdrenoToolsDriver").map { it.name + " " + it.version }.toMutableList().apply { add(0, "Global: ${getPackageNameVersionById(
            preferences?.getString(SELECTED_VULKAN_DRIVER, "")
        )}") }

        selectedDriverSpinner.let {
            val index = vulkanDriversId.indexOf(getVulkanDriver(selectedGameName)).coerceAtLeast(0)
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, vulkanDrivers)
            it.setSelection(index)
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    temporarySettings.vulkanDriver = vulkanDriversId[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        selectedD3DXRendererSpinner.let {
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, d3dxRenderers)
            it.setSelection(d3dxRenderers.indexOf(getD3DXRenderer(selectedGameName)))
            it.onItemSelectedListener = object : OnItemSelectedListener {
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

                    temporarySettings.d3dxRenderer = selectedItem
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        selectedDXVKSpinner.let { sp ->
            val dxvkVersions = listRatPackages("DXVK").map { it.name + " " + it.version }
            val dxvkVersionsId = listRatPackagesId("DXVK")
            val index = dxvkVersionsId.indexOf(getDXVKVersion(selectedGameName)).coerceAtLeast(0)
            sp.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dxvkVersions)
            sp.setSelection(index)
            sp.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    temporarySettings.dxvk = dxvkVersionsId[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        selectedWineD3DSpinner.let { sp ->
            val wineD3DVersions = listRatPackages("WineD3D").map { it.name + " " + it.version }
            val wineD3DVersionsId = listRatPackagesId("WineD3D")
            val index = wineD3DVersionsId.indexOf(getWineD3DVersion(selectedGameName)).coerceAtLeast(0)
            sp.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, wineD3DVersions)
            sp.setSelection(index)
            sp.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    temporarySettings.wineD3D = wineD3DVersionsId[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        selectedVKD3DSpinner.let { sp ->
            val vkd3dVersions = listRatPackages("VKD3D").map { it.name + " " + it.version }
            val vkd3dVersionsId = listRatPackagesId("VKD3D")
            val index = vkd3dVersionsId.indexOf(getVKD3DVersion(selectedGameName)).coerceAtLeast(0)
            sp.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, vkd3dVersions)
            sp.setSelection(index)
            sp.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    temporarySettings.vkd3d = vkd3dVersionsId[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        wineESyncSwitch.let { sw ->
            sw.isChecked = getWineESync(selectedGameName)
            sw.setOnClickListener {
                temporarySettings.wineESync = sw.isChecked
            }
        }
        wineServicesSwitch.let { sw ->
            sw.isChecked = getWineServices(selectedGameName)
            sw.setOnClickListener {
                temporarySettings.wineServices = sw.isChecked
            }
        }

        if (selectedGameName != getString(R.string.desktop_mode_init)) {
            enableWineVirtualDesktopSwitch.apply {
                isChecked = getWineVirtualDesktop(selectedGameName)

                setOnClickListener {
                    temporarySettings.wineVirtualDesktop = isChecked
                }
            }
        }

        cpuAffinitySpinner.adapter = CPUAffinityAdapter(requireActivity(), availableCPUs, cpuAffinitySpinner, type)

        selectedBox64Spinner.let {
            val index = box64VersionsId.indexOf(getBox64Version(selectedGameName)).coerceAtLeast(0)
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, box64Versions)
            it.setSelection(index)
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    temporarySettings.box64Version = box64VersionsId[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        selectedBox64ProfileSpinner.let {
            val index = box64ProfilesNames.indexOf(getBox64Preset(selectedGameName)).coerceAtLeast(0)
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, box64ProfilesNames)
            it.setSelection(index)
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    temporarySettings.box64Preset = box64ProfilesNames[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        buttonContinue.setOnClickListener {
            when (type) {
                EDIT_GAME_PREFERENCES -> {
                    val newName = editTextNewName.text.toString()
                    val newArguments = editTextArguments.text.toString()

                    if (newName == "") {
                        return@setOnClickListener
                    }

                    temporarySettings.let {
                        putDisplaySettings(selectedGameName, it.displayMode, it.displayResolution)
                        putVulkanDriver(selectedGameName, it.vulkanDriver)
                        putD3DXRenderer(selectedGameName, it.d3dxRenderer)
                        putDXVKVersion(selectedGameName, it.dxvk)
                        putWineD3DVersion(selectedGameName, it.wineD3D)
                        putVKD3DVersion(selectedGameName, it.vkd3d)
                        putWineESync(selectedGameName, it.wineESync)
                        putWineServices(selectedGameName, it.wineServices)
                        putWineVirtualDesktop(selectedGameName, it.wineVirtualDesktop)
                        putCpuAffinity(selectedGameName, it.cpuAffinity)
                        putSelectedVirtualControllerPreset(selectedGameName, it.virtualControllerPreset)
                        putVirtualControllerXInput(selectedGameName, it.virtualXInputController)

                        putControllerXInput(selectedGameName, it.controllerXInput[0], 0)
                        putControllerXInput(selectedGameName, it.controllerXInput[1], 1)
                        putControllerXInput(selectedGameName, it.controllerXInput[2], 2)
                        putControllerXInput(selectedGameName, it.controllerXInput[3], 3)

                        putControllerPreset(selectedGameName, it.controllerPreset[0], 0)
                        putControllerPreset(selectedGameName, it.controllerPreset[1], 1)
                        putControllerPreset(selectedGameName, it.controllerPreset[2], 2)
                        putControllerPreset(selectedGameName, it.controllerPreset[3], 3)

                        putBox64Version(selectedGameName, it.box64Version)
                        putBox64Preset(selectedGameName, it.box64Preset)
                    }

                    putExeArguments(selectedGameName, newArguments)
                    setGameName(selectedGameName, newName)
                }
                FILE_MANAGER_START_PREFERENCES -> {
                    temporarySettings.let {
                        if (it.vulkanDriver == "Global") {
                            it.vulkanDriver = preferences?.getString(SELECTED_VULKAN_DRIVER, "").toString()
                        }
                        if (it.box64Version == "Global") {
                            it.box64Version = preferences?.getString(SELECTED_BOX64, "").toString()
                        }
                        val driverType = if (it.vulkanDriver.startsWith("AdrenoToolsDriver-")) ADRENO_TOOLS_DRIVER else MESA_DRIVER

                        val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                            putExtra("exePath", exePath?.path)
                            putExtra("exeArguments", editTextArguments.text.toString())
                            putExtra("driverName", it.vulkanDriver)
                            putExtra("driverType", driverType)
                            putExtra("box64Version", it.box64Version)
                            putExtra("box64Preset", it.box64Preset)
                            putExtra("displayResolution", it.displayResolution)
                            putExtra("virtualControllerPreset", it.virtualControllerPreset)
                            putExtra("d3dxRenderer", it.d3dxRenderer)
                            putExtra("wineD3D", it.wineD3D)
                            putExtra("dxvk", it.dxvk)
                            putExtra("vkd3d", it.vkd3d)
                            putExtra("esync", it.wineESync)
                            putExtra("services", it.wineServices)
                            putExtra("virtualDesktop", it.wineVirtualDesktop)
                            putExtra("cpuAffinity", selectedCpuAffinity)
                        }

                        requireActivity().sendBroadcast(runWineIntent)
                        requireActivity().startActivity(
                            Intent(requireActivity(), EmulationActivity::class.java)
                        )
                    }
                }
            }

            dismiss()
        }

        requireActivity().registerReceiver(receiver, object : IntentFilter() {
            init {
                addAction(ACTION_UPDATE_CONTROLLERS_STATUS)
            }
        })

        buttonCancel.setOnClickListener {
            dismiss()
        }

        parentFragmentManager.setFragmentResultListener("invalidate", this) { _, _ ->
            exePathText.post {
                exePathText.text = getExePath(selectedGameName).substringAfter("$wineDisksFolder/").replaceFirstChar { it.uppercase() }
            }
        }

        return dialog
    }

    override fun onResume() {
        super.onResume()

        getGameIcon(selectedGameName)?.let {
            imageView.setImageBitmap(
                resizeBitmap(
                    it, imageView.layoutParams.width, imageView.layoutParams.height
                )
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().unregisterReceiver(receiver)
    }

    private fun resizeBitmap(originalBitmap: Bitmap, width: Int, height: Int): Bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

    companion object {
        const val EDIT_GAME_PREFERENCES = 0
        const val FILE_MANAGER_START_PREFERENCES = 1

        val aspectRatios = listOf("16:9", "4:3", "Native")
        val d3dxRenderers = listOf("DXVK", "WineD3D")
        val temporarySettings = TemporarySettings()

        class TemporarySettings(
            var displayMode: String = getDisplaySettings(selectedGameName)[0],
            var displayResolution: String = getDisplaySettings(selectedGameName)[1],
            var vulkanDriver: String = getVulkanDriver(selectedGameName),
            var d3dxRenderer: String = getD3DXRenderer(selectedGameName),
            var dxvk: String = getDXVKVersion(selectedGameName),
            var wineD3D: String = getWineD3DVersion(selectedGameName),
            var vkd3d: String = getVKD3DVersion(selectedGameName),
            var wineESync: Boolean = getWineESync(selectedGameName),
            var wineServices: Boolean = getWineServices(selectedGameName),
            var wineVirtualDesktop: Boolean = getWineVirtualDesktop(selectedGameName),
            var cpuAffinity: String = getCpuAffinity(selectedGameName),
            var virtualControllerPreset: String = getSelectedVirtualControllerPreset(selectedGameName),
            var virtualXInputController: Boolean = getVirtualControllerXInput(selectedGameName),
            var controllerXInput: BooleanArray = booleanArrayOf(
                getControllerXInput(selectedGameName, 0),
                getControllerXInput(selectedGameName, 1),
                getControllerXInput(selectedGameName, 2),
                getControllerXInput(selectedGameName, 3)
            ),
            var controllerPreset: MutableList<String> = mutableListOf(
                getControllerPreset(selectedGameName, 0),
                getControllerPreset(selectedGameName, 1),
                getControllerPreset(selectedGameName, 2),
                getControllerPreset(selectedGameName, 3)
            ),
            var controllerSwapAnalogs: BooleanArray = booleanArrayOf(
                getControllerXInputSwapAnalogs(selectedGameName, 0),
                getControllerXInputSwapAnalogs(selectedGameName, 1),
                getControllerXInputSwapAnalogs(selectedGameName, 2),
                getControllerXInputSwapAnalogs(selectedGameName, 3)
            ),
            var box64Version: String = getBox64Version(selectedGameName),
            var box64Preset: String = getBox64Preset(selectedGameName),
        )

        class CPUAffinityAdapter(
            val activity: Activity,
            private val arrayElements: Array<String>,
            private val spinner: Spinner,
            private val type: Int
        ) : SpinnerAdapter {
            val checked = BooleanArray(count)

            override fun registerDataSetObserver(p0: DataSetObserver?) {}
            override fun unregisterDataSetObserver(p0: DataSetObserver?) {}
            override fun getCount(): Int = arrayElements.count()
            override fun getItem(p0: Int): Any = arrayElements[p0]
            override fun getItemId(p0: Int): Long = p0.toLong()
            override fun hasStableIds(): Boolean = true
            override fun getItemViewType(p0: Int): Int = 0
            override fun getViewTypeCount(): Int = 1
            override fun isEmpty(): Boolean = arrayElements.isEmpty()
            override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
                val inflater = activity.layoutInflater
                val view = p1 ?: inflater.inflate(android.R.layout.simple_spinner_item, p2, false)
                view.findViewById<TextView>(android.R.id.text1).text = temporarySettings.cpuAffinity
                return view
            }
            override fun getDropDownView(p0: Int, p1: View?, p2: ViewGroup?): View {
                val inflater = activity.layoutInflater
                val view = inflater.inflate(R.layout.item_checkbox, p2, false)
                val checkBox = view.findViewById<CheckBox>(R.id.checkbox)

                checked[p0] = temporarySettings.cpuAffinity.contains(arrayElements[p0]) == true

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
                        temporarySettings.cpuAffinity = builder.toString()
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