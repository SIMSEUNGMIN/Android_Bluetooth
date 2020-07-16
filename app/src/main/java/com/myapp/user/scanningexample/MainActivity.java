package com.myapp.user.scanningexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //MAC ADDRESS
    private final String MAC_ADDR1 = "74:F0:7D:C9:CF:7E";
    private final String MAC_ADDR2 = "74:F0:7D:C9:AF:1C";
    private final String MAC_ADDR3 = "74:F0:7D:C9:B0:85";

    // Tag name for Log message
    private final static String TAG = "Central";
    // used to identify adding bluetooth names
    private final static int REQUEST_ENABLE_BT= 1;
    // used to request fine location permission
    private final static int REQUEST_FINE_LOCATION= 2;
    // ble adapter
    private BluetoothAdapter mBleAdapter;
    // flag for scanning
    private boolean isScanning = false;
    // flag for connection
    private boolean isConnected = false;
    // scan results for list
    private List<InfoDeviceList> mScanResults;
    // scan callback
    private ScanCallback mScanCallback;
    // ble scanner
    private BluetoothLeScanner mBleScanner;

    //Button
    private Button startScanBt;
    private Button finishSacanBt;

    //TextView
    private TextView status;

    //ListView
    private ListView resultListView;
    private ResultListAdapter resultListAdapter;

    //Location
    private CheckedTextView locationA;
    private CheckedTextView locationB;
    private CheckedTextView locationC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startScanBt = (Button) findViewById(R.id.start_scan);
        finishSacanBt = (Button) findViewById(R.id.finish_scan);

        status = (TextView) findViewById(R.id.status);

        resultListView = (ListView)findViewById(R.id.result_list);

        locationA = (CheckedTextView) findViewById(R.id.stamp_A);
        locationB = (CheckedTextView) findViewById(R.id.stamp_B);
        locationC = (CheckedTextView) findViewById(R.id.stamp_C);

        BluetoothManager mBleManager;
        mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = mBleManager.getAdapter();

        startScanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan(view);
            }
        });

        finishSacanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScan(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // finish app if the BLE is not supported
        if(!getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE )) {
            finish();
        }
    }

    private void startScan(View v){
        status.setText("Scanning...");

        // check ble adapter and ble enabled
        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            requestEnableBLE();
            status.setText("Scanning Failed: ble not enabled");
            return;
        }

        // check if location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
                status.setText("Scanning Failed: no fine location permission");
                return;
            }
        }

        // setup scan filters
        List<ScanFilter> filters= new ArrayList<>();
        ScanFilter scanFilter1= new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR1)
                .build();
        ScanFilter scanFilter2 = new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR2)
                .build();
        ScanFilter scanFilter3 = new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR3)
                .build();
        filters.add(scanFilter1);
        filters.add(scanFilter2);
        filters.add(scanFilter3);

        // scan settings
        // set low power scan mode
        ScanSettings settings= new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        //set callback
        mScanResults = new ArrayList<>();
        mScanCallback = new BleScanCallback(mScanResults);

        //init listview adapter
        resultListAdapter = new ResultListAdapter(this, mScanResults);
        //set list view adapter
        resultListView.setAdapter(resultListAdapter);

        //start scan
        mBleScanner = mBleAdapter.getBluetoothLeScanner();
        mBleScanner.startScan(filters, settings, mScanCallback);

        //set scanning flag
        isScanning = true;
    }

    private void stopScan(View v){
        status.setText("Finish Scan");
        mBleScanner.stopScan(mScanCallback);

        locationA.setChecked(false);
        locationB.setChecked(false);
        locationC.setChecked(false);

        locationA.setTextColor(Color.parseColor("#000000"));
        locationB.setTextColor(Color.parseColor("#000000"));
        locationC.setTextColor(Color.parseColor("#000000"));
    }

    // Request BLE enable
    private void requestEnableBLE() {
        Intent mBleEnableintent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(mBleEnableintent, REQUEST_ENABLE_BT);

    }

    //Request Fine Location permission
    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions( new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
    }

    private class BleScanCallback extends ScanCallback {
        private List<InfoDeviceList> cbScanResults;

        public BleScanCallback(List<InfoDeviceList> mScanResults) {
            cbScanResults = mScanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults( List<ScanResult> results ) {
            for( ScanResult result: results ) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed( int error ) {
            Log.e( TAG, "BLE scan failed with code " + error );
        }

        private void addScanResult(ScanResult result) {

            boolean isUrgent = false;
            int urgentLevel = 0;

            //get scanned device's name
            String deviceName = result.getScanRecord().getDeviceName();
            // get scanned device
            String deviceMac = result.getDevice().getAddress();
            // get scanned RSSI
            int rssi = result.getRssi();

            int duplicate = checkDuplication(deviceName);

           //check if the scanned result already exists in the cbScanResult
           if(duplicate != -1){
                cbScanResults.get(duplicate).setDeviceRssi(rssi);
           }
           else{
               cbScanResults.add(new InfoDeviceList(deviceName, deviceMac, rssi));
           }

            // log
            Log.d( TAG, "scan results device: " + result.getScanRecord().getDeviceName());
            Log.d(TAG, "scanRecord : " + result.getScanRecord().toString());

            String urgent = new String (result.getScanRecord().getManufacturerSpecificData(12336));

            Log.d(TAG, "df : " + urgent.charAt(0));

            //긴급한 상황인지 확인
            if(urgent.charAt(0) == '1'){
                isUrgent = true;
            }

            Log.d(TAG, "isUrgent : " + isUrgent);

            //get device's name index
            int deviceNameIndex = (result.getScanRecord().getDeviceName().charAt(0) - 65);

            //긴급한 상황이면 Level 확인
            if(isUrgent){
                switch (urgent.charAt(1)){
                    case '0':
                        changeColor(deviceNameIndex, "#ffff0000");
                    break;
                    case '1':
                        changeColor(deviceNameIndex, "#FFFF00");
                        break;
                    case '2':
                        changeColor(deviceNameIndex, "#008000");
                        break;
                    case '3':
                        changeColor(deviceNameIndex, "#0000FF");
                        break;
                }
            }

            //set list view adapter
            resultListAdapter.notifyDataSetChanged();

            changeLocation();
        }

        //check duplication
        private int checkDuplication(String deviceName) {
            for(int i = 0; i < cbScanResults.size(); i++){
                InfoDeviceList cur = cbScanResults.get(i);

                if(cur.getDeviceName().equals(deviceName)){
                    return i;
                }
            }

            return -1;
        }

        private void changeColor(int deviceIndex, String inputColor) {
            switch(deviceIndex){
                case 0 :
                    locationA.setTextColor(Color.parseColor(inputColor));
                    break;
                case 1 :
                    locationB.setTextColor(Color.parseColor(inputColor));
                    break;
                case 2 :
                    locationC.setTextColor(Color.parseColor(inputColor));
                    break;
            }
        }

        private void changeLocation() {
            int minRssiIndex = 0;
            int minRssi = Math.abs(cbScanResults.get(0).getDeviceRssi());

            for(int i = 1; i < cbScanResults.size(); i++){

                int curRssi = Math.abs(cbScanResults.get(i).getDeviceRssi());

                if(minRssi > curRssi){
                    minRssiIndex = i;
                    minRssi = curRssi;
                }
            }

            int maxDevice = (cbScanResults.get(minRssiIndex).getDeviceName().charAt(0) - 65);

            switch (maxDevice){
                case 0:
                    locationA.setChecked(true);
                    locationB.setChecked(false);
                    locationC.setChecked(false);
                    break;
                case 1:
                    locationA.setChecked(false);
                    locationB.setChecked(true);
                    locationC.setChecked(false);
                    break;
                case 2:
                    locationA.setChecked(false);
                    locationB.setChecked(false);
                    locationC.setChecked(true);
                    break;
            }
        }
    }
}

