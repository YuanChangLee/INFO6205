package com.phasmidsoftware.dsaipg.sort.par;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * ExperimentRunner is a utility class for running parallel sorting experiments.
 * It provides methods for experimenting with different cutoff values, recursion depths,
 * and combinations of both parameters.
 */
public class ExperimentReport {

    private static final int DEFAULT_ARRAY_SIZE = 2000000;
    private static final int DEFAULT_RANDOM_BOUND = 10000000;
    private static final int DEFAULT_ITERATIONS = 5;

    private Random random = new Random();
    private int arraySize = DEFAULT_ARRAY_SIZE;
    private int randomBound = DEFAULT_RANDOM_BOUND;
    private int iterations = DEFAULT_ITERATIONS;
    private String outputDir = "./";

    /**
     * Constructor with custom array size
     * 
     * @param arraySize Size of arrays to sort
     */
    public ExperimentReport(int arraySize) {
        this.arraySize = arraySize;
    }

    /**
     * Default constructor
     */
    public ExperimentReport() {
    }

    /**
     * Set the number of iterations for each experiment
     * 
     * @param iterations Number of iterations
     * @return this ExperimentRunner
     */
    public ExperimentReport setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    /**
     * Set the directory for output files
     * 
     * @param outputDir Directory for output files
     * @return this ExperimentRunner
     */
    public ExperimentReport setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    /**
     * Set the maximum value for random array elements
     * 
     * @param randomBound Maximum value for random array elements
     * @return this ExperimentRunner
     */
    public ExperimentReport setRandomBound(int randomBound) {
        this.randomBound = randomBound;
        return this;
    }

    /**
     * Main method to run all experiments
     */
    public void runAllExperiments() {
        System.out.println("Running all experiments with array size: " + arraySize);
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("ForkJoinPool parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        
        // Original experiments
        runCutoffExperiment();
        runRecursionDepthExperiment();
        runThreadCountExperiment();
        runCombinedExperiment();
        
        // New experiment with array sizes and cutoffs
        runArrayCutoffExperiment();
    }

    /**
     * Run experiment with various cutoff values
     */
    public void runCutoffExperiment() {
        System.out.println("\n=== CUTOFF EXPERIMENT ===");
        // Reset max depth to ensure we're only testing cutoff effects
        ParSort.maxDepth = 10; // High value to ensure it doesn't limit parallelism
        
        Map<Integer, Double> results = new HashMap<>();
        
        // Test a range of cutoff values
        int[] cutoffValues = {
            1000, 5000, 10000, 50000, 100000, 500000, 
            1000000, 1500000, 2000000, 2500000
        };
        
        for (int cutoff : cutoffValues) {
            ParSort.cutoff = cutoff;
            double avgTime = runSortingTest();
            results.put(cutoff, avgTime);
            System.out.println(String.format("Cutoff: %d, Average time: %.2f ms", 
                                            cutoff, avgTime));
        }
        
        writeResultsToFile("cutoff_results.csv", results);
    }

    /**
     * Run experiment with various recursion depths
     */
    public void runRecursionDepthExperiment() {
        System.out.println("\n=== RECURSION DEPTH EXPERIMENT ===");
        // Set cutoff to a reasonable value
        ParSort.cutoff = 10000;
        
        Map<Integer, Double> results = new HashMap<>();
        
        // Test different recursion depths (0 to 6, representing 1 to 64 partitions)
        for (int depth = 0; depth <= 6; depth++) {
            ParSort.maxDepth = depth;
            double avgTime = runSortingTest();
            results.put(depth, avgTime);
            int partitions = (int) Math.pow(2, depth);
            System.out.println(String.format("Max depth: %d (partitions: %d), Average time: %.2f ms", 
                                            depth, partitions, avgTime));
        }
        
        writeResultsToFile("depth_results.csv", results);
    }

    /**
     * Run experiment with various thread counts
     */
    public void runThreadCountExperiment() {
        System.out.println("\n=== THREAD COUNT EXPERIMENT ===");
        // Set cutoff to a reasonable value
        ParSort.cutoff = 10000;
        
        Map<Integer, Double> results = new HashMap<>();
        
        // Test different thread counts (powers of 2)
        int[] threadCounts = {1, 2, 4, 8, 16, 32};
        
        for (int threadCount : threadCounts) {
            // Set system property for ForkJoinPool parallelism
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", 
                              String.valueOf(threadCount));
            
            // Calculate max depth based on thread count
            ParSort.setMaxDepthFromThreadCount(threadCount);
            
            double avgTime = runSortingTest();
            results.put(threadCount, avgTime);
            System.out.println(String.format("Thread count: %d (max depth: %d), Average time: %.2f ms", 
                                            threadCount, ParSort.maxDepth, avgTime));
        }
        
        // Reset to default parallelism
        System.clearProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
        
        writeResultsToFile("thread_results.csv", results);
    }

    /**
     * Run experiment with combinations of cutoff and max depth values
     */
    public void runCombinedExperiment() {
        System.out.println("\n=== COMBINED EXPERIMENT ===");
        
        // Create map to store results with combined key
        Map<String, Double> results = new HashMap<>();
        
        // Test combinations of cutoff and max depth
        int[] cutoffValues = {10000, 100000, 500000, 1000000};
        int[] depthValues = {1, 2, 3, 4};
        
        for (int cutoff : cutoffValues) {
            for (int depth : depthValues) {
                ParSort.cutoff = cutoff;
                ParSort.maxDepth = depth;
                
                double avgTime = runSortingTest();
                String key = cutoff + "," + depth;
                results.put(key, avgTime);
                
                System.out.println(String.format("Cutoff: %d, Max depth: %d, Average time: %.2f ms", 
                                                cutoff, depth, avgTime));
            }
        }
        
        writeResultsToFile("combined_results.csv", results);
    }
    
