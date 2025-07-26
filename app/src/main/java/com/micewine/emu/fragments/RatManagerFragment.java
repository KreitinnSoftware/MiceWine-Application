package com.micewine.emu.fragments;

import static com.micewine.emu.core.RatPackageManager.listRatPackages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterRatPackage;
import com.micewine.emu.core.RatPackageManager;

import java.util.ArrayList;
import java.util.List;

public class RatManagerFragment extends Fragment {
    private final String prefix;
    private final String anotherPrefix;
    private final int type;

    public RatManagerFragment(String prefix, int type) {
        this.prefix = prefix;
        this.type = type;
        this.anotherPrefix = prefix;
    }

    public RatManagerFragment(String prefix, int type, String anotherPrefix) {
        this.prefix = prefix;
        this.type = type;
        this.anotherPrefix = anotherPrefix;
    }

    private RecyclerView recyclerView;
    private final ArrayList<AdapterRatPackage.Item> ratList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewGeneralSettings);

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterRatPackage(ratList, requireActivity(), false));

        ratList.clear();

        List<RatPackageManager.RatPackage> ratPackageList = listRatPackages(prefix, anotherPrefix);

        for (RatPackageManager.RatPackage ratPackage : ratPackageList) {
            addToAdapter(ratPackage.getName(), ratPackage.getVersion(), ratPackage.getFolderName(), ratPackage.getIsUserInstalled());
        }
    }

    private void addToAdapter(String title, String description, String itemFolderId, boolean canDelete) {
        ratList.add(
                new AdapterRatPackage.Item(title, description, itemFolderId, type, canDelete, null, false)
        );
    }
}