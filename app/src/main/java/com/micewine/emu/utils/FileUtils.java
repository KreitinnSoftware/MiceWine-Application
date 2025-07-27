package com.micewine.emu.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtils {
    public static void copyRecursively(File source, File target) {
        if (!source.exists()) {
            return;
        }

        Path sourcePath = source.toPath();
        Path targetPath = target.toPath();

        try (Stream<Path> paths = Files.walk(sourcePath)) {
            paths.forEach(path -> {
                try {
                    Path relativePath = sourcePath.relativize(path);
                    Path targetResolved = targetPath.resolve(relativePath);

                    if (Files.isDirectory(path)) {
                        if (!Files.exists(targetResolved)) {
                            Files.createDirectories(targetResolved);
                        }
                    } else {
                        Files.copy(path, targetResolved, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static void deleteDirectoryRecursively(Path directory) {
        if (!Files.exists(directory)) return;

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static String getFileExtension(File file) {
        if (file == null) return "";

        String name = file.getName();
        int lastDot = name.lastIndexOf('.');

        if (lastDot == -1 || lastDot == 0 || lastDot == name.length() - 1) {
            return "";
        }

        return name.substring(lastDot + 1);
    }
}
