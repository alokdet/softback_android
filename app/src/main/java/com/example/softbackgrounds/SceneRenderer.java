
package com.example.softbackgrounds;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class SceneRenderer {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void render(Canvas canvas, Scene scene) {
        if (scene == null) return;

        for (SceneNode node : scene.nodes) {
            renderNode(canvas, node);
        }
    }

    private void renderNode(Canvas canvas, SceneNode node) {
        if (node == null || !node.visible || node.opacity <= 0f) {
            return;
        }

        float centerX = node.x + node.width / 2f;
        float centerY = node.y + node.height / 2f;

        canvas.save();

        // Apply node rotation and scaling.
        canvas.rotate(node.rotation, centerX, centerY);
        canvas.scale(
                node.scaleX,
                node.scaleY,
                centerX,
                centerY
        );

        paint.setAlpha(
                Math.round(Math.max(0f, Math.min(1f, node.opacity)) * 255)
        );

        if (node.type != null) {
            switch (node.type) {

                case "rectangle":
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(node.color);

                    canvas.drawRect(
                            node.x, node.y,
                            node.x + node.width,
                            node.y + node.height,
                            paint
                    );
                    break;

                case "ellipse":
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(node.color);

                    canvas.drawOval(
                            new RectF(
                                    node.x,
                                    node.y,
                                    node.x + node.width,
                                    node.y + node.height
                            ),
                            paint
                    );
                    break;

                case "text":
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(node.color);
                    paint.setTextSize(node.fontSize);

                    if (node.text != null) {
                        canvas.drawText(
                                node.text,
                                node.x,
                                node.y + node.fontSize,
                                paint
                        );
                    }
                    break;
            }
        }

        // Children inherit their parent's transformation.
        for (SceneNode child : node.children) {
            renderNode(canvas, child);
        }

        canvas.restore();
    }
}
