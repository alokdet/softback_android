package com.example.softbackgrounds;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Allow the activity content to extend behind
        // the status bar and navigation bar.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Make both system-bar backgrounds transparent.
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        SoftCanvasView canvas = new SoftCanvasView(this);

        try {
            Scene scene = SceneLoader.loadJson(
                    getIntent().getStringExtra("sldf_data")
            );

            canvas.setScene(scene);
            setContentView(canvas);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Failed to load design: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}