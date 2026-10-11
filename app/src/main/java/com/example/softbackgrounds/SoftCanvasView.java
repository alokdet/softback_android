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
import com.example.softbackgrounds.selection.TransformController;

public class SoftCanvasView extends View implements GestureEngine.Listener {
    private final SceneRenderer renderer;
    private final SceneRuntime runtime;
    private final InteractionEngine interactionEngine;
    private final GestureDetector gestureDetector;
    private final SceneTransform transform;
    private final SelectionManager selectionManager;
    private final TransformController transformController;

    private boolean transforming = false;
    private int activeHandle = TransformController.NONE;

    private float transformStartX;
    private float transformStartY;
    private Scene scene;

    private SceneNode draggedNode;

    private float lastTouchSceneX;
    private float lastTouchSceneY;

    public SoftCanvasView(Context context) {
        super(context);

        renderer = new SceneRenderer(context);
        runtime = new SceneRuntime();
        interactionEngine = new InteractionEngine(context);
        gestureDetector = new GestureDetector(context, new GestureEngine(this));
        transform = new SceneTransform();
        transformController = new TransformController();

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

        // Keep the view hardware accelerated. RenderEffect/RenderNode effects
        // require a hardware canvas on Android 12+.
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
        if (node == null) return;

        canvas.save();
        runtime.applyParentTransforms(canvas, node);

        float effectiveScale = transform.getScale() * runtime.getParentWorldScale(node);

        transformController.draw(canvas, node, effectiveScale);
        canvas.restore();
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

        if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            onTouchEnd();
        }
        return true;
    }

    @Override
    public void onTouchEnd() {
        draggedNode = null;
        transforming = false;
        activeHandle = TransformController.NONE;
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
        PointF start = transform.viewToScene(startX, startY);
        PointF current = transform.viewToScene(currentX, currentY);

        // Start a resize or rotation operation.
        if (draggedNode == null && !transforming) {
            SceneNode selected = selectionManager.getSelectedNode();

            if (selected != null) {
                int handle = transformController.hitTest(selected, start.x, start.y, transform.getScale());

                if (handle != TransformController.NONE) {
                    activeHandle = handle;
                    transforming = true;

                    transformStartX = start.x;
                    transformStartY = start.y;

                    transformController.begin(selected, start.x, start.y);
                }
            }

            // If no handle was selected, move the element.
            if (!transforming) {
                draggedNode = runtime.hitTest(start.x, start.y);

                if (draggedNode == null) return;

                PointF startParent = runtime.scenePointToParent(
                        draggedNode,
                        start.x,
                        start.y
                );

                float effectiveScale =
                        transform.getScale() *
                                runtime.getParentWorldScale(draggedNode);

                int handle = transformController.hitTest(
                        draggedNode,
                        startParent.x,
                        startParent.y,
                        effectiveScale
                );

                if (handle != TransformController.NONE) {
                    transforming = true;
                    activeHandle = handle;

                    transformStartX = startParent.x;
                    transformStartY = startParent.y;

                    transformController.begin(
                            draggedNode,
                            startParent.x,
                            startParent.y
                    );

                    return;
                }

                lastTouchSceneX = startParent.x;
                lastTouchSceneY = startParent.y;
            }
        }

        if (transforming) {
            transformController.update(selectionManager.getSelectedNode(), activeHandle, transformStartX, transformStartY, current.x, current.y);
            invalidate();
            return;
        }

        if (draggedNode != null) {
            PointF currentParent = runtime.scenePointToParent(
                    draggedNode,
                    current.x,
                    current.y
            );

            transformController.update(
                    draggedNode,
                    activeHandle,
                    transformStartX,
                    transformStartY,
                    currentParent.x,
                    currentParent.y
            );

            invalidate();

//            float deltaX = parentCurrent.x - lastTouchSceneX;
//            float deltaY = parentCurrent.y - lastTouchSceneY;
//
//            interactionEngine.moveNode(draggedNode, deltaX, deltaY);
//
//            lastTouchSceneX = parentCurrent.x;
//            lastTouchSceneY = parentCurrent.y;
//
//            invalidate();
        }
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {

    }
}
