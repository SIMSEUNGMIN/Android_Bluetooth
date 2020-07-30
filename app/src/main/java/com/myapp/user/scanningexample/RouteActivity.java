package com.myapp.user.scanningexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RouteActivity extends AppCompatActivity {
    private ArrayList<InfoDeviceList> curMScanResult;

    private Handler mHandler = new Handler();
    private Runnable updateList = new Runnable() {
        @Override
        public void run() {
            //스캔 정보를 받아와서 새로 View를 생성
            curMScanResult = MainActivity.curMScanResult;

            ViewRoute viewRoute = new ViewRoute(RouteActivity.this, curMScanResult);
            setContentView(viewRoute);

            //핸들러 주기적 실행
            mHandler.postDelayed(updateList, 750);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //새로 View를 만드는 핸들러 동작 시작
        mHandler.post(updateList);
    }

    protected class ViewRoute extends View{
        //스캔 정보 리스트
        private ArrayList<InfoDeviceList> devicesList;

        //비콘이 위치할 지점을 저장해둔 List
        private List<Point> points;

        //화면 한 칸의 크기
        private float cellX;
        private float cellY;

        //Paint
        private Paint paint;

        public ViewRoute(Context context, ArrayList<InfoDeviceList> newBluetoothList) {
            super(context);
            this.devicesList = newBluetoothList;
        }

        public void onDraw(Canvas canvas){

            //화면의 전체 크기를 따옴
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();

            //3*3 크기를 만들기 위해 화면을 나눔
            cellX = canvasWidth/10;
            cellY = canvasHeight/12;

            points = new ArrayList<>();

            paint = new Paint();

            //지점 초기화
            initPoints();

            //비상구 그리기
            printEmergencyExit(canvas);

            //FindRoute
            FindRoute findRoute = new FindRoute(devicesList);
            Stack<Integer> resultPath = findRoute.start();

            //경로에 따라 선 그리기
            printResultPath(canvas, resultPath);

            //블루투스 포인트 그리기
            printBluetoothPoint(canvas);
        }

        //가장 짧은 거리 경로를 스택으로 받아와 경로를 출력
        private void printResultPath(Canvas canvas, Stack<Integer> resultPath) {
            int pathSize = resultPath.size();
            int cur = resultPath.pop();
            int next = 0;

            //스택에 있는 값을 하나씩 꺼내와 그에 맞춰 경로를 paint로 그림
            //cur 값과 next 값의 좌표를 통해 선을 그려나감
            for(int i = 0; i < pathSize-1; i++){
                next = resultPath.pop();

                Point start = points.get(cur);
                Point end = points.get(next);

                paint.setColor(Color.RED);
                paint.setStrokeWidth(20);
                canvas.drawLine(start.getCoordinateX(), start.getCoordinateY(),
                        end.getCoordinateX(), end.getCoordinateY(), paint);

                //비상구로 도달하는 지점에서 화살표 표시를 그리기 위한 부분
                if(i == pathSize-2){
                    //하단의 비상구
                    if(next == 11){
                        paint.setStyle(Paint.Style.FILL);

                        Path path = new Path();
                        path.moveTo(end.getCoordinateX(), end.getCoordinateY()+(cellY/7));
                        path.lineTo(end.getCoordinateX()-(cellX/2), end.getCoordinateY()-(cellY/2));
                        path.lineTo(end.getCoordinateX()+(cellX/2), end.getCoordinateY()-(cellY/2));
                        canvas.drawPath(path, paint);
                    }
                    //상단의 비상구
                    else{
                        paint.setStyle(Paint.Style.FILL);

                        Path path = new Path();
                        path.moveTo(end.getCoordinateX(), end.getCoordinateY()-(cellY/7));
                        path.lineTo(end.getCoordinateX()-(cellX/2), end.getCoordinateY()+(cellY/2));
                        path.lineTo(end.getCoordinateX()+(cellX/2), end.getCoordinateY()+(cellY/2));
                        canvas.drawPath(path, paint);
                    }
                }

                cur = next;
            }
        }

        //비콘의 포인트를 그리는 함수
        private void printBluetoothPoint(Canvas canvas) {
            //비상구를 제외한 나머지 점들을 스캔 결과 리스트의 urgentLevel에 따라
            //색을 입혀 그림
            for(int i = 0; i < points.size()-3; i++){
                paint.setColor(selectColor(i));
                paint.setStyle(Paint.Style.FILL);
                Point cur = points.get(i);
                canvas.drawCircle(cur.getCoordinateX(), cur.getCoordinateY(), 50, paint);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);
                canvas.drawCircle(cur.getCoordinateX(), cur.getCoordinateY(), 50, paint);
            }
        }

        //스캔 결과 리스트의 UrgentLevel에 따라 색을 선정하는 함수
        //if level = 0, red. if level = 1, Yellow. if level = 2 (default) or not urgent, green.
        private int selectColor(int i) {
            InfoDeviceList cur = devicesList.get(i);

            if(cur.getUrgent()){
                switch(cur.getUrgentLevel()){
                    case 0:
                        return Color.RED;
                    case 1:
                        return Color.YELLOW;
                    default:
                        return Color.GREEN;
                }
            }
            else return Color.GREEN;
        }

        //9개의 비콘 지점을 설정(항상 같은 위치에 존재할 수 있도록)
        private void initPoints() {
            points.add(new Point(cellX*2, cellY*3));
            points.add(new Point(cellX*5, cellY*3));
            points.add(new Point(cellX*8, cellY*3));
            points.add(new Point(cellX*2, cellY*6));
            points.add(new Point(cellX*5, cellY*6));
            points.add(new Point(cellX*8, cellY*6));
            points.add(new Point(cellX*2, cellY*9));
            points.add(new Point(cellX*5, cellY*9));
            points.add(new Point(cellX*8, cellY*9));
            points.add(new Point(cellX*2, cellY*1));
            points.add(new Point(cellX*8, cellY*1));
            points.add(new Point(cellX*5, cellY*11));
        }

        //비상구 그리는 함수
        private void printEmergencyExit(Canvas canvas) {
            paint.setColor(Color.GRAY);

            //순서대로 우측 상단, 좌측 상단, 하단의 가운데
            canvas.drawRect(cellX, (float) 0.0, (cellX*3), cellY, paint);
            canvas.drawRect((cellX*7), (float)0.0, (cellX*9), cellY, paint);
            canvas.drawRect((cellX*4), (cellY*11), (cellX*6), (cellY*12), paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7);

            //테두리
            canvas.drawRect(cellX, (float) 0.0, (cellX*3), cellY, paint);
            canvas.drawRect((cellX*7), (float)0.0, (cellX*9), cellY, paint);
            canvas.drawRect((cellX*4), (cellY*11), (cellX*6), (cellY*12), paint);
        }
    }
}
