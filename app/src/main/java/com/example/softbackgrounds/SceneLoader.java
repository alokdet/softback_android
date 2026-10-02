package com.example.softbackgrounds;

import android.content.Context;
import android.graphics.Color;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class SceneLoader {

    public static Scene load(
            Context context,
            String fileName
    ) throws Exception {

        InputStream inputStream =
                context.getAssets().open(fileName);

        Reader reader =
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                );

        Gson gson = new Gson();

        SLDFDocument document =
                gson.fromJson(
                        reader,
                        SLDFDocument.class
                );

        reader.close();

        if (document == null || document.scene == null) {
            throw new Exception("Invalid SLDF file");
        }

        // Convert Figma fill data to Android colors
        for (SceneNode node : document.scene.nodes) {
            processNode(node);
        }

        return document.scene;
    }

    public static Scene loadJson(String json) throws Exception {

        Gson gson = new Gson();

        SLDFDocument document =
                gson.fromJson(
                        json,
                        SLDFDocument.class
                );

        if (document == null || document.scene == null) {
            throw new Exception("Invalid SLDF JSON");
        }

        // Process Figma fill data
        for (SceneNode node : document.scene.nodes) {
            processNode(node);
        }

        return document.scene;
    }


    private static void processNode(SceneNode node) {

        if (node.fill != null &&
                "solid".equals(node.fill.type) &&
                node.fill.color != null) {

            float r = node.fill.color.r;
            float g = node.fill.color.g;
            float b = node.fill.color.b;

            node.color = Color.rgb(
                    (int) (r * 255),
                    (int) (g * 255),
                    (int) (b * 255)
            );
        }

        // Process children recursively
        for (SceneNode child : node.children) {
            processNode(child);
        }
    }


    public static class SLDFDocument {

        String format;
        int version;

        Scene scene;
    }
}
