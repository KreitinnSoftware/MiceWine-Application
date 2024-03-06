package com.micewine.emu.activities;

import static com.micewine.emu.coreutils.ShellExecutorCmd.ExecuteCMD;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.micewine.emu.EntryPoint.Init;
import com.micewine.emu.R;
import com.micewine.emu.activities.logAppOutput;
import com.micewine.emu.core.services.xserver.XServerLoader;
import com.micewine.emu.coreutils.GeneralUtils;
import com.micewine.emu.coreutils.ObbExtractor;
import com.micewine.emu.coreutils.RunServiceClass;
import com.micewine.emu.coreutils.ShellExecutorCmd;
import com.micewine.emu.databinding.ActivityMainBinding;
import com.micewine.emu.fragments.HomeFragment;
import com.micewine.emu.fragments.SettingsFragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.InterruptedByTimeoutException;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;
  private ProgressBar progressExtractBar;
  private FragmentManager fragmentManager;
  private RunServiceClass runServices = new RunServiceClass();
  private GeneralUtils generalAppUtils = new GeneralUtils();
  private FrameLayout content;
  private static final int PERMISSION_REQUEST_CODE = 123;
  private TextView progressUpdate;
  private ObbExtractor obbManager = new ObbExtractor();
  @SuppressLint("SdCardPath")
  public static File appRootDir = new File("/data/data/com.micewine.emu/files");
  public static File shellLoader = new File(appRootDir + "/loader.apk");
  public static File box64 = new File(appRootDir + "/box64");
  public static File usrDir = new File(appRootDir + "/usr");
  public static File homeDir = new File(appRootDir + "/home");
  public static File tmpDir = new File(usrDir + "/tmp");
  public static File wineFolder = new File(appRootDir + "/wine");
  public static File wineUtilsFolder = new File(appRootDir + "/wine-utils");
  public static File wine = new File(wineFolder + "/x86_64/bin/wine");
  public static File pulseAudio = new File(usrDir + "/bin/pulseaudio");
  public static File virgl_test_server = new File(usrDir + "/virglrenderer/bin/virgl_test_server");

  private ShellExecutorCmd shellExec = new ShellExecutorCmd();
  private logAppOutput logApp = new logAppOutput();
  private Init init = new Init();
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    fragmentManager = getFragmentManager();
    content = findViewById(R.id.content);
    progressExtractBar = findViewById(R.id.progressBar);
    progressUpdate = findViewById(R.id.updateProgress);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(R.string.app_name);
    }

    BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
    bottomNavigation.setOnItemSelectedListener(
        item -> {
          int id = item.getItemId();
          if (id == R.id.nav_Home) {
            FragmentLoader(new HomeFragment(), false);
          } else if (id == R.id.nav_settings) {
            FragmentLoader(new SettingsFragment(), false);
          }
          return true;
        });

    manageFilesPath();
    checkPermission();

    FragmentLoader(new HomeFragment(), true);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    progressExtractBar = findViewById(R.id.progressBar);
    progressUpdate = findViewById(R.id.updateProgress);

    progressExtractBar.setIndeterminate(true);
    progressExtractBar.setVisibility(View.VISIBLE);

    new Thread(() -> {
      if (!shellLoader.exists()) {
        copyAssets(this, "loader.apk", appRootDir.toString());
        ExecuteCMD("chmod 400 " + shellLoader);
      }

      if (!box64.exists()) {
        copyAssets(this, "box64", appRootDir.toString());
        ExecuteCMD("chmod 755 " + box64);
      }

      if (!usrDir.exists()) {
        copyAssets(this, "rootfs.zip", appRootDir.toString());
        ExecuteCMD("unzip -o " + appRootDir + "/rootfs.zip -d " + appRootDir);
        ExecuteCMD("rm " + appRootDir + "/rootfs.zip");
        ExecuteCMD("chmod 775 -R " + usrDir);
      }

      if (!wineFolder.exists()) {
        copyAssets(this, "wine.zip", appRootDir.toString());
        ExecuteCMD("unzip -o " + appRootDir + "/wine.zip -d " + appRootDir);
        ExecuteCMD("rm " + appRootDir + "/wine.zip");
      }

      if (!tmpDir.exists()) {
        tmpDir.mkdirs();
      }

      if (!homeDir.exists()) {
        homeDir.mkdirs();
      }

      runOnUiThread(() -> {
        progressExtractBar.setVisibility(View.GONE);
      });
    }).start();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == android.R.id.home) {
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void checkPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      // Se não foi concedida, solicita a permissão
      ActivityCompat.requestPermissions(
          this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }
  }

  private void FragmentLoader(Fragment fragment, boolean appinit) {
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    if (appinit) {
      fragmentTransaction.add(R.id.content, fragment);
    } else {
      fragmentTransaction.replace(R.id.content, fragment);
    }

    fragmentTransaction.commit();
  }

  public void manageFilesPath() {
    if (!appRootDir.exists()) {
      appRootDir.mkdirs();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    init.stopAll();
    this.binding = null;
  }

  private static void copyAssets(Context context, String filename, String outputPath) {
    AssetManager assetManager = context.getAssets();

    InputStream in = null;
    OutputStream out = null;
    try {
      in = assetManager.open(filename);
      File outFile = new File(outputPath, filename);
      out = new FileOutputStream(outFile);
      copyFile(in, out);
    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }
}
