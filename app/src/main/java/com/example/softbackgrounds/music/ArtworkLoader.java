
package com.example.softbackgrounds.music;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ArtworkLoader {

    private static volatile ArtworkLoader instance;

    private static final int IMAGE_SIZE = 256;
    private static final long MAX_DISK_CACHE = 40L * 1024 * 1024;

    private final ContentResolver resolver;
    private final ExecutorService executor;

    private final LruCache<String, Bitmap> memoryCache;

    private final ConcurrentHashMap<String, Future<?>> inFlight =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<ImageView, Request> viewRequests =
            new ConcurrentHashMap<>();

    private final File diskDirectory;

    private static class Request {
        final String key;
        volatile Future<?> future;

        Request(String key) {
            this.key = key;
        }
    }

    private ArtworkLoader(Context context) {

        Context app = context.getApplicationContext();

        resolver = app.getContentResolver();
        executor = Executors.newFixedThreadPool(2);

        memoryCache = new LruCache<String, Bitmap>(20 * 1024) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        diskDirectory = new File(
                app.getCacheDir(),
                "music_artwork"
        );

        if (!diskDirectory.exists()) {
            diskDirectory.mkdirs();
        }
    }

    public static ArtworkLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (ArtworkLoader.class) {
                if (instance == null) {
                    instance = new ArtworkLoader(context);
                }
            }
        }
        return instance;
    }

    public void load(Uri uri, ImageView imageView) {

        cancel(imageView);

        if (uri == null) {
            imageView.setTag(null);
            placeholder(imageView);
            return;
        }

        String key = uri.toString();

        Request request = new Request(key);
        viewRequests.put(imageView, request);

        imageView.setTag(request);
        placeholder(imageView);

        Bitmap cached = memoryCache.get(key);

        if (cached != null) {
            imageView.setImageBitmap(cached);
            viewRequests.remove(imageView, request);
            return;
        }

        Future<?> shared = inFlight.get(key);

        if (shared == null) {
            Future<?> created = executor.submit(() -> {
                Bitmap bitmap = loadBitmap(key, uri);

                if (bitmap != null) {
                    memoryCache.put(key, bitmap);
                }

                inFlight.remove(key);

                // Notify all views waiting for this artwork.
                for (ImageView view : viewRequests.keySet()) {
                    Request pending = viewRequests.get(view);

                    if (pending != null && pending.key.equals(key)) {
                        view.post(() -> {
                            Request current = viewRequests.get(view);

                            if (current == pending) {
                                if (bitmap != null) {
                                    view.setImageBitmap(bitmap);
                                } else {
                                    placeholder(view);
                                }

                                viewRequests.remove(view, pending);
                            }
                        });
                    }
                }
            });

            Future<?> previous = inFlight.putIfAbsent(key, created);

            if (previous != null) {
                created.cancel(true);
                request.future = previous;
            } else {
                request.future = created;
            }

        } else {
            request.future = shared;
        }
    }

    public void cancel(ImageView imageView) {

        Request request = viewRequests.remove(imageView);

        if (request != null) {
            imageView.setTag(null);
        }
    }

    private Bitmap loadBitmap(String key, Uri uri) {

        Bitmap bitmap = readDisk(key);

        if (bitmap != null) return bitmap;

        bitmap = decode(uri);

        if (bitmap != null) {
            writeDisk(key, bitmap);
        }

        return bitmap;
    }

    private Bitmap decode(Uri uri) {

        try (InputStream input = resolver.openInputStream(uri)) {

            if (input == null) return null;

            BitmapFactory.Options bounds =
                    new BitmapFactory.Options();

            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bounds);

            if (Thread.currentThread().isInterrupted()) {
                return null;
            }

            try (InputStream second = resolver.openInputStream(uri)) {

                if (second == null) return null;

                BitmapFactory.Options options =
                        new BitmapFactory.Options();

                options.inSampleSize = sampleSize(
                        bounds.outWidth,
                        bounds.outHeight
                );

                options.inPreferredConfig = Bitmap.Config.RGB_565;

                return BitmapFactory.decodeStream(
                        second,
                        null,
                        options
                );
            }

        } catch (Exception e) {
            return null;
        }
    }

    private int sampleSize(int width, int height) {

        int sample = 1;

        while (
                width / (sample * 2) >= IMAGE_SIZE &&
                        height / (sample * 2) >= IMAGE_SIZE
        ) {
            sample *= 2;
        }

        return sample;
    }

    private Bitmap readDisk(String key) {

        File file = cacheFile(key);

        if (!file.exists()) return null;

        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
    }

    private synchronized void writeDisk(
            String key,
            Bitmap bitmap
    ) {

        File file = cacheFile(key);

        try (FileOutputStream output = new FileOutputStream(file)) {

            bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    85,
                    output
            );

            output.flush();

            trimDiskCache();

        } catch (Exception ignored) {
        }
    }

    private File cacheFile(String key) {

        try {

            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    ));

            StringBuilder name = new StringBuilder();

            for (byte b : hash) {
                name.append(String.format("%02x", b));
            }

            return new File(diskDirectory, name + ".jpg");

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private synchronized void trimDiskCache() {

        File[] files = diskDirectory.listFiles();

        if (files == null) return;

        long total = 0;

        for (File file : files) {
            total += file.length();
        }

        if (total <= MAX_DISK_CACHE) return;

        java.util.Arrays.sort(
                files,
                (a, b) -> Long.compare(
                        a.lastModified(),
                        b.lastModified()
                )
        );

        for (File file : files) {

            if (total <= MAX_DISK_CACHE * 0.8) break;

            long size = file.length();

            if (file.delete()) {
                total -= size;
            }
        }
    }

    public void invalidate(Uri uri) {

        if (uri == null) return;

        String key = uri.toString();

        memoryCache.remove(key);

        File file = cacheFile(key);

        if (file.exists()) {
            file.delete();
        }
    }

    private void placeholder(ImageView view) {
        view.setImageResource(android.R.drawable.ic_media_play);
    }
}
