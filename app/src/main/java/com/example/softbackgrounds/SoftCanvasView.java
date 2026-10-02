package com.example.softbackgrounds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.softbackgrounds.interaction.GestureEngine;
import com.example.softbackgrounds.interaction.InteractionEngine;
import com.example.softbackgrounds.runtime.SceneRuntime;
import com.example.softbackgrounds.runtime.SceneTransform;
import com.example.softbackgrounds.selection.SelectionManager;

public class SoftCanvasView extends View implements GestureEngine.Listener {
    private final SceneRenderer renderer;
    private final SceneRuntime runtime;
    private final InteractionEngine interactionEngine;
    private final GestureDetector gestureDetector;
    private final SceneTransform transform;
    private final SelectionManager selectionManager;
    private Scene scene;

    private SceneNode draggedNode;

    private float lastTouchSceneX;
    private float lastTouchSceneY;

    public SoftCanvasView(Context context) {
        super(context);

        renderer = new SceneRenderer();
        runtime = new SceneRuntime();
        interactionEngine = new InteractionEngine(context);
        gestureDetector = new GestureDetector(context, new GestureEngine(this));
        transform = new SceneTransform();

        selectionManager = new SelectionManager();
        selectionManager.setSelectionListener(node -> {
            invalidate();

            if (node != null) {
                android.util.Log.d(
                        "SoftLauncherSelection",
                        "Selected: " + node.name
                );
            } else {
                android.util.Log.d(
                        "SoftLauncherSelection",
                        "Selection cleared"
                );
            }
        });

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setScene(Scene scene) {
        this.scene = scene;

        transform.update(scene, getWidth(), getHeight());
        runtime.setScene(scene);
        invalidate();
    }

    public Scene getScene() {
        return scene;
    }

    public SceneRuntime getRuntime() {
        return runtime;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    private PointF toSceneCoordinates(float x, float y) {
        return transform.viewToScene(x, y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (scene == null) {
            return;
        }
        canvas.save();
        canvas.translate(transform.getOffsetX(), transform.getOffsetY());
        canvas.scale(transform.getScale(), transform.getScale());
        renderer.render(canvas, scene);
        drawSelection(canvas, selectionManager.getSelectedNode());
        canvas.restore();
    }

    private void drawSelection(Canvas canvas, SceneNode node) {
        if (node == null) {
            return;
        }

        Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(android.graphics.Color.rgb(0, 120, 255));
        paint.setStyle(android.graphics.Paint.Style.STROKE);

        paint.setStrokeWidth(2f / transform.getScale());
        canvas.drawRect(node.x, node.y, node.x + node.width, node.y + node.height, paint);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        if (scene != null) {
            transform.update(scene, width, height);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onTouchEnd() {
        draggedNode = null;
    }

    @Override
    public void onTap(float x, float y) {
        PointF point = transform.viewToScene(x, y);
        SceneNode node = runtime.hitTest(point.x, point.y);

        if (node != null) {
            interactionEngine.handleTap(node);
        }
    }

    @Override
    public void onLongPress(float x, float y) {
        PointF point = transform.viewToScene(x, y);
        SceneNode node = runtime.hitTest(point.x, point.y);

        if (node != null) {
            selectionManager.select(node);
        } else {
            selectionManager.deselect();
        }
    }

    @Override
    public void onDrag(float startX, float startY, float currentX, float currentY) {
        PointF current = transform.viewToScene(currentX, currentY);

        if (draggedNode == null) {
            PointF start = transform.viewToScene(startX, startY);

            draggedNode = runtime.hitTest(start.x, start.y);

            if (draggedNode == null) {
                return;
            }

            lastTouchSceneX = start.x;
            lastTouchSceneY = start.y;
        }

        float deltaX = current.x - lastTouchSceneX;
        float deltaY = current.y - lastTouchSceneY;

        interactionEngine.moveNode(draggedNode, deltaX, deltaY);

        lastTouchSceneX = current.x;
        lastTouchSceneY = current.y;

        invalidate();
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {

    }
}
