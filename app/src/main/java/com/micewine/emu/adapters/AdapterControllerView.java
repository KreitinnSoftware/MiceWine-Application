package com.micewine.emu.adapters;

import static com.micewine.emu.fragments.ControllerViewFragment.getControllerBitmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;

import java.util.List;

public class AdapterControllerView extends RecyclerView.Adapter<AdapterControllerView.ViewHolder> {
    private final List<ControllerViewList> controllerViewList;
    private final Context context;

    public AdapterControllerView(List<ControllerViewList> controllerViewList, Context context) {
        this.controllerViewList = controllerViewList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_controller_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ControllerViewList item = controllerViewList.get(position);

        holder.settingsName.setText(item.controllerName);
        holder.controllerImage.setImageBitmap(
                getControllerBitmap(780, 400, item.controllerID, context)
        );
    }

    @Override
    public int getItemCount() {
        return controllerViewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView settingsName = itemView.findViewById(R.id.title_preferences_model);
        private final ImageView controllerImage = itemView.findViewById(R.id.controllerImage);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ControllerViewList {
        public String controllerName;
        public int controllerID;

        public ControllerViewList(String controllerName, int controllerID) {
            this.controllerName = controllerName;
            this.controllerID = controllerID;
        }
    }
}