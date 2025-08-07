package com.micewine.emu.utils;

import static com.micewine.emu.activities.MainActivity.wineDisksFolder;

import java.io.File;
import java.io.IOException;

import mslinks.ShellLink;
import mslinks.ShellLinkException;

public class DriveUtils {
    public static String parseWindowsPath(String unixPath) {
        return unixPath.replace(wineDisksFolder.getPath(), "").substring(1).replace("/", "\\");
    }

    public static String parseUnixPath(String windowsPath) {
        char lowerCaseLetter = Character.toLowerCase(windowsPath.charAt(0));
        return wineDisksFolder.getPath() + "/" + lowerCaseLetter + windowsPath.substring(1).replace("\\", "/");
    }
}