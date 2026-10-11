package com.example.softbackgrounds;

import android.content.Context;
import android.graphics.Color;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class SceneLoader {

    public static Scene load(Context context, String fileName) throws Exception {
        InputStream inputStream = context.getAssets().open(fileName);
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try {
            SceneDocument document = new Gson().fromJson(reader, SceneDocument.class);
            validate(document);
            processScene(document.scene);
            return document.scene;
        } finally {
            reader.close();
        }
    }

    public static Scene loadJson(String json) throws Exception {
        SceneDocument document = new Gson().fromJson(json, SceneDocument.class);
        validate(document);
        processScene(document.scene);
        return document.scene;
    }

    private static void validate(SceneDocument document) throws Exception {
        if (document == null || document.scene == null) {
            throw new Exception("Invalid SLDF JSON: missing scene");
        }
        if (!"SLDF".equals(document.format)) {
            throw new Exception("Invalid SLDF JSON: unsupported format " + document.format);
        }
        if (document.version <= 0) {
            throw new Exception("Invalid SLDF JSON: missing version");
        }
    }

    private static void processScene(Scene scene) {
        if (scene == null) return;
        for (SceneNode node : scene.nodes) {
            processNode(node);
        }
    }

    private static void processNode(SceneNode node) {
        if (node == null) return;

        // Normalize legacy v1 fill into the new fills list.
        if (node.fills == null) node.fills = new java.util.ArrayList<>();
        if (node.fill != null && node.fills.isEmpty()) {
            node.fills.add(node.fill);
        }

        // Preserve the old Android color field for existing rendering code.
        SceneNode.PaintDef solid = firstSolidPaint(node.fills);
        if (solid != null && solid.color != null) {
            node.color = PaintRenderer.color(solid.color, solid.opacity);
        }

        // If a v2 matrix exists, keep legacy fields usable for code that has not
        // migrated yet. Full matrix rendering comes in the transform phase.
        if (node.transform != null) {
            node.rotation = (float) Math.toDegrees(Math.atan2(node.transform.b, node.transform.a));
            node.scaleX = (float) Math.sqrt(
                    node.transform.a * node.transform.a + node.transform.b * node.transform.b
            );
            node.scaleY = (float) Math.sqrt(
                    node.transform.c * node.transform.c + node.transform.d * node.transform.d
            );
            if (node.scaleX < 0.0001f) node.scaleX = 1f;
            if (node.scaleY < 0.0001f) node.scaleY = 1f;
        }

        for (SceneNode child : node.children) {
            processNode(child);
        }
    }

    private static SceneNode.PaintDef firstSolidPaint(java.util.List<SceneNode.PaintDef> paints) {
        if (paints == null) return null;
        for (SceneNode.PaintDef paint : paints) {
            if (paint != null && "solid".equals(paint.type)) return paint;
        }
        return null;
    }

    private static int toColor(SceneNode.FillColor color, float opacity) {
        int r = clamp255(color.r);
        int g = clamp255(color.g);
        int b = clamp255(color.b);
        int a = clamp255(opacity);
        return Color.argb(a, r, g, b);
    }

    private static int clamp255(float value) {
        return Math.round(Math.max(0f, Math.min(1f, value)) * 255f);
    }

    public static class SceneDocument {
        String format;
        int version;
        Scene scene;
    }
}
