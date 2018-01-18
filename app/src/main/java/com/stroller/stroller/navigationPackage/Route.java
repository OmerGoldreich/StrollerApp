package com.stroller.stroller.navigationPackage;


import java.io.Serializable;
import java.util.List;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public class Route implements Serializable{
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLon endLocation;
    public String startAddress;
    public LatLon startLocation;
    public String instructions;

    public List<LatLon> points;
}
