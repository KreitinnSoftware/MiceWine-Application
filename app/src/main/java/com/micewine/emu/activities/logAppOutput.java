package com.micewine.emu.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.micewine.emu.R;
import com.micewine.emu.coreutils.ShellExecutorCmd;
import com.micewine.emu.databinding.LayoutlogShellOutputBinding;
import com.micewine.emu.nativeLoader.NativeLoader;
import com.micewine.emu.viewmodels.ViewModelAppLogs;


public class logAppOutput extends AppCompatActivity {

    private Integer transcriptRows;
    private int exit_code_status;
    private NativeLoader metods_natives = null;
    private FragmentManager fragmentManager;
    private LayoutlogShellOutputBinding binding;
    private TextView logOutAndInStd;
    private ViewModelAppLogs sharedLogs;
    private final StringBuilder logs = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutlogShellOutputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar action = getSupportActionBar();

        if (action != null) {
            action.hide();
        }

        MaterialToolbar toolBar = findViewById(R.id.toolbar_log);

        setSupportActionBar(toolBar);

        CollapsingToolbarLayout collapsingToolBar = findViewById(R.id.toolbar_log_layout);

        collapsingToolBar.setTitle("Logs");

        metods_natives = new NativeLoader();

        sharedLogs = new ViewModelProvider(this).get(ViewModelAppLogs.class);

        sharedLogs.setText(ShellExecutorCmd.stdOut);

        sharedLogs.getTextLiveData().observe(this, out -> {
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
        } else if (id == R.id.clear) {

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



