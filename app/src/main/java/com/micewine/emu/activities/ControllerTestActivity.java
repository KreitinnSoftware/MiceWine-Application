package com.micewine.emu.activities;

import static com.micewine.emu.controller.ControllerUtils.updateAxisState;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.micewine.emu.R;
import com.micewine.emu.controller.ControllerUtils;
import com.micewine.emu.databinding.ActivityControllerViewBinding;
import com.micewine.emu.fragments.ControllerViewFragment;

import java.util.List;

public class ControllerTestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityControllerViewBinding binding = ActivityControllerViewBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        Toolbar controllerViewToolbar = findViewById(R.id.controllerViewTitle);
        controllerViewToolbar.setTitle(getString(R.string.controller_view_title));

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener((v) -> onKeyDown(KeyEvent.KEYCODE_BACK, null));

        fragmentLoader(new ControllerViewFragment());
    }

    private void fragmentLoader(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
        );

        transaction.replace(R.id.controller_view_content, fragment);
        transaction.commit();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event == null) return true;
        updateAxisState(event);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {
            if (fragment instanceof ControllerViewFragment) {
                ((ControllerViewFragment) fragment).invalidateControllerView();
            }
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }

        if (event == null) return true;
        ControllerUtils.updateButtonsState(event);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof ControllerViewFragment) {
                ((ControllerViewFragment) fragment).invalidateControllerView();
            }
        }

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return onKeyDown(keyCode, event);
    }
}