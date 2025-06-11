package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity
import com.micewine.emu.activities.MainActivity.Companion.copyFile
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.adapters.AdapterGame
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.core.RatPackageManager.listRatPackagesId
import com.micewine.emu.databinding.FragmentShortcutsBinding
import com.micewine.emu.fragments.DebugSettingsFragment.Companion.availableCPUs
import com.micewine.emu.fragments.DeleteItemFragment.Companion.DELETE_GAME_ITEM
import com.micewine.emu.fragments.EditGamePreferencesFragment.Companion.EDIT_GAME_PREFERENCES
import java.io.File

class ShortcutsFragment : Fragment() {
    private var binding: FragmentShortcutsBinding? = null
    private var rootView: View? = null
    private var appName: TextView? = null
    private var searchItem: ImageButton? = null
    private var backButton: ImageButton? = null
    private var searchInput: TextInputEditText? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var imManager: InputMethodManager? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShortcutsBinding.inflate(inflater, container, false)
        rootView = binding!!.root

        recyclerView = rootView?.findViewById(R.id.recyclerViewGame)

        appName = rootView?.findViewById(R.id.appName)
        searchItem = rootView?.findViewById(R.id.searchItem)
        backButton = rootView?.findViewById(R.id.backButton)
        searchInput = rootView?.findViewById(R.id.searchInput)
        imManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        initialize()

