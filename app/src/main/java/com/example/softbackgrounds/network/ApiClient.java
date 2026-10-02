package com.example.softbackgrounds.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String BASE_URL = "http://192.168.31.35:5000/";
    private static Retrofit retrofit;

    private ApiClient() {

    }

    public static SoftBackgroundsAPI getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SoftBackgroundsAPI.class);
    }
}