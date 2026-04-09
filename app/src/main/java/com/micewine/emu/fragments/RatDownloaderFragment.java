package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.adapters.AdapterFiles.MEGABYTE;
import static com.micewine.emu.core.RatPackageManager.checkPackageInstalled;
import static com.micewine.emu.core.RatPackageManager.getRatCategoryString;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

public class RatDownloaderFragment extends Fragment {
    private final String prefix;
    private final int type;
    private final String anotherPrefix;
    private final boolean isInitialSetup;

    public RatDownloaderFragment(int type) {
        this.prefix = getRatCategoryString(type);
        this.type = type;
        this.anotherPrefix = prefix;
        this.isInitialSetup = false;
    }

    public RatDownloaderFragment(int type, boolean isInitialSetup) {
        this.prefix = getRatCategoryString(type);
        this.type = type;
        this.anotherPrefix = prefix;
        this.isInitialSetup = isInitialSetup;
    }

    public RatDownloaderFragment(int type, String anotherPrefix) {
        this.prefix = getRatCategoryString(type);
        this.type = type;
        this.anotherPrefix = anotherPrefix;
        this.isInitialSetup = false;
    }

    public RatDownloaderFragment(int type, String anotherPrefix, boolean isInitialSetup) {
        this.prefix = getRatCategoryString(type);
        this.type = type;
        this.anotherPrefix = anotherPrefix;
        this.isInitialSetup = isInitialSetup;}

    private final ArrayList<AdapterRatPackage.Item> ratList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewGeneralSettings);

        setAdapter();

        return rootView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        recyclerView.setAdapter(new AdapterRatPackage(ratList, requireActivity(), true, isInitialSetup));

        ratList.clear();

        new Thread(() -> {
            List<RepoRatPackage> packageList = fetchPackages();

            packageList.forEach((repoRatPackage) -> {
                if (repoRatPackage.ratPackage.category.equals(prefix) || repoRatPackage.ratPackage.category.equals(anotherPrefix)) {
                    addToAdapter(repoRatPackage.ratPackage.name, repoRatPackage.ratPackage.version, repoRatPackage.repoRatName, checkPackageInstalled(repoRatPackage.ratPackage.name, repoRatPackage.ratPackage.category, repoRatPackage.ratPackage.version));
                }
            });

            recyclerView.post(() -> {
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void addToAdapter(String title, String description, String repoRatName, boolean installed) {
        ratList.add(
                new AdapterRatPackage.Item(title, description, "", type, false, repoRatName, installed)
        );
    }

    public static List<RepoRatPackage> fetchPackages() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://github.com/KreitinnSoftware/MiceWine-Packages/releases/download/default/index.json").build();
        Type type = new TypeToken<Map<String, RatPackageModel>>() {}.getType();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException();

            Map<String, RatPackageModel> map = gson.fromJson(response.body().string(), type);
            List<RepoRatPackage> packagesList = new ArrayList<>(map.size());

            for (Map.Entry<String, RatPackageModel> entry : map.entrySet()) {
                RatPackageModel value = entry.getValue();

                if (deviceArch.equals(value.architecture) || "any".equals(value.architecture) || "Wine".equals(value.category)) {
                    packagesList.add(new RepoRatPackage(entry.getKey(), value));
                }
            }

            packagesList.sort((a, b) -> {
                List<Integer> va = extractNumbers(a.ratPackage.version);
                List<Integer> vb = extractNumbers(b.ratPackage.version);

                int max = Math.max(va.size(), vb.size());
                for (int i = 0; i < max; i++) {
                    int na = i < va.size() ? va.get(i) : 0;
                    int nb = i < vb.size() ? vb.get(i) : 0;

                    if (na != nb) {
                        return Integer.compare(nb, na);
                    }
                }
                return 0;
            });


            return packagesList;
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<Integer> extractNumbers(String version) {
        List<Integer> numbers = new ArrayList<>();
        Matcher m = Pattern.compile("\\d+").matcher(version);

        while (m.find()) {
            numbers.add(Integer.parseInt(m.group()));
        }
        return numbers;
    }


    public static class RepoRatPackage {
        public String repoRatName;
        public RatPackageModel ratPackage;

        public RepoRatPackage(String repoRatName, RatPackageModel ratPackage) {
            this.repoRatName = repoRatName;
            this.ratPackage = ratPackage;
        }
    }

    public static class RatPackageModel {
        public String name;
        public String category;
        public String version;
        public String architecture;
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

    @SuppressLint("DefaultLocale")
    public static boolean downloadPackage(String name, ProgressBar progressBar, TextView progressText) {
        progressBar.post(() -> progressBar.setVisibility(View.VISIBLE));
        progressText.post(() -> progressText.setVisibility(View.VISIBLE));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://github.com/KreitinnSoftware/MiceWine-Packages/releases/download/default/" + name)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return false;

            ProgressResponseBody progressBody = new ProgressResponseBody(response.body(), (bytesRead, contentLength, done) -> {
                int progress = (contentLength > 0) ? (int) (bytesRead * 100 / contentLength) : 0;
                progressBar.post(() -> progressBar.setProgress(progress));

                long now = System.currentTimeMillis();
                long deltaTime = now - lastTimeStamp;
                if (deltaTime > 500) {
                    float deltaSeconds = deltaTime / 1000F;
                    megabytesPerSecond = ((bytesRead - lastBytesRead) / (float) MEGABYTE) / deltaSeconds;
                    lastTimeStamp = now;
                    lastBytesRead = bytesRead;

                    String progressBarText = String.format("%s%% - %.2fM/%.2fM - %.2f MB/s", progress, (float) bytesRead / MEGABYTE, (float) contentLength / MEGABYTE, megabytesPerSecond);

                    progressText.post(() -> progressText.setText(progressBarText));
                }
            });

            File file = new File(tmpDir, name);
            try (BufferedSource source = progressBody.source();
                 OutputStream output = new FileOutputStream(file)
            ) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = source.inputStream().read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}