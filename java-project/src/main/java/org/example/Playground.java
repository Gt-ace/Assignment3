package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.apache.spark.sql.functions.*;

public class Playground {


    private static final Logger LOGGER = LoggerFactory.getLogger(Playground.class);

    public static void run(boolean local) throws IOException {

        LOGGER.info("Starting the task");
        playWithSQL();
    }

    public static void playWithMapReduce(){
        SparkConf sparkConf = null;

        String datasetFileName = "playground.txt";
        String datasetFilePath ="../datasets/" + datasetFileName;
        String applicationName = "Playground";

        Date t0 = new Date(); //Mark the start timestamp

        sparkConf = new SparkConf().setAppName(applicationName).setMaster("local[14]")
                .set("spark.executor.instances", "1")
                .set("spark.executor.cores", "2")
                .set("spark.cores.max", "14")
                .set("spark.driver.cores", "1")
                .set("spark.executor.memory", "4g");

        JavaSparkContext sparkContext =  new JavaSparkContext(sparkConf);
        LOGGER.info("Loading text file");
        JavaRDD<String> textFile = sparkContext.textFile(datasetFilePath, 3);

        //------------------ The textFile is not yet there..you need to collect it ------------
        List<String> allLines = textFile.collect();
        for (String line : allLines) {
            System.out.println("Line: " + line);
            // Perform operations on each line here
            // But then this is not going to be distributed!
        }

        //------------------ Lets do some distribution: find the count of words of each length ------------

        JavaRDD<Tuple2<Integer, Integer>> wordLengthsAndCounts = textFile
                .flatMap(line -> Arrays.asList(line.split(" ")).iterator()) // Split lines into words
                .filter(word -> !word.isEmpty()) // Filter out empty strings
                .map(word -> new Tuple2<>(word.length(), 1)); // (length, count=1) for each word

        // Perform an action to aggregate the results
        // reduce: Combines all elements of the RDD using a commutative and associative function.
        // Here, we sum up all the word lengths and all the word counts.
        Tuple2<Integer, Integer> totalLengthAndCount = wordLengthsAndCounts.reduce((acc, value) ->
                new Tuple2<>(acc._1() + value._1(), acc._2() + value._2())
        );


        int totalWordLength = totalLengthAndCount._1();
        int totalWordCount = totalLengthAndCount._2();

        double averageWordLength = 0.0;
        if (totalWordCount > 0) {
            averageWordLength = (double) totalWordLength / totalWordCount;
            LOGGER.info("Calculated average word length.");
        } else {
            LOGGER.warn("No words found in the file. Average word length is 0.");
        }

        //Print the result
        System.out.println("--------------------------------------------------");
        System.out.println("Total words processed: " + totalWordCount);
        System.out.println("Total combined length of all words: " + totalWordLength);
        System.out.printf("Average word length: %.2f%n", averageWordLength);
        System.out.println("--------------------------------------------------");

        //------------------------ How about word count? :-) ------------------------

        //JavaPairRDD<String, Integer> wordCounts = textFile;
                 // Split lines into words - this is called flat mapping
                 // Map each word to (word, 1)
                //  finally reduce the sum counts for each word

        //Ok, can we ask the worker to already reduce its part?

        //wordCounts.collect().forEach(pair -> System.out.println(pair._1() + ": " + pair._2()));

        //-----------------------------------------------------------------------------

        sparkContext.stop();
        sparkContext.close();
    }

    public static void playWithRDD(){
        SparkConf sparkConf = null;

        String datasetFileName = "playground.txt";
        String datasetFilePath ="../datasets/" + datasetFileName;
        String applicationName = "Playground";

        Date t0 = new Date(); //Mark the start timestamp

        sparkConf = new SparkConf().setAppName(applicationName).setMaster("local[14]")
                .set("spark.executor.instances", "1")
                .set("spark.executor.cores", "2")
                .set("spark.cores.max", "14")
                .set("spark.driver.cores", "1")
                .set("spark.executor.memory", "4g");

        JavaSparkContext sparkContext =  new JavaSparkContext(sparkConf);
        LOGGER.info("Loading text file");
        JavaRDD<String> textFile = sparkContext.textFile(datasetFilePath, 3);

        long numLines = textFile.count();
        LOGGER.info("Number of lines in the file: {}", numLines);

        JavaRDD<String> linesWithSpark = textFile.filter(line -> line.contains("Spark"));
        System.out.printf("Lines containing 'Spark': %d\n", linesWithSpark.count());

        JavaRDD<Integer> lineLengths = textFile.map(line -> line.length());
        LOGGER.info("Total characters in the file: {}", lineLengths.reduce((a, b) -> a + b));

        sparkContext.stop();
        sparkContext.close();
    }

