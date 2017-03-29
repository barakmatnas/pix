package com.barak.pix.Models;

/**
 * Created by barakhalevi on 5 Jul 2016.
 */
public class PixLocation {

    public Double lat;
    public Double lon;
    public String name;
    public long time;

    public PixLocation() {

    }

    public PixLocation(Double username, Double longit , String name ,long time) {
        this.lat = username;
        this.lon = longit;
        this.name = name;
        this.time = time;

    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }

    public String getName() {
        return name;
    }
}