package com.micewine.emu.core;

import static com.micewine.emu.fragments.SetupFragment.progressBarIsIndeterminate;
import static com.micewine.emu.fragments.SetupFragment.progressBarValue;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.tukaani.xz.SeekableFileInputStream;
import org.tukaani.xz.SeekableXZInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TarUtils {
    public static boolean isXZ(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] header = new byte[6];
            if (fis.read(header) != 6) return false;

            return header[0] == (byte) 0xFD &&
                    header[1] == 0x37 &&
                    header[2] == 0x7A &&
                    header[3] == 0x58 &&
                    header[4] == 0x5A &&
                    header[5] == 0x00;
        } catch (IOException e) {
            return false;
        }
    }


    public static InputStream getFileStreamFromTarXZ(String filePath, String fileName) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            XZCompressorInputStream xzCompressorInputStream = new XZCompressorInputStream(bufferedInputStream);
            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(xzCompressorInputStream)
        ) {
            TarArchiveEntry entry;

            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (!fileName.equals(entry.getName())) continue;
                if (entry.isDirectory()) return null;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[8196];
                int len;

                while ((len = tarArchiveInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }

                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    private static long getTarXZFileSize(String filePath) {
        try (SeekableFileInputStream fileInputStream = new SeekableFileInputStream(filePath);
             SeekableXZInputStream xz = new SeekableXZInputStream(fileInputStream)
        ) {
            return xz.length();
        } catch (IOException e) {
            return 0;
        }
    }

    public static void untar(String filePath, String outputDir) throws IOException {
        AtomicBoolean isExtracting = new AtomicBoolean(true);
        AtomicLong bytesExtracted = new AtomicLong(0);
        long totalBytes = getTarXZFileSize(filePath);

        progressBarIsIndeterminate = false;

        new Thread(() -> {
            while (isExtracting.get()) {
                progressBarValue = (int) ((float) bytesExtracted.get() / (float) totalBytes * 100F);
                try {
                    Thread.sleep(125);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();

        try (
            FileInputStream fileInputStream = new FileInputStream(filePath);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            XZCompressorInputStream xzInputStream = new XZCompressorInputStream(bufferedInputStream);
            TarArchiveInputStream tarInputStream = new TarArchiveInputStream(xzInputStream)
        ) {
            TarArchiveEntry entry;

            while ((entry = tarInputStream.getNextEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                    continue;
                }

                outputFile.getParentFile().mkdirs();

                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[8196];
                int bytesRead;
                while ((bytesRead = tarInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    bytesExtracted.addAndGet(8196);
                }

                fileOutputStream.close();
            }
        }

        isExtracting.set(false);
    }
}
