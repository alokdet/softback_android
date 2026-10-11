package com.example.softbackgrounds;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.content.Context;

import java.util.List;

/**
 * Phase-2 SLDF renderer.
 *
 * Rendering is deliberately split into transform -> clip -> geometry -> paints -> strokes -> children.
 * This gives later Figma features a stable place to plug into the pipeline.
 */
public class SceneRenderer implements GeometryRenderer.PaintRendererFacade {

    private final Context context;
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint layerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Scene currentScene;

    public SceneRenderer() {
        this(null);
    }

    public SceneRenderer(Context context) {
        this.context = context;
    }

    public void render(Canvas canvas, Scene scene) {
        if (scene == null) return;
        currentScene = scene;
        try {
            for (SceneNode node : scene.nodes) {
                renderNode(canvas, node);
            }
        } finally {
            currentScene = null;
        }
    }

    private void renderNode(Canvas canvas, SceneNode node) {
        if (node == null || !node.visible || node.opacity <= 0f) return;

        canvas.save();
        TransformRenderer.apply(canvas, node);

        if (node.clipsContent) {
            Path clip = GeometryRenderer.createPath(node);
            if (clip != null) canvas.clipPath(clip);
            else canvas.clipRect(new RectF(0, 0, node.width, node.height));
        }

        boolean isolatedOpacity = node.opacity < 0.999f;
        if (isolatedOpacity) {
            layerPaint.reset();
            layerPaint.setAlpha(Math.round(clamp01(node.opacity) * 255f));
            canvas.saveLayer(null, layerPaint);
        }

        EffectRenderer.drawEffects(canvas, node, new EffectRenderer.ContentDrawer() {
            @Override
            public void draw(Canvas target) {
                drawNode(target, node);
            }
        });

        for (SceneNode child : node.children) {
            renderNode(canvas, child);
        }

        if (isolatedOpacity) canvas.restore();
        canvas.restore();
    }

    private void drawNode(Canvas canvas, SceneNode node) {
        if (node.type == null) return;

        String type = node.type.toLowerCase();
        if ("text".equals(type)) {
            drawText(canvas, node);
            return;
        }

        Path path = GeometryRenderer.createPath(node);
        if (path == null) return;

        drawFills(canvas, path, node);
        drawStrokes(canvas, path, node);
    }

    @Override
    public void drawFills(Canvas canvas, Path path, SceneNode node) {
        List<SceneNode.PaintDef> fills = node.fills;
        if (fills == null || fills.isEmpty()) {
            if (node.fill != null) fills = java.util.Collections.singletonList(node.fill);
            else return;
        }

        for (SceneNode.PaintDef definition : fills) {
            if (definition != null && "image".equalsIgnoreCase(definition.type)) {
                canvas.save();
                canvas.clipPath(path);
                boolean drawn = ImageRenderer.draw(canvas, currentScene, definition, node, fillPaint);
                canvas.restore();
                if (drawn) continue;
            }
            if (!PaintRenderer.apply(fillPaint, definition,
                    new RectF(0, 0, node.width, node.height))) {
                continue;
            }
            canvas.drawPath(path, fillPaint);
        }
    }

    private void drawStrokes(Canvas canvas, Path path, SceneNode node) {
        if (node.strokes == null || node.strokes.isEmpty()) return;

        for (SceneNode.PaintDef definition : node.strokes) {
            if (!PaintRenderer.apply(strokePaint, definition,
                    new RectF(0, 0, node.width, node.height))) {
                continue;
            }
            StrokeRenderer.apply(strokePaint, node.stroke);
            canvas.drawPath(path, strokePaint);
        }
    }

    private void drawText(Canvas canvas, SceneNode node) {
        TextRenderer.draw(canvas, node, context);
    }

    private static SceneNode.PaintDef firstPaint(List<SceneNode.PaintDef> paints, SceneNode.PaintDef fallback) {
        if (paints != null) {
            for (SceneNode.PaintDef p : paints) {
                if (p != null && p.color != null && p.opacity > 0f) return p;
            }
        }
        return fallback;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
