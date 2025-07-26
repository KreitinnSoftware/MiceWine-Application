package com.micewine.emu.utils;

import static com.micewine.emu.activities.MainActivity.wineDisksFolder;

import java.io.File;
import java.io.IOException;

public class DriveUtils {
    public static class DriveInfo {
        String letter;
        String source;
        String relativePath;

        public DriveInfo(String letter, String source, String relativePath) {
            this.letter = letter;
            this.source = source;
            this.relativePath = relativePath;
        }

        public String getWindowsPath() {
            return getWindowsPath(false);
        }

        public String getWindowsPath(boolean isAbsolute) {
            String path = letter + ":\\" + relativePath.replace("/", "\\");
            if (isAbsolute) {
                path = wineDisksFolder.getPath().replace("/", "\\") + "\\" + path;
            }
            return path;
        }

        public String getUnixPath() {
            String cleanRelativePath = relativePath;

            while (cleanRelativePath.endsWith("/")) {
                cleanRelativePath = cleanRelativePath.substring(0, cleanRelativePath.length() - 1);
            }

            return wineDisksFolder.getPath() + "/" + letter + ":" + "/" + cleanRelativePath;
        }
    }

    public static DriveInfo parseUnixPath(String path) {
        File[] drives = wineDisksFolder.listFiles();
        if (drives == null) return null;

        for (File file : drives) {
            try {
                if (file.isDirectory() && path.startsWith(file.getAbsolutePath())) {
                    String symlinkTo = file.getCanonicalPath();
                    String letter = String.valueOf(file.getAbsolutePath().replace(wineDisksFolder.getPath(), "").charAt(0));
                    String relativePath = path.replace(wineDisksFolder.getPath(), "").replace(letter + ":/", "");

                    return new DriveInfo(letter, symlinkTo, relativePath);
                }
            } catch (IOException ignored) {
                return null;
            }
        }
        return null;
    }

    public static DriveInfo parseWindowsPath(String path) {
        String unixPath = wineDisksFolder.getPath() + path.replace("\\", "/");
        return parseUnixPath(unixPath);
    }
}