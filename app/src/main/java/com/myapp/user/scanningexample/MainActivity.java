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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    // scan results
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startScanBt = (Button) findViewById(R.id.start_scan);
        finishSacanBt = (Button) findViewById(R.id.finish_scan);

        status = (TextView) findViewById(R.id.status);

        resultListView = (ListView)findViewById(R.id.result_list);

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
        ScanFilter scanFilter= new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR1)
                .setDeviceAddress(MAC_ADDR2)
                .setDeviceAddress(MAC_ADDR3)
                .build();
        filters.add(scanFilter);

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

        //start scan
        mBleScanner = mBleAdapter.getBluetoothLeScanner();
        mBleScanner.startScan(filters, settings, mScanCallback);

        //set scanning flag
        isScanning = true;
    }

    private void stopScan(View v){
        status.setText("Finish Scan");
        mBleScanner.stopScan(mScanCallback);
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
            status.setText("scanned Device : " + result.getScanRecord().getDeviceName());

            //set list view adapter
            resultListView.setAdapter(resultListAdapter);
            resultListAdapter.notifyDataSetChanged();
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

    }
}

