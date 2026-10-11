package com.example.softbackgrounds;

import android.graphics.Canvas;
import android.graphics.Matrix;

/** Applies an SLDF node transform without reconstructing it from rotation/scale. */
public final class TransformRenderer {
    private TransformRenderer() {}

    public static void apply(Canvas canvas, SceneNode node) {
        if (node.transform != null) {
            SceneNode.Transform t = node.transform;
            Matrix matrix = new Matrix();
            matrix.setValues(new float[]{
                    t.a, t.c, t.tx,
                    t.b, t.d, t.ty,
                    0f, 0f, 1f
            });
            canvas.concat(matrix);
            return;
        }

        // SLDF v1 fallback.
        float centerX = node.x + node.width / 2f;
        float centerY = node.y + node.height / 2f;
        canvas.translate(node.x, node.y);
        if (node.rotation != 0f) {
            canvas.rotate(node.rotation, node.width / 2f, node.height / 2f);
        }
        if (node.scaleX != 1f || node.scaleY != 1f) {
            canvas.scale(node.scaleX, node.scaleY, node.width / 2f, node.height / 2f);
        }
    }
}
