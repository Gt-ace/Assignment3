# Assignment 3 Report

## Team Members

Please list the members here
- Arthur Van Petegem
- Benjamin Hikaru Pfister

## Responses to questions posed in the assignment

_Note:_ Include the Spark execution history for each task. Name the zip file as `assignment-3-task-<x>-history.zip`.

### Task 1: Word counting

1. If you were given an additional requirement of excluding certain words (for example, conjunctions), at which step you would do this and why? (0.1 pt)
- At the mapping stage, since filtering early reduces the number of key-value pairs that have to enter the shuffle phase, which lowers overhead and reduces the amount of work in later stages. It is also better than filtering after the reduce operation since it reduces data movement across the network.


2. In Lecture 1, the potential of optimizing the mapping step through combined mapping and reduce was discussed. How would you use this in this task? (in your answer you can either provide a description or a pseudo code). Optional: Implement this optimization and observe the effect on performance (i.e., time taken for completion). (0.1 pt)
- Instead of emitting individual (word, 1) pairs for every word occurrence and then shuffling all of these pairs across the network to the reducers, we perform a local aggregation within each partition first. This means that each mapper maintains a local hash table or dictionary that accumulates word counts for all words processed within that partition. Only after processing all records in the partition does the mapper emit the aggregated counts as (word, count) pairs.
- For example, if the word "the" appears 100 times in a partition, instead of emitting 100 pairs of ("the", 1) that must be shuffled across the network, we emit a single pair ("the", 100). This  reduces the volume of data transferred during the shuffle phase.


3. In local execution mode (i.e. standalone mode), change the number of cores that is allocated by the master (.setMaster("local[<n>]") and measure the time it takes for the applicationto complete in each case. For each value of core allocation, run the experiment 5 times (to rule out large variances). Plot a graph showing the time taken for completion (with standard deviation) vs the number of cores allocated. Interpret and explain the results briefly in few sentences. (0.4 pt)
- The plot was added in the folder (task1.3.png). We tested with 1, 2, 4 and 8 cores with 5 runs each. 
Cores 1 -> 2 had the largest speed up due to a outlier in core 1, likely due to initialization overhead. 2->4 cores also improved the performance a bit. Going from 4 to 8 cores did not help, since the Laptop we ran it on only has 4 physical cores. 

4. Examine the execution history. Explain your observations regarding the planning of jobs, stages, and tasks. (0.4 pt)
- Jobs: Each action operation triggers one Spark job. Spark decomposed the task into 2 stages: ShuffleMapStage and ResultStage. The SuffleMapStage had 3 tasks, one per partition. Operations used were flatMap and mapToPair. Tasks ran in parallel on 2 executors. 
The ResultsStage also has 3 tasks, one per shuffle partition. reduceByKey and collect were used. Tasks ran in parallel, reading shuffle data. 
- Spark automatically splits the job at the shuffle. Stage 0 handles everything before the shuffle (map operations) and Stage 1 handles everything afterwards (reduce). Stage 1 starts after Stage 0 completes since it depends on the shuffled data. 
- All tasks ran in parallel since there were only 3 tasks per stage. 
- Stage 0 has much more overhead, since the shuffle write/read is the most expensive operation in this job. 


### Task 2

1. For each of the above computation, analyze the execution history and describe the key stages and tasks that were involved. In particular, identify where data shuffling occurred and explain why. (0.5pt)


2. You had to manually partition the data. Why was this essential? Which feature of the dataset did you use to partition and why?(0.5pt)


3. Optional: Notice that in the already provided pre-processing (in the class DatasetHelper), the long form of timeseries data, i.e., with a column _field that contained values like temperature etc., has been converted to wide form, i.e. individual column for each measurement kind through and operation called pivoting. Analyze the execution log and describe why this happens to be an expensive transformation.

### Task 3

1. Explain how the K-Means program you have implemented, specifically the centroid estimation and recalculation, is parallelized by Spark (0.5pt)


## Declarations (if any)
