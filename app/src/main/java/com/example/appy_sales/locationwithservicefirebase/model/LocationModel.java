package com.example.appy_sales.locationwithservicefirebase.model;

/**
 * Created by Appy-Sales on 04-01-2018.
 */

public class LocationModel {
   public String latitude;
    public String longitude;

    public LocationModel() {
    }

    public LocationModel(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
