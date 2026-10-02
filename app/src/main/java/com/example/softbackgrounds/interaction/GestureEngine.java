package com.example.softbackgrounds.interaction;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureEngine extends GestureDetector.SimpleOnGestureListener {

    public interface Listener {
        void onTap(float x, float y);
        void onLongPress(float x, float y);
        void onDrag(float startX, float startY, float currentX, float currentY);
        void onFling(float startX, float startY, float velocityX, float velocityY);
        void onTouchEnd();
    }

    private final Listener listener;

    private float downX;
    private float downY;

    public GestureEngine(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        downX = event.getX();
        downY = event.getY();

        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        listener.onTap(event.getX(), event.getY());
        listener.onTouchEnd();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        listener.onLongPress(event.getX(), event.getY());
    }

    @Override
    public boolean onScroll(MotionEvent first, MotionEvent current, float distanceX, float distanceY) {
        listener.onDrag(downX, downY, current.getX(), current.getY());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent first, MotionEvent current, float velocityX, float velocityY) {
        listener.onFling(first.getX(), first.getY(), velocityX, velocityY);
        return true;
    }
}
