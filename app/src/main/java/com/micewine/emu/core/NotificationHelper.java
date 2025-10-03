package com.micewine.emu.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.micewine.emu.R;

public class NotificationHelper {
    private static NotificationManager notificationManager;
    private static final String CHANNEL_ID = "rootfs_download";
    private static final String NOTIFICATION_NAME = "Downloads";
    private static boolean createdChannel = false;

    private static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_LOW
        );
        channel.enableLights(false);
        channel.setLightColor(Color.BLUE);
        channel.enableVibration(false);
        channel.setSound(null, null);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        createdChannel = true;
    }

    public static NotificationCompat.Builder createNotificationBuilder(Context context) {
        if (notificationManager == null) notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!createdChannel) createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentTitle(context.getString(R.string.downloading_rootfs));
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        builder.setOngoing(true);
        builder.setContentIntent(pendingIntent);
        builder.setProgress(100, 0, false);;

        return builder;
    }

    public static void updateNotification(NotificationCompat.Builder builder) {
        notificationManager.notify(1, builder.build());
    }

    public static void removeAllNotifications() {
        notificationManager.cancelAll();
    }
}
