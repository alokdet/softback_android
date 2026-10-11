package com.example.softbackgrounds;

import android.graphics.DashPathEffect;
import android.graphics.Paint;

/** Applies SLDF stroke configuration to an Android Paint. */
public final class StrokeRenderer {
    private StrokeRenderer() {}

    public static void apply(Paint paint, SceneNode.StrokeDef stroke) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(0f, stroke == null ? 1f : stroke.weight));
        paint.setStrokeCap(cap(stroke == null ? null : stroke.cap));
        paint.setStrokeJoin(join(stroke == null ? null : stroke.join));
        paint.setPathEffect(null);

        if (stroke != null && stroke.dashPattern != null && stroke.dashPattern.length >= 2) {
            float[] intervals = new float[stroke.dashPattern.length];
            for (int i = 0; i < intervals.length; i++) {
                intervals[i] = Math.max(0.001f, stroke.dashPattern[i]);
            }
            paint.setPathEffect(new DashPathEffect(intervals, 0f));
        }
    }

    private static Paint.Cap cap(String value) {
        if (value == null) return Paint.Cap.BUTT;
        switch (value.toLowerCase()) {
            case "round": return Paint.Cap.ROUND;
            case "square": return Paint.Cap.SQUARE;
            default: return Paint.Cap.BUTT;
        }
    }

    private static Paint.Join join(String value) {
        if (value == null) return Paint.Join.MITER;
        switch (value.toLowerCase()) {
            case "round": return Paint.Join.ROUND;
            case "bevel": return Paint.Join.BEVEL;
            default: return Paint.Join.MITER;
        }
    }
}
