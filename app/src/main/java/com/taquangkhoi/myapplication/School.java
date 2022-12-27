package com.taquangkhoi.myapplication;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class School {
    private String name;
    private String address;
    private String phone;
    private LatLng latLng;
    private String placeId;

    public School(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public School(String name, String address, String Lat, String Lng, String placeId) {
        this.name = name;
        this.address = address;

        Log.i("School", "School: creating new LatLng" + Lat + " " + Lng);
        try {
            this.latLng = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lng));
        } catch (NumberFormatException e) {
            Log.i("School", "School error: " + e.getMessage());
        }
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String toString() {
        return "School: " + name + " " + address + " " + phone + " " + latLng;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
