package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.appRootDir;
import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.adapters.AdapterFiles.MEGABYTE;
import static com.micewine.emu.adapters.AdapterRatPackage.ROOTFS;

import android.annotation.SuppressLint;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

public class RootFSDownloaderFragment extends Fragment {
    public final ArrayList<AdapterRatPackage.Item> rootFsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    public TextView progressBarProgress;
    public TextView textView;
    public ImageView imageView;
    public static boolean rootFSIsDownloaded = false;
    public static boolean downloadingRootFS = false;

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

        return rootView;
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

    private interface ProgressListener {
        void onProgress(long bytesRead, long contentLength, boolean done);
    }

    private static class ProgressResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @NonNull
        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(new ForwardingSource(responseBody.source()) {
                    long totalBytesRead = 0;

                    @Override
                    public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                        long bytesRead = super.read(sink, byteCount);
                        totalBytesRead += (bytesRead != -1) ? bytesRead : 0;
                        progressListener.onProgress(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                        return bytesRead;
                    }
                });
            }
            return bufferedSource;
        }
    }

    private static float megabytesPerSecond = 0;
    private static float lastBytesRead = 0;
    private static long lastTimeStamp = 0;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public int downloadRootFS(String commit) {
        progressBar.post(() -> {
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            progressBarProgress.setVisibility(View.VISIBLE);
        });

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://github.com/KreitinnSoftware/MiceWine-RootFS-Generator/releases/download/" + commit + "/MiceWine-RootFS-" + commit + "-" + deviceArch + ".rat")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return response.code();

            ProgressResponseBody progressBody = new ProgressResponseBody(response.body(), (bytesRead, contentLength, done) -> {
                int progress = (contentLength > 0) ? (int) (bytesRead * 100 / contentLength) : 0;

                long now = System.currentTimeMillis();
                long deltaTime = now - lastTimeStamp;
                if (deltaTime > 500) {
                    float deltaSeconds = deltaTime / 1000F;
                    megabytesPerSecond = ((bytesRead - lastBytesRead) / (float) MEGABYTE) / deltaSeconds;
                    lastTimeStamp = now;
                    lastBytesRead = bytesRead;
                }

                String progressBarText = String.format("%s%% - %.2fM/%.2fM - %.2f MB/s", progress, (float) bytesRead / MEGABYTE, (float) contentLength / MEGABYTE, megabytesPerSecond);

                progressBar.post(() -> {
                    progressBar.setProgress(progress);
                    progressBarProgress.setText(progressBarText);
                });
            });

            File file = new File(appRootDir, "rootfs.rat");

            try (BufferedSource source = progressBody.source();
                 OutputStream output = new FileOutputStream(file)
            ) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = source.inputStream().read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }

            return 0;
        } catch (IOException e) {
            return -1;
        }
    }
}