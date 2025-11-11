package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TaskKMeans implements Serializable {

    public static class DataPoint implements Serializable {
        private final double[] features;

        public DataPoint(double[] features) {
            this.features = features;
        }

        public double[] getFeatures() {
            return features;
        }

        @Override
        public String toString() {
            return "DataPoint{" + Arrays.toString(features) + '}';
        }

        // For convergence check, we need to compare centroids
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataPoint dataPoint = (DataPoint) o;
            return Arrays.equals(features, dataPoint.features);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(features);
        }
    }

    /**
     * Calculates the Euclidean distance between two feature vectors (double[]).
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return The Euclidean distance.
     */
    public static double euclideanDistance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * Finds the index of the closest centroid for a given data point.
     * @param point The data point.
     * @param centroids A list of current centroids.
     * @return The index of the closest centroid.
     */
    public static int findClosestCentroid(DataPoint point, List<DataPoint> centroids) {
        double minDistance = Double.MAX_VALUE;
        int closestCentroidId = -1;

        for (int i = 0; i < centroids.size(); i++) {
            double distance = euclideanDistance(point.getFeatures(), centroids.get(i).getFeatures());
            if (distance < minDistance) {
                minDistance = distance;
                closestCentroidId = i;
            }
        }

        return closestCentroidId;
    }

    /**
     * Calculates the new centroid (mean) for a cluster of data points.
     * @param pointsInCluster An Iterable of data points belonging to one cluster.
     * @return A new DataPoint representing the mean of the cluster.
     */
    public static DataPoint calculateNewCentroid(Iterable<DataPoint> pointsInCluster) {
        List<DataPoint> pointsList = new ArrayList<>();
        pointsInCluster.forEach(pointsList::add);

        if (pointsList.isEmpty()) {
            return new DataPoint(new double[0]);
        }

        int numFeatures = pointsList.get(0).getFeatures().length;
        double[] sum = new double[numFeatures];

        for (DataPoint point : pointsList) {
            double[] features = point.getFeatures();
            for (int i = 0; i < numFeatures; i++) {
                sum[i] += features[i];
            }
        }

        double[] newCentroidFeatures = new double[numFeatures];
        for (int i = 0; i < numFeatures; i++) {
            newCentroidFeatures[i] = sum[i] / pointsList.size();
        }

        return new DataPoint(newCentroidFeatures);
    }


    public static void run(boolean local) {
        //Initialize Spark Session
        SparkConf sparkConf = null;
        String sparkApplicationName = "ParallelKMeans";
        String datasetFileName = "dataset-showering.csv";
        String sparkMasterUrl = "spark://spark-master:7077";

        if(local){
            sparkConf = new SparkConf().setAppName(sparkApplicationName).setMaster("local[*]");
        }else {
            sparkConf = new SparkConf().setAppName(sparkApplicationName).setMaster(sparkMasterUrl);
        }

        // Initialize SparkSession using the configuration
        SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(sparkSession.sparkContext());

        // Load Data from CSV into a Dataset<Row>
        // Use SparkSession.read() to load CSV into a Dataset<Row>
        Dataset<Row> rawData = DatasetHelper.getDataset(sparkSession, datasetFileName, local);
        rawData = rawData.drop("timestamp").drop("unix_timestamp");

        //Filter out potentially noisy data
        rawData = rawData.filter("volume >= 50");

        System.out.println("Schema of loaded CSV:");
        rawData.printSchema();
        System.out.println("First 5 rows of raw data:");
        rawData.show(5);

        // Split Data into Training and Test Sets using Dataset.randomSplit()
        double[] weights = {0.8, 0.2}; // 80% for training, 20% for testing
        long seed = 123L; // For reproducibility
        Dataset<Row>[] splits = rawData.randomSplit(weights, seed);
        Dataset<Row> trainingDataset = splits[0];
        Dataset<Row> testDataset = splits[1];
        System.out.println("Training data points (Dataset): " + trainingDataset.count());
        System.out.println("Test data points (Dataset): " + testDataset.count());

        // Convert Dataset<Row> to JavaRDD<DataPoint> for our custom K-Means algorithm
        // This step extracts the features (assuming all columns are features of type Double)
        JavaRDD<DataPoint> trainingDataRDD = trainingDataset.javaRDD().map(row -> {
            double[] features = new double[row.length()];
            for (int i = 0; i < row.length(); i++) {
                // Assuming all columns are numeric (doubles) and represent features
                features[i] = row.getDouble(i);
            }
            return new DataPoint(features);
        }).cache(); // Cache the RDD as it will be used multiple times

        JavaRDD<DataPoint> testDataRDD = testDataset.javaRDD().map(row -> {
            double[] features = new double[row.length()];
            for (int i = 0; i < row.length(); i++) {
                features[i] = row.getDouble(i);
            }
            return new DataPoint(features);
        }).cache(); // Cache the RDD as it will be used once for final assignment

        // K-Means Parameters
        final int k = 4; // Number of clusters
        final int maxIterations = 100;
        final double convergenceThreshold = 1e-4; // How much centroids can change before stopping

        // Initialize Centroids (randomly pick k data points from the TRAINING dataset)
        // Create a mutable copy since takeSample returns an immutable list
        List<DataPoint> currentCentroids = new ArrayList<>(trainingDataRDD.takeSample(false, k, new Random().nextLong()));

        System.out.println("\nInitial Centroids:");
        currentCentroids.forEach(c -> System.out.println(Arrays.toString(c.getFeatures())));

        // K-Means Iteration Loop (Training on trainingDataRDD)
        for (int iter = 0; iter < maxIterations; iter++) {
            System.out.println("\nIteration " + (iter + 1));

            // Broadcast current centroids to all worker nodes
            Broadcast<List<DataPoint>> centroidsBroadcast = jsc.broadcast(currentCentroids);

            // E-step: Assign each training data point to its closest centroid
            JavaPairRDD<Integer, DataPoint> clusteredPoints = trainingDataRDD.mapToPair(point -> {
                int closestCentroidId = findClosestCentroid(point, centroidsBroadcast.value());
                return new Tuple2<>(closestCentroidId, point);
            });

            // M-step: Calculate new centroids based on the mean of assigned points
            JavaPairRDD<Integer, DataPoint> newCentroidsRDD = clusteredPoints.groupByKey()
                    .mapToPair(tuple -> {
                        int clusterId = tuple._1();
                        Iterable<DataPoint> pointsInCluster = tuple._2();
                        DataPoint newCentroid = calculateNewCentroid(pointsInCluster);
                        return new Tuple2<>(clusterId, newCentroid);
                    });

            List<Tuple2<Integer, DataPoint>> newCentroidsList = newCentroidsRDD.collect();
            // Create a mutable copy of the list before sorting
            newCentroidsList = new ArrayList<>(newCentroidsList);
            newCentroidsList.sort((a, b) -> Integer.compare(a._1(), b._1()));

            // Check for convergence
            boolean converged = true;
            for (int i = 0; i < k; i++) {
                if (i < newCentroidsList.size()) {
                    DataPoint newCentroid = newCentroidsList.get(i)._2();
                    DataPoint oldCentroid = currentCentroids.get(i);
                    double distance = euclideanDistance(newCentroid.getFeatures(), oldCentroid.getFeatures());
                    if (distance > convergenceThreshold) {
                        converged = false;
                    }
                    currentCentroids.set(i, newCentroid);
                }
            }

            System.out.println("Current Centroids:");
            currentCentroids.forEach(c -> System.out.println(Arrays.toString(c.getFeatures())));

            if (converged) {
                System.out.println("\nK-Means converged after " + (iter + 1) + " iterations.");
                break;
            }
        }

        // Testing Assign test data points to clusters
        System.out.println("\n--- Test Data Cluster Assignments ---");
        Broadcast<List<DataPoint>> finalCentroidsBroadcast = jsc.broadcast(currentCentroids);
        testDataRDD.mapToPair(point -> {
            int finalClusterId = findClosestCentroid(point, finalCentroidsBroadcast.value());
            return new Tuple2<>(point, finalClusterId);
        }).collect().forEach(result -> {
            System.out.println("Test Point: " + Arrays.toString(result._1().getFeatures()) +
                    ", Assigned Cluster: " + result._2());
        });


        // Stop Spark Session
        sparkSession.stop();
        jsc.close();
    }
}