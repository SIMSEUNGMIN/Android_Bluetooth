package com.myapp.user.scanningexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends BLEActivity {

    //Button
    private Button startScanBt;
    private Button finishSacanBt;

    //TextView
    private TextView status;
    private TextView location;

    //ListView
    private ListView resultListView;
    private ResultListAdapter resultListAdapter;

    //스캔 결과에 대한 리스트 (RouteActivity에서 사용 가능하기 위함)
    static ArrayList<InfoDeviceList> curMScanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startScanBt = (Button) findViewById(R.id.start_scan);
        finishSacanBt = (Button) findViewById(R.id.finish_scan);

        status = (TextView) findViewById(R.id.status);
        location = (TextView) findViewById(R.id.location);

        resultListView = (ListView)findViewById(R.id.result_list);

        //init listView adapter
        resultListAdapter = new ResultListAdapter(this, getMScanResults());
        //set list view adapter
        resultListView.setAdapter(resultListAdapter);

        //스캔 시작 버튼 클릭시 이벤트
        startScanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Scanning...");
                //스캔 시작
                scanLeDevice(true);

                //RouteActivity 호출, 새로운 화면 생성
                Intent callRouteActivity = new Intent(getApplicationContext(), RouteActivity.class);
                startActivity(callRouteActivity);

                //resultListView를 업데이트하기 위한 핸들러 동작 시작
                mHandler.post(updateList);
            }
        });

        //스캔 종류 버튼 클릭시 이벤트
        finishSacanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("Finish...");
                //스캔 종료
                scanLeDevice(false);
                //핸들러 동작 종료
                mHandler.removeMessages(0);
            }
        });
    }

    //Handler
    private Handler mHandler = new Handler();
    private Runnable updateList = new Runnable() {
        @Override
        public void run() {
            //새로운 결과리스트를 받아와 resultListAdapter에 저장하여 목록을 업데이트 시켜줌
            curMScanResult = getMScanResults();
            resultListAdapter.setResultList(curMScanResult);
            resultListAdapter.notifyDataSetChanged();

            //새로운 결과리스트를 사용해서 현재 위치를 찾아내어 목록 하단에 표시
            FindRoute findRoute = new FindRoute(curMScanResult);
            int curLocation = findRoute.findCurLocation();
            location.setText("Location : " + (char)(curLocation + 65));

            //핸들러가 750mills 주기로 동작하도록 설정
            mHandler.postDelayed(updateList, 750);
        }
    };
}