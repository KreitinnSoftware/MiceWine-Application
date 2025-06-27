package com.micewine.emu.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.gson
import com.micewine.emu.activities.MainActivity.Companion.preferences

class EnvVarsSettingsFragment : Fragment() {
    private var addEnvButton: FloatingActionButton? = null
    private var recyclerView: RecyclerView? = null

    private val envVarsList = mutableListOf<EnvironmentVariable>()
    private lateinit var envVarsAdapter: EnvironmentVarsAdapter

    companion object {
        const val ENV_VARS_KEY = "environmentVariables"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_env_vars_settings, container, false)

        addEnvButton = view.findViewById(R.id.addEnvVar)
        recyclerView = view.findViewById(R.id.rvEnvVars)

        loadEnvironmentVariables()
        setupRecyclerView()
        addEnvButton?.setOnClickListener {
            showDialog(null)
        }

        return view
    }

    private fun loadEnvironmentVariables() {
        val savedVarsJson = preferences?.getString(ENV_VARS_KEY, null)
        savedVarsJson?.let {
            val type = object : TypeToken<List<EnvironmentVariable>>() {}.type
            val savedVars = gson.fromJson<List<EnvironmentVariable>>(it, type)
            envVarsList.clear()
            envVarsList.addAll(savedVars)
        }
    }

    private fun saveEnvironmentVariables() {
        preferences?.edit()?.apply {
            putString(ENV_VARS_KEY, gson.toJson(envVarsList))
            apply()
        }
    }

    private fun setupRecyclerView() {
        envVarsAdapter = EnvironmentVarsAdapter(envVarsList,
            onItemClick = { position -> showDialog(position) },
            onDeleteClick = { position -> deleteEnvironmentVar(position) }
        )
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = envVarsAdapter
    }

    private fun showDialog(position: Int?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_edit_env_vars, null)
        val envDialogTitle = dialogView.findViewById<TextView>(R.id.envDialogTitle)

        if (position == null) {
            envDialogTitle.text = getString(R.string.env_add_action)
        } else {
            envDialogTitle.text = getString(R.string.env_edit_action)
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(dialogView).create()

        val keyInput = dialogView.findViewById<EditText>(R.id.etDialogKey)
        val valueInput = dialogView.findViewById<EditText>(R.id.etDialogValue)

        if (position != null) {
            keyInput.setText(envVarsList[position].key)
            valueInput.setText(envVarsList[position].value)
        }

        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        buttonSave.setOnClickListener {
            val key = keyInput.text.toString().trim()
            val value = valueInput.text.toString().trim()

            if (key.isNotEmpty() && value.isNotEmpty()) {
                val envVar = EnvironmentVariable(key, value)
                if (position == null) {
                    envVarsList.add(envVar)
                    envVarsAdapter.notifyItemInserted(envVarsList.size - 1)
                } else {
                    envVarsList[position] = envVar
                    envVarsAdapter.notifyItemChanged(position)
                }
                saveEnvironmentVariables()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteEnvironmentVar(position: Int) {
        envVarsList.removeAt(position)
        envVarsAdapter.notifyItemRemoved(position)
        saveEnvironmentVariables()
    }

    data class EnvironmentVariable(val key: String, val value: String)

    class EnvironmentVarsAdapter(
        private val envVars: List<EnvironmentVariable>,
        private val onItemClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.Adapter<EnvironmentVarsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_env_var, parent, false)
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
}