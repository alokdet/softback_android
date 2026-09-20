package com.example.softbackgrounds;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class SoftCanvasView extends View {

    private final SceneRenderer renderer;
    private Scene scene;

    public SoftCanvasView(Context context) {

        super(context);

        renderer = new SceneRenderer();

        setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
        );
    }

    public void setScene(Scene scene) {

        this.scene = scene;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (scene == null) {
            return;
        }

        renderer.render(
                canvas,
                scene
        );
    }
}
