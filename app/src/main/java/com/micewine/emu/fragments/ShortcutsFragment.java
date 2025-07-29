package com.micewine.emu.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.SHORTCUT_SERVICE;

import static com.micewine.emu.activities.MainActivity.copyFile;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.core.RatPackageManager.listRatPackagesId;
import static com.micewine.emu.core.WineWrapper.extractIcon;
import static com.micewine.emu.fragments.DebugSettingsFragment.availableCPUs;
import static com.micewine.emu.fragments.DeleteItemFragment.DELETE_GAME_ITEM;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.EDIT_GAME_PREFERENCES;
import static com.micewine.emu.utils.FileUtils.getFileExtension;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.reflect.TypeToken;
import com.micewine.emu.R;
import com.micewine.emu.activities.MainActivity;
import com.micewine.emu.adapters.AdapterGame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ShortcutsFragment extends Fragment {
    private static RecyclerView recyclerView;
    private TextView appName;
    private ImageButton searchItem;
    private ImageButton backButton;
    private TextInputEditText searchInput;
    private ItemTouchHelper itemTouchHelper;
    private InputMethodManager immManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shortcuts, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewGame);

        appName = rootView.findViewById(R.id.appName);
        searchItem = rootView.findViewById(R.id.searchItem);
        backButton = rootView.findViewById(R.id.backButton);
        searchInput = rootView.findViewById(R.id.searchInput);
        immManager = (InputMethodManager) requireContext().getSystemService(INPUT_METHOD_SERVICE);

        initialize();

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(requireContext());

        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        layoutManager.setFlexWrap(FlexWrap.WRAP);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(10));
        recyclerView.setAdapter(new AdapterGame(gameList, 1F, requireActivity()));

        searchItem.setOnClickListener((v) -> {
            searchItem.setVisibility(View.GONE);
            appName.setVisibility(View.GONE);
            searchInput.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);

            searchInput.setText("");
            searchInput.requestFocus();
            immManager.showSoftInput(searchInput, 0);
        });

        backButton.setOnClickListener((v) -> {
            searchItem.setVisibility(View.VISIBLE);
            appName.setVisibility(View.VISIBLE);
            searchInput.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);

            immManager.hideSoftInputFromWindow(requireActivity().getWindow().getDecorView().getWindowToken(), 0);

            AdapterGame adapter = (AdapterGame) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.filterList("");
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                AdapterGame adapter = (AdapterGame) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.filterList(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        setupDragAndDrop();

        registerForContextMenu(recyclerView);

        return rootView;
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int flags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                return makeMovementFlags(flags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (target.getAdapterPosition() == 0 || viewHolder.getAdapterPosition() == 0) return false;

                requireActivity().closeContextMenu();

                int initialPosition = viewHolder.getAdapterPosition();
                int finalPosition = target.getAdapterPosition();

                AdapterGame.GameItem movedGame = gameList.get(initialPosition);

                gameList.remove(initialPosition);
                gameList.add(finalPosition, movedGame);

                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.notifyItemMoved(initialPosition, finalPosition);
                }

                saveShortcuts();

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (selectedGameName.equals(getString(R.string.desktop_mode_init))) {
            requireActivity().getMenuInflater().inflate(R.menu.game_list_context_menu_lite, menu);
        } else {
            requireActivity().getMenuInflater().inflate(R.menu.game_list_context_menu, menu);
        }

        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(selectedGameName)).findFirst().orElse(-1);
        if (index == 0) return;

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(index);

        if (viewHolder != null) {
            itemTouchHelper.startDrag(viewHolder);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addToLauncher) {
            addGameToLauncher(requireContext(), selectedGameName);
        } else if (item.getItemId() == R.id.removeGameItem) {
            new DeleteItemFragment(DELETE_GAME_ITEM).show(requireActivity().getSupportFragmentManager(), "");
        } else if (item.getItemId() == R.id.editGameItem) {
            new EditGamePreferencesFragment(EDIT_GAME_PREFERENCES).show(requireActivity().getSupportFragmentManager(), "");
        }
        return super.onContextItemSelected(item);
    }

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        int spacing;

        public GridSpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.top = spacing;
            outRect.bottom = spacing;
        }
    }

    private static ArrayList<AdapterGame.GameItem> gameList = new ArrayList<>();
    private static final FileManagerControllerSettings fileManagerControllerSettings = new FileManagerControllerSettings();

    public static class FileManagerControllerSettings {
        public boolean virtualControllerIsXInput = true;
        public String virtualControllerPreset = "default";
        public boolean[] controllerIsXInput = new boolean[] { true, true, true, true };
        public ArrayList<String> controllerPreset = new ArrayList<>(Arrays.asList("default", "default", "default", "default"));
        public boolean[] controllerSwapAnalogs = new boolean[] { false, false, false, false };
    }

    public final static int MESA_DRIVER = 0;
    public final static int ADRENO_TOOLS_DRIVER = 1;

    public static void initialize() {
        gameList = getGameList();
    }

    public static void putWineVirtualDesktop(String name, boolean enabled) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).wineVirtualDesktop = enabled;

        saveShortcuts();
    }

    public static boolean getWineVirtualDesktop(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return false;

        return gameList.get(index).wineVirtualDesktop;
    }

    public static void putCpuAffinity(String name, String cpuAffinity) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).cpuAffinityCores = cpuAffinity;

        saveShortcuts();
    }

    public static String getCpuAffinity(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return String.join(",", availableCPUs);

        return gameList.get(index).cpuAffinityCores;
    }

    public static void putWineServices(String name, boolean enabled) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).wineServices = enabled;

        saveShortcuts();
    }

    public static boolean getWineServices(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return false;

        return gameList.get(index).wineServices;
    }

    public static void putWineESync(String name, boolean enabled) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).wineESync = enabled;

        saveShortcuts();
    }

    public static boolean getWineESync(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return true;

        return gameList.get(index).wineESync;
    }
    public static void putVKD3DVersion(String name, String vkd3dVersion) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).vkd3dVersion = vkd3dVersion;

        saveShortcuts();
    }

    public static String getVKD3DVersion(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "";

        return gameList.get(index).vkd3dVersion;
    }

    public static void putWineD3DVersion(String name, String wineD3DVersion) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).wineD3DVersion = wineD3DVersion;

        saveShortcuts();
    }

    public static String getWineD3DVersion(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "";

        return gameList.get(index).wineD3DVersion;
    }

    public static void putDXVKVersion(String name, String dxvkVersion) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).dxvkVersion = dxvkVersion;

        saveShortcuts();
    }

    public static String getDXVKVersion(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "";

        return gameList.get(index).dxvkVersion;
    }

    public static void putD3DXRenderer(String name, String d3dxRenderer) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).d3dxRenderer = d3dxRenderer;

        saveShortcuts();
    }

    public static String getD3DXRenderer(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "DXVK";

        return gameList.get(index).d3dxRenderer;
    }

    public static void putVulkanDriverType(String name, int driverType) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).vulkanDriverType = driverType;

        saveShortcuts();
    }

    public static int getVulkanDriverType(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return MESA_DRIVER;

        return gameList.get(index).vulkanDriverType;
    }

    public static void putVulkanDriver(String name, String driverName) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).vulkanDriver = driverName;

        if (driverName.startsWith("AdrenoToolsDriver-")) {
            putVulkanDriverType(name, ADRENO_TOOLS_DRIVER);
        } else {
            putVulkanDriverType(name, MESA_DRIVER);
        }

        saveShortcuts();
    }

    public static String getVulkanDriver(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "";

        return gameList.get(index).vulkanDriver;
    }

    public static void putDisplaySettings(String name, String displayMode, String displayResolution) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).displayMode = displayMode;
        gameList.get(index).displayResolution = displayResolution;

        saveShortcuts();
    }

    public static List<String> getDisplaySettings(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return List.of("16:9", "1280x720");

        return List.of(gameList.get(index).displayMode, gameList.get(index).displayResolution);
    }

    public static void putBox64Version(String name, String box64VersionId) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).box64Version = box64VersionId;

        saveShortcuts();
    }

    public static String getBox64Version(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) {
            List<String> box64Packages = listRatPackagesId("Box64");
            if (box64Packages.isEmpty()) return null;
            return box64Packages.get(0);
        }

        return gameList.get(index).box64Version;
    }

    public static void putBox64Preset(String name, String presetName) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).box64Preset = presetName;

        saveShortcuts();
    }

    public static String getBox64Preset(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "default";

        return gameList.get(index).box64Preset;
    }

    public static void putControllerXInputSwapAnalogs(String name, boolean enabled, int controllerIndex) {
        if (controllerIndex == -1) return;
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) {
            fileManagerControllerSettings.controllerSwapAnalogs[controllerIndex] = enabled;
            return;
        }

        gameList.get(index).wineVirtualDesktop = enabled;

        saveShortcuts();
    }

    public static boolean getControllerXInputSwapAnalogs(String name, int controllerIndex) {
        if (controllerIndex == -1) return false;
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return fileManagerControllerSettings.controllerSwapAnalogs[controllerIndex];

        return gameList.get(index).controllersXInputSwapAnalogs.get(controllerIndex);
    }

    public static void putControllerXInput(String name, boolean enabled, int controllerIndex) {
        if (controllerIndex == -1) return;
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) {
            fileManagerControllerSettings.controllerIsXInput[controllerIndex] = enabled;
            return;
        }

        gameList.get(index).controllersEnableXInput.set(controllerIndex, enabled);

        saveShortcuts();
    }

    public static boolean getControllerXInput(String name, int controllerIndex) {
        if (controllerIndex == -1) return true;
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return fileManagerControllerSettings.controllerIsXInput[controllerIndex];

        return gameList.get(index).controllersEnableXInput.get(controllerIndex);
    }

    public static void putControllerPreset(String name, String presetName, int controllerIndex) {
        if (controllerIndex == -1) return;
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) {
            fileManagerControllerSettings.controllerPreset.set(controllerIndex, presetName);
            return;
        }

        gameList.get(index).controllersPreset.set(controllerIndex, presetName);

        saveShortcuts();
    }

    public static String getControllerPreset(String name, int controllerIndex) {
        if (controllerIndex == -1) return "";
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return fileManagerControllerSettings.controllerPreset.get(controllerIndex);

        return gameList.get(index).controllersPreset.get(controllerIndex);
    }
    public static void putVirtualControllerXInput(String name, boolean enabled) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) {
            fileManagerControllerSettings.virtualControllerIsXInput = enabled;
            return;
        }

        gameList.get(index).virtualControllerEnableXInput = enabled;

        saveShortcuts();
    }

    public static boolean getVirtualControllerXInput(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return fileManagerControllerSettings.virtualControllerIsXInput;

        return gameList.get(index).virtualControllerEnableXInput;
    }

    public static void putSelectedVirtualControllerPreset(String name, String presetName) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) {
            fileManagerControllerSettings.virtualControllerPreset = presetName;
            return;
        }

        gameList.get(index).virtualControllerPreset = presetName;

        saveShortcuts();
    }

    public static String getSelectedVirtualControllerPreset(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return fileManagerControllerSettings.virtualControllerPreset;

        return gameList.get(index).virtualControllerPreset;
    }

    public static void putExeArguments(String name, String exeArguments) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).exeArguments = exeArguments;

        saveShortcuts();
    }

    public static String getExeArguments(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "";

        return gameList.get(index).exeArguments;
    }
    public static void putExePath(String name, String exePath) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).exePath = exePath;

        saveShortcuts();
    }

    public static String getExePath(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "";

        return gameList.get(index).exePath;
    }

    public static void putEnableXInput(String name, boolean enabled) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).enableXInput = enabled;

        saveShortcuts();
    }

    public static boolean getEnableXInput(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return true;

        return gameList.get(index).enableXInput;
    }

    public static void putEnableDInput(String name, boolean enabled) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).enableDInput = enabled;

        saveShortcuts();
    }

    public static boolean getEnableDInput(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return true;

        return gameList.get(index).enableDInput;
    }

    public static void addGameToList(String path, String prettyName, String iconPath) {
        boolean gameExists = gameList.stream().anyMatch(i -> i.name.equals(prettyName));
        if (gameExists) return;

        gameList.add(
                new AdapterGame.GameItem(prettyName, path, "", iconPath)
        );

        saveShortcuts();

        if (recyclerView == null) return;

        recyclerView.post(() -> {
            AdapterGame adapter = (AdapterGame) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemInserted(gameList.size());
                adapter.filterList("");
            }
        });
    }

    public static void removeGameFromList(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.remove(index);

        saveShortcuts();

        AdapterGame adapter = (AdapterGame) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemInserted(gameList.size());
            adapter.filterList("");
        }
    }

    public static void updateShortcuts() {
        if (recyclerView == null) return;
        AdapterGame adapter = (AdapterGame) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.filterList("");
        }
    }

    public static void setGameName(String name, String newName) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        gameList.get(index).name = newName;

        saveShortcuts();

        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemChanged(index);
        }
    }

    public static void setIconToGame(String name, File iconFile) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        String fileExtension = getFileExtension(iconFile).toLowerCase();

        File cacheIconFile = new File(usrDir, "/icons/" + name + "-icon");

        switch (fileExtension) {
            case "exe" -> extractIcon(iconFile.getPath(), cacheIconFile.getPath());
            case "ico", "png", "jpg", "jpeg", "bmp" -> {
                try {
                    InputStream inputStream = new FileInputStream(iconFile);
                    OutputStream outputStream = new FileOutputStream(cacheIconFile);

                    copyFile(inputStream, outputStream);

                    inputStream.close();
                    outputStream.close();
                } catch (IOException ignored) {
                    return;
                }
            }
        }

        gameList.get(index).iconPath = usrDir.getPath() + "/icons/" + name + "-icon";

        saveShortcuts();

        if (recyclerView == null) return;

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        });
    }

    public static Bitmap getGameIcon(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return null;

        return BitmapFactory.decodeFile(gameList.get(index).iconPath);
    }

    public static String getGameExeArguments(String name) {
        int index = IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return "";

        return gameList.get(index).exeArguments;
    }

    public static void saveShortcuts() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("gameList", gson.toJson(gameList));
        editor.apply();
    }

    private static ArrayList<AdapterGame.GameItem> getGameList() {
        String json = preferences.getString("gameList", "");
        Type listType = new TypeToken<ArrayList<AdapterGame.GameItem>>() {}.getType();
        ArrayList<AdapterGame.GameItem> gameList = gson.fromJson(json, listType);

        return (gameList != null ? gameList : new ArrayList<>());
    }

    public static void addGameToLauncher(Context context, String name) {
        int index =IntStream.range(0, gameList.size()).filter(i -> gameList.get(i).name.equals(selectedGameName)).findFirst().orElse(-1);
        if (index == -1) return;

        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(SHORTCUT_SERVICE);

        if (shortcutManager.isRequestPinShortcutSupported()) {
            Intent intent = new Intent(context, MainActivity.class);

            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra("shortcutName", name);

            ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(context, name)
                    .setShortLabel(name)
                    .setIcon(
                            new File(gameList.get(index).iconPath).exists() ?
                                    Icon.createWithBitmap(BitmapFactory.decodeFile(gameList.get(index).iconPath)) :
                                    Icon.createWithResource(context, R.drawable.default_icon)
                    )
                    .setIntent(intent)
                    .build();

            Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo);
            PendingIntent successCallback = PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE);

            shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.getIntentSender());
        }
    }
}