package com.example.softbackgrounds;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.example.softbackgrounds.motion.MotionEngine;
import com.example.softbackgrounds.motion.MotionState;
import com.example.softbackgrounds.motion.MotionStateListener;

public class BackdropService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new BackdropEngine();
    }

    private class BackdropEngine extends Engine
            implements MotionStateListener {

        private final Handler handler =
                new Handler(Looper.getMainLooper());

        private final GradientRenderer renderer =
                new GradientRenderer();

        private final MotionEngine motionEngine =
                new MotionEngine(BackdropService.this);

        private boolean visible = false;

        private static final long FRAME_DELAY = 16;

        private final Runnable drawRunnable =
                new Runnable() {
                    @Override
                    public void run() {

                        if (!visible) {
                            return;
                        }

                        drawFrame();

                        handler.postDelayed(
                                this,
                                FRAME_DELAY
                        );
                    }
                };

        public BackdropEngine() {

            /*
             * Tell MotionEngine that this
             * wallpaper wants to receive
             * motion updates.
             */
            motionEngine.setMotionStateListener(this);
        }

        // ------------------------------------------
        // MotionStateListener
        // ------------------------------------------

        @Override
        public void onMotionStateChanged(
                MotionState motionState) {

            /*
             * Forward the motion values to
             * GradientRenderer.
             */
            renderer.setMotion(
                    motionState.pitch,
                    motionState.roll
            );
        }

        // ------------------------------------------
        // Wallpaper visibility
        // ------------------------------------------

        @Override
        public void onVisibilityChanged(
                boolean visible) {

            this.visible = visible;

            if (visible) {

                startWallpaper();

            } else {

                stopWallpaper();
            }
        }

        // ------------------------------------------
        // Surface created
        // ------------------------------------------

        @Override
        public void onSurfaceCreated(
                SurfaceHolder holder) {

            super.onSurfaceCreated(holder);

            if (visible) {
                startRendering();
            }
        }

        // ------------------------------------------
        // Surface changed
        // ------------------------------------------

        @Override
        public void onSurfaceChanged(
                SurfaceHolder holder,
                int format,
                int width,
                int height) {

            super.onSurfaceChanged(
                    holder,
                    format,
                    width,
                    height
            );
        }

        // ------------------------------------------
        // Surface destroyed
        // ------------------------------------------

        @Override
        public void onSurfaceDestroyed(
                SurfaceHolder holder) {

            stopRendering();

            super.onSurfaceDestroyed(holder);
        }

        // ------------------------------------------
        // Engine destroyed
        // ------------------------------------------

        @Override
        public void onDestroy() {

            stopWallpaper();

            super.onDestroy();
        }

        // ------------------------------------------
        // Start wallpaper
        // ------------------------------------------

        private void startWallpaper() {

            if (!visible) {
                return;
            }

            /*
             * Start sensors.
             */
            motionEngine.start();

            /*
             * Start rendering.
             */
            startRendering();
        }

        // ------------------------------------------
        // Stop wallpaper
        // ------------------------------------------

        private void stopWallpaper() {

            /*
             * Stop rendering.
             */
            stopRendering();

            /*
             * Stop sensors.
             */
            motionEngine.stop();
        }

        // ------------------------------------------
        // Start rendering
        // ------------------------------------------

        private void startRendering() {

            if (!visible) {
                return;
            }

            /*
             * Remove any old callbacks first.
             * This prevents multiple render loops.
             */
            handler.removeCallbacks(
                    drawRunnable
            );

            handler.post(drawRunnable);
        }

        // ------------------------------------------
        // Stop rendering
        // ------------------------------------------

        private void stopRendering() {

            handler.removeCallbacks(
                    drawRunnable
            );
        }

        // ------------------------------------------
        // Draw one frame
        // ------------------------------------------

        private void drawFrame() {

            if (!visible) {
                return;
            }

            SurfaceHolder holder =
                    getSurfaceHolder();

            Canvas canvas = null;

            try {

                canvas = holder.lockCanvas();

                if (canvas != null) {

                    renderer.draw(canvas);
                }

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                if (canvas != null) {

                    try {

                        holder.unlockCanvasAndPost(
                                canvas
                        );

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
