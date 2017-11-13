package com.example.gpsmaptest;

/**
 * 거리 순으로 특정 브랜드 점포 정보를 가져온다.
 */

public class DistModel {

    String type;
    double lat;
    double lng;
    double dist;

    public DistModel(String type, double lat, double lng, double dist) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.dist = dist;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }
}