    /**
     * Run experiment with different array sizes and different cutoff values
     * This experiment aims to find the optimal cutoff value for each array size
     */
    public void runArrayCutoffExperiment() {
        System.out.println("\n=== ARRAY SIZE AND CUTOFF EXPERIMENT ===");
        
        // Reset max depth to ensure we're only testing cutoff effects
        ParSort.maxDepth = 10; // High value to ensure it doesn't limit parallelism
        
        // Store all results in a map with "arraySize,cutoff" as key
        Map<String, Double> results = new HashMap<>();
        
        // Test different array sizes
        int[] arraySizes = {
            100000, 250000, 500000, 1000000, 2000000, 5000000, 10000000
        };
        
        // Test different cutoff values for each array size
        int[] cutoffValues = {
            1000, 5000, 10000, 50000, 100000, 500000, 1000000
        };
        
        int originalSize = this.arraySize;
        
        for (int size : arraySizes) {
            System.out.println("Testing array size: " + size);
            this.arraySize = size;
            
            Map<Integer, Double> bestCutoffs = new HashMap<>();
            
            for (int cutoff : cutoffValues) {
                // Skip cutoffs larger than the array size
                if (cutoff > size) continue;
                
                ParSort.cutoff = cutoff;
                double avgTime = runSortingTest();
                
                String key = size + "," + cutoff;
                results.put(key, avgTime);
                bestCutoffs.put(cutoff, avgTime);
                
                System.out.println(String.format("  Cutoff: %d, Average time: %.2f ms", 
                                               cutoff, avgTime));
            }
            
            // Find the best cutoff for this array size
            int bestCutoff = Collections.min(bestCutoffs.entrySet(), Map.Entry.comparingByValue()).getKey();
            double bestTime = bestCutoffs.get(bestCutoff);
            System.out.println(String.format("  Best cutoff for size %d: %d (%.2f ms)", 
                                          size, bestCutoff, bestTime));
            
            // Add a special entry for the best cutoff
            results.put(size + ",best", (double)bestCutoff);
        }
        
        // Restore original array size
        this.arraySize = originalSize;
        
        writeResultsToFile("array_cutoff_results.csv", results);
        
        // Also create a separate file with only the best cutoffs
        Map<Integer, Double> bestCutoffs = new HashMap<>();
        for (int size : arraySizes) {
            String key = size + ",best";
            if (results.containsKey(key)) {
                bestCutoffs.put(size, results.get(key));
            }
        }
        writeResultsToFile("best_cutoffs.csv", bestCutoffs);
    }

    /**
     * Run a sorting test with current parameters
     * 
     * @return Average sorting time in milliseconds
     */
    private double runSortingTest() {
        int[] array = new int[arraySize];
        long totalTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            // Generate random array
            for (int j = 0; j < array.length; j++) {
                array[j] = random.nextInt(randomBound);
            }
            
            // Clone array to ensure fair comparison (don't reuse sorted array)
            int[] testArray = array.clone();
            
            // Time the sorting
            long startTime = System.currentTimeMillis();
            ParSort.sort(testArray, 0, testArray.length);
            long endTime = System.currentTimeMillis();
            
            // Verify array is sorted
            boolean sorted = isSorted(testArray);
            if (!sorted) {
                System.err.println("ERROR: Array not properly sorted!");
            }
            
            totalTime += (endTime - startTime);
        }
        
        return (double) totalTime / iterations;
    }

    /**
     * Check if array is sorted
     * 
     * @param array Array to check
     * @return true if array is sorted, false otherwise
     */
    private boolean isSorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Write experiment results to a CSV file
     * 
     * @param filename Output filename
     * @param results Map of parameter values to sorting times
     */
    private <T> void writeResultsToFile(String filename, Map<T, Double> results) {
        try {
            String filePath = outputDir + filename;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath)));
            
            // Write header
            writer.write("parameter,time_ms\n");
            
            // Write data
            for (Map.Entry<T, Double> entry : results.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            
            writer.close();
            System.out.println("Results written to " + filePath);
            
        } catch (IOException e) {
            System.err.println("Error writing results to file: " + e.getMessage());
        }
    }

    /**
     * Main method to run the experiments
     */
    public static void main(String[] args) {
        // Process command line arguments
        int arraySize = DEFAULT_ARRAY_SIZE;
        int iterations = DEFAULT_ITERATIONS;
        String outputDir = "./src/";
        boolean runArrayCutoffOnly = false;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--size") && i+1 < args.length) {
                arraySize = Integer.parseInt(args[i+1]);
                i++;
            } else if (args[i].equals("--iterations") && i+1 < args.length) {
                iterations = Integer.parseInt(args[i+1]);
                i++;
            } else if (args[i].equals("--output") && i+1 < args.length) {
                outputDir = args[i+1];
                i++;
            } else if (args[i].equals("--array-cutoff-only")) {
                runArrayCutoffOnly = true;
            }
        }
        
        // Create experiment runner
        ExperimentReport runner = new ExperimentReport(arraySize)
            .setIterations(iterations)
            .setOutputDir(outputDir);
        
        if (runArrayCutoffOnly) {
            // Run only the array-cutoff experiment
            runner.runArrayCutoffExperiment();
        } else {
            // Run all experiments
            runner.runAllExperiments();
        }
    }
}