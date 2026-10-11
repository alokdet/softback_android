package com.example.softbackgrounds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
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
import android.widget.Toast;

import com.example.softbackgrounds.adapter.DesignAdapter;
import com.example.softbackgrounds.model.Design;
import com.example.softbackgrounds.model.DesignSummary;
import com.example.softbackgrounds.viewmodel.DesignViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DesignAdapter.OnDesignClickListener {

    private static final int spanCount = 2;
    private static final int spacing = 42;

    private static final boolean includeEdge = true;
    private DesignAdapter adapter;
    private RecyclerView recyclerView;
    private DesignViewModel viewModel;

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
        button = findViewById(R.id.action_btn);

        adapter = new DesignAdapter(this);

        recyclerView.setLayoutManager(new FocusGridLayoutManager(this));
        recyclerView.setItemAnimator(null);
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(adapter);
        //recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        viewModel = new ViewModelProvider(this).get(DesignViewModel.class);
        observeDesigns();

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

                Intent intent = new Intent(getApplicationContext(), MusicTestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void observeDesigns() {
        //showLoading();
        viewModel.getDesigns().observe(this, response -> {
            //hideLoading();
            if (response == null) {
                //showError();
                Toast.makeText(this, "Unable to load designs.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (response.getDesigns() == null || response.getDesigns().isEmpty()) {
                //showEmpty();
                Toast.makeText(this, "Empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            //emptyText.setVisibility(View.GONE);
            //recyclerView.setVisibility(View.VISIBLE);
            adapter.setDesigns(response.getDesigns());
        });
    }

    private void openDesign(Design design) {
        if (design.getData() == null) {
            Toast.makeText(this, "Design data is unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }
        String sldfJson = design.getData().toString();

        Intent intent = new Intent(getApplicationContext(), MainActivity3.class);
        intent.putExtra("sldf_data", sldfJson);
        startActivity(intent);
    }

    @Override
    public void onDesignClick(DesignSummary design) {
        Toast.makeText(this, "Selected: " + design.getName(), Toast.LENGTH_SHORT).show();
        viewModel.getDesign(design.getId()).observe(this, completeDesign -> {
            //hideLoading();
            if (completeDesign == null) {
                Toast.makeText(this, "Unable to download design.", Toast.LENGTH_SHORT).show();
                return;
            }
            openDesign(completeDesign);
        });
    }
}