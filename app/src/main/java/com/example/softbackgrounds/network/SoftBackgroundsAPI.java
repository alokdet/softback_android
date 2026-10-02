package com.example.softbackgrounds.network;

import com.example.softbackgrounds.model.Design;
import com.example.softbackgrounds.model.DesignListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SoftBackgroundsAPI {

    @GET("api/v1/designs")
    Call<DesignListResponse> getDesigns();

    @GET("api/v1/designs/{id}")
    Call<Design> getDesign(@Path("id") String designId);
}