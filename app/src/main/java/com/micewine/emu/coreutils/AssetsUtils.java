package com.micewine.emu.coreutils;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;

public class AssetsUtils {

    public InputStream openAsset(Context context, String assetFilePath) {
        try {
            AssetManager assetManager = context.getAssets();
            return assetManager.open(assetFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