        recyclerView?.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.FLEX_START
            flexWrap = FlexWrap.WRAP
        }
        recyclerView?.addItemDecoration(
            GridSpacingItemDecoration(10)
        )

        searchItem?.setOnClickListener {
            searchItem?.visibility = View.GONE
            appName?.visibility = View.GONE

            searchInput?.setText("")
            searchInput?.visibility = View.VISIBLE
            searchInput?.requestFocus()
            imManager?.showSoftInput(searchInput, 0)

            backButton?.visibility = View.VISIBLE
        }

        backButton?.setOnClickListener {
            searchItem?.visibility = View.VISIBLE
            appName?.visibility = View.VISIBLE

            searchInput?.visibility = View.GONE
            backButton?.visibility = View.GONE

            imManager?.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)

            (recyclerView?.adapter as? AdapterGame)?.filterList("")
        }

        searchInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                (recyclerView?.adapter as? AdapterGame)?.filterList(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        setAdapter()
        setupDragAndDrop()

        registerForContextMenu(recyclerView!!)

        return rootView
    }



    private fun setupDragAndDrop() {
        val callback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (target.adapterPosition == 0 || viewHolder.adapterPosition == 0) return false

                requireActivity().closeContextMenu()

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                val initialGame = gameList[fromPosition]

                gameList.removeAt(fromPosition)
                gameList.add(toPosition, initialGame)

                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

                saveShortcuts()

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            override fun isLongPressDragEnabled(): Boolean = false
            override fun isItemViewSwipeEnabled(): Boolean = false
        }

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    private fun dpToPx(dp: Float, context: Context): Float {
        val density = context.resources.displayMetrics.density
        return dp * density
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        if (selectedGameName == getString(R.string.desktop_mode_init)) {
            requireActivity().menuInflater.inflate(R.menu.game_list_context_menu_lite, menu)
        } else {
            requireActivity().menuInflater.inflate(R.menu.game_list_context_menu, menu)
        }

        val index = gameListNames.indexOfFirst { it.name == selectedGameName }
        if (index == 0) return

        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(gameListNames.indexOfFirst { it.name == selectedGameName }) ?: return

        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addToLauncher -> {
                addGameToLauncher(requireContext(), selectedGameName)
            }
            R.id.removeGameItem -> {
                DeleteItemFragment(DELETE_GAME_ITEM).show(requireActivity().supportFragmentManager, "")
            }
            R.id.editGameItem -> {
                EditGamePreferencesFragment(EDIT_GAME_PREFERENCES).show(requireActivity().supportFragmentManager, "")
            }
        }

        return super.onContextItemSelected(item)
    }

    class GridSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.left = spacing
            outRect.right = spacing
            outRect.top = spacing
            outRect.bottom = spacing
        }
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(
            AdapterGame(gameListNames, 1F, requireActivity())
        )

        gameListNames.clear()

        gameList.forEach {
            addToAdapter(it.name, it.exePath, it.exeArguments, it.iconPath)
        }
    }

    private fun addToAdapter(name: String, exePath: String, exeArguments: String, iconPath: String) {
        gameListNames.add(
            AdapterGame.GameItem(name, exePath, exeArguments, iconPath)
        )
    }

    companion object {
        private val gson = Gson()
        private var recyclerView: RecyclerView? = null
        private var gameListNames: MutableList<AdapterGame.GameItem> = mutableListOf()
        private var gameList: MutableList<GameItem> = mutableListOf()

        const val ACTION_UPDATE_WINE_PREFIX_SPINNER = "com.micewine.emu.ACTION_UPDATE_WINE_PREFIX_SPINNER"

        const val MESA_DRIVER = 0
        const val ADRENO_TOOLS_DRIVER = 1

        fun initialize() {
            gameList = getGameList()
        }

        fun putWineVirtualDesktop(name: String, enabled: Boolean) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].wineVirtualDesktop = enabled

            saveShortcuts()
        }

        fun getWineVirtualDesktop(name: String): Boolean {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return false

            return gameList[index].wineVirtualDesktop
        }

        fun putCpuAffinity(name: String, cpuCores: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].cpuAffinityCores = cpuCores

            saveShortcuts()
        }

        fun getCpuAffinity(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return availableCPUs.joinToString(",")

            return gameList[index].cpuAffinityCores
        }

        fun putWineServices(name: String, enabled: Boolean) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].wineServices = enabled

            saveShortcuts()
        }

        fun getWineServices(name: String): Boolean {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return false

            return gameList[index].wineServices
        }

        fun putWineESync(name: String, enabled: Boolean) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].wineESync = enabled

            saveShortcuts()
        }

        fun getWineESync(name: String): Boolean {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return gameList[index].wineESync
        }

        fun putVKD3DVersion(name: String, vkd3dVersion: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].vkd3dVersion = vkd3dVersion

            saveShortcuts()
        }

        fun getVKD3DVersion(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return ""

            return gameList[index].vkd3dVersion
        }

        fun putWineD3DVersion(name: String, wineD3DVersion: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].wineD3DVersion = wineD3DVersion

            saveShortcuts()
        }

        fun getWineD3DVersion(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return ""

            return gameList[index].wineD3DVersion
        }

        fun putDXVKVersion(name: String, dxvkVersion: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].dxvkVersion = dxvkVersion

            saveShortcuts()
        }

        fun getDXVKVersion(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return ""

            return gameList[index].dxvkVersion
        }

        fun putD3DXRenderer(name: String, d3dxRenderer: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].d3dxRenderer = d3dxRenderer

            saveShortcuts()
        }

        fun getD3DXRenderer(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return "DXVK"

            return gameList[index].d3dxRenderer
        }

        private fun putVulkanDriverType(name: String, driverType: Int) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].vulkanDriverType = driverType

            saveShortcuts()
        }

        fun getVulkanDriverType(name: String): Int {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return MESA_DRIVER

            return gameList[index].vulkanDriverType
        }

        fun putVulkanDriver(name: String, driverName: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].vulkanDriver = driverName

            if (driverName.startsWith("AdrenoToolsDriver-")) {
                putVulkanDriverType(name, ADRENO_TOOLS_DRIVER)
            } else (
                putVulkanDriverType(name, MESA_DRIVER)
            )

            saveShortcuts()
        }

        fun getVulkanDriver(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return ""

            return gameList[index].vulkanDriver
        }

        fun putDisplaySettings(name: String, displayMode: String, displayResolution: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].displayMode = displayMode
            gameList[index].displayResolution = displayResolution

            saveShortcuts()
        }

        fun getDisplaySettings(name: String): List<String> {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return listOf("16:9", "1280x720")

            return listOf(gameList[index].displayMode, gameList[index].displayResolution)
        }

        fun putBox64Version(name: String, box64VersionId: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].box64Version = box64VersionId

            saveShortcuts()
        }

        fun getBox64Version(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return listRatPackagesId("Box64-").firstOrNull() ?: ""

            return gameList[index].box64Version
        }

        fun putBox64Preset(name: String, presetName: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].box64Preset = presetName

            saveShortcuts()
        }

        fun getBox64Preset(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return "default"

            return gameList[index].box64Preset
        }

        fun putControllerXInputSwapAnalogs(name: String, enabled: Boolean, controllerIndex: Int) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].controllersXInputSwapAnalogs[controllerIndex] = enabled

            saveShortcuts()
        }

        fun getControllerXInputSwapAnalogs(name: String, controllerIndex: Int): Boolean {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return false

            return gameList[index].controllersXInputSwapAnalogs[controllerIndex]
        }

        fun putControllerXInput(name: String, enabled: Boolean, controllerIndex: Int) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].controllersEnableXInput[controllerIndex] = enabled

            saveShortcuts()
        }

        fun getControllerXInput(name: String, controllerIndex: Int): Boolean {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return false

            return gameList[index].controllersEnableXInput[controllerIndex]
        }

        fun putControllerPreset(name: String, presetName: String, controllerIndex: Int) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].controllersPreset[controllerIndex] = presetName

            saveShortcuts()
        }

        fun getControllerPreset(name: String, controllerIndex: Int): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1 || controllerIndex == -1) return "default"

            return gameList[index].controllersPreset[controllerIndex]
        }

        fun putVirtualControllerXInput(name: String, enabled: Boolean) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].virtualControllerEnableXInput = enabled

            saveShortcuts()
        }

        fun getVirtualControllerXInput(name: String): Boolean {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return false

            return gameList[index].virtualControllerEnableXInput
        }

        fun putSelectedVirtualControllerPreset(name: String, presetName: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].virtualControllerPreset = presetName

            saveShortcuts()
        }

        fun getSelectedVirtualControllerPreset(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return "default"

            return gameList[index].virtualControllerPreset
        }

        fun putExeArguments(name: String, exeArguments: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].exeArguments = exeArguments

            saveShortcuts()
        }

        fun getExeArguments(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return ""

            return gameList[index].exeArguments
        }

        fun putExePath(name: String, exePath: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].exePath = exePath

            saveShortcuts()
        }

        fun getExePath(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return ""

            return gameList[index].exePath
        }

        fun addGameToList(path: String, prettyName: String, icon: String) {
            val gameExists = gameList.any { it.name == prettyName }
            if (gameExists) return

            gameList.add(
                GameItem(
                    prettyName,
                    path,
                    "",
                    icon,
                    "Global",
                    "default",
                    mutableListOf("default", "default", "default", "default"),
                    mutableListOf(true, true, true, true),
                    mutableListOf(false, false, false, false),
                    "default",
                    true,
                    "16:9",
                    "1280x720",
                    "Global",
                    MESA_DRIVER,
                    "DXVK",
                    listRatPackagesId("DXVK").first(),
                    listRatPackagesId("WineD3D").first(),
                    listRatPackagesId("VKD3D").first(),
                    true,
                    false,
                    availableCPUs.joinToString(","),
                    false,
                    false
                )
            )
            gameListNames.add(
                AdapterGame.GameItem(prettyName, path, "", icon)
            )

            saveShortcuts()

            recyclerView?.post {
                recyclerView?.adapter?.notifyItemInserted(gameListNames.size)
            }
        }

        fun removeGameFromList(name: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList.removeAt(index)
            gameListNames.removeAt(index)

            saveShortcuts()

            recyclerView?.adapter?.notifyItemRemoved(index)
        }

        fun setGameName(name: String, newName: String) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            gameList[index].name = newName
            gameListNames[index].name = newName

            saveShortcuts()

            recyclerView?.adapter?.notifyItemChanged(index)
        }

        fun setIconToGame(name: String, context: Context, uri: Uri) {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return

            createIconCache(context, uri, name)

            gameList[index].iconPath = "$usrDir/icons/$name-icon"
            gameListNames[index].iconPath = "$usrDir/icons/$name-icon"

            saveShortcuts()

            recyclerView?.adapter?.notifyItemChanged(index)
        }

        fun getGameIcon(name: String): Bitmap? {
            val index = gameList.indexOfFirst { it.name == name }
            if (index == -1) return null

            return BitmapFactory.decodeFile(gameList[index].iconPath)
        }

        fun getGameExeArguments(name: String): String {
            val index = gameList.indexOfFirst { it.name == name }

            if (index == -1) return ""

            return gameList[index].exeArguments
        }

        private fun createIconCache(context: Context, uri: Uri, gameName: String) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = File("$usrDir/icons/$gameName-icon").outputStream()

            copyFile(inputStream!!, outputStream)
        }

        fun saveShortcuts() {
            preferences?.edit {
                putString("gameList", gson.toJson(gameList))
                apply()
            }
        }

        private fun getGameList(): MutableList<GameItem> {
            val json = preferences?.getString("gameList", "")
            val listType = object : TypeToken<MutableList<GameItem>>() {}.type
            val gameList = gson.fromJson<MutableList<GameItem>>(json, listType)

            return gameList ?: mutableListOf()
        }

        fun addGameToLauncher(context: Context, name: String) {
            val index = gameList.indexOfFirst { it.name == name }
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)

            if (shortcutManager!!.isRequestPinShortcutSupported) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("shortcutName", name)
                }

                val pinShortcutInfo = ShortcutInfo.Builder(context, name)
                    .setShortLabel(name)
                    .setIcon(
                        if (File(gameList[index].iconPath).exists()) {
                            Icon.createWithBitmap(BitmapFactory.decodeFile(gameList[index].iconPath))
                        } else {
                            Icon.createWithResource(context, R.drawable.default_icon)
                        }
                    )

                    .setIntent(intent)
                    .build()

                val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                val successCallback = PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE)

                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
            }
        }

        data class GameItem(
            var name: String,
            var exePath: String,
            var exeArguments: String,
            var iconPath: String,
            var box64Version: String,
            var box64Preset: String,
            var controllersPreset: MutableList<String>,
            var controllersEnableXInput: MutableList<Boolean>,
            var controllersXInputSwapAnalogs: MutableList<Boolean>,
            var virtualControllerPreset: String,
            var virtualControllerEnableXInput: Boolean,
            var displayMode: String,
            var displayResolution: String,
            var vulkanDriver: String,
            var vulkanDriverType: Int,
            var d3dxRenderer: String,
            var dxvkVersion: String,
            var wineD3DVersion: String,
            var vkd3dVersion: String,
            var wineESync: Boolean,
            var wineServices: Boolean,
            var cpuAffinityCores: String,
            var wineVirtualDesktop: Boolean,
            var enableXInput: Boolean
        )
    }
}
