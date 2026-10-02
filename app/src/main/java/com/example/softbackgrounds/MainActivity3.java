package com.example.softbackgrounds;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SoftCanvasView canvas = new SoftCanvasView(this);

        try {
            Scene scene = SceneLoader.loadJson(getIntent().getStringExtra("sldf_data"));
            canvas.setScene(scene);
            setContentView(canvas);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load design: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        getSupportActionBar().hide();
    }
}