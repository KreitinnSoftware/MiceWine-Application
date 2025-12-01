package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.fileManagerCwd;
import static com.micewine.emu.activities.MainActivity.floatingFileManagerCwd;
import static com.micewine.emu.activities.MainActivity.fileManagerDefaultDir;
import static com.micewine.emu.activities.MainActivity.selectedFilePath;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.activities.MainActivity.wineDisksFolder;
import static com.micewine.emu.fragments.AskInstallPackageFragment.ADTOOLS_DRIVER_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.MWP_PRESET_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.RAT_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.adToolsDriverCandidate;
import static com.micewine.emu.fragments.AskInstallPackageFragment.mwpPresetCandidate;
import static com.micewine.emu.fragments.AskInstallPackageFragment.ratCandidate;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.DeleteItemFragment.DELETE_GAME_ITEM;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.FILE_MANAGER_START_PREFERENCES;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_CREATE_LNK;
import static com.micewine.emu.fragments.RenameFragment.RENAME_FILE;
import static com.micewine.emu.fragments.ShortcutsFragment.addGameToList;
import static com.micewine.emu.utils.DriveUtils.parseUnixPath;
import static com.micewine.emu.utils.FileUtils.getFileExtension;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterFiles;
import com.micewine.emu.core.RatPackageManager;
import com.micewine.emu.core.WineWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mslinks.ShellLink;

