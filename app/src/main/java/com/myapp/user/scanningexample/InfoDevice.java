package com.myapp.user.scanningexample;

import android.bluetooth.BluetoothDevice;

public class InfoDevice{
    private BluetoothDevice device;
    private int Rssi;

    public InfoDevice(BluetoothDevice newDevice, int newRssi){
        device = newDevice;
        Rssi = newRssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setRssi(int rssi) {
        Rssi = rssi;
    }
}

