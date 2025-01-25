package com.micewine.emu.fragments

import com.micewine.emu.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EnvironmentVarsSettingsFragment : Fragment() {
    private lateinit var btnAddEnvVar: Button
    private lateinit var rvEnvVars: RecyclerView

    private val envVarsList = mutableListOf<EnvironmentVariable>()
    private lateinit var envVarsAdapter: EnvironmentVarsAdapter

    companion object {
        private const val PREFS_NAME = "EnvironmentVariablesPrefs"
        private const val ENV_VARS_KEY = "environment_variables"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_env_vars_settings, container, false)

        btnAddEnvVar = view.findViewById(R.id.btnAddEnvVar)
        rvEnvVars = view.findViewById(R.id.rvEnvVars)

        loadEnvironmentVariables()
        setupRecyclerView()
        setupAddButton()

        return view
    }

    private fun loadEnvironmentVariables() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val savedVarsJson = preferences.getString(ENV_VARS_KEY, null)
        savedVarsJson?.let {
            val type = object : TypeToken<List<EnvironmentVariable>>() {}.type
            val savedVars = Gson().fromJson<List<EnvironmentVariable>>(it, type)
            envVarsList.clear()
            envVarsList.addAll(savedVars)
        }
    }

    private fun saveEnvironmentVariables() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = preferences.edit()
        val varsJson = Gson().toJson(envVarsList)
        editor.putString(ENV_VARS_KEY, varsJson)
        editor.apply()
    }

    private fun setupRecyclerView() {
        envVarsAdapter = EnvironmentVarsAdapter(envVarsList,
            onItemClick = { position -> showEditDialog(position) },
            onDeleteClick = { position -> deleteEnvironmentVar(position) }
        )
        rvEnvVars.layoutManager = LinearLayoutManager(context)
        rvEnvVars.adapter = envVarsAdapter
    }

    private fun setupAddButton() {
        btnAddEnvVar.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_env_var, null)
        val keyInput = dialogView.findViewById<EditText>(R.id.etDialogKey)
        val valueInput = dialogView.findViewById<EditText>(R.id.etDialogValue)

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.env_add_action))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save_text)) { _, _ ->
                val key = keyInput.text.toString().trim()
                val value = valueInput.text.toString().trim()

                if (key.isNotEmpty() && value.isNotEmpty()) {
                    val envVar = EnvironmentVariable(key, value)
                    envVarsList.add(envVar)
                    envVarsAdapter.notifyItemInserted(envVarsList.size - 1)
                    saveEnvironmentVariables()
                }
            }
            .setNegativeButton(getString(R.string.cancel_text), null)
            .create()
            .show()
    }

    private fun showEditDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_env_var, null)
        val keyInput = dialogView.findViewById<EditText>(R.id.etDialogKey)
        val valueInput = dialogView.findViewById<EditText>(R.id.etDialogValue)

        val currentVar = envVarsList[position]
        keyInput.setText(currentVar.key)
        valueInput.setText(currentVar.value)

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.env_edit_action))
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val key = keyInput.text.toString().trim()
                val value = valueInput.text.toString().trim()

                if (key.isNotEmpty()) {
                    envVarsList[position] = EnvironmentVariable(key, value)
                    envVarsAdapter.notifyItemChanged(position)
                    saveEnvironmentVariables()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun deleteEnvironmentVar(position: Int) {
        envVarsList.removeAt(position)
        envVarsAdapter.notifyItemRemoved(position)
        saveEnvironmentVariables()
    }
}

data class EnvironmentVariable(val key: String, val value: String)

class EnvironmentVarsAdapter(
    private val envVars: List<EnvironmentVariable>,
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<EnvironmentVarsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_env_vars, parent, false)
        return ViewHolder(view, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(envVars[position])
    }

    override fun getItemCount() = envVars.size

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvKey: TextView = itemView.findViewById(R.id.tvEnvVarKey)
        private val tvValue: TextView = itemView.findViewById(R.id.tvEnvVarValue)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteEnvVar)

        init {
            itemView.setOnClickListener { onItemClick(adapterPosition) }
            btnDelete.setOnClickListener { onDeleteClick(adapterPosition) }
        }

        fun bind(envVar: EnvironmentVariable) {
            tvKey.text = envVar.key
            tvValue.text = envVar.value
        }
    }
}