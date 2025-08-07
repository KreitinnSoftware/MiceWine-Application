package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.ACTION_SETUP;
import static com.micewine.emu.activities.MainActivity.customRootFSPath;
import static com.micewine.emu.activities.MainActivity.fileManagerDefaultDir;
import static com.micewine.emu.activities.MainActivity.floatingFileManagerCwd;
import static com.micewine.emu.activities.MainActivity.selectedFilePath;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetType;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.exportBox64Preset;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.importBox64Preset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.exportControllerPreset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.importControllerPreset;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.ShortcutsFragment.putExePath;
import static com.micewine.emu.fragments.ShortcutsFragment.setIconToGame;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.exportVirtualControllerPreset;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.importVirtualControllerPreset;
import static com.micewine.emu.utils.DriveUtils.parseWindowsPath;
import static com.micewine.emu.utils.FileUtils.getFileExtension;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterFiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import io.ByteWriter;
import mslinks.LinkTargetIDList;
import mslinks.ShellLink;
import mslinks.ShellLinkException;
import mslinks.data.ItemID;
import mslinks.data.VolumeID;

public class FloatingFileManagerFragment extends DialogFragment {
    private final int operationType;
    private final String initialCwd;

    public FloatingFileManagerFragment(int operationType, String initialCwd) {
        this.operationType = operationType;
        this.initialCwd = initialCwd;
    }

