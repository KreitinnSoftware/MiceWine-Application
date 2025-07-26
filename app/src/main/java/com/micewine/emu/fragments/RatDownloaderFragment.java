package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.core.RatPackageManager.checkPackageInstalled;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import java.util.Map;
import java.util.TreeMap;

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

    public RatDownloaderFragment(String prefix, int type) {
        this.prefix = prefix;
        this.type = type;
        this.anotherPrefix = prefix;
    }

    public RatDownloaderFragment(String prefix, int type, String anotherPrefix) {
        this.prefix = prefix;
        this.type = type;
        this.anotherPrefix = anotherPrefix;
    }

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
        recyclerView.setAdapter(new AdapterRatPackage(ratList, requireActivity(), true));

        ratList.clear();

        new Thread(() -> {
            Map<String, RatPackageModel> packageList = fetchPackages();
            if (packageList != null) {
                packageList.forEach((name, packageModel) -> {
                    if (packageModel.category.equals(prefix) || packageModel.category.equals(anotherPrefix)) {
                        addToAdapter(packageModel.name, packageModel.version, "", false, name, checkPackageInstalled(packageModel.name, packageModel.category, packageModel.version));
                    }
                });

                recyclerView.post(() -> {
                    RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void addToAdapter(String title, String description, String folderId, boolean canDelete, String repoRatName, boolean installed) {
        ratList.add(
                new AdapterRatPackage.Item(title, description, folderId, type, canDelete, repoRatName, installed)
        );
    }

    private Map<String, RatPackageModel> fetchPackages() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://github.com/KreitinnSoftware/MiceWine-Repository/releases/download/default/index.json").build();
        Type type = new TypeToken<Map<String, RatPackageModel>>() {}.getType();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    Map<String, RatPackageModel> map = gson.fromJson(response.body().string(), type);
                    Map<String, RatPackageModel> filteredMap = new TreeMap<>();

                    for (Map.Entry<String, RatPackageModel> entry : map.entrySet()) {
                        RatPackageModel value = entry.getValue();

                        if (deviceArch.equals(value.architecture) || "any".equals(value.architecture)) {
                            filteredMap.put(entry.getKey(), value);
                        }
                    }

                    return filteredMap;
                }
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    public static class RatPackageModel {
        public String name;
        public String category;
        public String version;
        public String architecture;
        public String vkDriverLib;
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

    public static boolean downloadPackage(String name, ProgressBar progressBar) {
        progressBar.post(() -> progressBar.setVisibility(View.VISIBLE));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://github.com/KreitinnSoftware/MiceWine-Repository/releases/download/default/" + name)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return false;

            ProgressResponseBody progressBody = new ProgressResponseBody(response.body(), (bytesRead, contentLength, done) -> {
                int progress = (contentLength > 0) ? (int) (bytesRead * 100 / contentLength) : 0;
                progressBar.post(() -> progressBar.setProgress(progress));
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