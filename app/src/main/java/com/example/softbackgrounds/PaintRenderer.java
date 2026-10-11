package com.example.softbackgrounds;

import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Converts SLDF paints into Android Paint/Shader state. */
public final class PaintRenderer {
    private PaintRenderer() {}

    /** Applies only a gradient shader, preserving all existing Paint/TextPaint state. */
    public static boolean applyGradientShader(Paint paint, SceneNode.PaintDef definition, RectF bounds) {
        if (paint == null || definition == null || definition.type == null
                || definition.gradientStops == null || definition.gradientStops.size() < 2) {
            return false;
        }
        String type = normalizeType(definition.type);
        if (!type.startsWith("gradient_")) return false;
        Shader shader = createGradient(definition, bounds);
        if (shader == null) return false;
        paint.setShader(shader);
        return true;
    }

    public static boolean apply(Paint paint, SceneNode.PaintDef definition, RectF bounds) {
        if (definition == null || definition.type == null || definition.opacity <= 0f) {
            return false;
        }

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        String type = normalizeType(definition.type);
        if ("solid".equals(type)) {
            if (definition.color == null) return false;
            paint.setShader(null);
            paint.setAlpha(alpha(definition.opacity));
            paint.setColor(color(definition.color, definition.opacity));
            return true;
        }

        if (type.startsWith("gradient_") && definition.gradientStops != null
                && definition.gradientStops.size() >= 2) {
            Shader shader = createGradient(definition, bounds);
            if (shader == null) return false;
            paint.setShader(shader);
            // Gradient stop colors already contain their own alpha. The paint's
            // global opacity is applied through the shader colors below.
            paint.setAlpha(255);
            return true;
        }

        // Image paints are handled by ImageRenderer.
        return false;
    }

