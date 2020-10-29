package com.myapp.user.scanningexample;

import java.util.List;
import java.util.Stack;

public class FindRoute {
    private static final int INF = Integer.MAX_VALUE;
    private static List<InfoDeviceList> deviceLists;

    private static int allNodeNum = 15;
    private static int exitNodeNum = 3;

    private static int[][] weights;

    //방문 확인용 배열
    private static boolean[] visited;
    //해당 노드로 가는 거리 값 저장용 배열
    private static int[] distance;
    //해당 노드로 도달하기 위해 이전에 거친 노드들을 저장해두는 배열
    //(이것을 사용해서 시작 노드로부터 목적지 노드까지 경로 추출 가능)
    private static int[] prev;

    public FindRoute(List<InfoDeviceList> newDeviceList){
        this.deviceLists = newDeviceList;
    }

    public Stack start(){
        initVariable(); //그래프 및 변수 초기화

        updateGraph(); //그래프 업데이트

        System.out.println("현재 위치 : " + findCurLocation());

        int curLocation = findCurLocation();

        findShortestPath(curLocation); //모든 노드에 대한 최단 경로 찾음

        return findBestRoute(curLocation); //세 비상구 중 가장 가까운 곳에 대한 경로 스택으로 출력
    }

    //비상구 노드들 중 가장 도달 거리가 짧은 비상구를 선택
    private static Stack findBestRoute(int curLocation) {
        int bestRoute = Integer.MAX_VALUE;
        int bestIndex = 0;

        for(int i = allNodeNum - exitNodeNum; i < allNodeNum; i++){
            if(bestRoute > distance[i]){
                bestRoute = distance[i];
                bestIndex = i;
            }
        }

        //선택된 비상구 노드로 가는 경로를 저장
        return printBestRoute(bestIndex);
    }

    //비상구 노드로 가는 경로를 스택에 저장
    private static Stack printBestRoute(int bestIndex) {
        Stack<Integer> path = new Stack<>();
        path.push(bestIndex);

        int cur = bestIndex;

        while(prev[cur] != -1){
            cur = prev[cur];
            path.push(cur);
        }

        return path;
    }

    //출발 지점으로부터 모든 노드까지의 거리값을 구하는 함수 (다익스트라 사용)
    private static void findShortestPath(int curLocation) {
        for(int i = 0; i < allNodeNum; i++){
            distance[i] = weights[curLocation][i]; //cur에서의 모든 거리값을 담아줌
            prev[i] = curLocation; //이전 노드 값 초기화
        }

        prev[curLocation] = -1; //자기 자신은 -1로 변경

        visited[curLocation] = true;

        for(int i = 0; i < allNodeNum -1; i++){
            //가장 작은 distance 값을 가진 노드 찾기
            int smallIndex = findSmallIndex();
            visited[smallIndex] = true;

            for(int j = 0; j < allNodeNum; j++){
                if(!visited[j] && weights[smallIndex][j] != INF){
                    if(distance[j] > distance[smallIndex] + weights[smallIndex][j]){
                        //새로 찾은 길이 가중치가 더 작다면 새로 찾은 길로 변경
                        distance[j] = distance[smallIndex] + weights[smallIndex][j];
                        prev[j] = smallIndex;
                    }
                }
            }
        }
    }

    //가장 작은 거리 값을 가지는 노드를 선택하는 함수
    private static int findSmallIndex() {
        int min = Integer.MAX_VALUE;
        int minIndex = 0;

        for(int i = 0; i < distance.length; i++){
            if(!visited[i] && distance[i] != 0){
                if(min > distance[i] ){
                    min = distance[i];
                    minIndex = i;
                }
            }
        }

        return minIndex;
    }

    //현재 위치를 찾는 함수 (Rssi 값이 가장 큰 인덱스)
    public static int findCurLocation() {
        int minIndex = 0;
        int min = Math.abs(deviceLists.get(0).getDeviceRssi());

        for(int i = 1; i < deviceLists.size(); i++){
            InfoDeviceList cur = deviceLists.get(i);
            if(min > Math.abs(cur.getDeviceRssi())){
                min = Math.abs(cur.getDeviceRssi());
                minIndex = i;
            }
        }
        return minIndex;
    }

