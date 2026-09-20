package com.example.softbackgrounds;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;
import android.os.SystemClock;

public class GradientRenderer {

    private float pitch = 0f;
    private float roll = 0f;

    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap blobBitmap;
    private Canvas blobCanvas;
    private RenderNode blurNode;

    private int width;
    private int height;

    private long lastTime;

    // Our blobs
    private final Blob[] blobs = {

            new Blob(
                    0.15f, 0.20f,
                    0.75f,
                    0xFF5B2CFF
            ),

            new Blob(
                    0.80f, 0.25f,
                    0.70f,
                    0xFF0066FF
            ),

            new Blob(
                    0.75f, 0.80f,
                    0.80f,
                    0xFFFF2E88
            ),

            new Blob(
                    0.20f, 0.80f,
                    0.75f,
                    0xFF00C9A7
            )
    };

    public GradientRenderer() {

        lastTime = SystemClock.uptimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            blurNode = new RenderNode(
                    "GradientBlur"
            );
        }
    }

    public void setMotion(float pitch, float roll) {
        this.pitch = pitch;
        this.roll = roll;
    }

    public void draw(Canvas canvas) {

        int newWidth = canvas.getWidth();
        int newHeight = canvas.getHeight();

        if (newWidth <= 0 || newHeight <= 0) {
            return;
        }

        // Recreate bitmap if wallpaper size changes
        if (blobBitmap == null ||
                width != newWidth ||
                height != newHeight) {

            width = newWidth;
            height = newHeight;

            blobBitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
            );

            blobCanvas = new Canvas(blobBitmap);
        }

        // Calculate delta time
        long now = SystemClock.uptimeMillis();

        float deltaTime =
                (now - lastTime) / 1000f;

        lastTime = now;

        if (deltaTime > 0.05f) {
            deltaTime = 0.05f;
        }

        // Clear background
        blobCanvas.drawColor(0xFF080808);

        // Draw blobs
        for (Blob blob : blobs) {

            drawBlob(
                    blobCanvas,
                    blob,
                    deltaTime
            );
        }

        // Display the result
        canvas.drawBitmap(
                blobBitmap,
                0,
                0,
                bitmapPaint
        );
    }

    private void drawBlob(
            Canvas canvas,
            Blob blob,
            float deltaTime
    ) {

        /*
         * Convert normalized coordinates
         * into screen coordinates.
         */

        float baseX = blob.normalizedX * width;
        float baseY = blob.normalizedY * height;

        /*
         * Phone movement.
         *
         * roll moves blobs horizontally.
         * pitch moves blobs vertically.
         */

        float motionX = roll * 180f;
        float motionY = pitch * 180f;

        /*
         * Give each blob a slightly different
         * response so they don't all move together.
         */

        float x = baseX +
                motionX * blob.motionMultiplier;

        float y = baseY +
                motionY * blob.motionMultiplier;

        /*
         * Make blobs extremely large.
         */

        float radius =
                Math.max(width, height)
                        * blob.radiusMultiplier;

        /*
         * Radial gradient:
         *
         * center = solid color
         * edge   = transparent
         */

        int transparent =
                blob.color & 0x00FFFFFF;

        RadialGradient gradient =
                new RadialGradient(
                        x,
                        y,
                        radius,
                        blob.color,
                        transparent,
                        Shader.TileMode.CLAMP
                );

        blobPaint.setShader(gradient);

        canvas.drawCircle(
                x,
                y,
                radius,
                blobPaint
        );

        blobPaint.setShader(null);
    }

    private static class Blob {

        final float normalizedX;
        final float normalizedY;

        final float radiusMultiplier;

        final int color;

        final float motionMultiplier;

        Blob(
                float normalizedX,
                float normalizedY,
                float radiusMultiplier,
                int color
        ) {

            this.normalizedX = normalizedX;
            this.normalizedY = normalizedY;

            this.radiusMultiplier =
                    radiusMultiplier;

            this.color = color;

            /*
             * Slightly different movement for
             * every blob.
             */

            this.motionMultiplier =
                    0.7f +
                            (normalizedX * 0.6f);
        }
    }
}
