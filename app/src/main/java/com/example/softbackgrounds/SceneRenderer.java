package com.example.softbackgrounds;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class SceneRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void render(Canvas canvas, Scene scene) {
        for (SceneNode node : scene.nodes) {
            renderNode(canvas, node);
        }
    }

    private void renderNode(Canvas canvas, SceneNode node) {
        if (!node.visible) {
            return;
        }

        paint.setAlpha((int) (node.opacity * 255));

        switch (node.type) {
            case "rectangle":
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(node.color);

                canvas.drawRect(node.x, node.y, node.x + node.width, node.y + node.height, paint);
                break;
            case "ellipse":
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(node.color);

                canvas.drawOval(new RectF(node.x, node.y, node.x + node.width, node.y + node.height), paint);
                break;
            case "text":
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(node.color);

                paint.setTextSize(node.fontSize);

                canvas.drawText(node.text, node.x, node.y + node.fontSize, paint);
                break;
        }

        for (SceneNode child : node.children) {
            renderNode(canvas, child);
        }
    }
}
