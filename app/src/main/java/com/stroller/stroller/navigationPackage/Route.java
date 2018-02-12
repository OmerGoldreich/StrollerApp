package com.stroller.stroller.navigationPackage;


import java.io.Serializable;
import java.util.List;

public class Route implements Serializable{
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLon endLocation;
    public String startAddress;
    public LatLon startLocation;
    public String instructions;
    public List<LatLon> instructionsPoints;
    public List<LatLon> points;
    public int originalDuration;
    public int minutes;
}
