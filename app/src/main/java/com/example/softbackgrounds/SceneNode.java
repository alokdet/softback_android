package com.example.softbackgrounds;

import java.util.ArrayList;
import java.util.List;

public class SceneNode {

    // Identity
    public String id;
    public String type;
    public String name;

    // Position and size in Figma pixels
    public float x;
    public float y;
    public float width;
    public float height;

    // Transform
    public float rotation;
    public float scaleX = 1f;
    public float scaleY = 1f;

    // Appearance
    public float opacity = 1f;
    public boolean visible = true;

    // Android color
    public int color;

    // Figma fill
    public Fill fill;

    // Text
    public String text;
    public float fontSize;

    // Child nodes
    public final List<SceneNode> children = new ArrayList<>();


    public SceneNode() {
    }


    public void addChild(SceneNode child) {
        children.add(child);
    }


    public static class Fill {

        public String type;
        public FillColor color;
        public float opacity = 1f;
    }


    public static class FillColor {

        public float r;
        public float g;
        public float b;
    }
}
