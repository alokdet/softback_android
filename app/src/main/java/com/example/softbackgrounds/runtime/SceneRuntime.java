
package com.example.softbackgrounds.runtime;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.example.softbackgrounds.Scene;
import com.example.softbackgrounds.SceneNode;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

public class SceneRuntime {

    private Scene scene;
    private final Map<SceneNode, SceneNode> parentMap = new IdentityHashMap<>();

    public void setScene(Scene scene) {
        this.scene = scene;
        parentMap.clear();

        if (scene != null) {
            for (SceneNode node : scene.nodes) {
                registerParents(node, null);
            }
        }
    }

    private void registerParents(SceneNode node, SceneNode parent) {
        if (node == null) return;

        if (parent != null) {
            parentMap.put(node, parent);
        }

        for (SceneNode child : node.children) {
            registerParents(child, node);
        }
    }

    public Scene getScene() {
        return scene;
    }

    public SceneNode hitTest(float x, float y) {

        if (scene == null) return null;

        for (int i = scene.nodes.size() - 1; i >= 0; i--) {

            SceneNode node = scene.nodes.get(i);

            SceneNode result = hitTestNode(node, x, y);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private SceneNode hitTestNode(
            SceneNode node,
            float x,
            float y
    ) {

        if (node == null || !node.visible || node.opacity <= 0f) {
            return null;
        }

        float centerX = node.x + node.width / 2f;
        float centerY = node.y + node.height / 2f;

        // Reverse the node's rotation.
        float dx = x - centerX;
        float dy = y - centerY;

        double angle = Math.toRadians(-node.rotation);

        float rotatedX = (float) (
                dx * Math.cos(angle) -
                        dy * Math.sin(angle)
        );

        float rotatedY = (float) (
                dx * Math.sin(angle) +
                        dy * Math.cos(angle)
        );

        // Reverse node scaling.
        float scaleX = Math.abs(node.scaleX) < 0.0001f
                ? 1f : node.scaleX;

        float scaleY = Math.abs(node.scaleY) < 0.0001f
                ? 1f : node.scaleY;

        float localX = centerX + rotatedX / scaleX;
        float localY = centerY + rotatedY / scaleY;

        // Check children first because they are visually above parents.
        for (int i = node.children.size() - 1; i >= 0; i--) {

            SceneNode result = hitTestNode(
                    node.children.get(i),
                    localX,
                    localY
            );

            if (result != null) {
                return result;
            }
        }

        // Check this node.
        boolean inside =
                localX >= node.x &&
                        localX <= node.x + node.width &&
                        localY >= node.y &&
                        localY <= node.y + node.height;

        return inside ? node : null;
    }

    public PointF scenePointToParent(
            SceneNode node,
            float sceneX,
            float sceneY
    ) {
        float x = sceneX;
        float y = sceneY;

        java.util.ArrayList<SceneNode> ancestors =
                new java.util.ArrayList<>();

        SceneNode parent = parentMap.get(node);

        while (parent != null) {
            ancestors.add(parent);
            parent = parentMap.get(parent);
        }

        // Apply inverse transformations from root to nearest parent.
        for (int i = ancestors.size() - 1; i >= 0; i--) {

            SceneNode p = ancestors.get(i);

            float cx = p.x + p.width / 2f;
            float cy = p.y + p.height / 2f;

            float dx = x - cx;
            float dy = y - cy;

            double angle = Math.toRadians(-p.rotation);

            float rx = (float) (
                    dx * Math.cos(angle) -
                            dy * Math.sin(angle)
            );

            float ry = (float) (
                    dx * Math.sin(angle) +
                            dy * Math.cos(angle)
            );

            float sx = Math.abs(p.scaleX) < 0.0001f ? 1f : p.scaleX;
            float sy = Math.abs(p.scaleY) < 0.0001f ? 1f : p.scaleY;

            x = cx + rx / sx;
            y = cy + ry / sy;
        }

        return new PointF(x, y);
    }

    public void applyParentTransforms(Canvas canvas, SceneNode node) {
        ArrayList<SceneNode> ancestors = new ArrayList<>();

        SceneNode parent = parentMap.get(node);

        while (parent != null) {
            ancestors.add(parent);
            parent = parentMap.get(parent);
        }

        // Apply transformations from the root parent downwards.
        for (int i = ancestors.size() - 1; i >= 0; i--) {
            SceneNode p = ancestors.get(i);

            float cx = p.x + p.width / 2f;
            float cy = p.y + p.height / 2f;

            canvas.rotate(p.rotation, cx, cy);
            canvas.scale(p.scaleX, p.scaleY, cx, cy);
        }
    }

    public float getParentWorldScale(SceneNode node) {
        float result = 1f;

        SceneNode parent = parentMap.get(node);

        while (parent != null) {
            float sx = Math.abs(parent.scaleX);
            float sy = Math.abs(parent.scaleY);

            result *= (float) Math.sqrt(sx * sy);

            parent = parentMap.get(parent);
        }

        return result;
    }
}
