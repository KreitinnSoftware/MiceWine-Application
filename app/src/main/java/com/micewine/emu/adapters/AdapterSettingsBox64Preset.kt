package com.micewine.emu.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_AVX
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_BIGBLOCK
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_CALLRET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DF
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DIRTY
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTNAN
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTROUND
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FORWARD
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_NATIVEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_PAUSE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_SAFEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_STRONGMEM
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WAIT
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WEAKBARRIER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_X87DOUBLE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_MMAP32
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SSE42
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SWITCH
import com.micewine.emu.adapters.AdapterPreset.Companion.clickedPresetName
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64AlignedAtomics
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Avx
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64BigBlock
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64CallRet
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64DF
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Dirty
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64FastNan
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64FastRound
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Forward
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64MMap32
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64NativeFlags
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Pause
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64SafeFlags
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Sse2
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64StrongMem
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Wait
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64WeakBarrier
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64X87Double
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64AlignedAtomics
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64Avx
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64BigBlock
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64CallRet
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64DF
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64Dirty
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64FastNan
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64FastRound
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64Forward
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64MMap32
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64NativeFlags
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64Pause
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64SafeFlags
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64Sse2
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64StrongMem
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64Wait
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64WeakBarrier
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.putBox64X87Double
import com.micewine.emu.fragments.InfoDialogFragment

class AdapterSettingsBox64Preset(
    private val settingsList: MutableList<Box64ListSpinner>,
    private val activity: FragmentActivity
) :
    RecyclerView.Adapter<AdapterSettingsBox64Preset.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_settings_preferences_item, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]
        holder.settingsName.setText(sList.titleSettings)
        holder.settingsDescription.setText(sList.descriptionSettings)

        if (activity.getString(sList.descriptionSettings) == " ") {
            holder.settingsDescription.visibility = View.GONE
        }

        when (sList.type) {
            SWITCH -> {
                holder.spinnerOptions.visibility = View.GONE
                holder.settingsSwitch.visibility = View.VISIBLE
                holder.seekBar.visibility = View.GONE
                holder.seekBarValue.visibility = View.GONE

                val element = when (sList.key) {
                    BOX64_DYNAREC_FASTNAN -> getBox64FastNan(clickedPresetName)
                    BOX64_DYNAREC_FASTROUND -> getBox64FastRound(clickedPresetName)
                    BOX64_DYNAREC_ALIGNED_ATOMICS -> getBox64AlignedAtomics(clickedPresetName)
                    BOX64_DYNAREC_NATIVEFLAGS -> getBox64NativeFlags(clickedPresetName)
                    BOX64_DYNAREC_WAIT -> getBox64Wait(clickedPresetName)
                    BOX64_SSE42 -> getBox64Sse2(clickedPresetName)
                    BOX64_MMAP32 -> getBox64MMap32(clickedPresetName)
                    BOX64_DYNAREC_DF -> getBox64DF(clickedPresetName)
                    else -> false
                }

                holder.settingsSwitch.isChecked = element
                holder.settingsSwitch.setOnClickListener {
                    when (sList.key) {
                        BOX64_DYNAREC_FASTNAN -> putBox64FastNan(clickedPresetName, holder.settingsSwitch.isChecked)
                        BOX64_DYNAREC_FASTROUND -> putBox64FastRound(clickedPresetName, holder.settingsSwitch.isChecked)
                        BOX64_DYNAREC_ALIGNED_ATOMICS -> putBox64AlignedAtomics(clickedPresetName, holder.settingsSwitch.isChecked)
                        BOX64_DYNAREC_NATIVEFLAGS -> putBox64NativeFlags(clickedPresetName, holder.settingsSwitch.isChecked)
                        BOX64_DYNAREC_WAIT -> putBox64Wait(clickedPresetName, holder.settingsSwitch.isChecked)
                        BOX64_SSE42 -> putBox64Sse2(clickedPresetName, holder.settingsSwitch.isChecked)
                        BOX64_MMAP32 -> putBox64MMap32(clickedPresetName, holder.settingsSwitch.isChecked)
                        BOX64_DYNAREC_DF -> putBox64DF(clickedPresetName, holder.settingsSwitch.isChecked)
                    }
                }
            }
            SPINNER -> {
                holder.settingsSwitch.visibility = View.GONE
                holder.spinnerOptions.visibility = View.VISIBLE
                holder.seekBar.visibility = View.GONE
                holder.seekBarValue.visibility = View.GONE

                holder.spinnerOptions.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, sList.spinnerOptions!!)

                val element = when (sList.key) {
                    BOX64_DYNAREC_BIGBLOCK -> getBox64BigBlock(clickedPresetName)
                    BOX64_DYNAREC_STRONGMEM -> getBox64StrongMem(clickedPresetName)
                    BOX64_DYNAREC_WEAKBARRIER -> getBox64WeakBarrier(clickedPresetName)
                    BOX64_DYNAREC_PAUSE -> getBox64Pause(clickedPresetName)
                    BOX64_DYNAREC_X87DOUBLE -> getBox64X87Double(clickedPresetName)
                    BOX64_DYNAREC_SAFEFLAGS -> getBox64SafeFlags(clickedPresetName)
                    BOX64_DYNAREC_CALLRET -> getBox64CallRet(clickedPresetName)
                    BOX64_DYNAREC_DIRTY -> getBox64Dirty(clickedPresetName)
                    BOX64_DYNAREC_FORWARD -> getBox64Forward(clickedPresetName)
                    BOX64_AVX -> getBox64Avx(clickedPresetName)
                    else -> ""
                }.toString()

                holder.spinnerOptions.setSelection(sList.spinnerOptions!!.indexOf(element))

                holder.spinnerOptions.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val selectedItem = parent?.getItemAtPosition(position).toString()

                            when (sList.key) {
                                BOX64_DYNAREC_BIGBLOCK -> putBox64BigBlock(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_STRONGMEM -> putBox64StrongMem(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_WEAKBARRIER -> putBox64WeakBarrier(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_PAUSE -> putBox64Pause(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_X87DOUBLE -> putBox64X87Double(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_SAFEFLAGS -> putBox64SafeFlags(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_CALLRET -> putBox64CallRet(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_DIRTY -> putBox64Dirty(clickedPresetName, selectedItem.toInt())
                                BOX64_DYNAREC_FORWARD -> putBox64Forward(clickedPresetName, selectedItem.toInt())
                                BOX64_AVX -> putBox64Avx(clickedPresetName, selectedItem.toInt())
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val settingsName: TextView = itemView.findViewById(R.id.title_preferences_model)
        val settingsDescription: TextView = itemView.findViewById(R.id.description_preferences_model)
        val spinnerOptions: Spinner = itemView.findViewById(R.id.keyBindSpinner)
        val settingsSwitch: SwitchCompat = itemView.findViewById(R.id.optionSwitch)
        val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
        val seekBarValue: TextView = itemView.findViewById(R.id.seekBarValue)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val settingsModel = settingsList[getAdapterPosition()]

            InfoDialogFragment(
                settingsName.text.toString(),
                activity.getString(settingsModel.descriptionSettings)
            ).show(activity.supportFragmentManager, "")
        }
    }

    class Box64ListSpinner(
        var titleSettings: Int,
        var descriptionSettings: Int,
        var spinnerOptions: Array<String>?,
        var type: Int,
        var key: String
    )
}