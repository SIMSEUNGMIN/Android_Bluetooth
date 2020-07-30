package com.myapp.user.scanningexample;

public class InfoDeviceList {
    //스캔 결과 리스트에 필요한 변수들
    private char deviceName;
    private String deviceMac;
    private int deviceRssi;
    private boolean isUrgent;
    private int urgentLevel;

    public InfoDeviceList(char newDeviceName, String newDeviceMac, int newDeviceRssi, boolean newIsUrgent, int newLevel){
        deviceName = newDeviceName;
        deviceMac = newDeviceMac;
        deviceRssi = newDeviceRssi;
        isUrgent = newIsUrgent;
        urgentLevel = newLevel;
    }

    public char getDeviceName() {
        return deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public int getDeviceRssi() {
        return deviceRssi;
    }

    public boolean getUrgent() {
        return isUrgent;
    }

    public int getUrgentLevel() {
        return urgentLevel;
    }

    public void setDeviceName(char deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public void setDeviceRssi(int deviceRssi) {
        this.deviceRssi = deviceRssi;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public void setUrgentLevel(int urgentLevel) {
        this.urgentLevel = urgentLevel;
    }
}