    private static Shader createGradient(SceneNode.PaintDef p, RectF b) {
        List<SceneNode.GradientStop> validStops = normalizedStops(p.gradientStops);
        if (validStops.size() < 2) return null;

        int n = validStops.size();
        int[] colors = new int[n];
        float[] positions = new float[n];
        for (int i = 0; i < n; i++) {
            SceneNode.GradientStop stop = validStops.get(i);
            positions[i] = clamp01(stop.position);
            colors[i] = color(stop.color, stop.opacity * p.opacity);
        }

        // Figma gradient handles are normalized to the node's object bounds.
        // When Figma/runtime omits gradientHandlePositions but supplies a
        // gradientTransform, reconstruct the handles in normalized space before
        // converting them to Android pixels. Do NOT pass this normalized matrix
        // directly to Shader.setLocalMatrix(); Android expects a pixel-space
        // matrix there.
        SceneNode.GradientHandle h0;
        SceneNode.GradientHandle h1;
        SceneNode.GradientHandle h2;
        boolean hasRealHandles = p.gradientHandlePositions != null
                && p.gradientHandlePositions.size() >= 2;

        if (hasRealHandles) {
            h0 = handle(p, 0, fallbackHandle(0f, 0.5f));
            h1 = handle(p, 1, fallbackHandle(1f, 0.5f));
            h2 = handle(p, 2, fallbackHandle(0f, 1f));
        } else if (p.gradientTransform != null && p.gradientTransform.length >= 6) {
            h0 = transformedHandle(p.gradientTransform, 0f, 0.5f);
            h1 = transformedHandle(p.gradientTransform, 1f, 0.5f);
            h2 = transformedHandle(p.gradientTransform, 0f, 1f);
        } else {
            h0 = fallbackHandle(0f, 0.5f);
            h1 = fallbackHandle(1f, 0.5f);
            h2 = fallbackHandle(0f, 1f);
        }

        float x0 = b.left + h0.x * b.width();
        float y0 = b.top + h0.y * b.height();
        float x1 = b.left + h1.x * b.width();
        float y1 = b.top + h1.y * b.height();
        float x2 = b.left + h2.x * b.width();
        float y2 = b.top + h2.y * b.height();

        String type = normalizeType(p.type);
        Shader shader;
        if ("gradient_linear".equals(type)) {
            shader = new LinearGradient(x0, y0, x1, y1, colors, positions, Shader.TileMode.CLAMP);
        } else if ("gradient_radial".equals(type)) {
            // Android's RadialGradient is circular. Use handle 1 as the radius
            // and keep handle 2 available for future elliptical/custom shaders.
            float radius = distance(x0, y0, x1, y1);
            if (radius < 0.001f) radius = Math.max(0.001f, Math.max(b.width(), b.height()) * 0.5f);
            shader = new RadialGradient(x0, y0, radius, colors, positions, Shader.TileMode.CLAMP);
        } else if ("gradient_angular".equals(type)) {
            shader = new SweepGradient(x0, y0, colors, positions);

            // Figma's angular gradient has an explicit direction. Android's sweep
            // starts at 3 o'clock, so rotate the shader to match handle 1.
            float angle = (float) Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));
            Matrix matrix = new Matrix();
            matrix.setRotate(angle, x0, y0);
            shader.setLocalMatrix(matrix);
        } else if ("gradient_diamond".equals(type)) {
            // Android has no native diamond shader. A sweep fallback is preferable
            // to silently dropping the paint; a dedicated bitmap/custom shader can
            // replace this later without changing the SLDF format.
            shader = new SweepGradient(x0, y0, colors, positions);
        } else {
            return null;
        }

        return shader;
    }

    private static List<SceneNode.GradientStop> normalizedStops(List<SceneNode.GradientStop> source) {
        List<SceneNode.GradientStop> result = new ArrayList<>();
        if (source == null) return result;
        for (SceneNode.GradientStop stop : source) {
            if (stop == null || stop.color == null) continue;
            if (Float.isNaN(stop.position) || Float.isInfinite(stop.position)) continue;
            result.add(stop);
        }
        Collections.sort(result, new Comparator<SceneNode.GradientStop>() {
            @Override
            public int compare(SceneNode.GradientStop a, SceneNode.GradientStop b) {
                return Float.compare(a.position, b.position);
            }
        });
        return result;
    }

    private static SceneNode.GradientHandle fallbackHandle(float x, float y) {
        SceneNode.GradientHandle h = new SceneNode.GradientHandle();
        h.x = x;
        h.y = y;
        return h;
    }

    private static SceneNode.GradientHandle transformedHandle(float[] matrix, float x, float y) {
        SceneNode.GradientHandle h = new SceneNode.GradientHandle();
        h.x = matrix[0] * x + matrix[2] * y + matrix[4];
        h.y = matrix[1] * x + matrix[3] * y + matrix[5];
        return h;
    }

    private static SceneNode.GradientHandle handle(SceneNode.PaintDef p, int index,
                                                    SceneNode.GradientHandle fallback) {
        if (p.gradientHandlePositions == null || index >= p.gradientHandlePositions.size()) {
            return fallback;
        }
        SceneNode.GradientHandle h = p.gradientHandlePositions.get(index);
        if (h == null || Float.isNaN(h.x) || Float.isNaN(h.y)
                || Float.isInfinite(h.x) || Float.isInfinite(h.y)) {
            return fallback;
        }
        return h;
    }

    private static String normalizeType(String value) {
        return value == null ? "" : value.toLowerCase().replace('-', '_');
    }

    private static float distance(float x0, float y0, float x1, float y1) {
        return (float) Math.hypot(x1 - x0, y1 - y0);
    }

    public static int color(SceneNode.FillColor c, float opacity) {
        if (c == null) return 0;
        int a = alpha(opacity);
        return android.graphics.Color.argb(a,
                channel(c.r), channel(c.g), channel(c.b));
    }

    private static int alpha(float value) {
        return Math.round(clamp01(value) * 255f);
    }

    private static int channel(float value) {
        return Math.round(clamp01(value) * 255f);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
