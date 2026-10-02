package com.example.softbackgrounds.runtime;

import com.example.softbackgrounds.Scene;
import com.example.softbackgrounds.SceneNode;

public class SceneRuntime {

    private Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public SceneNode hitTest(float x, float y) {
        if (scene == null) {
            return null;
        }

        // Traverse from topmost layer to bottom.
        for (int i = scene.nodes.size() - 1; i >= 0; i--) {
            SceneNode node = hitTestNode(scene.nodes.get(i), x, y);

            if (node != null) {
                return node;
            }
        }
        return null;
    }

    private SceneNode hitTestNode(SceneNode node, float x, float y) {
        if (!node.visible || node.opacity <= 0f) {
            return null;
        }

        // Children are visually above their parent.
        for (int i = node.children.size() - 1; i >= 0; i--) {
            SceneNode child = hitTestNode(node.children.get(i), x, y);

            if (child != null) {
                return child;
            }
        }

        if (contains(node, x, y)) {
            return node;
        }
        return null;
    }

    private boolean contains(SceneNode node, float x, float y) {
        float left = node.x;
        float top = node.y;
        float right = node.x + node.width;
        float bottom = node.y + node.height;

        return x >= left
                && x <= right
                && y >= top
                && y <= bottom;
    }
}
