package com.micewine.emu.utils;

import static com.micewine.emu.activities.MainActivity.wineDisksFolder;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilePathResolver {
    public static String resolvePath(Context context, Uri uri) {
        String realPath = resolveRealPathFromUri(context, uri);

        if (realPath != null) {
            File[] wineDisks = wineDisksFolder.listFiles();
            if (wineDisks == null) return realPath;

            try {
                for (File disk : wineDisks) {
                    String symlinkTo = disk.getCanonicalPath();
                    if (realPath.startsWith(symlinkTo)) {
                        return realPath.replaceFirst(symlinkTo, disk.getPath());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return realPath;
    }

    /*
    private fun getDosHomeDirs(): List<String> {
        val dosHomeDirs = mutableListOf<String>()
        val dosHomeDir = File(MainActivity.fileManagerDefaultDir)

        if (dosHomeDir.exists() && dosHomeDir.isDirectory) {
            dosHomeDirs.addAll(dosHomeDir.listFiles()?.map { it.absolutePath } ?: emptyList())
        }

        return dosHomeDirs
    }
     */

    private static String resolveRealPathFromUri(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] docSplit = docId.split(":");
                String type = docSplit[0];

                if ("primary".equalsIgnoreCase(type)) {
                    if (docSplit.length > 1) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + docSplit[1];
                    } else {
                        return Environment.getExternalStorageDirectory().toString() + "/";
                    }
                } else {
                    File file = new File("storage" + "/" + docId.replace(":", "/"));
                    if (file.exists()) {
                        return "/storage/" + docId.replace(":", "/");
                    }
                    List<String> availableExternalStorages = getStorageDirectories();
                    String root = "";

                    for (String storagePath : availableExternalStorages) {
                        if (docSplit[1].startsWith("/")) {
                            root = storagePath + docSplit[1];
                        } else {
                            root = storagePath + "/" + docSplit[1];
                        }
                    }
                    if (root.contains(type)) {
                         return "storage" + "/" + docId.replace(":", "/");
                    } else {
                        if (root.startsWith("/storage/") || root.startsWith("storage/")) {
                            return root;
                        } else if (root.startsWith("/")) {
                            return "/storage" + root;
                        } else {
                            return "/storage/" + root;
                        }
                    }
                }
            } else if (isRawDownloadsDocument(uri)) {
                String fileName = getFilePath(context, uri);
                String subFolderName = getSubFolders(uri);
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + subFolderName + fileName;
                }
                String docId = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));

                return getDataColumn(context, contentUri, null, null);
            } else if (isDownloadsDocument(uri)) {
                String fileName = getFilePath(context, uri);

                if (fileName != null) {
                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                }

                String docId = DocumentsContract.getDocumentId(uri);

                if (docId.startsWith("raw:")) {
                    docId = docId.replaceFirst("raw:", "");
                    if (new File(docId).exists()) return docId;
                }
                if (docId.startsWith("raw%3A%2F")) {
                    docId = docId.replaceFirst("raw%3A%2F", "");
                    if (new File(docId).exists()) return docId;
                }
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] docSplit = docId.split(":");
                String selection = "_id=?";
                String[] selectionArgs = new String[] { docSplit[1] };

                return getDataColumn(context, null, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            String dataColumn = getDataColumn(context, uri, null, null);
            if (dataColumn == null) {
                Log.e("FileResolver", "Failed to resolve path from uri.");
            }
            return dataColumn;
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getSubFolders(Uri uri) {
        String replaceChars = uri.toString()
                .replace("%2F", "/")
                .replace("%20", " ")
                .replace("%3A", ":");

        String[] bits = replaceChars.split("/");

        int size = bits.length;
        if (size < 6) {
            return "";
        }

        String sub5 = bits[size - 2];
        String sub4 = bits[size - 3];
        String sub3 = bits[size - 4];
        String sub2 = bits[size - 5];
        String sub1 = bits[size - 6];

        if ("Download".equals(sub1)) {
            return sub2 + "/" + sub3 + "/" + sub4 + "/" + sub5 + "/";
        } else if ("Download".equals(sub2)) {
            return sub3 + "/" + sub4 + "/" + sub5 + "/";
        } else if ("Download".equals(sub3)) {
            return sub4 + "/" + sub5 + "/";
        } else if ("Download".equals(sub4)) {
            return sub5 + "/";
        } else {
            return "";
        }
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[] { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    private static String getFilePath(Context context, Uri uri) {

        String[] projection = new String[] { MediaStore.Files.FileColumns.DISPLAY_NAME };
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isRawDownloadsDocument(Uri uri) {
        return uri.toString().contains("com.android.providers.downloads.documents/document/raw");
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static List<String> getStorageDirectories() {
        List<String> paths = new ArrayList<>();
        paths.add(Environment.getExternalStorageDirectory().toString()); // Adiciona o caminho padrão do armazenamento externo
        paths.addAll(getAdditionalStoragePaths());
        return paths;
    }

    private static List<String> getAdditionalStoragePaths() {
        List<String> storagePaths = new ArrayList<>();
        File file = new File("/storage");

        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    storagePaths.add(f.getAbsolutePath());
                }
            }
        }

        return storagePaths;
    }

}