package com.myapp.user.scanningexample;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class BLEActivity extends AppCompatActivity {

    //Tag name for Log message
    private final static String TAG = "Central";
    //used to identify adding bluetooth names
    private final static int REQUEST_ENABLE_BT= 1;
    //used to request fine location permission
    private final static int REQUEST_FINE_LOCATION= 2;

    //MAC ADDRESS (지도 상에 존재할 비콘들의 MAC 주소)
    private final String MAC_ADDR0 = "74:F0:7D:C9:CF:7E";
    private final String MAC_ADDR1 = "74:F0:7D:C9:AF:1C";
    private final String MAC_ADDR2 = "74:F0:7D:C9:B0:85";
//    private final String MAC_ADDR3 = "";
//    private final String MAC_ADDR4 = "";
//    private final String MAC_ADDR5 = "";
//    private final String MAC_ADDR6 = "";
//    private final String MAC_ADDR7 = "";
//    private final String MAC_ADDR8 = "";

    //BLE
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private BluetoothAdapter mBluetoothAdapter;
    private List<ScanFilter> filters;

    //스캔 결과 저장 리스트
    private List<InfoDeviceList> mScanResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Need Location Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }

        //객체 생성 및 초기화, BLE 세팅
        mScanResults = new ArrayList<>();
        initVariable();
        setupBLE();
        setupBLEDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"Destroy()");
        super.onDestroy();
    }

    //스캔 결과를 저장하는 리스트에 검색될 비콘 추가 (미리 MAC주소를 저장할 수 있게끔 함)
    protected void initVariable() {
//        mScanResults.add(new InfoDeviceList('A', MAC_ADDR0, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('B', MAC_ADDR1, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('C', MAC_ADDR2, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('D', MAC_ADDR3, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('E', MAC_ADDR4, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('F', MAC_ADDR5, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('G', MAC_ADDR6, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('H', MAC_ADDR7, -1000, false, 0));
//        mScanResults.add(new InfoDeviceList('I', MAC_ADDR7, -1000, false, 0));

        mScanResults.add(new InfoDeviceList('A', "00:00:00:00:00:00", -100, true, 2));
        mScanResults.add(new InfoDeviceList('B', "11:11:11:11:11:11", -100, true, 1));
        mScanResults.add(new InfoDeviceList('C', "22:22:22:22:22:22", -100, true, 0));
        mScanResults.add(new InfoDeviceList('D', "33:33:33:33:33:33", -100, true, 2));
        mScanResults.add(new InfoDeviceList('E', "44:44:44:44:44:44", -100, true, 2));
        mScanResults.add(new InfoDeviceList('F', "55:55:55:55:55:55", -50, true, 1));
        mScanResults.add(new InfoDeviceList('G', "66:66:66:66:66:66", -100, true, 2));
        mScanResults.add(new InfoDeviceList('H', "77:77:77:77:77:77", -100, true, 2));
        mScanResults.add(new InfoDeviceList('I', "88:88:88:88:88:88", -100, true, 2));
    }

    //자식 클래스에서 스캔 결과를 가져갈 수 있도록 반환하는 함수
    protected ArrayList<InfoDeviceList> getMScanResults(){
        return (ArrayList<InfoDeviceList>) mScanResults;
    }

    //BLE 관련 설정, (지원 확인 및 Bluetooth adapter 초기화)
    private void setupBLE() {
        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getApplicationContext(), "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter. For API level 18 and above,
        // get a reference to BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    //BLE Setup (BLE 속성 세팅)
    private void setupBLEDevice() {
        //BLE가 켜지지 않은 상태라면 BLE를 켜도록 한다.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            //BLE가 켜져 있는 상태라면 scan filter 설정 및 스캔 모드 설정

            //set scan filters -> 원하는 MAC 주소만 블루투스 검색 시 받을 수 있게 하기 위해서 스캔 필터 설정
            setScanFilters();

            //set scan mode
            // SCAN_MODE_BALANCED: 3초 스캐닝 후 3초 쉬고 반복
            // SCAN_MODE_LOW_POWER: 5초마다 스캐닝
            // SCAN_MODE_LOW_LATENCY: 100ms 미만으로 스캐닝
            // SCAN_MODE_OPPORTUNISTIC: 5초마다 스캐닝, 한 번 스캐닝할 때 많이 ..
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
    }

    //원한는 맥주소들만 블루투스 검색 시 받을 수 있도록 설정 (지도상에 존재하는 비콘의 MAC주소)
    private void setScanFilters() {
        filters = new ArrayList<>();

        //지도상에 존재하는 비콘들로 설정
        ScanFilter scanFilter0= new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR0)
                .build();
        ScanFilter scanFilter1= new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR1)
                .build();
        ScanFilter scanFilter2 = new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR2)
                .build();
