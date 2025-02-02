/*
 * Copyright (c) 2017-2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.misc.randomwalk;

import java.util.Random;

/**
 * The RandomWalk class simulates a two-dimensional random walk. A "drunkard"
 * moves in a random direction for a specified number of steps, and the distance
 * from the starting point is measured. Additionally, multiple random walk
 * experiments can be performed to compute average distances.
 */
public class RandomWalk {
	
    private int x=0; // Current x location of the drunkard
    private int y=0; // Current y location of the drunkard
    
    private final Random random = new Random();

    /**
     * Method to compute the distance from the origin (the lamp-post where the drunkard starts) to his current position.
     *
     * @return the (Euclidean) distance from the origin to the current position.
     */
    public double distance() {
        // TO BE IMPLEMENTED 
        return Math.sqrt((long)x*x + (long)y*y);
        // END SOLUTION
    }

    /**
     * Private method to move the current position, that's to say the drunkard moves
     *
     * @param dx the distance he moves in the x direction
     * @param dy the distance he moves in the y direction
     */
    private void move(int dx, int dy) {
        // TO BE IMPLEMENTED  do move
        x = x + dx; // update x location
        y = y + dy; // update y location
        // END SOLUTION
    }

    /**
     * Perform a random walk of m steps
     *
     * @param m the number of steps the drunkard takes
     */
    private void randomWalk(int m) {
    	 
        for(int i=0;i<m;i++) {
            randomMove(); // take one random step
        }
    }

    /**
     * Private method to generate a random move according to the rules of the situation.
     * That's to say, moves can be (+-1, 0) or (0, +-1).
     */
    private void randomMove() {
        boolean ns = random.nextBoolean(); // if it is true, move on the y axis, false move on the x axis.
        int step = random.nextBoolean() ? 1 : -1; // if it is true, positive direction, false negative direction.
        move(ns ? step : 0, ns ? 0 : step);
    }
    
    /**
     * Perform multiple random walk experiments, returning the mean distance.
     *
     * @param m the number of steps for each experiment
     * @param n the number of experiments to run
     * @return the mean distance
     */
    public static double randomWalkMulti(int m, int n) {
        double totalDistance = 0;
        for (int i = 0; i < n; i++) {
            RandomWalk walk = new RandomWalk();
            walk.randomWalk(m); // Simulate m steps
            totalDistance = totalDistance + walk.distance(); // add the resulting distance to the total 
        }
        return totalDistance / n; // Return the average distance
    }

    /**
     * The main method serves as the entry point to the RandomWalk program. It performs
     * either a single random walk experiment or several experiments, based on the
     * provided input arguments, and prints the mean distance.
     *
     * @param args command-line arguments where:
     *             args[0] specifies the number of steps for a random walk (required),
     *             and args[1] optionally specifies the number of experiments (default is 30).
     *             If args is empty, the method throws a RuntimeException indicating invalid syntax.
     */
    public static void main(String[] args) {
        int [] stepValues = {10,20,30,40,50,60};
        int n = 100;
        for(int i=0;i<stepValues.length;i++) {
            int m = stepValues[i];
            double meanDistance = randomWalkMulti(m,n);
            System.out.println(m + " steps: " + meanDistance + " over " + n + " experiments");
        }
        }
}