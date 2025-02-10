package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_WINE_PREFIX
import com.micewine.emu.activities.MainActivity.Companion.copyFile
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.activities.MainActivity.Companion.winePrefix
import com.micewine.emu.activities.MainActivity.Companion.winePrefixesDir
import com.micewine.emu.adapters.AdapterGame
import com.micewine.emu.core.HighlightState
import com.micewine.emu.databinding.FragmentShortcutsBinding
import com.micewine.emu.fragments.CreatePresetFragment.Companion.WINEPREFIX_PRESET
import com.micewine.emu.fragments.DeleteItemFragment.Companion.DELETE_WINE_PREFIX
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max

class ShortcutsFragment : Fragment() {
    private var binding: FragmentShortcutsBinding? = null
    private var rootView: View? = null
    private var layoutManager: GridLayoutManager? = null
    private var preferences: SharedPreferences? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var selectedWinePrefixSpinner: Spinner? = null
    private var createWinePrefixButton: ImageButton? = null
    private var deleteWinePrefixButton: ImageButton? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_UPDATE_WINE_PREFIX_SPINNER -> {
                    val prefixName = intent.getStringExtra("prefixName")
                    val winePrefixes = winePrefixesDir.listFiles()?.map { it.name }?.toTypedArray() ?: emptyArray()

                    selectedWinePrefixSpinner?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, winePrefixes)

                    if (prefixName == null) {
                        selectedWinePrefixSpinner?.setSelection(0)
                    } else {
                        selectedWinePrefixSpinner?.setSelection(winePrefixes.indexOf(prefixName))
                    }
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShortcutsBinding.inflate(inflater, container, false)
        rootView = binding!!.root