//            ScanFilter scanFilter3 = new ScanFilter.Builder()
//                    .setDeviceAddress(MAC_ADDR3)
//                    .build();
//            ScanFilter scanFilter4 = new ScanFilter.Builder()
//                    .setDeviceAddress(MAC_ADDR4)
//                    .build();
//            ScanFilter scanFilter5 = new ScanFilter.Builder()
//                    .setDeviceAddress(MAC_ADDR5)
//                    .build();
//            ScanFilter scanFilter6 = new ScanFilter.Builder()
//                    .setDeviceAddress(MAC_ADDR6)
//                    .build();
//            ScanFilter scanFilter7 = new ScanFilter.Builder()
//                    .setDeviceAddress(MAC_ADDR7)
//                    .build();
//            ScanFilter scanFilter8 = new ScanFilter.Builder()
//                    .setDeviceAddress(MAC_ADDR8)
//                    .build();

        //스캔 시 이용되는 필터에 추가
        filters.add(scanFilter0);
        filters.add(scanFilter1);
        filters.add(scanFilter2);
//            filters.add(scanFilter3);
//            filters.add(scanFilter4);
//            filters.add(scanFilter5);
//            filters.add(scanFilter6);
//            filters.add(scanFilter7);
//            filters.add(scanFilter8);
    }

    //스캔 시작 및 종료 함수
    protected boolean scanLeDevice(final boolean enable) {
        if (enable) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "스캔 시작", Toast.LENGTH_SHORT).show();
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        }
        else {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "스캔 종료", Toast.LENGTH_SHORT).show();
                mLEScanner.stopScan(mScanCallback);
            }
        }

        return enable;
    }

    // BLE signal 수신 시 호출되는 함수
    private ScanCallback mScanCallback = new ScanCallback() {

        //스캔이 되었을 때 스캔 정보를 저장하는 함수 (addScanResult) 호출
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for( ScanResult result: results ) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int error) {
            Log.e(TAG, "BLE scan failed with code " + error);
        }

        //스캔 필터를 거쳐 들어온 정보를 저장(지도 상에 존재하는 비콘의 정보)
        private void addScanResult(ScanResult result) {
            //get scanned device
            String deviceMac = result.getDevice().getAddress();
            //get scanned RSSI
            int deviceRssi = result.getRssi();

            //urgent (들어온 데이터 패킷 내에서 위급상황에 대한 정보 추출)
            String urgent = new String (result.getScanRecord().getManufacturerSpecificData(12336));

            boolean isUrgent = false;

            //긴급한 상황인지 확인
            if(urgent.charAt(0) == '1'){
                isUrgent = true;
            }

            //긴급에 따른 Level 정보 추출 (urgent = false일 경우에도 0으로 추출 필요)
            int urgentLevel = (urgent.charAt(1) - 48);

            //update the mScanResults (리스트의 값을 업데이트)
            //리스트를 돌면서 입력된 스캔 정보의 맥 주소와 리스트의 맥 주소가 맞다면 정보를 업데이트
            //맞지 않을 경우에는 그냥 넘김
            for(int i = 0; i < mScanResults.size(); i++){
                InfoDeviceList cur = mScanResults.get(i);
                if(cur.getDeviceMac().equals(deviceMac)){
                    cur.setDeviceRssi(deviceRssi);
                    cur.setUrgent(isUrgent);
                    cur.setUrgentLevel(urgentLevel);
                }
            }
        }
    };
}