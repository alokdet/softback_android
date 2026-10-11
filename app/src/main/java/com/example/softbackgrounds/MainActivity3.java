package com.example.softbackgrounds;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class MainActivity3 extends AppCompatActivity {

    private static final int REQUEST_HOME_ROLE = 1001;

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

        //requestDefaultHomeRole();
    }

    private void requestDefaultHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager =
                    getSystemService(RoleManager.class);

            if (roleManager == null
                    || !roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                Toast.makeText(this,
                        "Home role is unavailable on this device",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                // Our app is already the default Home app.
                return;
            }

            Intent requestIntent =
                    roleManager.createRequestRoleIntent(
                            RoleManager.ROLE_HOME);

            startActivityForResult(requestIntent, REQUEST_HOME_ROLE);

        } else {
            // Older Android versions: let the user choose
            // a Home app through Android's activity resolver.
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(homeIntent);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_HOME_ROLE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,
                        "Default launcher selected",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Default launcher selection was cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}