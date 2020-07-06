package com.myapp.user.scanningexample;

public class InfoDeviceList {
    private String deviceName;
    private String deviceMac;
    private int deviceRssi;

    public InfoDeviceList(String newDeviceName, String newDeviceMac, int newDeviceRssi){
        deviceName = newDeviceName;
        deviceMac = newDeviceMac;
        deviceRssi = newDeviceRssi;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public int getDeviceRssi() {
        return deviceRssi;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public void setDeviceRssi(int deviceRssi) {
        this.deviceRssi = deviceRssi;
    }
}
