package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.activities.WelcomeActivity.selectedRatPackages;
import static com.micewine.emu.adapters.AdapterFiles.MEGABYTE;
import static com.micewine.emu.core.RatPackageManager.installRat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterRatPackageDownload;
import com.micewine.emu.core.RatPackageManager;
import com.micewine.emu.databinding.FragmentCoreComponentsDownloaderBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

public class CoreComponentsDownloaderFragment extends Fragment {
    private final ArrayList<AdapterRatPackageDownload.Item> ratList = new ArrayList<>();
    private AdapterRatPackageDownload adapter;
    public final static String ACTION_START_DOWNLOAD = "com.micewine.emu.ACTION_START_DOWNLOAD";
    public final static String ACTION_END_DOWNLOAD = "com.micewine.emu.ACTION_END_DOWNLOAD";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_START_DOWNLOAD.equals(intent.getAction())) {
                populateList();
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = FragmentCoreComponentsDownloaderBinding.inflate(inflater, container, false).getRoot();

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);

        adapter = new AdapterRatPackageDownload(ratList, requireContext());

        recyclerView.setAdapter(adapter);

        requireActivity().registerReceiver(broadcastReceiver, new IntentFilter(ACTION_START_DOWNLOAD) {{
            addAction(ACTION_END_DOWNLOAD);
        }});

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(broadcastReceiver);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void populateList() {
        ratList.clear();
        selectedRatPackages.forEach((this::addToAdapter));

        adapter.notifyDataSetChanged();

        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < selectedRatPackages.size(); i++) {
            int buttonIndex = i;

            executor.execute(() -> {
                DownloadCallback downloadCallback = (progress, megabytesPerSecond, bytesRead, contentLength) ->
                        requireActivity().runOnUiThread(() -> {
                            ratList.get(buttonIndex).installing = false;
                            ratList.get(buttonIndex).progress = progress;
                            ratList.get(buttonIndex).megabytesPerSecond = megabytesPerSecond;
                            ratList.get(buttonIndex).bytesRead = bytesRead;
                            ratList.get(buttonIndex).contentLength = contentLength;

                            adapter.notifyItemChanged(buttonIndex, AdapterRatPackageDownload.PAYLOAD_UPDATE_PROGRESS);
                        });

                downloadPackage(selectedRatPackages.get(buttonIndex).repoRatName, downloadCallback);

                File file = new File(tmpDir, selectedRatPackages.get(buttonIndex).repoRatName);

                SetupFragment.ProgressCallback installCallback = new SetupFragment.ProgressCallback() {
                    @Override
                    public void onProgressChanged(int progress) {
                        requireActivity().runOnUiThread(() -> {
                            ratList.get(buttonIndex).installing = true;
                            ratList.get(buttonIndex).progress = progress;
                            ratList.get(buttonIndex).megabytesPerSecond = 0;
                            ratList.get(buttonIndex).bytesRead = 0;
                            ratList.get(buttonIndex).contentLength = 0;

                            adapter.notifyItemChanged(buttonIndex, AdapterRatPackageDownload.PAYLOAD_UPDATE_PROGRESS);
                        });
                    }

                    @Override
                    public void setProgressBarIndeterminate(boolean indeterminate) {
                    }

                    @Override
                    public void setDialogText(String text) {
                    }
                };

                installRat(new RatPackageManager.RatPackage(file.getPath()), installCallback);

                file.delete();
                requireActivity().runOnUiThread(() -> {
                    ratList.get(buttonIndex).installed = true;
                    adapter.notifyItemChanged(buttonIndex, AdapterRatPackageDownload.PAYLOAD_MARK_INSTALLED);
                });
            });
        }

        executor.shutdown();

        new Thread(() -> {
            try {
                executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
            requireContext().sendBroadcast(new Intent(ACTION_END_DOWNLOAD));
        }).start();
    }

    private void addToAdapter(RatDownloaderFragment.RepoRatPackage repoRatPackage) {
        ratList.add(
                new AdapterRatPackageDownload.Item(repoRatPackage.ratPackage.name, repoRatPackage.ratPackage.version)
        );
    }

    public interface DownloadCallback {
        void updateProgress(int progress, float megabytesPerSecond, long bytesRead, long contentLength);
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

    public static void downloadPackage(String name, DownloadCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://github.com/KreitinnSoftware/MiceWine-Packages/releases/download/default/" + name)
                .build();

        final float[] megabytesPerSecond = {0};
        final float[] lastBytesRead = {0};
        final long[] lastTimeStamp = {0};

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return;

            ProgressResponseBody progressBody = new ProgressResponseBody(response.body(), (bytesRead, contentLength, done) -> {
                int progress = (contentLength > 0) ? (int) (bytesRead * 100 / contentLength) : 0;

                long now = System.currentTimeMillis();
                long deltaTime = now - lastTimeStamp[0];
                if (deltaTime > 500) {
                    float deltaSeconds = deltaTime / 1000F;
                    megabytesPerSecond[0] = ((bytesRead - lastBytesRead[0]) / (float) MEGABYTE) / deltaSeconds;
                    lastTimeStamp[0] = now;
                    lastBytesRead[0] = bytesRead;

                    callback.updateProgress(progress, megabytesPerSecond[0], bytesRead, contentLength);
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

        } catch (IOException ignored) {
        }
    }
}