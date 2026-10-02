package com.example.softbackgrounds.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.softbackgrounds.model.Design;
import com.example.softbackgrounds.model.DesignListResponse;
import com.example.softbackgrounds.repository.DesignRepository;

public class DesignViewModel extends ViewModel {

    private final DesignRepository repository;

    public DesignViewModel() {
        repository = new DesignRepository();
    }

    public LiveData<DesignListResponse> getDesigns() {
        return repository.getDesigns();
    }

    public LiveData<Design> getDesign(String designId) {
        return repository.getDesign(designId);
    }
}
