package com.example.softbackgrounds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity3 extends AppCompatActivity {

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
        SoftCanvasView canvas =
                new SoftCanvasView(this);

        try {

            Scene scene =
                    SceneLoader.load(
                            this,
                            "softlauncher_scene.sldf.json"
                    );

            canvas.setScene(scene);

            setContentView(canvas);

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Failed to load SLDF",
                    Toast.LENGTH_LONG
            ).show();
        }

        getSupportActionBar().hide();
    }
}