
package com.example.softbackgrounds.selection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.softbackgrounds.SceneNode;

public class TransformController {

    public static final int NONE = -1;
    public static final int TOP_LEFT = 0;
    public static final int TOP = 1;
    public static final int TOP_RIGHT = 2;
    public static final int RIGHT = 3;
    public static final int BOTTOM_RIGHT = 4;
    public static final int BOTTOM = 5;
    public static final int BOTTOM_LEFT = 6;
    public static final int LEFT = 7;
    public static final int ROTATE = 8;

    private static final float MIN_SIZE = 24f;

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float startX, startY;
    private float startWidth, startHeight;
    private float startRotation;
    private float startPointerAngle;
    private boolean maintainAspectRatio = false;

    public TransformController() {
        borderPaint.setColor(Color.rgb(0, 120, 255));
        borderPaint.setStyle(Paint.Style.STROKE);

        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);

        handleBorderPaint.setColor(Color.rgb(0, 120, 255));
        handleBorderPaint.setStyle(Paint.Style.STROKE);
    }

    public void setMaintainAspectRatio(boolean enabled) {
        maintainAspectRatio = enabled;
    }

    public boolean isMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    public void draw(Canvas canvas, SceneNode node, float scale) {
        if (node == null) return;

        float safeScale = Math.max(scale, 0.01f);

        float cx = node.x + node.width / 2f;
        float cy = node.y + node.height / 2f;

        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(node.rotation);

        float halfW = node.width / 2f;
        float halfH = node.height / 2f;

        borderPaint.setStrokeWidth(1.5f / safeScale);

        canvas.drawRect(-halfW, -halfH, halfW, halfH, borderPaint);

        float handleRadius = 5f / safeScale;
        float stroke = 1.5f / safeScale;

        handleBorderPaint.setStrokeWidth(stroke);

        // Eight resize handles.
        float[][] points = getResizePoints(halfW, halfH);

        for (float[] point : points) {
            canvas.drawCircle(point[0], point[1], handleRadius, handlePaint);
            canvas.drawCircle(point[0], point[1], handleRadius, handleBorderPaint);
        }

        // Rotation handle.
        float rotateY = -halfH - (28f / safeScale);

        canvas.drawLine(0, -halfH, 0, rotateY, borderPaint);
        canvas.drawCircle(0, rotateY, handleRadius, handlePaint);
        canvas.drawCircle(0, rotateY, handleRadius, handleBorderPaint);
        canvas.restore();
    }

    private float[][] getResizePoints(float halfW, float halfH) {
        return new float[][]{
                {-halfW, -halfH},
                {0, -halfH},
                {halfW, -halfH},
                {halfW, 0},
                {halfW, halfH},
                {0, halfH},
                {-halfW, halfH},
                {-halfW, 0}
        };
    }

    public int hitTest(SceneNode node, float x, float y, float scale) {
        if (node == null) return NONE;

        float safeScale = Math.max(scale, 0.01f);

        float cx = node.x + node.width / 2f;
        float cy = node.y + node.height / 2f;

        float dx = x - cx;
        float dy = y - cy;

        double angle = Math.toRadians(-node.rotation);

        float localX = (float) (dx * Math.cos(angle) - dy * Math.sin(angle));
        float localY = (float) (dx * Math.sin(angle) + dy * Math.cos(angle));

        float halfW = node.width / 2f;
        float halfH = node.height / 2f;

        float hitRadius = 18f / safeScale;

        // Check rotation handle first.
        float rotateY = -halfH - (28f / safeScale);

        if (distance(localX, localY, 0, rotateY) <= hitRadius) {
            return ROTATE;
        }

        float[][] points = getResizePoints(halfW, halfH);

        for (int i = 0; i < points.length; i++) {
            if (distance(localX, localY, points[i][0], points[i][1]) <= hitRadius) {
                return i;
            }
        }
        return NONE;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x1 - x2, y1 - y2);
    }

    public void begin(SceneNode node, float x, float y) {
        startX = node.x;
        startY = node.y;

        startWidth = node.width;
        startHeight = node.height;

        startRotation = node.rotation;

        float cx = node.x + node.width / 2f;
        float cy = node.y + node.height / 2f;

        startPointerAngle = (float) Math.atan2(y - cy, x - cx);
    }

    /**
     * Move a node using parent-local pointer coordinates. Always calculates from
     * the gesture baseline, preventing cumulative drift.
     */
    public void updateMove(SceneNode node, float startTouchX, float startTouchY,
                           float currentX, float currentY) {
        if (node == null) return;
        node.x = startX + (currentX - startTouchX);
        node.y = startY + (currentY - startTouchY);
    }

    public void update(SceneNode node, int handle, float startTouchX, float startTouchY, float currentX, float currentY) {
        if (node == null || handle == NONE) return;

        if (handle == ROTATE) {
            float cx = startX + startWidth / 2f;
            float cy = startY + startHeight / 2f;

            float currentAngle = (float) Math.atan2(currentY - cy, currentX - cx);
            float delta = currentAngle - startPointerAngle;
            delta = (float) Math.atan2(Math.sin(delta), Math.cos(delta));
            node.rotation = startRotation + (float) Math.toDegrees(delta);
            return;
        }

        float deltaX = currentX - startTouchX;
        float deltaY = currentY - startTouchY;

        double angle = Math.toRadians(-startRotation);

        float localDX = (float) (deltaX * Math.cos(angle) - deltaY * Math.sin(angle));
        float localDY = (float) (deltaX * Math.sin(angle) + deltaY * Math.cos(angle));

        boolean left = handle == TOP_LEFT || handle == LEFT || handle == BOTTOM_LEFT;
        boolean right = handle == TOP_RIGHT || handle == RIGHT || handle == BOTTOM_RIGHT;
        boolean top = handle == TOP_LEFT || handle == TOP || handle == TOP_RIGHT;
        boolean bottom = handle == BOTTOM_LEFT || handle == BOTTOM || handle == BOTTOM_RIGHT;

        float newWidth = startWidth;
        float newHeight = startHeight;

        float shiftX = 0;
        float shiftY = 0;

        if (left) {
            float change = Math.min(localDX, startWidth - MIN_SIZE);
            newWidth = startWidth - change;
            shiftX = change;
        } else if (right) {
            newWidth = Math.max(MIN_SIZE, startWidth + localDX);
        }

        if (top) {
            float change = Math.min(localDY, startHeight - MIN_SIZE);
            newHeight = startHeight - change;
            shiftY = change;
        } else if (bottom) {
            newHeight = Math.max(MIN_SIZE, startHeight + localDY);
        }

        if (maintainAspectRatio) {
            float aspectRatio = startWidth / startHeight;

            if (left || right) {
                newHeight = newWidth / aspectRatio;
            } else if (top || bottom) {
                newWidth = newHeight * aspectRatio;
            }

            newWidth = Math.max(MIN_SIZE, newWidth);
            newHeight = Math.max(MIN_SIZE, newHeight);
        }

        double rotation = Math.toRadians(startRotation);

        float worldShiftX = (float) (shiftX * Math.cos(rotation) - shiftY * Math.sin(rotation));
        float worldShiftY = (float) (shiftX * Math.sin(rotation) + shiftY * Math.cos(rotation));

        node.x = startX + worldShiftX;
        node.y = startY + worldShiftY;

        node.width = newWidth;
        node.height = newHeight;
    }
}
