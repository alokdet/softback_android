package com.example.softbackgrounds.model;

import java.util.List;

public class DesignListResponse {

    private int count;
    private List<DesignSummary> designs;

    public int getCount() {
        return count;
    }

    public List<DesignSummary> getDesigns() {
        return designs;
    }
}
