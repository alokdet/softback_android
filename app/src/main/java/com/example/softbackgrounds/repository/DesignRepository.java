package com.example.softbackgrounds.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.softbackgrounds.model.Design;
import com.example.softbackgrounds.model.DesignListResponse;
import com.example.softbackgrounds.network.ApiClient;
import com.example.softbackgrounds.network.SoftBackgroundsAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DesignRepository {

    private final SoftBackgroundsAPI api;

    public DesignRepository() {
        api = ApiClient.getApi();
    }

    public LiveData<DesignListResponse> getDesigns() {
        MutableLiveData<DesignListResponse> result = new MutableLiveData<>();

//        api.getDesigns().enqueue(
//                new Callback<DesignListResponse>() {
//
//                    @Override
//                    public void onResponse(
//                            Call<DesignListResponse> call,
//                            Response<DesignListResponse> response) {
//
//                        if (response.isSuccessful()
//                                && response.body() != null) {
//
//                            result.postValue(
//                                    response.body()
//                            );
//
//                        } else {
//
//                            result.postValue(null);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(
//                            Call<DesignListResponse> call,
//                            Throwable t) {
//
//                        result.postValue(null);
//                    }
//                }
//        );

        api.getDesigns().enqueue(
                new Callback<DesignListResponse>() {

                    @Override
                    public void onResponse(
                            Call<DesignListResponse> call,
                            Response<DesignListResponse> response) {

                        android.util.Log.d(
                                "SoftBackgroundsAPI",
                                "HTTP CODE: " + response.code()
                        );

                        if (response.body() != null) {

                            android.util.Log.d(
                                    "SoftBackgroundsAPI",
                                    "COUNT: " + response.body().getCount()
                            );

                            android.util.Log.d(
                                    "SoftBackgroundsAPI",
                                    "DESIGNS: " + response.body().getDesigns()
                            );

                            result.postValue(response.body());

                        } else {

                            android.util.Log.e(
                                    "SoftBackgroundsAPI",
                                    "BODY IS NULL"
                            );

                            result.postValue(null);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<DesignListResponse> call,
                            Throwable t) {

                        android.util.Log.e(
                                "SoftBackgroundsAPI",
                                "REQUEST FAILED",
                                t
                        );

                        result.postValue(null);
                    }
                }
        );

        return result;
    }

    public LiveData<Design> getDesign(String designId) {

        MutableLiveData<Design> result =
                new MutableLiveData<>();

        api.getDesign(designId).enqueue(
                new Callback<Design>() {

                    @Override
                    public void onResponse(
                            Call<Design> call,
                            Response<Design> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            result.postValue(
                                    response.body()
                            );

                        } else {

                            result.postValue(null);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Design> call,
                            Throwable t) {

                        result.postValue(null);
                    }
                }
        );

        return result;
    }
}
