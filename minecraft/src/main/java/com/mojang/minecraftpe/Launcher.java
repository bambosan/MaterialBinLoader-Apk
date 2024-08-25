package com.mojang.minecraftpe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Launcher extends com.mojang.minecraftpe.MainActivity {
    @Override
    public void onCreate(Bundle bundle) {
        try {
            @SuppressLint("DiscouragedPrivateApi") Method addAssetPath = getAssets().getClass().getDeclaredMethod("addAssetPath", String.class);
            String mcSource = getIntent().getStringExtra("MC_SRC");
            addAssetPath.invoke(getAssets(), mcSource);

            ArrayList<String> mcSplitSrc = getIntent().getStringArrayListExtra("MC_SPLIT_SRC");
            if (mcSplitSrc != null){
                for (String splitSource : mcSplitSrc) {
                    addAssetPath.invoke(getAssets(), splitSource);
                }
            }
            super.onCreate(bundle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("fmod");
        System.loadLibrary("minecraftpe");
        System.loadLibrary("mc");
        System.loadLibrary("materialbinloader");
    }
}