        recyclerView = rootView?.findViewById(R.id.recyclerViewGame)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())!!

        layoutManager = recyclerView?.layoutManager as GridLayoutManager?

        val spanCount = max(1F, requireActivity().resources.displayMetrics.widthPixels / dpToPx(150, requireContext())).toInt()

        recyclerView?.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView?.addItemDecoration(GridSpacingItemDecoration(spanCount, 20))

        setAdapter(requireActivity(), preferences!!)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.toolbar_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val query = newText.orEmpty()
                        val currentList = loadGameList(preferences!!)

                        val filteredList = if (query.isEmpty()) {
                            currentList.map {
                                AdapterGame.GameList(File(it[1]), it[0], it[2], it[3])
                            }
                        } else {
                            currentList.filter {
                                it[0].contains(query, ignoreCase = true)
                            }.map {
                                AdapterGame.GameList(File(it[1]), it[0], it[2], it[3])
                            }
                        }

                        val updatedList = listOf(
                            AdapterGame.GameList(
                                File(""),
                                getString(R.string.desktop_mode_init),
                                "",
                                ""
                            )
                        ) + filteredList

                        (recyclerView?.adapter as? AdapterGame)?.updateList(updatedList)
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true
            }
        }, viewLifecycleOwner)

        setupDragAndDrop()

        recyclerView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (gameList.size > 1 && (recyclerView?.childCount ?: 0) > 1 &&
                    !preferences!!.getBoolean(HIGHLIGHT_SHORTCUT_PREFERENCE_KEY, false) &&
                    HighlightState.fromOrdinal(preferences!!.getInt(HighlightState.HIGHLIGHT_PREFERENCE_KEY, 0)) == HighlightState.HIGHLIGHT_DONE) {
                    val secondItemView = recyclerView?.findViewHolderForAdapterPosition(1)?.itemView

                    secondItemView?.let { view ->
                        TapTargetView.showFor(requireActivity(),
                            TapTarget.forView(view.findViewById(R.id.img_game), getString(R.string.highlight_shortcuts))
                                .transparentTarget(true)
                                .cancelable(true)
                        )
                    }
                    recyclerView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                    preferences!!.edit().apply {
                        putBoolean(HIGHLIGHT_SHORTCUT_PREFERENCE_KEY, true)
                        apply()
                    }
                }
            }
        })

        registerForContextMenu(recyclerView!!)

        selectedWinePrefixSpinner = rootView?.findViewById(R.id.selectedWinePrefixSpinner)

        val winePrefixes = winePrefixesDir.listFiles()?.map { it.name }?.toTypedArray() ?: emptyArray()

        selectedWinePrefixSpinner?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, winePrefixes)
        selectedWinePrefixSpinner?.setSelection(winePrefixes.indexOf(winePrefix?.name))
        selectedWinePrefixSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                preferences!!.edit().apply {
                    putString(SELECTED_WINE_PREFIX, parent?.selectedItem.toString())
                    apply()
                }

                setSharedVars(requireActivity())

                recyclerView?.adapter?.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        createWinePrefixButton = rootView?.findViewById(R.id.createWinePrefix)
        createWinePrefixButton?.setOnClickListener {
            lifecycleScope.launch {
                setSharedVars(requireActivity())
                CreatePresetFragment(WINEPREFIX_PRESET).show(requireActivity().supportFragmentManager, "")
            }
        }

        deleteWinePrefixButton = rootView?.findViewById(R.id.deleteWinePrefix)
        deleteWinePrefixButton?.setOnClickListener {
            DeleteItemFragment(DELETE_WINE_PREFIX, requireContext()).show(requireActivity().supportFragmentManager, "")
        }

        activity?.registerReceiver(receiver, object : IntentFilter(ACTION_UPDATE_WINE_PREFIX_SPINNER) {})

        return rootView
    }

    private fun setupDragAndDrop() {
        val callback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (target.adapterPosition == 0 || viewHolder.adapterPosition == 0) return false

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                val currentList = loadGameList(preferences!!)
                val gameItem = currentList.removeAt(fromPosition - 1)
                currentList.add(toPosition - 1, gameItem)

                val movedGameItem = gameList.removeAt(fromPosition)
                gameList.add(toPosition, movedGameItem)

                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

                preferences!!.edit().apply {
                    putString("gameList", gson.toJson(currentList))
                    apply()
                }

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean = true
            override fun isItemViewSwipeEnabled(): Boolean = false
        }

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    private fun dpToPx(dp: Int, context: Context): Float {
        val density = context.resources.displayMetrics.density
        return dp * density
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        }
    }

    companion object {
        private val gson = Gson()
        private val listType = object : TypeToken<MutableList<Array<String>>>() {}.type
        private var recyclerView: RecyclerView? = null
        private val gameList: MutableList<AdapterGame.GameList> = ArrayList()

        const val HIGHLIGHT_SHORTCUT_PREFERENCE_KEY = "highlightedShortcut"
        const val ACTION_UPDATE_WINE_PREFIX_SPINNER = "com.micewine.emu.ACTION_UPDATE_WINEPREFIX_SPINNER"

        fun setAdapter(activity: Activity, preferences: SharedPreferences) {
            recyclerView?.adapter = AdapterGame(gameList, activity)

            gameList.clear()

            addToAdapter(recyclerView!!, activity.getString(R.string.desktop_mode_init), activity.getString(R.string.desktop_mode_init), "", "")

            for (game in loadGameList(preferences)) {
                val name = game[0]
                val exePath = game[1]
                val icon = game[2]
                val exeArguments = game[3]

                addToAdapter(recyclerView!!, exePath, name, icon, exeArguments)
            }
        }

        private fun addToAdapter(recyclerView: RecyclerView, exeFile: String, name: String, icon: String, exeArguments: String) {
            val pos = recyclerView.adapter?.itemCount!!

            gameList.add(pos, AdapterGame.GameList(File(exeFile), name, icon, exeArguments))

            recyclerView.adapter?.notifyItemInserted(pos)
        }

        private fun loadGameList(preferences: SharedPreferences): MutableList<Array<String>> {
            return gson.fromJson(preferences.getString("gameList", ""), listType) ?: mutableListOf()
        }

        fun saveToGameList(preferences: SharedPreferences, path: String, prettyName: String, icon: String) {
            val editor = preferences.edit()
            val currentList = loadGameList(preferences)
            val game = arrayOf(prettyName, path, icon, "")
            val gameExists = currentList.any { it[0] == game[0] }
            val index = currentList.count()

            if (gameExists) {
                return
            } else {
                currentList.add(index, game)
            }

            editor.putString("gameList", gson.toJson(currentList))
            editor.apply()

            recyclerView?.adapter?.notifyItemInserted(index + 1)
        }

        fun removeGameFromList(preferences: SharedPreferences, array: Array<String>) {
            val editor = preferences.edit()
            val currentList = loadGameList(preferences)
            val index = currentList.indexOfFirst { it[0] == array[0] && it[1] == array[1] }

            currentList.removeAt(index)

            editor.putString("gameList", gson.toJson(currentList))
            editor.apply()

            gameList.removeAt(index + 1)

            recyclerView?.adapter?.notifyItemRemoved(index + 1)
        }

        fun editGameFromList(preferences: SharedPreferences, gameArray: Array<String>, newName: String, newArguments: String) {
            val editor = preferences.edit()
            val currentList = loadGameList(preferences)
            val index = currentList.indexOfFirst { it[0] == gameArray[0] && it[1] == gameArray[1] && it[3] == gameArray[3] }

            currentList[index][0] = newName
            currentList[index][3] = newArguments

            editor.putString("gameList", gson.toJson(currentList))
            editor.apply()

            gameList[index + 1].name = newName
            gameList[index + 1].exeArguments = newArguments

            recyclerView?.adapter?.notifyItemChanged(index + 1)
        }

        fun setIconToGame(context: Context, preferences: SharedPreferences, uri: Uri, gameArray: Array<String>) {
            val editor = preferences.edit()
            val currentList = loadGameList(preferences)
            val index = currentList.indexOfFirst { it[0] == gameArray[0] && it[1] == gameArray[1] }

            createIconCache(context, uri, gameArray[0])

            currentList[index][2] = "$usrDir/icons/${gameArray[0]}-icon"

            editor.putString("gameList", gson.toJson(currentList))
            editor.apply()

            gameList[index + 1].imageGame = "$usrDir/icons/${gameArray[0]}-icon"

            recyclerView?.adapter?.notifyItemChanged(index + 1)
        }

        private fun createIconCache(context: Context, uri: Uri, gameName: String) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = File("$usrDir/icons/$gameName-icon").outputStream()

            copyFile(inputStream!!, outputStream)
        }
    }
}
