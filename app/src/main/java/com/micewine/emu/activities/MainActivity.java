package com.micewine.emu.activities;

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
import com.micewine.emu.core.services.xserver.XserverLoader;
import com.micewine.emu.core.services.xserver.destroyXserver;
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
  public static String appRootDir = "/data/data/com.micewine.emu/files";
  public static File shellLoader = new File(appRootDir + "/loader.apk");
  public static String box64 = appRootDir + "/box64";
  public static String usrDir = appRootDir + "/usr";
  public static String wineFolder = appRootDir + "/wine";
  public static String homeDir = appRootDir + "/home";
  public static String tmpDir = usrDir + "/tmp";
  public static String wineUtilsFolder = appRootDir + "/wine-utils";

  private ShellExecutorCmd shellExec = new ShellExecutorCmd();
  private logAppOutput logApp = new logAppOutput();
  private Init init = new Init();
    private destroyXserver destroy = new destroyXserver();
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

    if (!shellLoader.exists()) {
      copyAssets(this, "loader.apk", appRootDir);
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

      String root = "storage/emulated/0/root.zip";
      new Thread(
              () -> {
                obbManager.extractZip(
                    this,
                    root,
                    appRootDir,
                    progressExtractBar,
                    progressUpdate,
                    MainActivity.this);
              })
          .start();
        
    manageFilesPath();
    checkPermission();
    FragmentLoader(new HomeFragment(), true);
    XServerInicialization.run();
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
    // Obtém o diretório raiz do armazenamento interno do aplicativo
    File rootDir = new File("/data/data/com.micewine.emu/files");

    // Cria a pasta principal se não existir
    if (!rootDir.exists()) {
      rootDir.mkdirs();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    destroy.stopXserver();
    init.stop();
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
    } catch (IOException e) {
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

  private Runnable XServerInicialization =
      () -> runServices.runService(XserverLoader.class, this);
}
