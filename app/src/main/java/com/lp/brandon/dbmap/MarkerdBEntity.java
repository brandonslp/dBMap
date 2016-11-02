package com.lp.brandon.dbmap;

/**
 * Created by brand on 1/11/2016.
 */
public class MarkerdBEntity {

    private double dB;
    private String latitude;
    private String longitude;
    private boolean status;

    public MarkerdBEntity(double dB, String latitude, String longitude, boolean status) {
        this.dB = dB;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public double getdB() {
        return dB;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public boolean isStatus() {
        return status;
    }
}
