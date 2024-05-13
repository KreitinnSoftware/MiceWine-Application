package com.micewine.emu.core;

import static com.micewine.emu.activities.MainActivity.appRootDir;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ObbExtractor {
    private Context ctx;

    public void extractZip(Context ctx, String zipFilePath, String destinationPath, ProgressBar progressBar, TextView tvProgress, Activity atv) {
        this.ctx = ctx;
        try {

            File zipFile = new File(zipFilePath);
            File destinationDir = new File(destinationPath);
            File assignedPath = new File(appRootDir + "/box64");


            if (!zipFile.exists() || !destinationDir.exists() || assignedPath.exists()) {
                atv.runOnUiThread(() -> Toast.makeText(this.ctx, "Arquives has been intalled, setup desktop", Toast.LENGTH_SHORT).show());
                atv.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                return;
            }

            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }


            FileInputStream fis = new FileInputStream(zipFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);


            int totalEntries = 0;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                totalEntries++;
            }


            zis.close();
            fis = new FileInputStream(zipFile);
            bis = new BufferedInputStream(fis);
            zis = new ZipInputStream(bis);


            int processedEntries = 0;

            while ((entry = zis.getNextEntry()) != null) {
                processedEntries++;


                String entryPath = destinationPath + File.separator + entry.getName();


                if (entry.isDirectory()) {
                    File dir = new File(entryPath);
                    dir.mkdirs();
                    continue;
                }

                File entryFile = new File(entryPath);


                entryFile.getParentFile().mkdirs();


                FileOutputStream fos = new FileOutputStream(entryFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = zis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }


                bos.close();


                int pEntries = processedEntries;
                int tEntries = totalEntries;

                int progress = (int) ((processedEntries / (float) totalEntries) * 100);
                progressBar.setProgress(progress);
                atv.runOnUiThread(() -> {
                    tvProgress.setText(progress + "%" + "(" + pEntries + "/" + tEntries + ")");
                });
            }

            zis.close();

            atv.runOnUiThread(() -> Toast.makeText(this.ctx, "Arquives has been intalled, setup desktop", Toast.LENGTH_SHORT).show());
            atv.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            atv.runOnUiThread(() -> tvProgress.setText(" "));
            atv.runOnUiThread(() -> tvProgress.setVisibility(View.GONE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}