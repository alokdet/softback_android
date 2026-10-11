package com.example.softbackgrounds;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    // Figma/design canvas size
    public float width;
    public float height;

    // Root-level nodes
    public final List<SceneNode> nodes = new ArrayList<>();

    // Self-contained image/media assets exported by the Figma plugin.
    public final List<Asset> assets = new ArrayList<>();

    public static class Asset {
        public String id;
        public String mimeType;
        public int width;
        public int height;
        public String data; // base64
    }

    public void addNode(SceneNode node) {
        nodes.add(node);
    }
}
