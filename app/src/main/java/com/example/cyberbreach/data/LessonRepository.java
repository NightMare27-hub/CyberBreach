package com.example.cyberbreach.data;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.example.cyberbreach.util.NetworkUtil;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LessonRepository {

    public enum Source { REMOTE, CACHE, BUNDLED }

    public interface Callback {
        void onLoaded(List<LessonParser.LessonData> lessons, Source source);
        void onError(String message);
    }

    private static class Loaded {
        final List<LessonParser.LessonData> lessons;
        final Source source;

        Loaded(List<LessonParser.LessonData> lessons, Source source) {
            this.lessons = lessons;
            this.source = source;
        }
    }

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());

    public LessonRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public void refresh(Callback callback) {
        IO.execute(() -> {
            try {
                if (NetworkUtil.isOnline(context)) {
                    String json = HttpClient.get(AppConfig.LESSONS_URL);
                    List<LessonParser.LessonData> lessons = LessonParser.parsePack(json);
                    writeCache(json);
                    deliver(callback, new Loaded(lessons, Source.REMOTE));
                    return;
                }
            } catch (IOException | JSONException e) {
                // fall through to local cache
            }

            Loaded local = readLocalCache();
            if (local != null) {
                deliver(callback, local);
            } else {
                main.post(() -> callback.onError("No lesson data available"));
            }
        });
    }

    private void deliver(Callback callback, Loaded loaded) {
        main.post(() -> callback.onLoaded(loaded.lessons, loaded.source));
    }

    private Loaded readLocalCache() {
        try {
            File cache = cacheFile();
            if (cache.exists()) {
                String json = readAll(new FileInputStream(cache));
                return new Loaded(LessonParser.parsePack(json), Source.CACHE);
            }
        } catch (IOException | JSONException e) {
            // ignore
        }
        return null;
    }

    private File cacheFile() {
        File dir = context.getExternalFilesDir(null);
        if (dir == null || !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = context.getFilesDir();
        }
        return new File(dir, AppConfig.LESSONS_CACHE_FILE);
    }

    private void writeCache(String json) throws IOException {
        File target = cacheFile();
        File tmp = new File(target.getParentFile(), AppConfig.LESSONS_CACHE_FILE + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
        }
        if (!tmp.renameTo(target)) {
            throw new IOException("Could not replace lessons cache file");
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
