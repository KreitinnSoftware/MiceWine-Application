package com.micewine.emu.activities;

import androidx.appcompat.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.micewine.emu.adapters.AdapterSettings;
import com.micewine.emu.core.services.wine.WineService;
import com.micewine.emu.coreutils.ShellExecutorCmd;
import com.micewine.emu.databinding.LayoutlogShellOutputBinding;
import com.micewine.emu.models.SettingsList;
import com.micewine.emu.viewmodels.ViewModelAppLogs;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import android.widget.TextView;
import com.micewine.emu.R;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import com.micewine.emu.nativeLoader.NativeLoader;


public class logAppOutput extends AppCompatActivity {
    
    private Integer transcriptRows;
   private int exit_code_status;
    private NativeLoader metods_natives = null;
   private FragmentManager fragmentManager;
    private LayoutlogShellOutputBinding binding;
    private TextView logOutAndInStd;
   private ViewModelAppLogs sharedLogs;
    private StringBuilder logs = new StringBuilder();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = LayoutlogShellOutputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    
        ActionBar action = getSupportActionBar();
        
        if(action != null) {
            action.hide();
        }

        MaterialToolbar toolBar = findViewById(R.id.toolbar_log);
        
        setSupportActionBar(toolBar);

        CollapsingToolbarLayout collapsingToolBar = findViewById(R.id.toolbar_log_layout);
    
        collapsingToolBar.setTitle("Logs");
        
        metods_natives = new NativeLoader();
        
        sharedLogs = new ViewModelProvider(this).get(ViewModelAppLogs.class);
        
        sharedLogs.setText(ShellExecutorCmd.stdOut);
        
        sharedLogs.getTextLiveData().observe(this , out -> {
            binding.logShell.setText(out);
        });
        
    }
   public boolean onCreateOptionsMenu(Menu menu) {
        
        
           getMenuInflater().inflate(R.menu.menu_clear, menu);
       return super.onCreateOptionsMenu(menu);
    }
        
    
      @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }else if (id == R.id.clear){
            
           binding.logShell.setText("");
        }

        return super.onOptionsItemSelected(item);
    }
   

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
    
    
    
}



