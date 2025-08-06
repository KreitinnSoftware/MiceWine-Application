package com.micewine.emu.adapters;

import static com.micewine.emu.activities.MainActivity.ACTION_SELECT_FILE_MANAGER;
import static com.micewine.emu.activities.MainActivity.customRootFSPath;
import static com.micewine.emu.activities.MainActivity.fileManagerCwd;
import static com.micewine.emu.activities.MainActivity.fileManagerDefaultDir;
import static com.micewine.emu.activities.MainActivity.selectedFilePath;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.core.WineWrapper.extractIcon;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.outputFile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.core.RatPackageManager;
import com.micewine.emu.fragments.FloatingFileManagerFragment;
import com.micewine.emu.utils.DriveUtils;

import java.io.File;
import java.util.List;

import mslinks.ShellLink;

public class AdapterFiles extends RecyclerView.Adapter<AdapterFiles.ViewHolder> {
    private final List<FileList> fileList;
    private final Context context;
    private final boolean isFloatFilesDialog;

    public AdapterFiles(List<FileList> fileList, Context context, boolean isFloatFilesDialog) {
        this.fileList = fileList;
        this.context = context;
        this.isFloatFilesDialog = isFloatFilesDialog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_files_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileList item = fileList.get(position);

        if (fileManagerCwd.equals(fileManagerDefaultDir)) {
            holder.fileName.setText(item.file.getName().toUpperCase());
        } else {
            holder.fileName.setText(item.file.getName());
        }

        holder.fileName.setSelected(true);

        if (item.file.isDirectory()) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder);

            File[] fileList = item.file.listFiles();
            if (fileList == null) return;

            int fileCount = fileList.length;

            holder.fileDescription.setVisibility(View.VISIBLE);

            String countText;

            if (fileCount == 0) {
                countText = context.getString(R.string.empty_text);
            } else if (fileCount == 1) {
                countText = fileCount + " " + context.getString(R.string.item_text);
            } else {
                countText = fileCount + " " + context.getString(R.string.items_text);
            }

            holder.fileDescription.setText(countText);
        } else if (item.file.isFile()) {
            double fileSize = item.file.length();
            String fileExtension = item.file.getName().substring(item.file.getName().lastIndexOf(".") + 1);

            holder.fileDescription.setText(formatSize(fileSize));

            switch (fileExtension.toLowerCase()) {
                case "exe" -> {
                    File iconFile = new File(usrDir, "icons/" + item.file.getName().replace("." + fileExtension, "") + "-thumbnail");

                    extractIcon(item.file.getPath(), iconFile.getPath());

                    if (iconFile.exists() && iconFile.length() > 0) {
                        new Thread(() -> {
                            Bitmap parsedIcon = decodeFileThumbnail(iconFile, holder.fileIcon.getLayoutParams().width, holder.fileIcon.getLayoutParams().height);
                            holder.fileIcon.post(() -> holder.fileIcon.setImageBitmap(parsedIcon));
                        }).start();
                    } else {
                        holder.fileIcon.setImageResource(R.drawable.unknown_exe);
                    }
                }
                case "lnk" -> {
                    try {
                        ShellLink shellLink = new ShellLink(item.file);
                        DriveUtils.DriveInfo drive = DriveUtils.parseWindowsPath(shellLink.resolveTarget());

                        if (drive != null) {
                            File file = new File(drive.getUnixPath());
                            File iconFile = new File(usrDir, "icons/" + item.file.getName().replace("." + fileExtension, "") + "-thumbnail");

                            extractIcon(file.getPath(), iconFile.getPath());

                            if (iconFile.exists() && iconFile.length() > 0) {
                                holder.fileIcon.setImageBitmap(BitmapFactory.decodeFile(iconFile.getPath()));
                            } else {
                                holder.fileIcon.setImageResource(R.drawable.ic_log);
                            }
                        } else {
                            holder.fileIcon.setImageResource(R.drawable.ic_log);
                        }
                    } catch (Exception e) {
                        holder.fileIcon.setImageResource(R.drawable.ic_log);
                    }
                }
                case "rat", "mwp" -> holder.fileIcon.setImageResource(R.drawable.ic_rat_package);
                case "zip" -> {
                    boolean isAdrenoToolsPackage = new RatPackageManager.AdrenoToolsPackage(item.file.getPath()).getName() != null;

                    holder.fileIcon.setImageResource(isAdrenoToolsPackage ? R.drawable.ic_rat_package : R.drawable.ic_log);
                }
                case "dll" -> holder.fileIcon.setImageResource(R.drawable.ic_dll);
                case "bat" -> holder.fileIcon.setImageResource(R.drawable.ic_batch);
                case "ico", "png", "jpg", "jpeg", "bmp" -> new Thread(() -> {
                    Bitmap parsedIcon = decodeFileThumbnail(item.file, holder.fileIcon.getLayoutParams().width, holder.fileIcon.getLayoutParams().height);
                    holder.fileIcon.post(() -> holder.fileIcon.setImageBitmap(parsedIcon));
                }).start();
                case "mp3", "ogg", "wav", "flac", "aac", "wma", "aiff" -> holder.fileIcon.setImageResource(R.drawable.ic_music);
                default -> holder.fileIcon.setImageResource(R.drawable.ic_log);
            }
        }
    }

    private Bitmap decodeFileThumbnail(File file, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getPath(), options);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView fileName = itemView.findViewById(R.id.title_preferences_model);
        private final TextView fileDescription = itemView.findViewById(R.id.description_preferences_model);
        private final ImageView fileIcon =  itemView.findViewById(R.id.set_img);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() < 0) return;

            FileList item = fileList.get(getAdapterPosition());

            if (isFloatFilesDialog) {
                if (item.file.getName().equals("..")) {
                    fileManagerCwd = new File(fileManagerCwd).getParent();
                    FloatingFileManagerFragment.refreshFiles();
                } else if (item.file.isFile()) {
                    if (item.file.getName().toLowerCase().endsWith(".rat")) {
                        customRootFSPath = item.file.getPath();
                    } else {
                        outputFile = item.file;
                    }
                } else if (item.file.isDirectory()) {
                    fileManagerCwd = item.file.getPath();
                    FloatingFileManagerFragment.refreshFiles();
                }
            } else {
                Intent intent = new Intent(ACTION_SELECT_FILE_MANAGER);

                intent.putExtra("selectedFile", item.file.getPath());

                context.sendBroadcast(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (getAdapterPosition() < 0) return false;

            FileList item = fileList.get(getAdapterPosition());

            selectedFilePath = item.file.getPath();

            return (fileManagerCwd.equals(fileManagerDefaultDir)) || selectedFilePath.equals("..");
        }
    }

    public static class FileList {
        public File file;

        public FileList(File file) {
            this.file = file;
        }
    }

    public final static int GIGABYTE = (1024 * 1024 * 1024);
    public final static int MEGABYTE = (1024 * 1024);
    public final static int KILOBYTE = 1024;

    public static String formatSize(double value) {
        if (value < KILOBYTE) return Math.round(value * 100F) / 100F + "B";
        if (value < MEGABYTE) return Math.round(value * 100F / KILOBYTE) / 100F + "KB";
        if (value < GIGABYTE) return Math.round(value * 100F / MEGABYTE) / 100F + "MB";
        return Math.round(value * 100F / GIGABYTE) / 100F + "GB";
    }
}