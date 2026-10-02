package com.example.softbackgrounds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.KeyguardManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LockActivity extends AppCompatActivity {
    private float downY;
    private float currentY;

    private boolean unlocking = false;

    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Make our Activity visible over Android's lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        }

        setContentView(R.layout.activity_lock);
        getSupportActionBar().hide();

        layout = (ConstraintLayout) findViewById(R.id.mainlayout);

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getY();
                        currentY = downY;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        currentY = motionEvent.getY();

                        float distance = downY - currentY;

                        if (distance > 150 && !unlocking) {
                            unlock();
                        }

                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check whether Android's Keyguard is currently active.
        KeyguardManager keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        if (keyguardManager != null) {
            boolean locked = keyguardManager.isKeyguardLocked();

            // We'll use this state later for our lock-screen controller.
        }
    }

    public void requestSystemUnlock() {

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        if (keyguardManager == null) {
            finish();
            return;
        }

        if (!keyguardManager.isKeyguardLocked()) {
            finish();
            return;
        }

        keyguardManager.requestDismissKeyguard(
                this,
                new KeyguardManager.KeyguardDismissCallback() {

                    @Override
                    public void onDismissSucceeded() {
                        finish();
                    }

                    @Override
                    public void onDismissCancelled() {
                        // Remain on SoftLauncher lock screen.
                    }

                    @Override
                    public void onDismissError() {
                        // Remain on SoftLauncher lock screen.
                    }
                }
        );
    }

    private void unlock() {
        unlocking = true;
        requestSystemUnlock();
    }
}