    //그래프 및 배열 초기화
    private static void initVariable() {
        initWeight();
        visited = new boolean[allNodeNum];
        distance = new int[allNodeNum];
        prev = new int[allNodeNum];
    }

    //그래프 초기화
    private static void initWeight() {
        //가중치 초기 값 -> 자기 자신(0), 비상구와 연결(1), 비상구와 한 칸 차이(2), 그외의 연결(3), 연결X(INF)
        //-> 지도 자체에서 연결된 간선들만을 기준으로 함 (비콘에서 수집되는 urgent값 포함 X)
        weights =
                new int[][]{
                        //A, B, C, D, E, F, G, H, I, J, K, L, M, N, O
                        {0, 3, INF, 10, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF}, //A
                        {3, 0, 5, INF, 12, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF}, //B
                        {INF, 5, 0, INF, INF, 15, INF, INF, INF, INF, INF, INF, 10, INF, INF}, //C
                        {10, INF, INF, 0, 3, INF, 7, 7, INF, INF, INF, INF, INF, INF, INF}, //D
                        {INF, 12, INF, 3, 0, 5, INF, INF, INF, INF, INF, INF, INF, INF, INF}, //E
                        {INF, INF, 15, INF, 5, 0, 7, INF, INF, 7, 10, INF, INF, INF, INF}, //F
                        {INF, INF, INF, 7, INF, 7, 0, 7, 2, 7, INF, INF, INF, INF, INF}, //G
                        {INF, INF, INF, 7, INF, INF, 7, 0, 5, INF, INF, INF, INF, INF, INF}, //H
                        {INF, INF, INF, INF, INF, INF, 2, 5, 0, 4, INF, INF, INF, 5, INF}, //I
                        {INF, INF, INF, INF, INF, 7, 7, INF, 4, 0, INF, 20, INF, INF, INF}, //J
                        {INF, INF, INF, INF, INF, 10, INF, INF, INF, INF, 0, 5, INF, INF, INF}, //K
                        {INF, INF, INF, INF, INF, INF, INF, INF, INF, 20, 5, 0, INF, INF, 5}, //L
                        {INF, INF, 10, INF, INF, INF, INF, INF, INF, INF, INF, INF, 0, INF, INF}, //M
                        {INF, INF, INF, INF, INF, INF, INF, INF, 5, INF, INF, INF, INF, 0, INF}, //N
                        {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5, INF, INF, 0} //O
                };
    }

    //위급 레벨에 따른 가중치 설정으로 그래프를 업데이트
    private static void updateGraph() {
        for(int i = 0; i < deviceLists.size(); i++){
            InfoDeviceList cur = deviceLists.get(i);
            int weight = 0;
            int nodeNum = allNodeNum - exitNodeNum;

            //수집된 스캔 정보를 살펴보면서 getUrgent == true일 경우
            //레벨에 따라 가중치 선정 공식 적용
            //가중치 선정 공식 : 기준값(100) + (신호를 받는 노드 개수 -urgentLevel -1) * 레벨별 추가할 값(20)
            //공식을 사용해 나온 가중치로 그래프의 edge를 추가 업데이트
            if(cur.getUrgent()){
                weight = 100 + (nodeNum - cur.getUrgentLevel() - 1) * 20;
            }

            if(weight != 0){
                updateDstWeight(i, weight);
            }
        }
    }


    //목적지로 향하는 edge의 가중치 값만 증가
    private static void updateDstWeight(int cur, int weight) {
        //무방향 그래프이기 때문에 그래프 배열에서 서로의 반대방향에 대한 weight도 같이 증가
        for(int i = 0; i < weights[cur].length; i++){
            if(weights[cur][i] != INF){
//                weights[cur][i] += weight;
                weights[i][cur] += weight;
            }
        }
    }
}
