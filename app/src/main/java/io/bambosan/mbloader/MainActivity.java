package io.bambosan.mbloader;

import org.jetbrains.annotations.NotNull;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String MC_PACKAGE_NAME = "com.mojang.minecraftpe";
    private static final String LAUNCHER_DEX_NAME = "launcher.dex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView listener = findViewById(R.id.listener);

        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File cacheDexDir = new File(getCodeCacheDir(), "dex");
                handleCacheCleaning(cacheDexDir, handler, listener);
                ApplicationInfo mcInfo = getPackageManager().getApplicationInfo(MC_PACKAGE_NAME, PackageManager.GET_META_DATA);
                Object pathList = getPathList(getClassLoader());
                processDexFiles(mcInfo, cacheDexDir, pathList, handler, listener);
                processNativeLibraries(mcInfo, pathList, handler, listener);
                launchMinecraft(mcInfo);
            } catch (Exception e) {
                Intent fallbackActivity = new Intent(this, Fallback.class);
                handleException(e, fallbackActivity);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void handleCacheCleaning(@NotNull File cacheDexDir, Handler handler, TextView listener) {
        if (cacheDexDir.exists() && cacheDexDir.isDirectory()) {
            handler.post(() -> listener.setText("-> " + cacheDexDir.getAbsolutePath() + " not empty, do cleaning"));
            for (File file : Objects.requireNonNull(cacheDexDir.listFiles())) {
                if (file.delete()) {
                    handler.post(() -> listener.append("\n-> " + file.getName() + " deleted"));
                }
            }
        } else {
            handler.post(() -> listener.setText("-> " + cacheDexDir.getAbsolutePath() + " is empty, skip cleaning"));
        }
    }

    private Object getPathList(@NotNull ClassLoader classLoader) throws Exception {
        Field pathListField = Objects.requireNonNull(classLoader.getClass().getSuperclass()).getDeclaredField("pathList");
        pathListField.setAccessible(true);
        return pathListField.get(classLoader);
    }

    private void processDexFiles(ApplicationInfo mcInfo, File cacheDexDir, @NotNull Object pathList, @NotNull Handler handler, TextView listener) throws Exception {
        Method addDexPath = pathList.getClass().getDeclaredMethod("addDexPath", String.class, File.class);
        File launcherDex = new File(cacheDexDir, LAUNCHER_DEX_NAME);

        copyFile(getAssets().open(LAUNCHER_DEX_NAME), launcherDex);
        handler.post(() -> listener.append("\n-> " + LAUNCHER_DEX_NAME + " copied to " + launcherDex.getAbsolutePath()));

        if (launcherDex.setReadOnly()) {
            addDexPath.invoke(pathList, launcherDex.getAbsolutePath(), null);
            handler.post(() -> listener.append("\n-> " + LAUNCHER_DEX_NAME + " added to dex path list"));
        }

        try (ZipFile zipFile = new ZipFile(mcInfo.sourceDir)) {
            for (int i = 2; i >= 0; i--) {
                String dexName = "classes" + (i == 0 ? "" : i) + ".dex";
                ZipEntry dexFile = zipFile.getEntry(dexName);
                if (dexFile != null) {
                    File mcDex = new File(cacheDexDir, dexName);
                    copyFile(zipFile.getInputStream(dexFile), mcDex);
                    handler.post(() -> listener.append("\n-> " + mcInfo.sourceDir + "/" + dexName + " copied to " + mcDex.getAbsolutePath()));
                    if (mcDex.setReadOnly()) {
                        addDexPath.invoke(pathList, mcDex.getAbsolutePath(), null);
                        handler.post(() -> listener.append("\n-> " + dexName + " added to dex path list"));
                    }
                }
            }
        }
    }

    private void processNativeLibraries(@NotNull ApplicationInfo mcInfo, @NotNull Object pathList, @NotNull Handler handler, TextView listener) throws Exception {
        Method addNativePath = pathList.getClass().getDeclaredMethod("addNativePath", Collection.class);
        ArrayList<String> libDirList = new ArrayList<>();
        libDirList.add(mcInfo.nativeLibraryDir);
        addNativePath.invoke(pathList, libDirList);
        handler.post(() -> listener.append("\n-> " + mcInfo.nativeLibraryDir + " added to native library directory path"));
    }

    private void launchMinecraft(@NotNull ApplicationInfo mcInfo) throws ClassNotFoundException {
        Class<?> launcherClass = getClassLoader().loadClass("com.mojang.minecraftpe.Launcher");
        Intent mcActivity = new Intent(this, launcherClass);
        mcActivity.putExtra("MC_SRC", mcInfo.sourceDir);

        if (mcInfo.splitSourceDirs != null) {
            ArrayList<String> listSrcSplit = new ArrayList<>();
            Collections.addAll(listSrcSplit, mcInfo.splitSourceDirs);
            mcActivity.putExtra("MC_SPLIT_SRC", listSrcSplit);
        }
        startActivity(mcActivity);
        finish();
    }

    private void handleException(@NotNull Exception e, @NotNull Intent fallbackActivity) {
        String logMessage = e.getCause() != null ? e.getCause().toString() : e.toString();
        fallbackActivity.putExtra("LOG_STR", logMessage);
        startActivity(fallbackActivity);
        finish();
    }

    private static void copyFile(InputStream from, @NotNull File to) throws IOException {
        File parentDir = to.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create directories");
        }
        if (!to.exists() && !to.createNewFile()) {
            throw new IOException("Failed to create new file");
        }
        try (BufferedInputStream input = new BufferedInputStream(from);
             BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(to.toPath()))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }
}