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
        if (!statusFile.exists()) {
            return 0;
        }

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
        if (!statusFile.exists()) {
            return "0";
        }

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

    public static int getWinPidByName(String processName) {
        String[] taskList = runCommandWithOutput(
                getEnv() + "BOX64_LOG=0 WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "' " + IS_BOX64 + " wine tasklist" , false
        ).split("\n");

        for (String s : taskList) {
            if (s.contains(processName)) {
                return Integer.parseInt(s.trim().replaceAll("  +", " ").split(" ")[1]);
            }
        }

        return -1;
    }

    public static List<ExeProcess> getExeProcesses() {
        List<ExeProcess> exeProcesses = new ArrayList<>();

        File[] processes = new File("/proc").listFiles();

        if (processes != null) {
            for (File process : processes) {
                if (process.isDirectory()) {
                    Integer unixPid;

                    try {
                        unixPid = Integer.parseInt(process.getName());
                    } catch (NumberFormatException ignored) {
                        unixPid = null;
                    }

                    File cmdlineFile = new File(process, "cmdline");
                    if (cmdlineFile.exists() && unixPid != null) {
                        try {
                            String cmdline = Files.readAllLines(cmdlineFile.toPath()).toString().trim();
                            String processName = cmdline.split("\u0000")[0];

                            if (processName != null && processName.toLowerCase().endsWith(".exe")) {
                                processName = processName.substring(1);
                                String cwd = runCommandWithOutput("readlink " + process.toPath() + "/cwd", false).trim();
                                String path = getProcessPath(processName, cwd);
                                String iconPath = usrDir + "/icons/" + processName.substring(0, processName.indexOf(".exe")) + "-thumbnail";
                                int ramUsageKB = getProcessRamUsageKB(unixPid);
                                float cpuUsage;

                                try {
                                    cpuUsage = Float.parseFloat(runCommandWithOutput("ps -p " + unixPid + " -o %cpu=", false).trim());
                                } catch (NumberFormatException ignored) {
                                    cpuUsage = 0F;
                                }

                                extractIcon(path, iconPath);

                                exeProcesses.add(
                                        new ExeProcess(processName, unixPid, cwd, path, iconPath, ramUsageKB, cpuUsage / availableCPUs.length)
                                );
                            }
                        } catch (IOException ignored) {
                        }
                    }
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