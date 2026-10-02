package com.example.softbackgrounds.model;

import com.google.gson.JsonObject;

public class Design {

    private String _id;
    private String name;
    private int version;
    private String status;
    private String thumbnail;
    private JsonObject data;
    private String createdAt;
    private String updatedAt;

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public JsonObject getData() {
        return data;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
