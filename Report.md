# Assignment 3 Report

## Team Members

Please list the members here
- Arthur Van Petegem
- Benjamin Hikaru Pfister

## Responses to questions posed in the assignment

_Note:_ Include the Spark execution history for each task. Name the zip file as `assignment-3-task-<x>-history.zip`.
- We put the execution histories in the logs/ folder.

### Task 1: Word counting

1. If you were given an additional requirement of excluding certain words (for example, conjunctions), at which step you would do this and why? (0.1 pt)
- At the mapping stage, since filtering early reduces the number of key-value pairs that have to enter the shuffle phase, which lowers overhead and reduces the amount of work in later stages. It is also better than filtering after the reduce operation since it reduces data movement across the network.


2. In Lecture 1, the potential of optimizing the mapping step through combined mapping and reduce was discussed. How would you use this in this task? (in your answer you can either provide a description or a pseudo code). Optional: Implement this optimization and observe the effect on performance (i.e., time taken for completion). (0.1 pt)
- Instead of emitting individual (word, 1) pairs for every word occurrence and then shuffling all of these pairs across the network to the reducers, we perform a local aggregation within each partition first. This means that each mapper maintains a local hash table or dictionary that accumulates word counts for all words processed within that partition. Only after processing all records in the partition does the mapper emit the aggregated counts as (word, count) pairs.
- For example, if the word "the" appears 100 times in a partition, instead of emitting 100 pairs of ("the", 1) that must be shuffled across the network, we emit a single pair ("the", 100). This  reduces the volume of data transferred during the shuffle phase.


3. In local execution mode (i.e. standalone mode), change the number of cores that is allocated by the master (.setMaster("local[<n>]") and measure the time it takes for the applicationto complete in each case. For each value of core allocation, run the experiment 5 times (to rule out large variances). Plot a graph showing the time taken for completion (with standard deviation) vs the number of cores allocated. Interpret and explain the results briefly in few sentences. (0.4 pt)
- The plot was added in the folder (task1.3.png in the images folder). We tested with 1, 2, 4 and 8 cores with 5 runs each. 
Cores 1 -> 2 had the largest speed up due to a outlier in core 1, likely due to initialization overhead. 2->4 cores also improved the performance a bit. Going from 4 to 8 cores did not help, since the Laptop we ran it on only has 4 physical cores. 

4. Examine the execution history. Explain your observations regarding the planning of jobs, stages, and tasks. (0.4 pt)
- Jobs: Each action operation triggers one Spark job. Spark decomposed the task into 2 stages: ShuffleMapStage and ResultStage. The SuffleMapStage had 3 tasks, one per partition. Operations used were flatMap and mapToPair. Tasks ran in parallel on 2 executors. 
The ResultsStage also has 3 tasks, one per shuffle partition. reduceByKey and collect were used. Tasks ran in parallel, reading shuffle data. 
- Spark automatically splits the job at the shuffle. Stage 0 handles everything before the shuffle (map operations) and Stage 1 handles everything afterwards (reduce). Stage 1 starts after Stage 0 completes since it depends on the shuffled data. 
- All tasks ran in parallel since there were only 3 tasks per stage. 
- Stage 0 has much more overhead, since the shuffle write/read is the most expensive operation in this job. 


### Task 2

1. For each of the above computation, analyze the execution history and describe the key stages and tasks that were involved. In particular, identify where data shuffling occurred and explain why. (0.5pt)
- Monthly CO2 Deltas:
Key Stages: 
  - Data Loading & Pivoting (Jobs 0-3): Reading CSV with 6 parallel tasks, converted data to wide format (one column per measurement type). Shuffle happened here because Spark needed to group all measurements for each timestamp together
  - Calculate Hourly Averages (Jobs 4-9): Grouped data by month and hour, then calculated average CO2 for each combination. Shuffle happened here aswell since Spark needs all records with the same month and hour together on the same computer
  - Calculate hourly change (stage 17-28): For each month compared each hour's CO2 to previous hour to find changes. Shuffle happened here too since Spark needs all hours for each month on the same computer and in the correct order
  - Find maximum changes (Stage 35): Find biggest increase and decrease for each month, data organized so minimal shuffling here

- Correlations
Key Stages:
  - Read data: Load dataset with 6 tasks, shuffle to prepare for calculating correlations
  - Calculate statistics: Computed averages and variations, shuffle happened to combine results from all computers
  - Correlations: Combined all statistics into one correlation number (just 1 task)

2. You had to manually partition the data. Why was this essential? Which feature of the dataset did you use to partition and why?(0.5pt)
- We added the repartition(col('month')) to organize data by month before doing calculations. This is needed to ensure correctness, since the lag function needs to look at the previous hour for each row, so if January's hours are distributed across different computers, Spark can't really find the previous hour. Without manual partitioning, we'd get wrong results or missing values.
- Performance: By partitioning by month upfront, shuffling is only done once instead of multiple times. This also creates 12 balanced groups that can be processed in parallel, which is much faster than letting Spark figure out organization during the calculation.
- We chose the month for partitioning because it matches the task to find changes within the months. It also matches the window function, which minimizes data movement. Also, in each month there is roughly equal amounts of data, so partitioning by month is also quite balanced.

### Task 3

1. Explain how the K-Means program you have implemented, specifically the centroid estimation and recalculation, is parallelized by Spark (0.5pt)
- The K-Means algorithm is parallelized in our implementations through spark's RDD operations in 2 main steps:
- 1. Assignment Step: Current centroids are broadcast to all worker nodes, then each worker processes its paritionpartition of data points in parallel. Every point calculates distances to all centroids to find the closest one, and then returns pairs of clusterid and datapoint. Since all points are independent this can be run in parallel. 
- 2. Recalculation Step: groupByKey shuffles data so points with the same clusterid end up together. New centroid for each cluster is computer in parallel, and each task independently calculates the mean position of all points in its cluster. Finally new centroids are collected back to the driver for convergence checking. 
- Optimizations here include broadcasting centroids and filtering early. The parallelization distributes data and computation across the cluster, making it a good algorithm for larger datasets.

### Declarations
Claude AI was used to debug the setup: There was a lot of problems with the containers, Claude AI helped with that by providing the terminal commands to debug why workers were exiting early or not starting. 