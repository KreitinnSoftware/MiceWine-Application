package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.miceWineVersion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micewine.emu.BuildConfig;
import com.micewine.emu.R;
import com.micewine.emu.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        TextView appVersion = rootView.findViewById(R.id.ApplicationVersion);
        TextView appBuildDate = rootView.findViewById(R.id.ApplicationBuildDate);

        appVersion.setText(miceWineVersion);
        appBuildDate.setText(BuildConfig.APP_BUILD_DATE);

        return rootView;
    }
}