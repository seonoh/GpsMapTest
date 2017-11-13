package com.example.gpsmaptest;

import java.io.Serializable;

/**
 * Created by 선오 on 2017-07-20.
 */

public class CoffeeStoreModel implements Serializable {
    String NM;
    String ADDRESS;
    int X_AXIS;
    int Y_AXIS;
    String type;

    //거리 추가
    double dist;

    @Override
    public String toString() {
        return "CoffeeStoreModel{" +
                "NM='" + NM + '\'' +
                ", ADDRESS='" + ADDRESS + '\'' +
                ", X_AXIS=" + X_AXIS +
                ", Y_AXIS=" + Y_AXIS +
                ", type='" + type + '\'' +
                ", dist=" + dist +
                ", index=" + index +
                '}';
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    //필요에 의해 컬럼을 추가하였다.
    // 서버에서 받은 데이터로 세팅되는 것이 아니라, 작위적으로 후에 세팅된다.
    int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getNM() {
        return NM;
    }

    public void setNM(String NM) {
        this.NM = NM;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public void setADDRESS(String ADDRESS) {
        this.ADDRESS = ADDRESS;
    }

    public int getX_AXIS() {
        return X_AXIS;
    }

    public void setX_AXIS(int x_AXIS) {
        X_AXIS = x_AXIS;
    }

    public int getY_AXIS() {
        return Y_AXIS;
    }

    public void setY_AXIS(int y_AXIS) {
        Y_AXIS = y_AXIS;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
