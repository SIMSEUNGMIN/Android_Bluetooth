package com.myapp.user.scanningexample;

public class Point {
    //어떠한 지점의 x,y 좌표를 저장
    private float coordinateX;
    private float coordinateY;

    public Point(float newX, float newY){
        this.coordinateX = newX;
        this.coordinateY = newY;
    }

    public float getCoordinateX() {
        return coordinateX;
    }

    public float getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateX(float coordinateX) {
        this.coordinateX = coordinateX;
    }

    public void setCoordinateY(float coordinateY) {
        this.coordinateY = coordinateY;
    }
}