    public static void playWithDatasets(){
        String datasetFileName = "playground.txt";
        String datasetFilePath ="../datasets/" + datasetFileName;
        String applicationName = "Playground";
        //-----------------------Datasets----------------------------------------------

        //To work with datasets, you need spark session.
        SparkSession spark = SparkSession.builder()
                .appName("TextFileDatasetProcessing")
                .master("local[*]")
                .getOrCreate();


        Dataset<Row> textDataset = spark.read().text(datasetFilePath);
        LOGGER.info("Text file loaded from: {}", datasetFilePath);

        // 3. Basic Processing and Inspection

        // Show the schema of the Dataset
        LOGGER.info("Schema of the loaded text file:");
        textDataset.printSchema();
        // Expected output:
        // root
        //  |-- value: string (nullable = true)

        // Show the first 20 rows of the Dataset
        LOGGER.info("First few lines of the text file:");
        textDataset.show();

        // Count the total number of lines in the file
        long lineCount = textDataset.count();
        LOGGER.info("Total number of lines in the file: {}", lineCount);

        // Filter lines that contain a specific word (case-insensitive)
        String searchWord = "spark";
        Dataset<Row> linesWithSparkDataset = textDataset.filter(
                lower(textDataset.col("value")).contains(searchWord)
        );
        LOGGER.info("Lines containing '{}' (case-insensitive):", searchWord);
        linesWithSparkDataset.show();
        LOGGER.info("Number of lines containing '{}': {}", searchWord, linesWithSparkDataset.count());

        // Add a new column representing the length of each line
        Dataset<Row> linesWithLength = textDataset.withColumn(
                "lineLength", length(textDataset.col("value"))
        );
        LOGGER.info("Lines with an added 'lineLength' column:");
        linesWithLength.show();
        linesWithLength.printSchema();

        // Find the average line length
        double averageLineLength = linesWithLength.select(avg("lineLength")).head().getDouble(0);
        LOGGER.info("Average line length: {}", String.format("%.2f", averageLineLength));

        // Select only the 'value' column and convert it to a Dataset<String>
        // This is useful if you want to apply RDD-like transformations
        Dataset<String> stringDataset = textDataset.as(Encoders.STRING());
        LOGGER.info("First few lines as a Dataset<String>:");
        stringDataset.show();

        // Example of a transformation on Dataset<String> (similar to RDD map)
        Dataset<Integer> lineLengthsDataset = stringDataset.map(
                (MapFunction<String, Integer>) line -> line.length(),
                Encoders.INT()
        );
        LOGGER.info("Line lengths as a Dataset<Integer>:");
        lineLengthsDataset.show();
        LOGGER.info("Count of line lengths: {}", lineLengthsDataset.count());
    }

    public static class Person implements Serializable {
        private String name;
        private int age;
        private String city;

        // Default constructor is required for Spark's encoder/decoder
        public Person() {
        }

        public Person(String name, int age, String city) {
            this.name = name;
            this.age = age;
            this.city = city;
        }

        // Getters and setters (required for Spark to access fields)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", city='" + city + '\'' +
                    '}';
        }
    }

    public static void playWithSQL(){
        String sparkMaster = "spark://spark-master:7077"; // "local[*]"
        //To work with datasets, you need spark session.
        SparkConf sparkConf = new SparkConf().setAppName("SQLTest")
                .set("spark.testing.memory", "2147480000");

        SparkSession spark = SparkSession.builder()
                .appName("TextFileDatasetProcessing")
                .master(sparkMaster)
                .config(sparkConf)
                .getOrCreate();

        //---------------------------------------------------------------------

        List<Person> peopleList = Arrays.asList(
                new Person("Alice", 30, "New York"),
                new Person("Bob", 25, "London"),
                new Person("Charlie", 35, "New York"),
                new Person("David", 40, "Paris"),
                new Person("Eve", 28, "London")
        );

        // 3. Convert the List to a Dataset<Person>
        // Encoders.bean(Person.class) is used to create a typed Dataset from a Java Bean.
        Dataset<Person> peopleDataset = spark.createDataset(peopleList, Encoders.bean(Person.class));
        LOGGER.info("Dataset<Person> created:");
        peopleDataset.show();
        peopleDataset.printSchema();

        // 4. Register the Dataset as a temporary SQL view
        // This makes the data accessible via SQL queries using the name "people_view".
        peopleDataset.createOrReplaceTempView("people_view");
        LOGGER.info("Dataset registered as a temporary view 'people_view'.");

        // 5. Execute SQL queries using spark.sql()

        // Query 1: Select all people older than 30
        LOGGER.info("\n--- SQL Query 1: People older than 30 ---");
        Dataset<Row> oldPeople = spark.sql("SELECT name, age, city FROM people_view WHERE age > 30");
        oldPeople.explain(true);
        oldPeople.show();
        oldPeople.printSchema();

        // Query 2: Count people by city
        LOGGER.info("\n--- SQL Query 2: Count people by city ---");
        Dataset<Row> peopleByCity = spark.sql("SELECT city, COUNT(*) as count FROM people_view GROUP BY city ORDER BY count DESC");
        //peopleByCity.explain();
        peopleByCity.show();
        peopleByCity.printSchema();

        // Query 3: Calculate the average age
        LOGGER.info("\n--- SQL Query 3: Average age ---");
        Dataset<Row> averageAge = spark.sql("SELECT AVG(age) as average_age FROM people_view");
        averageAge.show();

        // You can also mix Dataset API with SQL results
        LOGGER.info("\n--- Further processing on SQL results (e.g., filtering on oldPeople) ---");
        oldPeople.filter(oldPeople.col("city").equalTo("New York")).show();
    }

}