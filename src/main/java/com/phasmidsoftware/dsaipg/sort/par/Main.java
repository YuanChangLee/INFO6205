package com.phasmidsoftware.dsaipg.sort.par;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * This code has been fleshed out by Ziyao Qiao. Thanks very much.
 * CONSIDER tidy it up a bit.
 */
public class Main {

    /**
     * The main method serves as the entry point for the program. It processes command-line arguments,
     * configures sorting parameters, performs parallel sorting on a random array, measures execution time,
     * and writes the performance results to a CSV file.
     *
     * @param args command-line arguments used for configuring program execution.
     */
    public static void main(String[] args) {
        processArgs(args);
        System.out.println("Degree of parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        System.out.println("Max recursion depth: " + ParSort.maxDepth);
        
        Random random = new Random();
        int[] array = new int[2000000];
        Collection<Long> timeList = new ArrayList<>();
        
        // Default experiment varies cutoff
        runCutoffExperiment(random, array, timeList);
        
        try {
            FileOutputStream fis = new FileOutputStream("./src/result.csv");
            OutputStreamWriter isr = new OutputStreamWriter(fis);
            BufferedWriter bw = new BufferedWriter(isr);
            int j = 0;
            for (long i : timeList) {
                String content = (double) 10000 * (j + 51) / 2000000 + "," + (double) i / 10 + "\n";
                j++;
                bw.write(content);
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Runs experiments varying the cutoff value.
     * 
     * @param random Random number generator
     * @param array Array to sort
     * @param timeList Collection to store time measurements
     */
    private static void runCutoffExperiment(Random random, int[] array, Collection<Long> timeList) {
        System.out.println("Running cutoff experiment...");
        for (int j = 50; j < 100; j++) {
            ParSort.cutoff = 10000 * (j + 1);
            long time;
            long startTime = System.currentTimeMillis();
            for (int t = 0; t < 10; t++) {
                for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
                ParSort.sort(array, 0, array.length);
            }
            long endTime = System.currentTimeMillis();
            time = (endTime - startTime);
            timeList.add(time);

            System.out.println("cutoff：" + (ParSort.cutoff) + "\t\t10times Time:" + time + "ms");
        }
    }
    
    /**
     * Runs experiments varying the max recursion depth (thread partitioning).
     * 
     * @param random Random number generator
     * @param array Array to sort
     * @param timeList Collection to store time measurements
     */
    private static void runDepthExperiment(Random random, int[] array, Collection<Long> timeList) {
        System.out.println("Running recursion depth experiment...");
        // Use optimal cutoff found in previous experiment
        ParSort.cutoff = 1000; // Set to optimal value from cutoff experiment
        
        for (int depth = 0; depth <= 6; depth++) { // Test depths 0-6 (1-64 partitions)
            ParSort.maxDepth = depth;
            long time;
            long startTime = System.currentTimeMillis();
            for (int t = 0; t < 10; t++) {
                for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
                ParSort.sort(array, 0, array.length);
            }
            long endTime = System.currentTimeMillis();
            time = (endTime - startTime);
            timeList.add(time);

            System.out.println("maxDepth：" + (ParSort.maxDepth) + " (partitions: " + 
                               (1 << ParSort.maxDepth) + ")\t\t10times Time:" + time + "ms");
        }
    }
    
    /**
     * Runs experiments with combined approach (varying both cutoff and recursion depth).
     * 
     * @param random Random number generator
     * @param array Array to sort
     * @param timeList Collection to store time measurements
     */
    private static void runCombinedExperiment(Random random, int[] array, Collection<Long> timeList) {
        System.out.println("Running combined cutoff and depth experiment...");
        
        // Test combinations of cutoff and depth
        int[] cutoffs = {500000, 1000000, 1500000, 2000000};
        int[] depths = {1, 2, 3, 4};
        
        for (int cutoff : cutoffs) {
            for (int depth : depths) {
                ParSort.cutoff = cutoff;
                ParSort.maxDepth = depth;
                
                long time;
                long startTime = System.currentTimeMillis();
                for (int t = 0; t < 10; t++) {
                    for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
                    ParSort.sort(array, 0, array.length);
                }
                long endTime = System.currentTimeMillis();
                time = (endTime - startTime);
                timeList.add(time);

                System.out.println("cutoff：" + cutoff + ", maxDepth: " + depth + 
                                   " (partitions: " + (1 << depth) + ")\t\t10times Time:" + time + "ms");
            }
        }
    }

    /**
     * Processes the command-line arguments by iterating through the provided array of arguments.
     * Each argument is checked for specific prefixes (e.g., "-" symbols), and arguments with such prefixes
     * are further handled using {@link #processArg(String[])}. The method continuously modifies the arguments array
     * by removing processed elements.
     *
     * @param args an array of strings representing command-line arguments to be processed.
     *             Each argument can include options, flags, or parameters that configure the program's behavior.
     */
    private static void processArgs(String[] args) {
        String[] xs = args;
        while (xs.length > 0)
            if (xs[0].startsWith("-")) xs = processArg(xs);
    }

    /**
     * Processes a given array of strings, extracting a subset of elements and applying a command
     * processing operation on the first two elements of the input array.
     *
     * @param xs the input array of strings where the first two elements are used for command processing
     *           and the remaining elements are returned as the result.
     * @return an array of strings containing the elements of the input array excluding the first two.
     */
    private static String[] processArg(String[] xs) {
        String[] result = new String[0];
        if (xs.length >= 2) {
            System.arraycopy(xs, 2, result, 0, xs.length - 2);
            processCommand(xs[0], xs[1]);
        }
        return result;
    }

    /**
     * Processes a command and performs an associated action based on the given inputs.
     *
     * @param x the command identifier, which specifies the operation to perform.
     *          Supported values: 
     *          "N" for setting cutoff value
     *          "P" for setting the parallelism level
     *          "D" for setting max recursion depth
     *          "T" for setting thread count (will calculate max depth)
     *          "E" for selecting which experiment to run
     * @param y the value associated with the command.
     */
    private static void processCommand(String x, String y) {
        if (x.equalsIgnoreCase("N")) {
            ParSort.cutoff = Integer.parseInt(y);
            System.out.println("Setting cutoff to: " + ParSort.cutoff);
        }
        else if (x.equalsIgnoreCase("P")) {
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", y);
            System.out.println("Setting parallelism to: " + y);
        }
        else if (x.equalsIgnoreCase("D")) {
            ParSort.maxDepth = Integer.parseInt(y);
            System.out.println("Setting max recursion depth to: " + ParSort.maxDepth);
        }
        else if (x.equalsIgnoreCase("T")) {
            int threadCount = Integer.parseInt(y);
            ParSort.setMaxDepthFromThreadCount(threadCount);
            System.out.println("Setting thread count to: " + threadCount + 
                               " (max depth: " + ParSort.maxDepth + ")");
        }
        else if (x.equalsIgnoreCase("E")) {
            setConfig(x, Integer.parseInt(y));
        }
    }

    /**
     * Configures a key-value pair in the application's configuration.
     * This method stores the specified key and associated integer value
     * into the configuration map.
     *
     * @param x the key to be stored in the configuration
     * @param i the integer value to be associated with the specified key
     */
    private static void setConfig(String x, int i) {
        configuration.put(x, i);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<String, Integer> configuration = new HashMap<>();
}