public class FileManagerFragment extends Fragment {
    private TextView currentFolderText;
    private static RecyclerView recyclerView;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (ACTION_REFRESH_FILES.equals(intent.getAction())) {
                String currentWorkingDir = fileManagerCwd.replaceFirst(wineDisksFolder.getPath(), "") + "/";
                if (currentWorkingDir.length() > 1) {
                    currentWorkingDir = currentWorkingDir.substring(1);
                }
                String updatedText = currentWorkingDir.substring(0, 1).toUpperCase() + currentWorkingDir.substring(1);
                currentFolderText.setText(updatedText);

                new Thread(() -> {
                    File[] newFileList = new File(fileManagerCwd).listFiles();

                    fileList.clear();

                    if (!fileManagerCwd.equals(fileManagerDefaultDir)) {
                        fileList.add(
                                new AdapterFiles.FileList(new File(".."))
                        );
                    }

                    if (newFileList != null) {
                        Arrays.sort(newFileList, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

                        for (File file : newFileList) {
                            if (file.isDirectory()) {
                                fileList.add(
                                        new AdapterFiles.FileList(file)
                                );
                            }
                        }
                        for (File file : newFileList) {
                            if (file.isFile()) {
                                fileList.add(
                                        new AdapterFiles.FileList(file)
                                );
                            }
                        }
                    }

                    recyclerView.post(() -> {
                        ViewPropertyAnimator animator = recyclerView.animate();

                        animator.alpha(0F);
                        animator.setDuration(70L);
                        animator.withEndAction(() -> {
                            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                            if (adapter != null) adapter.notifyDataSetChanged();
                            animator.alpha(1F);
                            animator.setDuration(70L);
                            animator.start();
                        });
                        animator.start();
                    });
                }).start();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_file_manager, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewFiles);

        recyclerView.setAdapter(new AdapterFiles(fileList, requireContext(), false));
        currentFolderText = rootView.findViewById(R.id.currentFolder);

        if (fileManagerCwd == null) fileManagerCwd = fileManagerDefaultDir;

        registerForContextMenu(recyclerView);

        currentFolderText.setText("/");
        currentFolderText.setSelected(true);

        return rootView;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onStart() {
        super.onStart();
        requireActivity().registerReceiver(receiver, new IntentFilter(ACTION_REFRESH_FILES), 0);
        refreshFiles(requireContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        File file = new File(selectedFilePath);
        String fileExtension = selectedFilePath.substring(selectedFilePath.lastIndexOf(".") + 1).toLowerCase();

        switch (fileExtension) {
            case "rat" -> {
                ratCandidate = new RatPackageManager.RatPackage(selectedFilePath);
                requireActivity().getMenuInflater().inflate(R.menu.file_list_context_menu_package, menu);
            }
            case "mwp" -> {
                List<String> mwpLines;

                try {
                    mwpLines = Files.readAllLines(file.toPath());
                } catch (IOException ignored) {
                    requireActivity().getMenuInflater().inflate(R.menu.file_list_context_menu_default, menu);
                    return;
                }

                if (!mwpLines.isEmpty()) {
                    switch (mwpLines.get(0)) {
                        case "controllerPreset" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(CONTROLLER_PRESET, file);
                        case "virtualControllerPreset" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(VIRTUAL_CONTROLLER_PRESET, file);
                        case "box64Preset", "box64PresetV2" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(BOX64_PRESET, file);
                    }
                    requireActivity().getMenuInflater().inflate(R.menu.file_list_context_menu_package, menu);
                    return;
                }

                requireActivity().getMenuInflater().inflate(R.menu.file_list_context_menu_default, menu);
            }
            case "exe", "msi", "bat", "lnk" -> requireActivity().getMenuInflater().inflate(R.menu.file_list_context_menu_exe, menu);
            case "zip" -> {
                adToolsDriverCandidate = new RatPackageManager.AdrenoToolsPackage(selectedFilePath);

                boolean isValidAdToolsPackage = (adToolsDriverCandidate.getName() != null);

                requireActivity().getMenuInflater().inflate(isValidAdToolsPackage ? R.menu.file_list_context_menu_package : R.menu.file_list_context_menu_default, menu);
            }
            default -> requireActivity().getMenuInflater().inflate(R.menu.file_list_context_menu_default, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        File file = new File(selectedFilePath);
        String fileExtension = getFileExtension(file);

        if (item.getItemId() == R.id.addToHome) {
            String fileNameWithoutExtension = file.getName().replace("." + fileExtension, "");

            if (fileExtension.equalsIgnoreCase("exe")) {
                String iconPath = usrDir.getPath() + "/icons/" + fileNameWithoutExtension;

                WineWrapper.extractIcon(selectedFilePath, iconPath);

                addGameToList(selectedFilePath, fileNameWithoutExtension, iconPath);

                Toast.makeText(requireContext(), "'" + fileNameWithoutExtension + "' Added to Home Screen.", Toast.LENGTH_SHORT).show();
            } else if (fileExtension.equals("bat") || fileExtension.equals("msi")) {
                addGameToList(selectedFilePath, fileNameWithoutExtension, "");
            } else {
                Toast.makeText(requireContext(), R.string.incompatible_selected_file, Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.createLnk) {
            new FloatingFileManagerFragment(OPERATION_CREATE_LNK, "/storage/emulated/0").show(requireActivity().getSupportFragmentManager(), "");
        } else if (item.getItemId() == R.id.executeExe) {
            File targetFile = file;

            if (fileExtension.equals("lnk")) {
                try {
                    ShellLink shellLink = new ShellLink(file);
                    String parsedUnixPath = parseUnixPath(shellLink.resolveTarget());
                    targetFile = new File(parsedUnixPath);
                } catch (Exception ignored) {
                    Toast.makeText(requireContext(), R.string.lnk_read_fail, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

            new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, targetFile).show(requireActivity().getSupportFragmentManager(), "");

        } else if (item.getItemId() == R.id.deleteFile) {
            new DeleteItemFragment(DELETE_GAME_ITEM).show(requireActivity().getSupportFragmentManager(), "");
        } else if (item.getItemId() == R.id.renameFile) {
            new RenameFragment(RENAME_FILE, file.getName()).show(requireActivity().getSupportFragmentManager(), "");
        } else if (item.getItemId() == R.id.installPackage) {
            switch (fileExtension) {
                case "rat" -> new AskInstallPackageFragment(RAT_PACKAGE).show(requireActivity().getSupportFragmentManager(), "");
                case "mwp" -> new AskInstallPackageFragment(MWP_PRESET_PACKAGE).show(requireActivity().getSupportFragmentManager(), "");
                case "zip" -> new AskInstallPackageFragment(ADTOOLS_DRIVER_PACKAGE).show(requireActivity().getSupportFragmentManager(), "");
            }
        }

        return super.onContextItemSelected(item);
    }

    public static String ACTION_REFRESH_FILES = "com.micewine.emu.ACTION_REFRESH_FILES";
    public static ArrayList<AdapterFiles.FileList> fileList = new ArrayList<>();

    public static void refreshFiles(Context context) {
        context.sendBroadcast(new Intent(ACTION_REFRESH_FILES));
    }

    public static void deleteFile(String filePath) {
        int index = -1;

        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).file.getPath().equals(filePath)) {
                index = i;
                break;
            }
        }

        if (index != -1 && new File(filePath).delete()) {
            fileList.remove(index);
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemRemoved(index);
            }
        }
    }


    public static void renameFile(String filePath, String destFilePath, Context context) {
        File originalFile = new File(filePath);
        File destFile = new File(destFilePath);

        originalFile.renameTo(destFile);

        refreshFiles(context);
    }
}