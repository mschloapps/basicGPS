package com.mschloapps.basicgps;

public class GPSLoc {
    private double Lat;
    private double Long;
    private double Alt;
    private String Name;

    public GPSLoc (String nm, double lat, double lng, double alt) {
        Lat = lat;
        Long = lng;
        Alt = alt;
        Name = nm;
    }

    public double getLat(){
        return Lat;
    }

    public double getLong(){
        return Long;
    }

    public double getAlt(){
        return Alt;
    }

    public String getName() {
        return Name;
    }
}
