package com.example.softbackgrounds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int spanCount = 2;
    private static final int spacing = 42;

    private static final boolean includeEdge = true;
    private LIstAdapter adapter;
    private List<Model> models;
    private RecyclerView recyclerView;

    private AppCompatButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

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
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        models = new ArrayList<>();
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));
        models.add(new Model("Title of te year", "Description of lorem ipsum ten eofh folk retail pool yes go to ball."));

        adapter = new LIstAdapter(models);

        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        button = findViewById(R.id.action_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(getApplicationContext(), BackdropService.class));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                try {
//                    startActivity(intent);
//                } catch (Exception e) {
//                    Intent fallbackIntent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
//                    startActivity(fallbackIntent);
//                }

                Intent intent = new Intent(getApplicationContext(), MainActivity3.class);
                startActivity(intent);
            }
        });
    }
}