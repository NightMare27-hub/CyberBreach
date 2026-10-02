package com.example.cyberbreach.data;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.util.NetworkUtil;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LevelRepository {

    public enum Source { REMOTE, CACHE, BUNDLED }

    public interface Callback {
        void onLoaded(List<Level> levels, Source source);
        void onError(String message);
    }

    private static class Loaded {
        final List<Level> levels;
        final Source source;

        Loaded(List<Level> levels, Source source) {
            this.levels = levels;
            this.source = source;
        }
    }

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());

    public LevelRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Synchronous and small: used once at app start so screens never see an empty list. */
    public List<Level> loadLocal() {
        Loaded loaded = readLocal();
        return loaded != null ? loaded.levels : new ArrayList<>();
    }

    /** Asynchronous: try the server, fall back to the saved copy, then the bundled file. */
    public void refresh(Callback callback) {
        IO.execute(() -> {
            try {
                if (NetworkUtil.isOnline(context)) {
                    String json = HttpClient.get(AppConfig.LEVELS_URL);
                    List<Level> levels = LevelParser.parsePack(json);
                    writeCache(json);
                    deliver(callback, new Loaded(levels, Source.REMOTE));
                    return;
                }
            } catch (IOException | JSONException e) {
                // fall through to local data
            }

            Loaded local = readLocal();
            if (local != null) {
                deliver(callback, local);
            } else {
                main.post(() -> callback.onError("No level data available"));
            }
        });
    }

    private void deliver(Callback callback, Loaded loaded) {
        main.post(() -> callback.onLoaded(loaded.levels, loaded.source));
    }

    private Loaded readLocal() {
        try {
            File cache = cacheFile();
            if (cache.exists()) {
                String json = readAll(new FileInputStream(cache));
                return new Loaded(LevelParser.parsePack(json), Source.CACHE);
            }
        } catch (IOException | JSONException e) {
            // cache is missing or damaged: try the bundled copy
        }
        try {
            String json = readAll(context.getAssets().open(AppConfig.BUNDLED_ASSET));
            return new Loaded(LevelParser.parsePack(json), Source.BUNDLED);
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    private File cacheFile() {
        File dir = context.getExternalFilesDir(null);
        if (dir == null || !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = context.getFilesDir();
        }
        return new File(dir, AppConfig.CACHE_FILE);
    }

    private void writeCache(String json) throws IOException {
        File target = cacheFile();
        File tmp = new File(target.getParentFile(), AppConfig.CACHE_FILE + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
        }
        if (!tmp.renameTo(target)) {
            throw new IOException("Could not replace cache file");
        }
    }

    private static String readAll(InputStream in) throws IOException {
        try (InputStream input = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = input.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
