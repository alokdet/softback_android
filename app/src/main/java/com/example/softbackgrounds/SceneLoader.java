package com.example.softbackgrounds;

import android.content.Context;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class SceneLoader {

    public static Scene load(
            Context context,
            String fileName
    ) throws Exception {

        InputStream inputStream =
                context.getAssets().open(fileName);

        Reader reader =
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                );

        Gson gson = new Gson();

        SLDFDocument document =
                gson.fromJson(
                        reader,
                        SLDFDocument.class
                );

        reader.close();

        return document.scene;
    }


    public static class SLDFDocument {

        String format;
        int version;

        Scene scene;
    }
}
