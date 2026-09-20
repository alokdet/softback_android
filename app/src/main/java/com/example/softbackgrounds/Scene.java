package com.example.softbackgrounds;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    // Figma/design canvas size
    public float width;
    public float height;

    // Root-level nodes
    public final List<SceneNode> nodes = new ArrayList<>();

    public void addNode(SceneNode node) {
        nodes.add(node);
    }
}
