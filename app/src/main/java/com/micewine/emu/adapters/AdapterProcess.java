package com.micewine.emu.adapters;

import static com.micewine.emu.core.ShellLoader.runCommand;
import static com.micewine.emu.core.WineWrapper.getCpuHexMask;
import static com.micewine.emu.core.WineWrapper.getProcessCPUAffinity;
import static com.micewine.emu.core.WineWrapper.maskToCpuAffinity;
import static com.micewine.emu.fragments.DebugSettingsFragment.availableCPUs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.core.WineWrapper;

import java.io.File;
import java.util.List;

public class AdapterProcess extends RecyclerView.Adapter<AdapterProcess.ViewHolder> {
    private final List<WineWrapper.ExeProcess> processList;
    private final Context context;

    public AdapterProcess(List<WineWrapper.ExeProcess> processList, Context context) {
        this.processList = processList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_process_item, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WineWrapper.ExeProcess item = processList.get(position);

        holder.processName.setText(item.getName());
        holder.processRamUsage.setText(
                String.format("%.2f MB", item.getRamUsageKB() / 1024F)
        );
        holder.processCPUUsage.setText(
                String.format("%.2f %%", item.getCpuUsage())
        );

        File iconFile = new File(item.getIconPath());
        Bitmap pIcon;
        if (iconFile.exists() && iconFile.length() > 0) {
            pIcon = BitmapFactory.decodeFile(item.getIconPath());
        } else {
            pIcon = drawableToBitmap(
                    ResourcesCompat.getDrawable(context.getResources(), R.drawable.unknown_exe, null)
            );
        }

        holder.processIcon.setImageBitmap(pIcon);
        holder.moreButton.setOnClickListener((v) -> {
            PopupMenu popup = new PopupMenu(context, holder.moreButton);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.process_more_menu, popup.getMenu());

            popup.setOnMenuItemClickListener((menuItem) -> {
                if (menuItem.getItemId() == R.id.killProcess) {
                    runCommand("kill -SIGINT " + item.getUnixPid(), false);
                    return true;
                } else if (menuItem.getItemId() == R.id.setAffinity) {
                    long affinityMask = Long.parseLong(getProcessCPUAffinity(item.getUnixPid()), 16);
                    boolean[] checkedItems = maskToCpuAffinity(affinityMask);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.Theme_MiceWine);

                    dialog.setTitle(item.getName());
                    dialog.setIcon(new BitmapDrawable(context.getResources(), pIcon));
                    dialog.setMultiChoiceItems(availableCPUs, checkedItems, (dialogInterface, index, isChecked) -> checkedItems[index] = isChecked);
                    dialog.setPositiveButton(R.string.confirm_text, (dialogInterface, index) -> {
                        StringBuilder strAffinity = new StringBuilder();

                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                strAffinity.append(",");
                                strAffinity.append(availableCPUs[i]);
                            }
                        }

                        if (strAffinity.length() > 0) {
                            strAffinity.deleteCharAt(0);
                        }

                        runCommand("taskset -p " + getCpuHexMask(strAffinity.toString()) + " " + item.getUnixPid(), false);

                        dialogInterface.dismiss();
                    });
                    dialog.setNegativeButton(R.string.cancel_text, null);
                    dialog.show();

                    return true;
                } else {
                    return false;
                }
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return processList.size();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView processName = itemView.findViewById(R.id.processName);
        TextView processRamUsage = itemView.findViewById(R.id.processRamUsage);
        TextView processCPUUsage = itemView.findViewById(R.id.processCPUUsage);
        ImageButton moreButton = itemView.findViewById(R.id.moreButton);
        ImageView processIcon = itemView.findViewById(R.id.processIcon);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {}
    }
}
