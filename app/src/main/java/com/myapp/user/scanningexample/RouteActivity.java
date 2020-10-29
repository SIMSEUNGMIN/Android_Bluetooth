package com.myapp.user.scanningexample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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

        //화면의 전체 크기
        private int canvasWidth;
        private int canvasHeight;

        //화면 한 칸의 크기
        private float cellX;
        private float cellY;

        //이미지 크기
        private int imageWidth;
        private int imageHeight;

        //Paint
        private Paint paint;

        public ViewRoute(Context context, ArrayList<InfoDeviceList> newBluetoothList) {
            super(context);
            this.devicesList = newBluetoothList;
        }

        public void onDraw(Canvas canvas){
            //화면의 전체 크기를 따옴
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();

            //지도 위에 비콘을 표시하기 위해 화면을 나눔
//            cellX = canvasWidth/35;
//            cellY = canvasHeight/20;

            points = new ArrayList<>();

            paint = new Paint();

            //배경 들고오기
            Resources res =  getResources();
            BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.third_map_gray, null);
            Bitmap bit = bd.getBitmap();

            imageWidth = (int) (bit.getWidth() * 1.4);
            imageHeight = (int) (bit.getHeight() * 1.4);

            cellX = imageWidth/35;
            cellY = imageHeight/20;

            //배경 그리기
//            canvas.drawBitmap(bit, null, new Rect(0 ,0, canvasWidth, canvasHeight), null);

            //좌우 같은 여백을 주고 그림을 넣기 위해 설정
            canvas.drawBitmap(bit, null,
                    new Rect((int)((canvasWidth-imageWidth)/2) ,(int)((canvasHeight-imageHeight)/2), canvasWidth-(int)((canvasWidth-imageWidth)/2), canvasHeight-(int)((canvasHeight-imageHeight)/2)), null);
