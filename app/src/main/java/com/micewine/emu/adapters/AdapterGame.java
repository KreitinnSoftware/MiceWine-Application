package com.micewine.emu.adapters;
import android.content.Context;
import android.os.Handler;
import android.widget.FrameLayout;
import com.micewine.emu.EmulationActivity;
import com.micewine.emu.databinding.MainActivityBinding;
import com.micewine.emu.models.GameList;

import android.content.Context;
import android.content.Intent;
import android.sax.RootElement;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.micewine.emu.LoriePreferences;
import com.micewine.emu.R;

import com.micewine.emu.activities.logAppOutput;
import com.micewine.emu.models.SettingsList;
import java.util.List;
import kotlin.jvm.Throws;

public class AdapterGame extends RecyclerView.Adapter<AdapterGame.ViewHolder> {
    
   private final Context context;
    private Handler handler = new Handler();
    private final List<GameList> GameList;

    public AdapterGame(List<GameList> GameList, Context context) {
        this.GameList = GameList;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterGame.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterGame.ViewHolder holder, int position) {

        GameList slist = GameList.get(position);
        holder.titleGame.setText(slist.getTitleGame());
        holder.GameImage.setImageResource(slist.getImageGame());
//        Picasso.get().load(slist.getImageSettings()).into(holder.imageSettings);

        // implementation 'com.squareup.picasso:picasso:2.71828'
    }

    @Override
    public int getItemCount() {
        return GameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView titleGame;
        private final ImageView GameImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
           

            titleGame = itemView.findViewById(R.id.title_game_model);
            GameImage = itemView.findViewById(R.id.img_game);
            itemView.setOnClickListener(this);
        }

      @Override
        public void onClick(View v) {
            GameList gameModel = GameList.get(getAdapterPosition());
            if (R.string.desktop_mode_init == gameModel.getTitleGame()){
               Intent intent = new Intent(context, EmulationActivity.class);
                   context.startActivity(intent);
                
            }
        }
        
        
        
        
            
}
    
    
}
    