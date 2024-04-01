package com.micewine.emu.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterSettings;
import com.micewine.emu.models.SettingsList;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar o layout do fragmento aqui
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewSettings);
        setAdapter(recyclerView);

//       LinearLayout x11_server_btn_ltr = rootView.findViewById(R.id.x11_pref_init);
//
//      LinearLayout log_btn_activity = rootView.findViewById(R.id.log_pref);
//        
//x11_server_btn_ltr.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//      Intent intent = new Intent(getActivity(), LoriePreferences.class);
//                startActivity(intent);
//       
//    }
//});
//      log_btn_activity.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//      Intent intent = new Intent(getActivity(), logShellOutput.class);
//                startActivity(intent);
//       
//    }
//});


        CollapsingToolbarLayout collapsingToolbarLayout = rootView.findViewById(R.id.toolbar_fragment_settings_layout);

        // Referenciar a MaterialToolbar
        MaterialToolbar materialToolbar = rootView.findViewById(R.id.toolbar_fragment_settings);

        collapsingToolbarLayout.setTitle("Configurações Gerais");


        return rootView;
    }


    private void setAdapter(RecyclerView recyclerView) {


        List<SettingsList> SettingsList = new ArrayList<>();
        AdapterSettings adapterSettings = new AdapterSettings(SettingsList, getContext());

        recyclerView.setAdapter(adapterSettings);
        SettingsList person = new SettingsList(R.string.x11_preference_title, R.string.x11_preferences_description, R.drawable.ic_settings_outline);
        SettingsList.add(person);
        person = new SettingsList(R.string.log_level_normal, R.string.log_description, R.drawable.ic_log);
        SettingsList.add(person);
        person = new SettingsList(R.string.about_preferences_title, R.string.about_preferences_description, R.drawable.ic_info_outline);
        SettingsList.add(person);

    }


}
