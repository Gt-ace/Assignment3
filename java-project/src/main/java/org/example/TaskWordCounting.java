package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

public class TaskWordCounting {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskWordCounting.class);

    static class WordMapper implements FlatMapFunction<String, String>
    {
        @Override
        public Iterator<String> call(String s) {
            //You need remove punctuations from the string, convert to lower case
            //and then split it by whitespace. Then put all strings that are some
            //sequence of alphanumeric characters to list and return the list iterator

            return null; //Of course, you dont return null, but the iterator.
        }

    }

    public static void run(boolean local) throws IOException {

        LOGGER.info("Starting the task");

        SparkConf sparkConf = null;

        String datasetFileName = "dataset-wordcount.txt";
        String datasetFilePath ="../datasets/" + datasetFileName;
        String applicationName = "WordCount";
        String hdfsDatasetPath = "hdfs://namenode:9000/datasets/";
        String sparkMaster = "spark://spark-master:7077";

        Date t0 = new Date(); //Mark the start timestamp

        if(local){
            sparkConf = new SparkConf().setAppName(applicationName).setMaster("local[*]").set("spark.executor.instances", "1").set("spark.executor.instances", "10") .set("spark.executor.memory", "4g");
        }else {
            datasetFilePath = hdfsDatasetPath + datasetFileName;
            sparkConf = new SparkConf().setAppName(applicationName).setMaster(sparkMaster);
        }

        JavaSparkContext sparkContext =  new JavaSparkContext(sparkConf);
        //sparkContext.setLogLevel("WARN");
        LOGGER.info("Loading text file");
        JavaRDD<String> textFile = sparkContext.textFile(datasetFilePath, 3);

        //=================================== Your code now =========================================

        //Step-A: using the available textFile, create a flat map of words by calling the WordMapper.
        LOGGER.info("Flat mapping to create word list");


        //-------------------------------------------------------------------------------------------
        Date t1 = new Date();
        LOGGER.info("Word list created in {}ms", t1.getTime()-t0.getTime());


        //Step B: Now invoke a mapping function that will create key value-pair for each word in the list
        LOGGER.info("Mapping function");


        //-------------------------------------------------------------------------------------------
        Date t2 = new Date();
        LOGGER.info("Mapping invoked in {}ms", t2.getTime()-t1.getTime());

        //Step C: Invoke a Reduce function that will sum up the values (against each key)
        LOGGER.info("Reducing function");


        //-------------------------------------------------------------------------------------------
        Date t3 = new Date();
        LOGGER.info("Reduce invoked in {}ms", t3.getTime()-t2.getTime());

        //Step D: Finally, output the counts for each word
        LOGGER.info("Collecting to driver");


        //-------------------------------------------------------------------------------------------
        Date t4 = new Date();
        LOGGER.info("Application completed in {}ms", t4.getTime()-t0.getTime());

        //If you want you can save the counts to a hdfs file
        if(!local) {
            //counts.repartition(1).saveAsTextFile("hdfs://namenode:9000/output/counts.txt");
        }
        sparkContext.stop();
        sparkContext.close();
    }
}