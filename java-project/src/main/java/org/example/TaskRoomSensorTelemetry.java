package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.apache.spark.sql.functions.*;

public class TaskRoomSensorTelemetry {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRoomSensorTelemetry.class);
    public static void run(boolean local){
        SparkSession sparkSession = null;

        try {
            LOGGER.info("Starting CO2 Pattern Analysis.");

            String sparkApplicationName = "RoomSensorTelemetry";
            String datasetFileName = "dataset-room-sensors.csv";
            SparkConf sparkConf = null;
            String sparkMasterUrl = "spark://spark-master:7077";

            if(local){
                sparkConf = new SparkConf().setAppName(sparkApplicationName).setMaster("local[*]");
            }else {
                sparkConf = new SparkConf().setAppName(sparkApplicationName).setMaster(sparkMasterUrl);
            }

            // Initialize SparkSession
            sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
            LOGGER.info("SparkSession initialized. SparkContext log level set to WARN.");

            // Retrieve data and form the dataframe
            Dataset<Row> df = DatasetHelper.getDataset(sparkSession, datasetFileName, local);

            //=================================== Your code now =========================================

            //-------------------------------------------------------------------------------------------
            // Step A: Calculate Average CO2 per hour grouped by month
            // Order by month and then hour to ensure correct sequence for window function within each month

            LOGGER.info("Step A: Calculating hourly average CO2 grouped by month");
            Dataset<Row> AvgCo2 = df.groupBy("month", "hour").agg(avg("co2").alias("avg_co2"))
                .orderBy("month", "hour");

            // Partition data by month for efficient processing within each month
            AvgCo2 = AvgCo2.repartition(col("month"));

            //-------------------------------------------------------------------------------------------
            // Step B: Calculate the difference between consecutive hourly averages within each month
            // The window function is now partitioned by 'month'. This means 'lag' will
            // only look at previous rows within the same month partition.

            WindowSpec windowSpec = Window.partitionBy("month").orderBy("hour");

            LOGGER.info("Step B: Calculating CO2 differences between consecutive hours");
            Dataset<Row> co2Differences = AvgCo2
                .withColumn("prev_avg_co2", lag("avg_co2", 1).over(windowSpec))
                .withColumn("co2_change", col("avg_co2").minus(col("prev_avg_co2")))
                .filter(col("co2_change").isNotNull()); // Remove first row of each month (no previous value)

            //-------------------------------------------------------------------------------------------
            // Step C: Find the maximum increase and maximum decrease for *each month*
            // We group by 'month' and then aggregate to find the max/min changes.
            // The 'when' clause ensures we only consider positive changes for max increase
            // and negative changes for max decrease. If a month has no increases/decreases,
            // the corresponding result will be null.

            LOGGER.info("Step C: Finding max increase and decrease per month");
            Dataset<Row> monthWiseResults = co2Differences
                .groupBy("month")
                .agg(
                    max(when(col("co2_change").gt(0), col("co2_change")).otherwise(null))
                        .alias("max_increase_ppm_per_hour"),
                    min(when(col("co2_change").lt(0), col("co2_change")).otherwise(null))
                        .alias("max_decrease_ppm_per_hour")
                )
                .orderBy("month");

            System.out.println("\n=== Month-wise Maximum CO2 Increase and Decrease (ppm/hour) ===");
            monthWiseResults.show(12);

            //-------------------------------------------------------------------------------------------
            // Step D: find the correlation between month and CO2 (Hint: this is a one-liner :)

            LOGGER.info("Step D: Calculating correlations with temporal factors");
            double monthCorrelation = df.stat().corr("month", "co2");
            System.out.printf("Correlation between month of year and CO2: %.4f%n", monthCorrelation);

            // Similarly, between hour and CO2
            double hourCorrelation = df.stat().corr("hour", "co2");
            System.out.printf("Correlation between hour of day and CO2: %.4f%n", hourCorrelation);

            // And, between weekday and CO2
            double weekdayCorrelation = df.stat().corr("weekday", "co2");
            System.out.printf("Correlation between day of week and CO2: %.4f%n%n", weekdayCorrelation);
            //-------------------------------------------------------------------------------------------

            // Analysis: Hour of day shows the strongest correlation with CO2 levels,
            // suggesting human activity patterns during the day significantly affect indoor CO2.
            // Month and weekday show weaker correlations.

            LOGGER.info("Analysis completed successfully.");

        } catch (Exception e) {
            LOGGER.error("An error occurred during Spark application execution: " + e.getMessage(), e);
        } finally {
            if (sparkSession != null) {
                sparkSession.stop();
                LOGGER.info("SparkSession stopped.");
            }
        }
    }
}
