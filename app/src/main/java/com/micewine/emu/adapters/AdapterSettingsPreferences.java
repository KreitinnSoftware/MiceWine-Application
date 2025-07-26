package com.micewine.emu.adapters;

import static com.micewine.emu.activities.GeneralSettingsActivity.CHECKBOX;
import static com.micewine.emu.activities.GeneralSettingsActivity.SEEKBAR;
import static com.micewine.emu.activities.GeneralSettingsActivity.SPINNER;
import static com.micewine.emu.activities.GeneralSettingsActivity.SWITCH;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_APPLIED;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.controller.XKeyCodes.getMapping;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.editControllerPreset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getControllerPreset;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.controller.XKeyCodes;

import java.util.Arrays;
import java.util.List;

public class AdapterSettingsPreferences extends RecyclerView.Adapter<AdapterSettingsPreferences.ViewHolder> {
    private final List<SettingsListSpinner> settingsList;
    private final FragmentActivity activity;

    public AdapterSettingsPreferences(List<SettingsListSpinner> settingsList, FragmentActivity activity) {
        this.settingsList = settingsList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_settings_preferences_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingsListSpinner item = settingsList.get(position);

        holder.settingsName.setText(item.titleSettings);
        holder.settingsDescription.setText(item.descriptionSettings);

        if (activity.getString(item.descriptionSettings).equals(" ")) {
            holder.settingsDescription.setVisibility(View.GONE);
        }

        switch (item.type) {
            case SWITCH -> {
                holder.spinnerOptions.setVisibility(View.GONE);
                holder.settingsSwitch.setVisibility(View.VISIBLE);
                holder.seekBar.setVisibility(View.GONE);
                holder.seekBarValue.setVisibility(View.GONE);

                holder.settingsSwitch.setChecked(Boolean.parseBoolean(preferences.getString(item.key, item.defaultValue)));
                holder.settingsSwitch.setOnClickListener((v) -> {
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putBoolean(item.key, !Boolean.parseBoolean(preferences.getString(item.key, item.defaultValue)));
                    editor.apply();
                });
            }
            case SPINNER -> {
                holder.spinnerOptions.setVisibility(View.VISIBLE);
                holder.settingsSwitch.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.seekBarValue.setVisibility(View.GONE);

                holder.spinnerOptions.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, item.spinnerOptions));
                holder.spinnerOptions.setSelection(Arrays.asList(item.spinnerOptions).indexOf(preferences.getString(item.key, item.defaultValue)));
                holder.spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String selectedItem = adapterView.getItemAtPosition(i).toString();

                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putString(item.key, selectedItem);
                        editor.apply();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
            case CHECKBOX -> {
                holder.spinnerOptions.setVisibility(View.VISIBLE);
                holder.settingsSwitch.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.seekBarValue.setVisibility(View.GONE);

                System.out.println(item.spinnerOptions.length);
                holder.spinnerOptions.setAdapter(new CheckableAdapter(activity, item.spinnerOptions, item, holder.spinnerOptions));
            }
            case SEEKBAR -> {
                holder.spinnerOptions.setVisibility(View.GONE);
                holder.settingsSwitch.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.VISIBLE);
                holder.seekBarValue.setVisibility(View.VISIBLE);

                holder.seekBar.setMin(item.seekBarMaxMinValues[0]);
                holder.seekBar.setMax(item.seekBarMaxMinValues[1]);
                holder.seekBar.setProgress(preferences.getInt(item.key, Integer.parseInt(item.defaultValue)));

                if (holder.seekBar.getProgress() == 0) {
                    holder.seekBarValue.setText(R.string.unlimited);
                } else {
                    holder.seekBarValue.setText(String.valueOf(holder.seekBar.getProgress()));
                }

                holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        seekBar.setProgress(seekBar.getProgress() / 24 * 24);

                        holder.seekBarValue.setText(String.valueOf(seekBar.getProgress()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putInt(item.key, seekBar.getProgress());

                        if (item.key.equals(WINE_DPI)) {
                            editor.putBoolean(WINE_DPI_APPLIED, false);
                        }

                        editor.apply();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView settingsName = itemView.findViewById(R.id.title_preferences_model);
        TextView settingsDescription = itemView.findViewById(R.id.description_preferences_model);
        Spinner spinnerOptions = itemView.findViewById(R.id.keyBindSpinner);
        SwitchCompat settingsSwitch = itemView.findViewById(R.id.optionSwitch);
        SeekBar seekBar = itemView.findViewById(R.id.seekBar);
        TextView seekBarValue = itemView.findViewById(R.id.seekBarValue);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public static class SettingsListSpinner {
        public int titleSettings;
        public int descriptionSettings;
        public String[] spinnerOptions;
        public int[] seekBarMaxMinValues;
        public int type;
        public String defaultValue;
        public String key;

        public SettingsListSpinner(int titleSettings, int descriptionSettings, String[] spinnerOptions, int[] seekBarMaxMinValues, int type, String defaultValue, String key) {
            this.titleSettings = titleSettings;
            this.descriptionSettings = descriptionSettings;
            this.spinnerOptions = spinnerOptions;
            this.seekBarMaxMinValues = seekBarMaxMinValues;
            this.type = type;
            this.defaultValue = defaultValue;
            this.key = key;
        }
    }

    private static class CheckableAdapter implements SpinnerAdapter {
        private final Activity activity;
        private final String[] arrayElements;
        private final SettingsListSpinner item;
        private final Spinner spinner;
        private final boolean[] checked;

        public CheckableAdapter(Activity activity, String[] arrayElements, SettingsListSpinner item, Spinner spinner) {
            this.activity = activity;
            this.arrayElements = arrayElements;
            this.item = item;
            this.spinner = spinner;
            this.checked = new boolean[arrayElements.length];
        }

        @Override
        public View getDropDownView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View pView = inflater.inflate(R.layout.item_checkbox, viewGroup, false);
            CheckBox checkBox = pView.findViewById(R.id.checkbox);
            String preferencesValue = preferences.getString(item.key, item.defaultValue);

            checked[i] = preferencesValue.contains(arrayElements[i]);

            checkBox.setChecked(checked[i]);
            checkBox.setText(arrayElements[i]);

            checkBox.setOnClickListener((v) -> {
                checked[i] = !checked[i];

                StringBuilder builder = new StringBuilder();
                SharedPreferences.Editor editor = preferences.edit();

                for (int i1 = 0; i1 < checked.length; i1++) {
                    if (checked[i1]) {
                        builder.append(",");
                        builder.append(arrayElements[i1]);
                    }
                }

                if (builder.length() > 0) {
                    builder.deleteCharAt(0);
                }

                editor.putString(item.key, builder.toString());
                editor.apply();

                spinner.setAdapter(this);
            });

            return pView;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public int getCount() {
            return arrayElements.length;
        }

        @Override
        public Object getItem(int i) {
            return arrayElements[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View pView;
            if (view == null) {
                pView = inflater.inflate(android.R.layout.simple_spinner_item, viewGroup, false);
            } else {
                pView = view;
            }

            TextView textView = pView.findViewById(android.R.id.text1);
            textView.setText(preferences.getString(item.key, item.defaultValue));

            return pView;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return arrayElements.length == 0;
        }
    }

    /*
    class CheckableAdapter(
        val activity: Activity,
        private val arrayElements: Array<String>,
        private val sList: SettingsListSpinner,
        val preferences: SharedPreferences,
        private val spinner: Spinner
    ) : SpinnerAdapter {
        val checked = BooleanArray(count)

        override fun registerDataSetObserver(p0: DataSetObserver?) {
        }

        override fun unregisterDataSetObserver(p0: DataSetObserver?) {
        }

        override fun getCount(): Int {
            return arrayElements.count()
        }

        override fun getItem(p0: Int): Any {
            return arrayElements[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
            val inflater = activity.layoutInflater
            val view = p1 ?: inflater.inflate(android.R.layout.simple_spinner_item, p2, false)

            view.findViewById<TextView>(android.R.id.text1).apply {
                text = preferences.getString(sList.key, sList.defaultValue)
            }

            return view
        }

        override fun getItemViewType(p0: Int): Int {
            return 0
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun isEmpty(): Boolean {
            return arrayElements.isEmpty()
        }

        override fun getDropDownView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val inflater = activity.layoutInflater
            val view = inflater.inflate(R.layout.item_checkbox, p2, false)
            val checkBox = view.findViewById<CheckBox>(R.id.checkbox)
            val preferencesValue = preferences.getString(sList.key, sList.defaultValue)

            checked[p0] = preferencesValue?.contains(arrayElements[p0]) == true

            checkBox.isChecked = checked[p0]
            checkBox.text = arrayElements[p0]

            checkBox.setOnClickListener {
                checked[p0] = !checked[p0]

                val builder: StringBuilder = StringBuilder()

                val editor = preferences.edit()

                for (i in checked.indices) {
                    if (checked[i]) {
                        builder.append(",")
                        builder.append(arrayElements[i])
                    }
                }

                if (builder.isNotEmpty()) {
                    builder.deleteCharAt(0)
                }

                editor.putString(sList.key, builder.toString())

                editor.apply()

                spinner.adapter = this
            }

            return view
        }
    }
     */
}