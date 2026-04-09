package com.micewine.emu.core;

import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_CORE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.MainActivity.appRootDir;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.ratPackagesDir;
import static com.micewine.emu.adapters.AdapterRatPackage.BOX64;
import static com.micewine.emu.adapters.AdapterRatPackage.CORE;
import static com.micewine.emu.adapters.AdapterRatPackage.DXVK;
import static com.micewine.emu.adapters.AdapterRatPackage.VKD3D;
import static com.micewine.emu.adapters.AdapterRatPackage.VK_DRIVER;
import static com.micewine.emu.adapters.AdapterRatPackage.WINE;
import static com.micewine.emu.adapters.AdapterRatPackage.WINED3D;
import static com.micewine.emu.core.ShellLoader.runCommand;
import static com.micewine.emu.core.TarUtils.getFileStreamFromTarXZ;
import static com.micewine.emu.core.TarUtils.isXZ;
import static com.micewine.emu.core.TarUtils.untar;
import static com.micewine.emu.utils.FileUtils.deleteDirectoryRecursively;

import static java.util.UUID.randomUUID;

import android.content.SharedPreferences;

import com.micewine.emu.fragments.SetupFragment;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RatPackageManager {
    public static void installRat(RatPackage ratPackage, SetupFragment.ProgressCallback callback) {
        callback.setProgressBarIndeterminate(false);
        callback.onProgressChanged(0);

        String extractDir = ratPackagesDir + "/" + ratPackage.category + "-" + randomUUID();
        if (!new File(extractDir).mkdirs()) return;

        if (ratPackage.isTarXZRat) {
            try {
                untar(ratPackage.ratFile.getPath(), extractDir, callback);
            } catch (IOException e) {
                return;
            }
        } else {
            try (ZipFile ratFile = new ZipFile(ratPackage.ratFile)) {
                ratFile.setRunInThread(true);
                ratFile.extractAll(extractDir);

                while (!ratFile.getProgressMonitor().getState().equals(ProgressMonitor.State.READY)) {
                    callback.onProgressChanged(ratFile.getProgressMonitor().getPercentDone());
                    Thread.sleep(125);
                }
            } catch (Exception ignored) {
                return;
            }
        }

        callback.onProgressChanged(0);

        runCommand("chmod -R 700 " + extractDir, false);
        runCommand("sh " + extractDir + "/makeSymlinks.sh " + extractDir + "/", false);

        new File(extractDir, "makeSymlinks.sh").delete();

        switch (ratPackage.category) {
            case "Core" -> {
                if (preferences == null) return;
                if (preferences.getString(SELECTED_CORE, "").isEmpty()) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SELECTED_CORE, new File(extractDir).getName());
                    editor.apply();
                }
            }
            case "Box64" -> {
                if (preferences == null) return;
                if (preferences.getString(SELECTED_BOX64, "").isEmpty()) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SELECTED_BOX64, new File(extractDir).getName());
                    editor.apply();
                }
            }
            case "VulkanDriver", "AdrenoTools" -> {
                if (preferences == null) return;
                if (preferences.getString(SELECTED_VULKAN_DRIVER, "").isEmpty()) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SELECTED_VULKAN_DRIVER, new File(extractDir).getName());
                    editor.apply();
                }

                File pkgHeader = new File(extractDir, "pkg-header");

                try (FileWriter writer = new FileWriter(pkgHeader)) {
                    writer.write(
                            "name=" + ratPackage.name + "\n" +
                            "category=" + ratPackage.category + "\n" +
                            "version=" + ratPackage.version + "\n" +
                            "architecture=" + ratPackage.architecture + "\n" +
                            "vkDriverLib=" + extractDir + "/files/usr/lib/" + ratPackage.driverLib + "\n"
                    );
                } catch (IOException ignored) {
                }
            }
        }

        setPackageExternal(extractDir);
    }

    public static void deleteRatPackageById(String id) {
        RatPackage ratPackage = getPackageById(id);

        if (ratPackage == null) return;
        if (ratPackage.isUserInstalled) {
            deleteDirectoryRecursively(new File(ratPackagesDir, id).toPath());
        }
    }

    public static boolean haveAnyPackageByCategory(int category) {
        return !listRatPackages(getRatCategoryString(category)).isEmpty();
    }

    public static List<RatPackage> listRatPackages(int category) {
        return listRatPackages(getRatCategoryString(category), getRatCategoryString(category));
    }

    public static List<RatPackage> listRatPackages(int category, int anotherCategory) {
        return listRatPackages(getRatCategoryString(category), getRatCategoryString(anotherCategory));
    }

    public static List<RatPackage> listRatPackages(String category) {
        return listRatPackages(category, category);
    }

    public static List<RatPackage> listRatPackages(String category, String anotherCategory) {
        ArrayList<RatPackage> packagesList = new ArrayList<>();

        File packagesDir = new File(appRootDir, "packages");
        if (!packagesDir.exists()) return packagesList;

        File[] files = packagesDir.listFiles();
        if (files == null) return packagesList;

        for (File file : files) {
            if (file.isDirectory() && ((file.getName().startsWith(category + "-") || file.getName().startsWith(anotherCategory + "-")) || category.isEmpty() || anotherCategory.isEmpty())) {
                File pkgHeader = new File(file, "pkg-header");
                if (!pkgHeader.exists()) continue;

                List<String> lines;

                try {
                    lines = Files.readAllLines(pkgHeader.toPath());
                } catch (IOException ignored) {
                    lines = null;
                }

                if (lines == null || lines.size() < 5) continue;

                String pkgName = lines.get(0).substring(lines.get(0).indexOf("=") + 1);

                if (file.getName().startsWith("AdrenoToolsDriver-")) {
                    pkgName += " (AdrenoTools)";
                }

                String pkgCategory = lines.get(1).substring(lines.get(1).indexOf("=") + 1);
                String pkgVersion = lines.get(2).substring(lines.get(2).indexOf("=") + 1);
                String pkgDriverLib = lines.get(4).substring(lines.get(4).indexOf("=") + 1);

                packagesList.add(
                        new RatPackage(pkgName, pkgCategory, pkgVersion, pkgDriverLib, file.getName(), new File(file, "pkg-external").exists())
                );
            }
        }

        return packagesList;
    }

    public static List<String> listRatPackagesId(int category) {
        return listRatPackagesId(getRatCategoryString(category), getRatCategoryString(category));
    }

    public static List<String> listRatPackagesId(int category, int anotherCategory) {
        return listRatPackagesId(getRatCategoryString(category), getRatCategoryString(anotherCategory));
    }

    public static List<String> listRatPackagesId(String type) {
        return listRatPackagesId(type, type);
    }

    public static List<String> listRatPackagesId(String type, String anotherType) {
        ArrayList<String> packagesList = new ArrayList<>();

        File packagesDir = new File(appRootDir, "packages");
        if (!packagesDir.exists()) return packagesList;

        File[] files = packagesDir.listFiles();
        if (files == null) return packagesList;

        for (File file : files) {
            if (file.isDirectory() && (file.getName().startsWith(type + "-") || file.getName().startsWith(anotherType + "-"))) {
                packagesList.add(file.getName());
            }
        }

        return packagesList;
    }

    public static RatPackage getPackageById(String id) {
        File pkgHeader = new File(ratPackagesDir, id + "/pkg-header");
        if (pkgHeader.exists() && id != null) {
            List<String> lines;

            try {
                lines = Files.readAllLines(pkgHeader.toPath());
            } catch (IOException ignored) {
                lines = null;
            }

            if (lines == null || lines.size() < 5) return null;

            String pkgName = lines.get(0).substring(lines.get(0).indexOf("=") + 1);
            String pkgCategory = lines.get(1).substring(lines.get(1).indexOf("=") + 1);
            String pkgVersion = lines.get(2).substring(lines.get(2).indexOf("=") + 1);
            String pkgDriverLib = lines.get(4).substring(lines.get(4).indexOf("=") + 1);

            if (pkgCategory.startsWith("AdrenoToolsDriver-")) {
                pkgName += " (AdrenoTools)";
            }

            return new RatPackage(pkgName, pkgCategory, pkgVersion, pkgDriverLib, id, new File(ratPackagesDir, id + "/pkg-external").exists());
        }

        return null;
    }

    public static String getPackageNameVersionById(String id) {
        File pkgHeader = new File(ratPackagesDir, id + "/pkg-header");
        if (pkgHeader.exists() && id != null) {
            try {
                List<String> lines = Files.readAllLines(pkgHeader.toPath());
                return lines.get(0).substring(lines.get(0).indexOf("=") + 1) + " " + lines.get(2).substring(lines.get(2).indexOf("=") + 1);
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    public static boolean checkPackageInstalled(String name, String category, String version) {
        List<RatPackage> ratPackageList = listRatPackages("", "");

        for (RatPackage ratPackage : ratPackageList) {
            if (ratPackage.name.equals(name) && ratPackage.category.equals(category) && ratPackage.version.equals(version)) {
                return true;
            }
        }

        return false;
    }

    public static void installADToolsDriver(AdrenoToolsPackage adrenoToolsPackage, SetupFragment.ProgressCallback callback) {
        callback.setProgressBarIndeterminate(false);
        callback.onProgressChanged(0);

        ZipFile adrenoToolsFile = adrenoToolsPackage.adrenoToolsFile;

        String extractDir = ratPackagesDir + "/AdrenoToolsDriver-" + randomUUID();

        try {
            adrenoToolsFile.setRunInThread(true);
            adrenoToolsFile.extractAll(extractDir);

            while (!adrenoToolsFile.getProgressMonitor().getState().equals(ProgressMonitor.State.READY)) {
                callback.onProgressChanged(adrenoToolsFile.getProgressMonitor().getPercentDone());
                Thread.sleep(125);
            }
        } catch (Exception ignored) {
            return;
        }

        runCommand("chmod -R 700 " + extractDir, false);

        String driverLibPath = extractDir + "/" + adrenoToolsPackage.driverLib;

        File pkgHeader = new File(extractDir, "pkg-header");

        try (FileWriter writer = new FileWriter(pkgHeader)) {
            writer.write("name=" + adrenoToolsPackage.name + "\ncategory=AdrenoToolsDriver\nversion=" + adrenoToolsPackage.version + "\narchitecture=aarch64\nvkDriverLib=" + driverLibPath + "\n");
        } catch (IOException ignored) {
        }

        setPackageExternal(extractDir);
    }

    private static void setPackageExternal(String extractDir) {
        try (FileWriter writer = new FileWriter(new File(extractDir, "pkg-external"))) {
            writer.write("");
        } catch (IOException ignored) {
        }
    }

    public static class AdrenoToolsPackage {
        String name;
        String version;
        String description;
        String driverLib;
        String author;
        ZipFile adrenoToolsFile;

        public AdrenoToolsPackage(String path) {
            adrenoToolsFile = new ZipFile(path);

            try {
                if (!adrenoToolsFile.isValidZipFile()) return;

                FileHeader metaHeader = adrenoToolsFile.getFileHeader("meta.json");
                ZipInputStream inputStream = adrenoToolsFile.getInputStream(metaHeader);

                String json;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    json = reader.lines().collect(Collectors.joining("\n"));
                    AdrenoToolsMetaInfo meta = gson.fromJson(json, AdrenoToolsMetaInfo.class);

                    name = meta.name;
                    version = meta.driverVersion;
                    description = meta.description;
                    driverLib = meta.libraryName;
                    author = meta.author;
                }
            } catch (IOException ignored) {
            }
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class AdrenoToolsMetaInfo {
        public String name;
        public String description;
        public String author;
        public String driverVersion;
        public String libraryName;
    }

    public static class RatPackage {
        String name;
        String category;
        String version;
        String architecture;
        String driverLib;
        boolean isUserInstalled;
        boolean isTarXZRat;
        File ratFile;
        String folderName;

        public RatPackage(String ratPath) {
            ratFile = new File(ratPath);
            InputStream inputStream = null;

            isTarXZRat = isXZ(ratPath);

            if (isTarXZRat) {
                inputStream = getFileStreamFromTarXZ(ratPath, "pkg-header");
            } else {
                try (ZipFile zipFile = new ZipFile(ratFile)) {
                    if (!zipFile.isValidZipFile() && !zipFile.isValidZipFile()) return;

                    FileHeader ratHeader = zipFile.getFileHeader("pkg-header");

                    try (InputStream is = zipFile.getInputStream(ratHeader)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[8196];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    }
                } catch (IOException ignored) {
                }
            }

            if (inputStream == null) return;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                List<String> lines = reader.lines().collect(Collectors.toList());
                if (lines.size() < 5) return;

                name = lines.get(0).substring(lines.get(0).indexOf("=") + 1);
                category = lines.get(1).substring(lines.get(1).indexOf("=") + 1);
                version = lines.get(2).substring(lines.get(2).indexOf("=") + 1);
                architecture = lines.get(3).substring(lines.get(3).indexOf("=") + 1);
                driverLib = lines.get(4).substring(lines.get(4).indexOf("=") + 1);
            } catch (IOException ignored) {
            }
        }

        public RatPackage(String name, String category, String version, String driverLib, String folderName, boolean isUserInstalled) {
            this.name = name;
            this.category = category;
            this.version = version;
            this.driverLib = driverLib;
            this.folderName = folderName;
            this.isUserInstalled = isUserInstalled;
        }

        public String getName() {
            return name;
        }

        public String getArchitecture() {
            return architecture;
        }

        public String getFolderName() {
            return folderName;
        }

        public String getDriverLib() {
            return driverLib;
        }

        public String getVersion() {
            return version;
        }

        public String getCategory() {
            return category;
        }

        public boolean getIsUserInstalled() {
            return isUserInstalled;
        }
    }

    public static Set<String> installablePackagesCategories = Set.of("Core", "VulkanDriver", "Box64", "Wine", "DXVK", "WineD3D", "VKD3D");

    public static int getRatCategoryId(String category) {
        return switch (category) {
            case "Core" -> CORE;
            case "VulkanDriver" -> VK_DRIVER;
            case "Box64" -> BOX64;
            case "Wine" -> WINE;
            case "DXVK" -> DXVK;
            case "WineD3D" -> WINED3D;
            case "VKD3D" -> VKD3D;
            default -> -1;
        };
    }

    public static String getRatCategoryString(int category) {
        return switch (category) {
            case CORE -> "Core";
            case VK_DRIVER -> "VulkanDriver";
            case BOX64 -> "Box64";
            case WINE -> "Wine";
            case DXVK -> "DXVK";
            case WINED3D -> "WineD3D";
            case VKD3D -> "VKD3D";
            default -> "";
        };
    }
}
