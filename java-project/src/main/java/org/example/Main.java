package org.example;

public class Main {

    public static void main(String[] args) {
        try {
            TaskWordCounting.run(false); // Run on Docker Spark cluster
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}