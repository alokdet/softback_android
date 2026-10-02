package com.example.softbackgrounds.runtime;

import android.graphics.PointF;

import com.example.softbackgrounds.Scene;

public class SceneTransform {
    private float scale = 1f;
    private float offsetX = 0f;
    private float offsetY = 0f;

    public void update(Scene scene, float viewWidth, float viewHeight) {
        if (scene == null || scene.width <= 0 || scene.height <= 0) {
            return;
        }

        scale = Math.min(viewWidth / scene.width, viewHeight / scene.height);

        offsetX = (viewWidth - scene.width * scale) / 2f;
        offsetY = (viewHeight - scene.height * scale) / 2f;
    }

    public PointF viewToScene(float viewX, float viewY) {
        return new PointF((viewX - offsetX) / scale, (viewY - offsetY) / scale);
    }

    public float getScale() {
        return scale;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }
}