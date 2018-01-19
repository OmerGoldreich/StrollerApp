package com.stroller.stroller.navigationPackage;

import java.io.Serializable;

public class Highlight implements Serializable{
    public final double latitude;
    public final double longitude;
    public String category;
    public String name;

    public Highlight(double latitude, double longitude,String category,String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name=name;
        this.category=category;
    }
}
