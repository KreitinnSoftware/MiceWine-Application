package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.adapters.AdapterRatPackage.ROOTFS;
import static com.micewine.emu.core.RootFSDownloaderService.DOWNLOAD_DONE;
import static com.micewine.emu.core.RootFSDownloaderService.DOWNLOAD_FAILED;
import static com.micewine.emu.core.RootFSDownloaderService.DOWNLOAD_START;
import static com.micewine.emu.core.RootFSDownloaderService.UPDATE_PROGRESS_BAR;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterRatPackage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RootFSDownloaderFragment extends Fragment {
    public final ArrayList<AdapterRatPackage.Item> rootFsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    public TextView progressBarProgress;
    public TextView textView;
    public ImageView imageView;
    public static boolean rootFSIsDownloaded = false;
    public static boolean downloadingRootFS = false;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DOWNLOAD_START.equals(intent.getAction())) {
                requireActivity().runOnUiThread(() -> {
                    imageView.setVisibility(View.VISIBLE);
                    textView.setText(R.string.downloading_rootfs);
                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBarProgress.setVisibility(View.VISIBLE);
                });
            } else if (UPDATE_PROGRESS_BAR.equals(intent.getAction())) {
                String progressBarText = intent.getStringExtra("progressText");
                int progress = intent.getIntExtra("progress", 0);

                requireActivity().runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                    progressBarProgress.setText(progressBarText);
                });
            } else if (DOWNLOAD_DONE.equals(intent.getAction())) {
                rootFSIsDownloaded = true;
                downloadingRootFS = false;

                requireActivity().runOnUiThread(() -> {
                    progressBarProgress.setText("100%");
                    textView.setText(R.string.download_successful);
                });
            } else if (DOWNLOAD_FAILED.equals(intent.getAction())) {
                String errorMessage = intent.getStringExtra("errorMessage");

                requireActivity().runOnUiThread(() -> {
                    progressBar.setProgress(0);
                    progressBarProgress.setText("IOException: " + errorMessage);
                });
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rootfs_downloader, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewRootFSDownloader);
        progressBar = rootView.findViewById(R.id.progressBar);
        progressBarProgress = rootView.findViewById(R.id.progressBarPercentage);
        textView = rootView.findViewById(R.id.textView);
        imageView = rootView.findViewById(R.id.logo);

        setAdapter();

        requireActivity().registerReceiver(broadcastReceiver, new IntentFilter() {{
            addAction(DOWNLOAD_START);
            addAction(UPDATE_PROGRESS_BAR);
            addAction(DOWNLOAD_FAILED);
            addAction(DOWNLOAD_DONE);
        }});

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(broadcastReceiver);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        recyclerView.setAdapter(new AdapterRatPackage(rootFsList, requireActivity(), false));

        rootFsList.clear();

        new Thread(() -> {
            List<RootFSPackage> packageList = fetchRootFS();

            if (packageList == null) return;

            for (RootFSPackage rootFSPackage : packageList) {
                addToAdapter(rootFSPackage.name + (rootFSPackage.isPreRelease ? " (Testing)" : ""), rootFSPackage.date, rootFSPackage.version);
            }

            addToAdapter(getString(R.string.select_rootfs_file), "", "Manual");

            recyclerView.post(() -> {
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void addToAdapter(String title, String description, String version) {
        rootFsList.add(
                new AdapterRatPackage.Item(title, description, version, ROOTFS, false, null, false)
        );
    }

    private final int MINUTE = 60;
    private final int HOUR = MINUTE * 60;
    private final int DAY = HOUR * 24;
    private final int MONTH = DAY * 30;
    private final int YEAR = DAY * 365;

    private String timeAgo(String isoTime) {
        OffsetDateTime dateTime = OffsetDateTime.parse(isoTime);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Duration duration = Duration.between(dateTime, now);
        long seconds = duration.getSeconds();

        if (seconds < MINUTE) {
            return getResources().getQuantityString(R.plurals.seconds_ago, (int) seconds, seconds);
        } else if (seconds < HOUR) {
            long minutes = seconds / MINUTE;
            return getResources().getQuantityString(R.plurals.minutes_ago, (int) minutes, minutes);
        } else if (seconds < DAY) {
            long hours = seconds / HOUR;
            return getResources().getQuantityString(R.plurals.hours_ago, (int) hours, hours);
        } else if (seconds < MONTH) {
            long days = seconds / DAY;
            return getResources().getQuantityString(R.plurals.days_ago, (int) days, days);
        } else if (seconds < YEAR) {
            long months = seconds / MONTH;
            return getResources().getQuantityString(R.plurals.months_ago, (int) months, months);
        } else {
            long years = seconds / YEAR;
            return getResources().getQuantityString(R.plurals.years_ago, (int) years, years);
        }
    }

    private List<RootFSPackage> fetchRootFS() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://api.github.com/repos/KreitinnSoftware/MiceWine-RootFS-Generator/releases").build();
        Type type = new TypeToken<List<GHRelease>>() {}.getType();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                List<GHRelease> releases = gson.fromJson(response.body().string(), type);
                ArrayList<RootFSPackage> rootFSPackages = new ArrayList<>(releases.size());

                for (GHRelease r : releases) {
                    if (r.tag_name.equals("b6a253b")) break;

                    rootFSPackages.add(new RootFSPackage(r.name, r.tag_name, timeAgo(r.published_at), Boolean.parseBoolean(r.prerelease)));
                }

                return rootFSPackages;
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    private static class GHRelease {
        public String name;
        public String tag_name;
        public String published_at;
        public String prerelease;
    }

    public static class RootFSPackage {
        public String name;
        public String version;
        public String date;
        public boolean isPreRelease;

        public RootFSPackage(String name, String version, String date, boolean isPreRelease) {
            this.name = name;
            this.version = version;
            this.date = date;
            this.isPreRelease = isPreRelease;
        }
    }
}