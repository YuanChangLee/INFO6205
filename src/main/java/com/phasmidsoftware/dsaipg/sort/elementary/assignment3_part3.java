package com.phasmidsoftware.dsaipg.sort.elementary;

import java.util.Random;

public class assignment3_part3 {

    public static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }
    
    public static void main(String[] args) {
        int[] sizes = new int[]{100, 200, 400, 800, 1600};  // 測試不同大小的陣列
        Random rand = new Random();

        for (int n : sizes) {
            int[] randomArray = new int[n];
            for (int i = 0; i < n; i++) {
                randomArray[i] = rand.nextInt(n);
            }

            int[] orderedArray = new int[n];
            for (int i = 0; i < n; i++) {
                orderedArray[i] = i;
            }

            int[] partiallyOrderedArray = new int[n];
            for (int i = 0; i < n / 2; i++) {
                partiallyOrderedArray[i] = i;
            }
            for (int i = n / 2; i < n; i++) {
                partiallyOrderedArray[i] = rand.nextInt(n);
            }

            int[] reverseOrderedArray = new int[n];
            for (int i = 0; i < n; i++) {
                reverseOrderedArray[i] = n - 1 - i;
            }

            measureAndPrint("Random", n, randomArray);
            measureAndPrint("Ordered", n, orderedArray);
            measureAndPrint("Partially Ordered", n, partiallyOrderedArray);
            measureAndPrint("Reverse Ordered", n, reverseOrderedArray);
            
            System.out.println("------------------------------------------------------------");
        }
    }

    public static void measureAndPrint(String type, int size, int[] array) {
        int[] copiedArray = array.clone();
        long startTime = System.nanoTime();
        insertionSort(copiedArray);
        long endTime = System.nanoTime();
        System.out.printf("Time for %s array of size %d is %.2f ms%n", type, size, (endTime - startTime) / 1_000_000.0);
    }

}
