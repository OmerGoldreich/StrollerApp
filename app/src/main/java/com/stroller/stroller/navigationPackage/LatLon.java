package com.stroller.stroller.navigationPackage;

import java.io.Serializable;

public class LatLon implements Serializable{
    public final double latitude;
    public final double longitude;

    public LatLon(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
