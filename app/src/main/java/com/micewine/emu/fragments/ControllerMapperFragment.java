package com.micewine.emu.fragments;

import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_X_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_X_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_Y_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_Y_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_RZ_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_RZ_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_X_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_X_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Y_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Y_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Z_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Z_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_A_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_B_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_L1_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_L2_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_R1_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_R2_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_SELECT_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_START_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_THUMBL_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_THUMBR_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_X_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_Y_KEY;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getDeadZone;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getMouseSensibility;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.putDeadZone;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.putMouseSensibility;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterSettingsController;

import java.util.ArrayList;

public class ControllerMapperFragment extends Fragment {
    private RecyclerView recyclerView;
    private final ArrayList<AdapterSettingsController.SettingsController> settingsList = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_controller, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewSettingsModel);

        setAdapter();

        SeekBar seekBarDeadZone = rootView.findViewById(R.id.seekBarDeadZone);

        seekBarDeadZone.setMin(25);
        seekBarDeadZone.setMax(75);
        seekBarDeadZone.setProgress(getDeadZone(clickedPresetName));

        TextView seekBarDeadZoneValue = rootView.findViewById(R.id.seekBarDeadZoneValue);

        seekBarDeadZoneValue.setText(seekBarDeadZone.getProgress() + "%");

        seekBarDeadZone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarDeadZoneValue.setText(seekBarDeadZone.getProgress() + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                putDeadZone(clickedPresetName, seekBar.getProgress());
            }
        });

        SeekBar mouseSensibilitySeekBar = rootView.findViewById(R.id.mouseSensibilitySeekBar);

        mouseSensibilitySeekBar.setMin(25);
        mouseSensibilitySeekBar.setMax(350);
        mouseSensibilitySeekBar.setProgress(getMouseSensibility(clickedPresetName));

        TextView mouseSensibilityValue = rootView.findViewById(R.id.mouseSensibilityValue);

        mouseSensibilityValue.setText(mouseSensibilitySeekBar.getProgress() + "%");

        mouseSensibilitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mouseSensibilityValue.setText(mouseSensibilitySeekBar.getProgress() + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                putMouseSensibility(clickedPresetName, seekBar.getProgress());
            }
        });

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterSettingsController(settingsList, requireContext()));

        settingsList.clear();

        addToAdapter(R.drawable.a_button, BUTTON_A_KEY);
        addToAdapter(R.drawable.x_button, BUTTON_X_KEY);
        addToAdapter(R.drawable.y_button, BUTTON_Y_KEY);
        addToAdapter(R.drawable.b_button, BUTTON_B_KEY);
        addToAdapter(R.drawable.rb_button, BUTTON_R1_KEY);
        addToAdapter(R.drawable.rt_button, BUTTON_R2_KEY);
        addToAdapter(R.drawable.lb_button, BUTTON_L1_KEY);
        addToAdapter(R.drawable.lt_button, BUTTON_L2_KEY);
        addToAdapter(R.drawable.select_button, BUTTON_START_KEY);
        addToAdapter(R.drawable.start_button, BUTTON_SELECT_KEY);
        addToAdapter(R.drawable.l_thumb, BUTTON_THUMBL_KEY);
        addToAdapter(R.drawable.r_thumb, BUTTON_THUMBR_KEY);
        addToAdapter(R.drawable.l_up, AXIS_Y_MINUS_KEY);
        addToAdapter(R.drawable.l_left, AXIS_X_MINUS_KEY);
        addToAdapter(R.drawable.l_down, AXIS_Y_PLUS_KEY);
        addToAdapter(R.drawable.l_right, AXIS_X_PLUS_KEY);
        addToAdapter(R.drawable.r_up, AXIS_RZ_MINUS_KEY);
        addToAdapter(R.drawable.r_left, AXIS_Z_MINUS_KEY);
        addToAdapter(R.drawable.r_down, AXIS_RZ_PLUS_KEY);
        addToAdapter(R.drawable.r_right, AXIS_Z_PLUS_KEY);
        addToAdapter(R.drawable.dpad_up, AXIS_HAT_Y_MINUS_KEY);
        addToAdapter(R.drawable.dpad_left, AXIS_HAT_X_MINUS_KEY);
        addToAdapter(R.drawable.dpad_down, AXIS_HAT_Y_PLUS_KEY);
        addToAdapter(R.drawable.dpad_right, AXIS_HAT_X_PLUS_KEY);
    }

    private void addToAdapter(int iconId, String keyId) {
        settingsList.add(
                new AdapterSettingsController.SettingsController(iconId, keyId)
        );
    }
}