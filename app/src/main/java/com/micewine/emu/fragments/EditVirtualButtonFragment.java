package com.micewine.emu.fragments;

import static com.micewine.emu.activities.VirtualControllerOverlayMapper.ACTION_INVALIDATE;
import static com.micewine.emu.controller.XKeyCodes.getKeyNames;
import static com.micewine.emu.controller.XKeyCodes.getMapping;
import static com.micewine.emu.views.VirtualKeyboardInputCreatorView.ANALOG;
import static com.micewine.emu.views.VirtualKeyboardInputCreatorView.BUTTON;
import static com.micewine.emu.views.VirtualKeyboardInputCreatorView.DPAD;
import static com.micewine.emu.views.VirtualKeyboardInputCreatorView.lastSelectedButton;
import static com.micewine.emu.views.VirtualKeyboardInputCreatorView.lastSelectedType;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_CIRCLE;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_RECTANGLE;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_SQUARE;
import static com.micewine.emu.views.VirtualKeyboardInputView.analogList;
import static com.micewine.emu.views.VirtualKeyboardInputView.buttonList;
import static com.micewine.emu.views.VirtualKeyboardInputView.dpadList;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.micewine.emu.R;

import java.util.List;

public class EditVirtualButtonFragment extends DialogFragment {
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_virtual_button, null);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        TextView radiusSeekbarValue = view.findViewById(R.id.radiusSeekbarValue);

        radiusSeekbarValue.setText(selectedButtonRadius + "%");

        SeekBar radiusSeekbar = view.findViewById(R.id.radiusSeekbar);

        radiusSeekbar.setMin(100);
        radiusSeekbar.setMax(400);
        radiusSeekbar.setProgress(selectedButtonRadius);
        radiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(seekBar.getProgress() / 5 * 5);
                radiusSeekbarValue.setText(seekBar.getProgress() + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        List<String> allKeyNames = getKeyNames(lastSelectedType != ANALOG);
        List<String> shapes = List.of("Circle", "Square", "Rectangle");

        Spinner shapeSpinner = view.findViewById(R.id.shapeSpinner);

        shapeSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, shapes));

        int shapeId = switch (selectedButtonShape) {
            case SHAPE_CIRCLE -> 0;
            case SHAPE_SQUARE -> 1;
            case SHAPE_RECTANGLE -> 2;
            default -> -1;
        };

        shapeSpinner.setSelection(shapeId);

        Spinner buttonSpinner = view.findViewById(R.id.buttonSpinner);

        buttonSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, allKeyNames));
        buttonSpinner.setSelection(allKeyNames.indexOf(selectedButtonKeyName));

        Spinner analogUpKeySpinner = view.findViewById(R.id.analogUpKeySpinner);

        analogUpKeySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, allKeyNames));
        analogUpKeySpinner.setSelection(allKeyNames.indexOf(selectedAnalogUpKeyName));

        Spinner analogDownKeySpinner = view.findViewById(R.id.analogDownKeySpinner);

        analogDownKeySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, allKeyNames));
        analogDownKeySpinner.setSelection(allKeyNames.indexOf(selectedAnalogDownKeyName));

        Spinner analogLeftKeySpinner = view.findViewById(R.id.analogLeftKeySpinner);

        analogLeftKeySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, allKeyNames));
        analogLeftKeySpinner.setSelection(allKeyNames.indexOf(selectedAnalogLeftKeyName));

        Spinner analogRightKeySpinner = view.findViewById(R.id.analogRightKeySpinner);

        analogRightKeySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, allKeyNames));
        analogRightKeySpinner.setSelection(allKeyNames.indexOf(selectedAnalogRightKeyName));

        if (lastSelectedType == ANALOG || lastSelectedType == DPAD) {
            TextView shapeText = view.findViewById(R.id.shapeText);
            TextView buttonMappingText = view.findViewById(R.id.buttonMappingText);

            shapeText.setVisibility(View.GONE);
            buttonMappingText.setVisibility(View.GONE);

            buttonSpinner.setVisibility(View.GONE);
            shapeSpinner.setVisibility(View.GONE);
        } else if (lastSelectedType == BUTTON) {
            LinearLayout layoutAnalogUp = view.findViewById(R.id.layoutAnalogUp);
            LinearLayout layoutAnalogDown = view.findViewById(R.id.layoutAnalogDown);
            LinearLayout layoutAnalogLeft = view.findViewById(R.id.layoutAnalogLeft);
            LinearLayout layoutAnalogRight = view.findViewById(R.id.layoutAnalogRight);

            layoutAnalogUp.setVisibility(View.GONE);
            layoutAnalogDown.setVisibility(View.GONE);
            layoutAnalogLeft.setVisibility(View.GONE);
            layoutAnalogRight.setVisibility(View.GONE);
        }

        saveButton.setOnClickListener((v) -> {
            if (lastSelectedType == BUTTON && !buttonList.isEmpty()) {
                buttonList.get(lastSelectedButton - 1).keyName = (buttonSpinner.getSelectedItem().toString());
                buttonList.get(lastSelectedButton - 1).buttonMapping = (getMapping(buttonSpinner.getSelectedItem().toString()));
                buttonList.get(lastSelectedButton - 1).radius = (radiusSeekbar.getProgress());

                switch (shapeSpinner.getSelectedItem().toString()) {
                    case "Circle" -> buttonList.get(lastSelectedButton - 1).shape = (SHAPE_CIRCLE);
                    case "Square" -> buttonList.get(lastSelectedButton - 1).shape = (SHAPE_SQUARE);
                    case "Rectangle" -> buttonList.get(lastSelectedButton - 1).shape = (SHAPE_RECTANGLE);
                }
            } else if (lastSelectedType == ANALOG && !analogList.isEmpty()) {
                analogList.get(lastSelectedButton - 1).upKeyName = (analogUpKeySpinner.getSelectedItem().toString());
                analogList.get(lastSelectedButton - 1).upKeyCodes = (getMapping(analogUpKeySpinner.getSelectedItem().toString()));

                analogList.get(lastSelectedButton - 1).downKeyName = (analogDownKeySpinner.getSelectedItem().toString());
                analogList.get(lastSelectedButton - 1).downKeyCodes = (getMapping(analogDownKeySpinner.getSelectedItem().toString()));

                analogList.get(lastSelectedButton - 1).leftKeyName = (analogLeftKeySpinner.getSelectedItem().toString());
                analogList.get(lastSelectedButton - 1).leftKeyCodes = (getMapping(analogLeftKeySpinner.getSelectedItem().toString()));

                analogList.get(lastSelectedButton - 1).rightKeyName = (analogRightKeySpinner.getSelectedItem().toString());
                analogList.get(lastSelectedButton - 1).rightKeyCodes = (getMapping(analogRightKeySpinner.getSelectedItem().toString()));

                analogList.get(lastSelectedButton - 1).radius = (radiusSeekbar.getProgress());
            } else if (lastSelectedType == DPAD && !dpadList.isEmpty()) {
                dpadList.get(lastSelectedButton - 1).upKeyName = (analogUpKeySpinner.getSelectedItem().toString());
                dpadList.get(lastSelectedButton - 1).upKeyCodes = (getMapping(analogUpKeySpinner.getSelectedItem().toString()));

                dpadList.get(lastSelectedButton - 1).downKeyName = (analogDownKeySpinner.getSelectedItem().toString());
                dpadList.get(lastSelectedButton - 1).downKeyCodes = (getMapping(analogDownKeySpinner.getSelectedItem().toString()));

                dpadList.get(lastSelectedButton - 1).leftKeyName = (analogLeftKeySpinner.getSelectedItem().toString());
                dpadList.get(lastSelectedButton - 1).leftKeyCodes = (getMapping(analogLeftKeySpinner.getSelectedItem().toString()));

                dpadList.get(lastSelectedButton - 1).rightKeyName = (analogRightKeySpinner.getSelectedItem().toString());
                dpadList.get(lastSelectedButton - 1).rightKeyCodes = (getMapping(analogRightKeySpinner.getSelectedItem().toString()));

                dpadList.get(lastSelectedButton - 1).radius = (radiusSeekbar.getProgress());
            }

            requireContext().sendBroadcast(new Intent(ACTION_INVALIDATE));
            dismiss();
        });

        cancelButton.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create();
    }

    public static String selectedButtonKeyName = "";
    public static String selectedAnalogUpKeyName = "";
    public static String selectedAnalogDownKeyName = "";
    public static String selectedAnalogLeftKeyName = "";
    public static String selectedAnalogRightKeyName = "";
    public static int selectedButtonShape = -1;
    public static int selectedButtonRadius = 0;
}