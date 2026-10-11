package com.example.softbackgrounds;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Base64;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Renders self-contained SLDF image assets as Figma image paints. */
public final class ImageRenderer {
    private static final Map<String, Bitmap> CACHE = new ConcurrentHashMap<>();

    private ImageRenderer() {}

    public static boolean draw(Canvas canvas, Scene scene, SceneNode.PaintDef paint,
                               SceneNode node, Paint targetPaint) {
        if (scene == null || paint == null || paint.assetId == null) return false;
        Scene.Asset asset = find(scene, paint.assetId);
        if (asset == null || asset.data == null) return false;

        Bitmap bitmap = decode(asset);
        if (bitmap == null || bitmap.isRecycled()) return false;

        RectF dst = new RectF(0, 0, node.width, node.height);
        if (dst.width() <= 0 || dst.height() <= 0) return false;

        targetPaint.reset();
        targetPaint.setAntiAlias(true);
        targetPaint.setFilterBitmap(true);
        targetPaint.setAlpha(Math.round(clamp01(paint.opacity) * 255f));

        String mode = paint.scaleMode == null ? "FILL" : paint.scaleMode.toUpperCase();
        if ("TILE".equals(mode)) {
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            targetPaint.setShader(shader);
            canvas.drawRect(dst, targetPaint);
            return true;
        }

        float sx = dst.width() / bitmap.getWidth();
        float sy = dst.height() / bitmap.getHeight();
        float scale;
        if ("FIT".equals(mode)) {
            scale = Math.min(sx, sy);
        } else {
            // FILL and CROP both preserve aspect ratio and cover the paint box.
            scale = Math.max(sx, sy);
        }

        float w = bitmap.getWidth() * scale;
        float h = bitmap.getHeight() * scale;
        float left = (dst.width() - w) * 0.5f;
        float top = (dst.height() - h) * 0.5f;
        android.graphics.Rect src = new android.graphics.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF out = new RectF(left, top, left + w, top + h);

        // Figma's imageTransform is preserved in SLDF. Apply it when present;
        // this is most useful for CROP and future exact image-transform support.
        if ("CROP".equals(mode) && paint.imageTransform != null && paint.imageTransform.length >= 6) {
            Matrix m = new Matrix();
            float[] t = paint.imageTransform;
            m.setValues(new float[]{
                    t[0], t[1], t[4],
                    t[2], t[3], t[5],
                    0f, 0f, 1f
            });
            canvas.save();
            canvas.clipRect(dst);
            canvas.concat(m);
            RectF transformed = new RectF(out);
            canvas.drawBitmap(bitmap, src, transformed, targetPaint);
            canvas.restore();
            return true;
        }

        canvas.drawBitmap(bitmap, src, out, targetPaint);
        return true;
    }

    private static Scene.Asset find(Scene scene, String id) {
        if (scene.assets == null) return null;
        for (Scene.Asset asset : scene.assets) {
            if (asset != null && id.equals(asset.id)) return asset;
        }
        return null;
    }

    private static Bitmap decode(Scene.Asset asset) {
        Bitmap cached = CACHE.get(asset.id);
        if (cached != null && !cached.isRecycled()) return cached;
        try {
            byte[] bytes = Base64.decode(asset.data, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) CACHE.put(asset.id, bitmap);
            return bitmap;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
