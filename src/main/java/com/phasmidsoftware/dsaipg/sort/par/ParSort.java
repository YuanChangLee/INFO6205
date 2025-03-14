package com.phasmidsoftware.dsaipg.sort.par;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

final class ParSort {

    public static int cutoff = 1000;
    
    public static int maxDepth = calculateMaxDepth();
    
    private static int calculateMaxDepth() {
        int processors = ForkJoinPool.getCommonPoolParallelism();
        int t = Integer.highestOneBit(processors); // 確保 t 是 2 的冪次方
        return (int) (Math.log(t) / Math.log(2));  // 設定遞迴深度為 lg(t)
    }

    public static void sort(int[] array, int from, int to, int depth) {
        if ((to - from) >= cutoff && depth < maxDepth) {  // 確保遞迴深度不超過 maxDepth
            int mid = from + (to - from) / 2;
            CompletableFuture<int[]> leftFuture = asyncSort(array, from, mid, depth + 1);
            CompletableFuture<int[]> rightFuture = asyncSort(array, mid, to, depth + 1);
            CompletableFuture<int[]> mergedFuture = leftFuture.thenCombine(rightFuture, ParSort::doMerge);
            mergedFuture.whenComplete((result, throwable) -> System.arraycopy(result, 0, array, from, result.length));
            mergedFuture.join();
        } else {
            Arrays.sort(array, from, to);
        }
    }
    
    public static void sort(int[] array, int from, int to) {
        sort(array, from, to, 0);
    }

    static int[] sortRecursive(int[] array, int from, int to, int depth) {
        int[] result = new int[to - from];
        System.arraycopy(array, from, result, 0, to - from);
        sort(result, 0, result.length, depth);
        return result;
    }
    
    static int[] sortRecursive(int[] array, int from, int to) {
        return sortRecursive(array, from, to, 0);
    }

    static int[] doMerge(int[] xs1, int[] xs2) {
        int[] result = new int[xs1.length + xs2.length];
        int i = 0;
        int j = 0;
        for (int k = 0; k < result.length; k++) {
            if (i >= xs1.length) result[k] = xs2[j++];
            else if (j >= xs2.length) result[k] = xs1[i++];
            else if (xs2[j] < xs1[i]) result[k] = xs2[j++];
            else result[k] = xs1[i++];
        }
        return result;
    }

    static CompletableFuture<int[]> asyncSort(int[] array, int from, int to, int depth) {
        return CompletableFuture.supplyAsync(
                () -> sortRecursive(array, from, to, depth)
        );
    }
    
    static CompletableFuture<int[]> asyncSort(int[] array, int from, int to) {
        return asyncSort(array, from, to, 0);
    }
    
    public static void setMaxDepthFromThreadCount(int threadCount) {
        if (threadCount <= 0) threadCount = 1;
        threadCount = Integer.highestOneBit(threadCount); // 確保 threadCount 是 2 的冪次方
        maxDepth = (int)(Math.log(threadCount) / Math.log(2));
    }
}
