package com.micewine.emu.core;

import static com.micewine.emu.activities.MainActivity.appRootDir;
import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.adapters.AdapterFiles.MEGABYTE;
import static com.micewine.emu.core.NotificationHelper.createNotificationBuilder;
import static com.micewine.emu.core.NotificationHelper.removeAllNotifications;
import static com.micewine.emu.core.NotificationHelper.updateNotification;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

public class RootFSDownloaderService extends Service {
    private NotificationCompat.Builder builder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String commit = intent.getStringExtra("commit");

        builder = createNotificationBuilder(this);
        startForeground(1, builder.build());

        new Thread(() -> {
            downloadRootFS(commit);
            stopForeground(true);
            stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForeground(true);
        stopSelf();
        removeAllNotifications();
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
    private void downloadRootFS(String commit) {
        sendBroadcast(new Intent(DOWNLOAD_START));

        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url("https://github.com/KreitinnSoftware/MiceWine-RootFS-Generator/releases/download/" + commit + "/MiceWine-RootFS-" + commit + "-" + deviceArch + ".rat")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return;

            ProgressResponseBody progressBody = new ProgressResponseBody(response.body(), (bytesRead, contentLength, done) -> {
                int progress = (contentLength > 0) ? (int) (bytesRead * 100 / contentLength) : 0;

                long now = System.currentTimeMillis();
                long deltaTime = now - lastTimeStamp;
                if (deltaTime > 500) {
                    float deltaSeconds = deltaTime / 1000F;
                    megabytesPerSecond = ((bytesRead - lastBytesRead) / (float) MEGABYTE) / deltaSeconds;
                    lastTimeStamp = now;
                    lastBytesRead = bytesRead;

                    String progressBarText = String.format("%s%% - %.2fM/%.2fM - %.2f MB/s", progress, (float) bytesRead / MEGABYTE, (float) contentLength / MEGABYTE, megabytesPerSecond);

                    builder.setProgress(100, progress, false);
                    builder.setContentText(progressBarText);
                    updateNotification(builder);

                    Intent updateProgressIntent = new Intent(UPDATE_PROGRESS_BAR);

                    updateProgressIntent.putExtra("progressText", progressBarText);
                    updateProgressIntent.putExtra("progress", progress);

                    sendBroadcast(updateProgressIntent);
                }
            });

            File file = new File(appRootDir, "rootfs.rat");

            try (BufferedSource source = progressBody.source();
                 OutputStream output = new FileOutputStream(file)
            ) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = source.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }

            sendBroadcast(new Intent(DOWNLOAD_DONE));
        } catch (IOException e) {
            Intent failedDownloadIntent = new Intent(DOWNLOAD_FAILED);

            failedDownloadIntent.putExtra("errorMessage", e.getMessage());

            sendBroadcast(failedDownloadIntent);
        }
    }

    public static String UPDATE_PROGRESS_BAR = "com.micewine.emu.UPDATE_PROGRESS_BAR";
    public static String DOWNLOAD_FAILED = "com.micewine.emu.DOWNLOAD_FAILED";
    public static String DOWNLOAD_START = "com.micewine.emu.DOWNLOAD_START";
    public static String DOWNLOAD_DONE = "com.micewine.emu.DOWNLOAD_DONE";
}
