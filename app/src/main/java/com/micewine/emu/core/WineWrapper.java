package com.micewine.emu.core;

import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.activities.MainActivity.wineDisksFolder;
import static com.micewine.emu.activities.MainActivity.winePrefix;
import static com.micewine.emu.activities.MainActivity.winePrefixesDir;
import static com.micewine.emu.core.EnvVars.getEnv;
import static com.micewine.emu.core.ShellLoader.runCommand;
import static com.micewine.emu.core.ShellLoader.runCommandWithOutput;
import static com.micewine.emu.fragments.DebugSettingsFragment.availableCPUs;

import android.system.ErrnoException;
import android.system.Os;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WineWrapper {
    private static final String IS_BOX64 = deviceArch.equals("x86_64") ? "" : "box64";

    public static String getCpuHexMask(String cpuAffinityMask) {
        int availCpus = Runtime.getRuntime().availableProcessors();
        List<Character> cpuMask = new ArrayList<>();

        for (int i = 0; i < availCpus; i++) {
            cpuMask.add('0');
        }

        String cpuAffinity = cpuAffinityMask.replace(",", "");

        for (char element : cpuAffinity.toCharArray()) {
            int index = Math.abs(Character.getNumericValue(element) - availCpus) - 1;
            cpuMask.set(index, '1');
        }

        StringBuilder binary = new StringBuilder();
        for (char c : cpuMask) {
            binary.append(c);
        }

        return Integer.toHexString(Integer.parseInt(binary.toString(), 2));
    }

    public static boolean[] maskToCpuAffinity(long mask) {
        int availCpus = Runtime.getRuntime().availableProcessors();
        boolean[] affinity = new boolean[availCpus];

        for (int i = 0; i < availCpus; i++) {
            if (((mask >> i) & 1L) == 1L) {
                affinity[i] = true;
            }
        }

        return affinity;
    }

    public static void waitForProcess(String name) {
        while (true) {
            List<ExeProcess> exeProcesses = getExeProcesses();
            for (ExeProcess exeProcess : exeProcesses) {
                if (exeProcess.name.equals(name)) {
                    return;
                }
            }
            try {
                Thread.sleep(125);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void wine(String args) {
        wine(args, null);
    }

    public static void wine(String args, String cwd) {
        runCommand(
                ((cwd != null) ? "cd " + cwd + ";" : "") + getEnv() + "WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "' " + IS_BOX64 + " wine " + args , true
        );
    }

    public static void killAll() {
        runCommand(getEnv() + "WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "' " + IS_BOX64 + " wineserver -k", false);
        runCommand("pkill -SIGINT -f .exe", false);
        runCommand("pkill -SIGINT -f wineserver", false);
    }

    public static void clearDrives() {
        char letter = 'e';

        while (letter <= 'y') {
            File diskFile = new File(wineDisksFolder + "/" + letter + ":");
            if (diskFile.exists()) {
                diskFile.delete();
            }
            letter++;
        }
    }

    public static void addDrive(String path) {
        runCommand("ln -sf " + path + " " + wineDisksFolder + "/" + getAvailableDisks().get(0) + ":", false);
    }

    private static List<String> getAvailableDisks() {
        char letter = 'e';

        List<String> availableDisks = new ArrayList<>();

        while (letter <= 'z') {
            File diskFile = new File(wineDisksFolder + "/" + letter + ":");
            if (!diskFile.exists()) {
                availableDisks.add(String.valueOf(letter));
            }
            letter++;
        }

        return availableDisks;
    }

    public static void extractIcon(String exePath, String output) {
        if (exePath.toLowerCase().endsWith(".exe")) {
            runCommand(
                    getEnv() + "wrestool -x -t 14 '" + getSanitizedPath(exePath) + "' > '" + output + "'", false
            );
        }
    }

    public static String getSanitizedPath(String filePath) {
        return filePath.replace("'", "'\\''");
    }

    private static String getProcessPath(String processName, String processCwd) {
        File file = new File(processCwd, processName);
        if (file.exists()) {
            return file.toString();
        }

        String[] searchPaths = {
                wineDisksFolder + "/c:/windows/system32",
                wineDisksFolder + "/c:/windows/",
        };

        for (String path : searchPaths) {
            File exeFile = new File(path, processName);
            if (exeFile.exists()) {
                return path + "/" + processName;
            }
        }

        return "";
    }

    private static int getProcessRamUsageKB(int pid) {
        File statusFile = new File("/proc/" + pid + "/status");
        if (!statusFile.exists()) return 0;

        try {
            List<String> lines = Files.readAllLines(statusFile.toPath());

            for (String line : lines) {
                if (line.startsWith("VmRSS:")) {
                    return Integer.parseInt(line.substring(line.indexOf(":") + 1).replace("kB", "").trim());
                }
            }
        } catch (IOException ignored) {
        }

        return 0;
    }

    public static String getProcessCPUAffinity(int pid) {
        File statusFile = new File("/proc/" + pid + "/status");
        if (!statusFile.exists()) return "0";

        try {
            List<String> lines = Files.readAllLines(statusFile.toPath());

            for (String line : lines) {
                if (line.startsWith("Cpus_allowed:")) {
                    return line.substring(line.indexOf(":") + 1).replace(",", "").trim();
                }
            }
        } catch (IOException ignored) {
        }

        return "0";
    }

    public static int getWinPidByIndex(int index) {
        if (index < 0) return -1;

        String[] taskList = runCommandWithOutput(
                getEnv() + "WINEDEBUG=0 BOX64_LOG=0 WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "' " + IS_BOX64 + " wine tasklist | grep .exe", false
        ).split("\n");

        if (index > taskList.length) return -1;

        return Integer.parseInt(taskList[index].replaceAll(" +", " ").split(" ")[1]);
    }

    public static List<ExeProcess> getExeProcesses() {
        List<ExeProcess> exeProcesses = new ArrayList<>();

        File[] processes = new File("/proc").listFiles();

        if (processes == null) return exeProcesses;

        for (File processFile : processes) {
            if (processFile.isDirectory()) {
                int unixPid;

                try {
                    unixPid = Integer.parseInt(processFile.getName());
                } catch (NumberFormatException ignored) {
                    continue;
                }

                File cmdlineFile = new File(processFile, "cmdline");
                if (!cmdlineFile.exists()) continue;

                try {
                    String cmdline = Files.readAllLines(cmdlineFile.toPath()).toString().trim();
                    String processName = cmdline.split("\u0000")[0].substring(1);

                    if (!processName.toLowerCase().endsWith(".exe")) continue;

                    processName = processName.substring(processName.lastIndexOf("\\") + 1);

                    String cwd;

                    try {
                        cwd = Os.readlink(processFile.toPath() + "/cwd");
                    } catch (ErrnoException ignored) {
                        cwd = "/";
                    }

                    String processPath = getProcessPath(processName, cwd);
                    String iconPath = usrDir + "/icons/" + processName.substring(0, processName.indexOf(".exe")) + "-thumbnail";
                    int ramUsageKB = getProcessRamUsageKB(unixPid);
                    float cpuUsage;

                    try {
                        cpuUsage = Float.parseFloat(runCommandWithOutput("ps -p " + unixPid + " -o %cpu=", false).trim());
                    } catch (NumberFormatException ignored) {
                        cpuUsage = 0F;
                    }

                    extractIcon(processPath, iconPath);

                    exeProcesses.add(
                            new ExeProcess(processName, unixPid, cwd, processPath, iconPath, ramUsageKB, cpuUsage / availableCPUs.length)
                    );
                } catch (IOException ignored) {
                }
            }
        }

        return exeProcesses;
    }

    public static class ExeProcess {
        String name;
        int unixPid;
        String cwd;
        String path;
        String iconPath;
        int ramUsageKB;
        float cpuUsage;

        public ExeProcess(String name, int unixPid, String cwd, String path, String iconPath, int ramUsageKB, float cpuUsage) {
            this.name = name;
            this.unixPid = unixPid;
            this.cwd = cwd;
            this.path = path;
            this.iconPath = iconPath;
            this.ramUsageKB = ramUsageKB;
            this.cpuUsage = cpuUsage;
        }

        public String getName() {
            return name;
        }

        public int getRamUsageKB() {
            return ramUsageKB;
        }

        public float getCpuUsage() {
            return cpuUsage;
        }

        public String getIconPath() {
            return iconPath;
        }

        public int getUnixPid() {
            return unixPid;
        }
    }
}