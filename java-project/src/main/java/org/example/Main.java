package org.example;

public class Main {

    public static void main(String[] args) {
        try {
            //TaskWordCounting.run(false); // Task 1: Run on Docker Spark cluster
            //TaskRoomSensorTelemetry.run(false); // Task 2: Run on Docker Spark cluster
            TaskKMeans.run(false); // Task 3: Run on Docker Spark cluster
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}