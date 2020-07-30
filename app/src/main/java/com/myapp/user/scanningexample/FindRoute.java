package com.myapp.user.scanningexample;

import java.util.List;
import java.util.Stack;

public class FindRoute {
    private static final int INF = Integer.MAX_VALUE;
    private static List<InfoDeviceList> deviceLists;

    private static int number = 12;

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
        initVariable(); //변수 초기화

        System.out.println("현재 위치 : " + findCurLocation());

        int curLocation = findCurLocation();

        findShortestPath(curLocation); //모든 노드에 대한 최단 경로 찾음

        return findBestRoute(curLocation); //세 비상구 중 가장 가까운 곳에 대한 경로 스택으로 출력
    }

    //비상구 노드들 중 가장 도달 거리가 짧은 비상구를 선택
    private static Stack findBestRoute(int curLocation) {
        int bestRoute = Integer.MAX_VALUE;
        int bestIndex = 0;

        for(int i = number-3; i < number; i++){
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
        for(int i = 0; i < number; i++){
            distance[i] = weights[curLocation][i]; //cur에서의 모든 거리값을 담아줌
            prev[i] = curLocation; //이전 노드 값 초기화
        }

        prev[curLocation] = -1; //자기 자신은 -1로 변경

        visited[curLocation] = true;

        for(int i = 0; i < number-1; i++){
            //가장 작은 distance 값을 가진 노드 찾기
            int smallIndex = findSmallIndex();
            visited[smallIndex] = true;

            for(int j = 0; j < number; j++){
                if(!visited[j] && weights[smallIndex][j] != INF){
                    if(distance[j] > distance[smallIndex] + weights[smallIndex][j]){
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
        int minIndex = -1;

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
        visited = new boolean[number];
        distance = new int[number];
        prev = new int[number];
    }

    //그래프 초기화
    private static void initWeight() {
        //가중치 초기 값 -> 자기 자신(0), 비상구와 연결(1), 비상구와 한 칸 차이(2), 그외의 연결(3), 연결X(INF)
        //-> 지도 자체에서 연결된 간선들만을 기준으로 함 (비콘에서 수집되는 urgent값 포함 X)
        weights =
                new int[][]{
                        {0, 2, INF, 2, 2, INF, INF, INF, INF, 1, INF, INF},
                        {2, 0, 2, 3, 3, 3, INF, INF, INF, INF, INF, INF},
                        {INF, 2, 0, INF, 2, 2, INF, INF, INF, INF, 1, INF},
                        {2, 3, INF, 0, 3, INF, 3, 2, INF, INF, INF, INF},
                        {2, 3, 2, 3, 0, 3, 3, 2, 3, INF, INF, INF},
                        {INF, 3, 2, INF, 3, 0, INF, 2, 3, INF, INF, INF},
                        {INF, INF, INF, 3, 3, INF, 0, 2, INF, INF, INF, INF},
                        {INF, INF, INF, 2, 2, 2, 2, 0, 2, INF, INF, 1},
                        {INF, INF, INF, INF, 3, 3, INF, 2, 0, INF, INF, INF},
                        {1, INF, INF, INF, INF, INF, INF, INF, INF, 0, INF, INF},
                        {INF, INF, 1, INF, INF, INF, INF, INF, INF, INF, 0, INF},
                        {INF, INF, INF, INF, INF, INF, INF, 1, INF, INF, INF, 0}
                };

        //수집된 스캔 정보를 살펴보면서 urgent = true일 경우
        //레벨에 따라 해당 간선에 weight 값 추가
        // (위급할 수록 weight 값을 증가시켜 그 길로 가지 못하게 함)
        for(int i = 0; i < deviceLists.size(); i++){
            InfoDeviceList cur = deviceLists.get(i);
            int weight = 0;

            if(cur.getUrgent()){
                switch(cur.getUrgentLevel()){
                    case 0:
                        weight = 4;
                        break;
                    case 1:
                        weight = 2;
                        break;
                    default:
                        weight = 0;
                        break;
                }
            }

            if(weight != 0){
                updateWeight(i, weight); //현재 위치의 붙어있는 edge에 weight 추가
            }
        }
    }

    //그래프의 weight를 증가시키는 함수
    private static void updateWeight(int cur, int weight) {
        //무방향 그래프이기 때문에 그래프 배열에서 서로의 반대방향에 대한 weight도 같이 증가
        for(int i = 0; i < weights[cur].length; i++){
            if(weights[cur][i] != INF){
                weights[cur][i] += weight;
                weights[i][cur] += weight;
            }
        }
    }
}
