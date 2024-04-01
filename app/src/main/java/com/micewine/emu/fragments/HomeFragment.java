package com.micewine.emu.fragments;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterGame;
import com.micewine.emu.databinding.FragmentHomeBinding;
import com.micewine.emu.models.GameList;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private View rootView;

    private int title;
    private int releaselink;
    private View view;
    private boolean ReleaseDownloaded;
    private String version;
    private final Context context = getContext();
    private FragmentManager fragmentManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar o layout do fragmento aqui


        fragmentManager = getFragmentManager();
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewGame);
        setAdapter(recyclerView);


        CollapsingToolbarLayout collapsingtoolBar = rootView.findViewById(R.id.toolbar_fragment_home_layout);

        MaterialToolbar toolBar = rootView.findViewById(R.id.toolbar_fragment_home);

        collapsingtoolBar.setTitle("Home Screen");


        return rootView;
    }


    private void setAdapter(RecyclerView recyclerView) {


        List<GameList> GameList = new ArrayList<>();
        AdapterGame adapterGame = new AdapterGame(GameList, getContext());

        recyclerView.setAdapter(adapterGame);
        GameList game = new GameList(R.string.desktop_mode_init, R.drawable.default_icon);
        GameList.add(game);

    }


}
