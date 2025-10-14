# Assignment 3 Report

## Team Members

Please list the members here

## Responses to questions posed in the assignment

_Note:_ Include the Spark execution history for each task. Name the zip file as `assignment-3-task-<x>-history.zip`.

### Task 1

- In local execution mode, change the number of cores that is allocated by the master (`.set-Master("local[<n>]"`) and measure the time it takes for the application to complete in each ase. For each value of core allocation, run the experiment 5 times (to rule out large variances). Plot a chart showing the time taken for completion (with standard deviation) vs the number of cores allocated. Interpret and explain the results briefly in few sentences. (0.5 pt)

- Examine the execution history. Explain your observations regarding the planning of jobs, stages, and tasks. (0.4 pt)

- If you were given an additional requirement of excluding certain words (for example, conjunctions), at which step you would do this and why?(0.1 pt)

### Task 2

- Analyze the execution history and describe the key stages and tasks that were involved. In particular, identify where data shuffling occurred and explain why. (0.5pt)

- Notice that in the already provided pre-processing, the long form of timeseries data has been converted to wide form through pivoting.  Analyze the execution log and describe why this happens to be an expensive transformation. (0.5pt)

### Task 3

-Explain how the K-Means program you have implemented, in specific the centroid estimation and recalculation, is parallelized by Spark (0.5pt)

## Declarations (if any)