//            canvas.drawBitmap(bit, 0, 0, paint);

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
                    paint.setStyle(Paint.Style.FILL);
                    Path path = new Path();

                    //하단의 비상구
                    switch(next){
                        case 12:
                            path.moveTo(end.getCoordinateX(), end.getCoordinateY()-(cellY/3));
                            path.lineTo(end.getCoordinateX()-(cellX/2), end.getCoordinateY()+(cellY/3));
                            path.lineTo(end.getCoordinateX()+(cellX), end.getCoordinateY()+(cellY/4));
                            break;
                        case 13:
                            path.moveTo(end.getCoordinateX(), end.getCoordinateY()+(cellY/7));
                            path.lineTo(end.getCoordinateX()-(cellX/2), end.getCoordinateY()-(cellY/2));
                            path.lineTo(end.getCoordinateX()+(cellX/2), end.getCoordinateY()-(cellY/2));
                            break;
                        case 14:
                            path.moveTo(end.getCoordinateX()+(cellX/7), end.getCoordinateY());
                            path.lineTo(end.getCoordinateX()-(cellX/2), end.getCoordinateY()-(cellY/2));
                            path.lineTo(end.getCoordinateX()-(cellX/2), end.getCoordinateY()+(cellY/2));
                            break;
                    }

                    canvas.drawPath(path, paint);

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
                canvas.drawCircle(cur.getCoordinateX(), cur.getCoordinateY(), 30, paint);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);
                canvas.drawCircle(cur.getCoordinateX(), cur.getCoordinateY(), 30, paint);
            }
        }

        //스캔 결과 리스트의 UrgentLevel에 따라 색을 선정하는 함수
        //if level = 0, red. if level = 1, Magenta. if level = 2, Yellow, if level = 3 (default) or not urgent, green.
        private int selectColor(int i) {
            InfoDeviceList cur = devicesList.get(i);

            if(cur.getUrgent()){
                switch(cur.getUrgentLevel()){
                    case 0:
                        return Color.RED;
                    case 1:
                        return Color.MAGENTA;
                    case 2:
                        return Color.YELLOW;
                    default:
                        return Color.GREEN;
                }
            }
            else return Color.GREEN;
        }

        //비콘 지점을 설정(비상구 포함) -> 중심 지점
        private void initPoints() {
            float whiteSpaceWidth = ((canvasWidth-imageWidth)/2);
            float whiteSpaceHeight = ((canvasHeight-imageHeight)/2);

            points.add(new Point(whiteSpaceWidth + (float)(cellX*5.6), whiteSpaceHeight + cellY*8)); //A
            points.add(new Point(whiteSpaceWidth + (float)(cellX*8.5), whiteSpaceHeight + cellY*7)); //B
            points.add(new Point(whiteSpaceWidth + cellX*12, whiteSpaceHeight + (float)(cellY*5.8))); //C
            points.add(new Point(whiteSpaceWidth + (float)(cellX*8.5), whiteSpaceHeight + (float)(cellY*11.5))); //D
            points.add(new Point(whiteSpaceWidth + (float)(cellX*11.5), whiteSpaceHeight + (float)(cellY*11.5))); //E
            points.add(new Point(whiteSpaceWidth + (float)(cellX*16.3), whiteSpaceHeight + (float)(cellY*11.5))); //F
            points.add(new Point(whiteSpaceWidth + (float)(cellX*12.7), whiteSpaceHeight + (float)(cellY*13.5))); //G
            points.add(new Point(whiteSpaceWidth + (float)(cellX*8.5), whiteSpaceHeight + (float)(cellY*15.2))); //H
            points.add(new Point(whiteSpaceWidth + (float)(cellX*12.7), whiteSpaceHeight + (float)(cellY*15.2))); //I
            points.add(new Point(whiteSpaceWidth + cellX*16, whiteSpaceHeight + (float)(cellY*15.2))); //J
            points.add(new Point(whiteSpaceWidth + (float)(cellX*26.5), whiteSpaceHeight + (float)(cellY*11.5))); //K
            points.add(new Point(whiteSpaceWidth + (float)(cellX*26.5), whiteSpaceHeight + (float)(cellY*15.2))); //L

            //비상구
            points.add(new Point(whiteSpaceWidth + cellX*10, whiteSpaceHeight + cellY*3)); //M
            points.add(new Point(whiteSpaceWidth + (float)(cellX*12.5), whiteSpaceHeight + cellY*18)); //N
            points.add(new Point(whiteSpaceWidth + cellX*31, whiteSpaceHeight + (float)(cellY*15.2))); //O
        }

        //비상구 그리는 함수
        private void printEmergencyExit(Canvas canvas) {
            float whiteSpaceWidth = ((canvasWidth-imageWidth)/2);
            float whiteSpaceHeight = ((canvasHeight-imageHeight)/2);

            paint.setColor(Color.GRAY);

            //사각형 그리기
            canvas.drawRect(whiteSpaceWidth + (float)(cellX*8.5), whiteSpaceHeight + (float)(cellY*2.6), whiteSpaceWidth + (float)(cellX*11.5), whiteSpaceHeight +(float)(cellY*3.5), paint); //M
            canvas.drawRect(whiteSpaceWidth + (float)(cellX*11.7), whiteSpaceHeight + (float)(cellY*17.6), whiteSpaceWidth + (float)(cellX*13.7), whiteSpaceHeight + (float)(cellY*18.5), paint); //N
            canvas.drawRect(whiteSpaceWidth + (float)(cellX*30.5), whiteSpaceHeight + (float)(cellY*14.2), whiteSpaceWidth + (float)(cellX*31.5), whiteSpaceHeight + (float)(cellY*16.2), paint); //O

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7);

            //테두리
            canvas.drawRect(whiteSpaceWidth + (float)(cellX*8.5), whiteSpaceHeight + (float)(cellY*2.6), whiteSpaceWidth + (float)(cellX*11.5), whiteSpaceHeight +(float)(cellY*3.5), paint); //M
            canvas.drawRect(whiteSpaceWidth + (float)(cellX*11.7), whiteSpaceHeight + (float)(cellY*17.6), whiteSpaceWidth + (float)(cellX*13.7), whiteSpaceHeight + (float)(cellY*18.5), paint); //N
            canvas.drawRect(whiteSpaceWidth + (float)(cellX*30.5), whiteSpaceHeight + (float)(cellY*14.2), whiteSpaceWidth + (float)(cellX*31.5), whiteSpaceHeight + (float)(cellY*16.2), paint); //O
        }
    }
}