    private static RecyclerView recyclerView;
    private static final ArrayList<AdapterFiles.FileList> fileList = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_floating_file_manager, null);

        TextView selectRootFSText = view.findViewById(R.id.selectRootFSFileText);
        EditText editText = view.findViewById(R.id.editText);
        MaterialButton saveButton = view.findViewById(R.id.saveButton);

        recyclerView  = view.findViewById(R.id.recyclerViewFiles);
        recyclerView.setAdapter(new AdapterFiles(fileList, requireContext(), true));

        floatingFileManagerCwd = initialCwd;
        fmOperationType = operationType;

        refreshFiles();

        setCancelable(operationType != OPERATION_SELECT_RAT);

        switch (operationType) {
            case OPERATION_SELECT_RAT -> {
                selectRootFSText.setVisibility(View.VISIBLE);
                editText.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);

                new Thread(() -> {
                    while (customRootFSPath == null) {
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    dismiss();
                }).start();
            }
            case OPERATION_EXPORT_PRESET -> {
                selectRootFSText.setVisibility(View.GONE);
                editText.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);

                switch (clickedPresetType) {
                    case VIRTUAL_CONTROLLER_PRESET -> editText.setText("VirtualController-" + clickedPresetName + ".mwp");
                    case CONTROLLER_PRESET -> editText.setText("PhysicalController-" + clickedPresetName + ".mwp");
                    case BOX64_PRESET -> editText.setText("Box64-" + clickedPresetName + ".mwp");
                }

                saveButton.setOnClickListener((v) -> {
                    outputFile = new File(floatingFileManagerCwd, editText.getText().toString());

                    if (outputFile.exists()) {
                        Toast.makeText(requireContext(), outputFile.getPath() + " " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    switch (clickedPresetType) {
                        case VIRTUAL_CONTROLLER_PRESET -> exportVirtualControllerPreset(clickedPresetName, outputFile);
                        case CONTROLLER_PRESET -> exportControllerPreset(clickedPresetName, outputFile);
                        case BOX64_PRESET -> exportBox64Preset(clickedPresetName, outputFile);
                    }

                    outputFile = null;

                    Toast.makeText(requireContext(), getString(R.string.preset_exported, clickedPresetName), Toast.LENGTH_LONG).show();

                    dismiss();
                });
            }
            case OPERATION_IMPORT_PRESET -> {
                selectRootFSText.setVisibility(View.GONE);
                editText.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);

                new Thread(() -> {
                    while (outputFile == null) {
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    switch (clickedPresetType) {
                        case VIRTUAL_CONTROLLER_PRESET -> {
                            boolean ret = importVirtualControllerPreset(requireContext(), outputFile);
                            if (!ret) Toast.makeText(requireContext(), R.string.invalid_virtual_controller_preset_file, Toast.LENGTH_SHORT).show();
                        }
                        case CONTROLLER_PRESET -> {
                            boolean ret = importControllerPreset(outputFile);
                            if (!ret) Toast.makeText(requireContext(), R.string.invalid_controller_preset_file, Toast.LENGTH_SHORT).show();
                        }
                        case BOX64_PRESET -> {
                            boolean ret = importBox64Preset(outputFile);
                            if (!ret) Toast.makeText(requireContext(), R.string.invalid_box64_preset_file, Toast.LENGTH_SHORT).show();
                        }
                    }

                    outputFile = null;

                    dismiss();
                }).start();
            }
            case OPERATION_SELECT_EXE -> {
                selectRootFSText.setVisibility(View.GONE);
                editText.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);

                new Thread(() -> {
                    while (outputFile == null) {
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    putExePath(selectedGameName, outputFile.getPath());
                    outputFile = null;

                    getParentFragmentManager().setFragmentResult("invalidate", new Bundle());

                    dismiss();
                }).start();
            }
            case OPERATION_SELECT_ICON -> {
                selectRootFSText.setVisibility(View.GONE);
                editText.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);

                new Thread(() -> {
                    while (outputFile == null) {
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    setIconToGame(selectedGameName, outputFile);
                    outputFile = null;

                    getParentFragmentManager().setFragmentResult("invalidate", new Bundle());

                    dismiss();
                }).start();
            }
            case OPERATION_CREATE_LNK -> {
                selectRootFSText.setVisibility(View.GONE);
                editText.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);

                saveButton.setOnClickListener((v) -> {
                    outputFile = new File(floatingFileManagerCwd, editText.getText().toString());

                    if (outputFile.exists()) {
                        Toast.makeText(requireContext(), outputFile.getPath() + " " + getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ShellLink shellLink = new ShellLink();

                    shellLink.getHeader().getLinkFlags().setHasLinkTargetIDList();
                    String linkPath = parseWindowsPath(selectedFilePath).replace("\\", "/");

                    LinkTargetIDList idList = new LinkTargetIDList();

                    String[] pathSegments = linkPath.split("/");

                    try {
                        idList.add(new ItemID().setType(ItemID.TYPE_CLSID));
                        idList.add(new ItemID().setType(ItemID.TYPE_DRIVE).setName(pathSegments[0]));
                        for (int i = 1; i < pathSegments.length; i++) {
                            idList.add(new ItemID().setType(ItemID.TYPE_DIRECTORY).setName(pathSegments[i]));
                        }

                        shellLink.createLinkInfo();
                        shellLink.getLinkInfo().createVolumeID().setDriveType(VolumeID.DRIVE_FIXED);
                        shellLink.getLinkInfo().setLocalBasePath(selectedFilePath);
                    } catch (ShellLinkException ignored) {
                        return;
                    }

                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        ByteWriter byteWriter = new ByteWriter(outputStream);

                        shellLink.getHeader().serialize(byteWriter);

                        if (shellLink.getHeader().getLinkFlags().hasLinkTargetIDList()) {
                            idList.serialize(byteWriter);
                        }
                        if (shellLink.getHeader().getLinkFlags().hasLinkInfo()) {
                            shellLink.getLinkInfo().serialize(byteWriter);
                        }
                        if (shellLink.getHeader().getLinkFlags().hasName()) {
                            byteWriter.writeUnicodeString(new File(selectedFilePath).getName());
                        }
                        if (shellLink.getHeader().getLinkFlags().hasWorkingDir()) {
                            byteWriter.writeUnicodeString(linkPath);
                        }
                        byteWriter.write4bytes(0);
                    } catch (IOException ignored) {
                    }

                    outputFile = null;
                    dismiss();
                });
            }
        }

        return new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (!calledSetup && operationType == OPERATION_SELECT_RAT) {
            calledSetup = true;
            requireContext().sendBroadcast(new Intent(ACTION_SETUP));
        }
    }

    public static boolean calledSetup = false;
    public static File outputFile = null;
    public static int fmOperationType = -1;

    public final static int OPERATION_SELECT_RAT = 0;
    public final static int OPERATION_EXPORT_PRESET = 1;
    public final static int OPERATION_IMPORT_PRESET = 2;
    public final static int OPERATION_SELECT_EXE = 3;
    public final static int OPERATION_SELECT_ICON = 4;
    public final static int OPERATION_CREATE_LNK = 5;

    public static void refreshFiles() {
        recyclerView.post(() -> {
            File[] newFileList = new File(floatingFileManagerCwd).listFiles();
            if (newFileList == null) return;
            Arrays.sort(newFileList, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemRangeRemoved(0, fileList.size());
            }

            fileList.clear();

            if (!floatingFileManagerCwd.equals(fileManagerDefaultDir)) {
                fileList.add(
                        new AdapterFiles.FileList(new File(".."))
                );
            }

            for (File file : newFileList) {
               if (file.isDirectory()) {
                   fileList.add(
                           new AdapterFiles.FileList(file)
                   );
               }
            }
            for (File file : newFileList) {
                if (file.isFile()) {
                    switch (fmOperationType) {
                        case OPERATION_SELECT_RAT -> {
                            if (file.getName().toLowerCase().endsWith(".rat")) {
                                fileList.add(
                                        new AdapterFiles.FileList(file)
                                );
                            }
                        }
                        case OPERATION_IMPORT_PRESET -> {
                            if (file.getName().toLowerCase().endsWith(".mwp")) {
                                try {
                                    String mwpType = Files.readAllLines(file.toPath()).get(0);

                                    switch (clickedPresetType) {
                                        case CONTROLLER_PRESET -> {
                                            if (mwpType.equals("controllerPreset")) {
                                                fileList.add(
                                                        new AdapterFiles.FileList(file)
                                                );
                                            }
                                        }
                                        case VIRTUAL_CONTROLLER_PRESET -> {
                                            if (mwpType.equals("virtualControllerPreset")) {
                                                fileList.add(
                                                        new AdapterFiles.FileList(file)
                                                );
                                            }
                                        }
                                        case BOX64_PRESET -> {
                                            if (mwpType.equals("box64Preset") || mwpType.equals("box64PresetV2")) {
                                                fileList.add(
                                                        new AdapterFiles.FileList(file)
                                                );
                                            }
                                        }
                                    }
                                } catch (IOException ignored) {
                                }
                            }
                        }
                        case OPERATION_SELECT_EXE -> {
                            String fileExtension = getFileExtension(file);
                            if (fileExtension.equalsIgnoreCase("exe")) {
                                fileList.add(
                                        new AdapterFiles.FileList(file)
                                );
                            }
                        }
                        case OPERATION_SELECT_ICON -> {
                            String fileExtension = getFileExtension(file).toLowerCase();
                            switch (fileExtension) {
                                case "exe", "ico", "png", "jpg", "jpeg", "bmp" -> fileList.add(
                                        new AdapterFiles.FileList(file)
                                );
                            }
                        }
                        default -> fileList.add(
                                new AdapterFiles.FileList(file)
                        );
                    }
                }
            }

            if (adapter != null) {
                adapter.notifyItemRangeInserted(0, fileList.size());
            }
        });
    }
}