package com.example.softbackgrounds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.softbackgrounds.motion.MotionEngine;
import com.example.softbackgrounds.motion.MotionState;
import com.example.softbackgrounds.motion.MotionStateListener;

public class MainActivity2 extends AppCompatActivity implements MotionStateListener {

    private MotionEngine motionEngine;
    private AppCompatTextView rollTV, pitchTV, sensorTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {

            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.DarkTheme_SoftBackgrounds);
                getWindow().setNavigationBarColor(Color.BLACK);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.LightTheme_SoftBackgrounds);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setNavigationBarColor(Color.WHITE);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        getSupportActionBar().hide();

        rollTV = findViewById(R.id.roll);
        pitchTV = findViewById(R.id.pitch);
        sensorTV = findViewById(R.id.sensor);

        motionEngine = new MotionEngine(this);
        motionEngine.setMotionStateListener(this);

        sensorTV.setText(motionEngine.getProviderType().name());
    }

    @Override
    public void onMotionStateChanged(MotionState motionState) {
        runOnUiThread(() -> {
//            rollTV.setText(String.valueOf(Math.round(motionState.roll)));
//            pitchTV.setText(String.valueOf(Math.round(motionState.pitch)));

            String roll = "ROLL: " + String.valueOf(Math.round(motionState.roll));
            String pitch = "PITCH: " + String.valueOf(Math.round(motionState.pitch));

            rollTV.setText(roll);
            pitchTV.setText(pitch);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        motionEngine.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        motionEngine.stop();
    }
}