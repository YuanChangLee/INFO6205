package com.phasmidsoftware.dsaipg.adt.pq;

import com.phasmidsoftware.dsaipg.util.Benchmark;
import com.phasmidsoftware.dsaipg.util.Benchmark_Timer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class assignment4 {

    public static void main(String[] args) {

        int M = 4090;
        int insertions = 16000;
        int removals = 4000;
        Random random = new Random();

        Comparator<Integer> comp = Comparator.comparingInt(a -> a);

        Benchmark<BinaryHeap<Integer>> binaryHeapBenchmark = new Benchmark_Timer<>(
                "Binary Heap",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );
        BinaryHeap<Integer> binaryHeap = new BinaryHeap<>(M, comp);
        double binaryHeapTime = binaryHeapBenchmark.run(binaryHeap, 1);
        System.out.println("Binary Heap Time: " + binaryHeapTime + " ms");
        System.out.println("Binary Heap spilled max: " + getMaxSpilled(binaryHeap.getSpilled(), comp));

        // Benchmark for Binary Heap with Floyd's Trick
        Benchmark<BinaryHeapFloyd<Integer>> binaryHeapFloydBenchmark = new Benchmark_Timer<>(
                "Binary Heap with Floyd's Trick",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );
        BinaryHeapFloyd<Integer> binaryHeapFloyd = new BinaryHeapFloyd<>(M, comp);
        double binaryHeapFloydTime = binaryHeapFloydBenchmark.run(binaryHeapFloyd, 1);
        System.out.println("Binary Heap with Floyd's Trick Time: " + binaryHeapFloydTime + " ms");
        System.out.println("Binary Heap with Floyd's Trick spilled max: " + getMaxSpilled(binaryHeapFloyd.getSpilled(), comp));

        // Benchmark for 4-ary Heap (不使用 Floyd’s Trick)
        Benchmark<FourAryHeap<Integer>> fourAryHeapBenchmark = new Benchmark_Timer<>(
                "4-ary Heap",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );
        FourAryHeap<Integer> fourAryHeap = new FourAryHeap<>(M, comp);
        double fourAryHeapTime = fourAryHeapBenchmark.run(fourAryHeap, 1);
        System.out.println("4-ary Heap Time: " + fourAryHeapTime + " ms");
        System.out.println("4-ary Heap spilled max: " + getMaxSpilled(fourAryHeap.getSpilled(), comp));

        // Benchmark for 4-ary Heap with Floyd's Trick
        Benchmark<FourAryHeapFloyd<Integer>> fourAryHeapFloydBenchmark = new Benchmark_Timer<>(
                "4-ary Heap with Floyd's Trick",
                null,
                pq -> {
                    for (int i = 0; i < insertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < removals; i++) {
                        pq.remove();
                    }
                },
                null
        );
        FourAryHeapFloyd<Integer> fourAryHeapFloyd = new FourAryHeapFloyd<>(M, comp);
        double fourAryHeapFloydTime = fourAryHeapFloydBenchmark.run(fourAryHeapFloyd, 1);
        System.out.println("4-ary Heap with Floyd's Trick Time: " + fourAryHeapFloydTime + " ms");
        System.out.println("4-ary Heap with Floyd's Trick spilled max: " + getMaxSpilled(fourAryHeapFloyd.getSpilled(), comp));

        // Benchmark for Fibonacci Heap (Bonus)
        int fibInsertions = 4000;
        int fibRemovals = 1000;
        Benchmark<FibonacciHeap<Integer>> fibonacciHeapBenchmark = new Benchmark_Timer<>(
                "Fibonacci Heap",
                null,
                pq -> {
                    for (int i = 0; i < fibInsertions; i++) {
                        pq.insert(random.nextInt());
                    }
                    for (int i = 0; i < fibRemovals; i++) {
                        pq.remove();
                    }
                },
                null
        );
        FibonacciHeap<Integer> fibonacciHeap = new FibonacciHeap<>(M, comp);
        double fibonacciHeapTime = fibonacciHeapBenchmark.run(fibonacciHeap, 1);
        System.out.println("Fibonacci Heap Time: " + fibonacciHeapTime + " ms");
        System.out.println("Fibonacci Heap spilled max: " + getMaxSpilled(fibonacciHeap.getSpilled(), comp));
    }

    // 共用方法：從 spilled 元素中找出具有最高優先權的那個（max-heap 中即最大者）
    public static <T> T getMaxSpilled(List<T> spilled, Comparator<T> comparator) {
        if (spilled.isEmpty()) return null;
        T max = spilled.get(0);
        for (T elem : spilled) {
            if (comparator.compare(elem, max) > 0) {
                max = elem;
            }
        }
        return max;
    }


    // 1. Binary Heap 
    public static class BinaryHeap<T> {
        private final com.phasmidsoftware.dsaipg.adt.pq.PriorityQueue<T> heap;
        private final int capacity;
        private final List<T> spilled = new ArrayList<>();

        public BinaryHeap(int capacity, Comparator<? super T> comparator) {
            this.capacity = capacity;
            this.heap = new com.phasmidsoftware.dsaipg.adt.pq.PriorityQueue<>(capacity, 1, true, (Comparator<T>) comparator, false);
        }

        public void insert(T item) {
            if (heap.size() == capacity) {
                T spilledElement = remove();
                spilled.add(spilledElement);
            }
            heap.give(item);
        }

        public T remove() {
            try {
                return heap.take();
            } catch (PQException e) {
                throw new RuntimeException(e);
            }
        }

        public List<T> getSpilled() {
            return spilled;
        }
    }


    // 2. Binary Heap with Floyd's Trick
    public static class BinaryHeapFloyd<T> {
        private T[] heap;
        private int size;
        private final Comparator<T> comparator;
        private final List<T> spilled = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public BinaryHeapFloyd(int capacity, Comparator<T> comparator) {
            this.heap = (T[]) new Object[capacity];
            this.size = 0;
            this.comparator = comparator;
        }

        public void insert(T item) {
            if (size == heap.length) {
                T spilledElement = heap[size - 1];
                spilled.add(spilledElement);
                size--;
            }
            heap[size] = item;
            size++;
            swimUp(size - 1);
        }

        public T remove() {
            if (size == 0) throw new IllegalStateException("Heap is empty");
            T result = heap[0];
            heap[0] = heap[size - 1];
            size--;
            snake(0);
            return result;
        }

        private void snake(int index) {
            int pos = sinkReturnIndex(index);
            swimUp(pos);
        }

        private int sinkReturnIndex(int index) {
            int current = index;
            T value = heap[current];
            while (2 * current + 1 < size) {
                int left = 2 * current + 1;
                int right = left + 1;
                int chosen = left;
                if (right < size && comparator.compare(heap[right], heap[left]) > 0) {
                    chosen = right;
                }
                if (comparator.compare(value, heap[chosen]) >= 0) break;
                heap[current] = heap[chosen];
                current = chosen;
            }
            heap[current] = value;
            return current;
        }

        private void swimUp(int index) {
            int current = index;
            T value = heap[current];
            while (current > 0) {
                int parent = (current - 1) / 2;
                if (comparator.compare(value, heap[parent]) <= 0) break;
                heap[current] = heap[parent];
                current = parent;
            }
            heap[current] = value;
        }

        public List<T> getSpilled() {
            return spilled;
        }
    }


    // 3. 4-ary Heap
    public static class FourAryHeap<T> {
        protected T[] heap;
        protected int size;
        protected final Comparator<T> comparator;
        protected final List<T> spilled = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public FourAryHeap(int capacity, Comparator<T> comparator) {
            this.heap = (T[]) new Object[capacity];
            this.size = 0;
            this.comparator = comparator;
        }

        public void insert(T value) {
            if (size == heap.length) {
                T spilledElement = heap[size - 1];
                spilled.add(spilledElement);
                size--;
            }
            heap[size] = value;
            siftUp(size);
            size++;
        }

        public T remove() {
            if (size == 0) throw new IllegalStateException("Heap is empty");
            T result = heap[0];
            heap[0] = heap[size - 1];
            size--;
            siftDown(0);
            return result;
        }

        protected void siftUp(int index) {
            T value = heap[index];
            while (index > 0) {
                int parentIndex = (index - 1) / 4;
                if (comparator.compare(value, heap[parentIndex]) <= 0) break;
                heap[index] = heap[parentIndex];
                index = parentIndex;
            }
            heap[index] = value;
        }

        protected void siftDown(int index) {
            T value = heap[index];
            while (index * 4 + 1 < size) {
                int childIndex = index * 4 + 1;
                int chosen = childIndex;
                for (int i = 1; i < 4; i++) {
                    if (childIndex + i < size &&
                            comparator.compare(heap[childIndex + i], heap[chosen]) > 0) {
                        chosen = childIndex + i;
                    }
                }
                if (comparator.compare(value, heap[chosen]) >= 0) break;
                heap[index] = heap[chosen];
                index = chosen;
            }
            heap[index] = value;
        }

        public List<T> getSpilled() {
            return spilled;
        }
    }


    //4. 4-ary Heap with Floyd's Trick (max-heap)
    public static class FourAryHeapFloyd<T> extends FourAryHeap<T> {
        public FourAryHeapFloyd(int capacity, Comparator<T> comparator) {
            super(capacity, comparator);
        }

        @Override
        public T remove() {
            if (size == 0) throw new IllegalStateException("Heap is empty");
            T result = heap[0];
            heap[0] = heap[size - 1];
            size--;
            snake(0);
            return result;
        }

        private void snake(int index) {
            int pos = siftDownReturnIndex(index);
            siftUpReturnIndex(pos);
        }

        private int siftDownReturnIndex(int index) {
            int current = index;
            T value = heap[current];
            while (current * 4 + 1 < size) {
                int childIndex = current * 4 + 1;
                int chosen = childIndex;
                for (int i = 1; i < 4; i++) {
                    if (childIndex + i < size &&
                            comparator.compare(heap[childIndex + i], heap[chosen]) > 0) {
                        chosen = childIndex + i;
                    }
                }
                if (comparator.compare(value, heap[chosen]) >= 0) break;
                heap[current] = heap[chosen];
                current = chosen;
            }
            heap[current] = value;
            return current;
        }

        private void siftUpReturnIndex(int index) {
            int current = index;
            T value = heap[current];
            while (current > 0) {
                int parentIndex = (current - 1) / 4;
                if (comparator.compare(value, heap[parentIndex]) <= 0) break;
                heap[current] = heap[parentIndex];
                current = parentIndex;
            }
            heap[current] = value;
        }
    }


    // 5. Fibonacci Heap
    public static class FibonacciHeap<T> {
        private Node<T> max;  
        private int n;        
        private final Comparator<T> comparator;
        private final int capacity;
        private final List<T> spilled = new ArrayList<>();

        public FibonacciHeap(int capacity, Comparator<T> comparator) {
            this.max = null;
            this.n = 0;
            this.comparator = comparator;
            this.capacity = capacity;
        }

        public void insert(T key) {
            if (n == capacity) {
                T spilledElement = remove();
                spilled.add(spilledElement);
            }
            Node<T> node = new Node<>(key);
            max = mergeLists(max, node);
            n++;
        }

        public T remove() {
            if (max == null)
                throw new IllegalStateException("Heap is empty");
            Node<T> z = max;
            T maxKey = z.key;
            if (z.child != null) {
                List<Node<T>> childList = new ArrayList<>();
                Node<T> current = z.child;
                do {
                    childList.add(current);
                    current = current.right;
                } while (current != z.child);
                for (Node<T> child : childList) {
                    child.left.right = child.right;
                    child.right.left = child.left;
                    child.parent = null;
                    child.left = child;
                    child.right = child;
                    max = mergeLists(max, child);
                }
            }
            if (z.right == z) {
                max = null;
            } else {
                z.left.right = z.right;
                z.right.left = z.left;
                max = z.right;
                consolidate();
            }
            n--;
            return maxKey;
        }

        private void consolidate() {
            int D = ((int) Math.floor(Math.log(n) / Math.log(2))) + 2;
            @SuppressWarnings("unchecked")
            Node<T>[] A = new Node[D];
            for (int i = 0; i < D; i++) {
                A[i] = null;
            }
            List<Node<T>> rootList = new ArrayList<>();
            if (max != null) {
                Node<T> current = max;
                do {
                    rootList.add(current);
                    current = current.right;
                } while (current != max);
            }
            for (Node<T> x : rootList) {
                int d = x.degree;
                while (A[d] != null) {
                    Node<T> y = A[d];
                    if (comparator.compare(x.key, y.key) < 0) {
                        Node<T> temp = x;
                        x = y;
                        y = temp;
                    }
                    link(y, x);
                    A[d] = null;
                    d++;
                }
                A[d] = x;
            }
            max = null;
            for (int i = 0; i < D; i++) {
                if (A[i] != null) {
                    A[i].left = A[i];
                    A[i].right = A[i];
                    max = mergeLists(max, A[i]);
                }
            }
        }

        private void link(Node<T> y, Node<T> x) {
            removeNodeFromList(y);
            y.parent = x;
            if (x.child == null) {
                x.child = y;
                y.left = y;
                y.right = y;
            } else {
                y.left = x.child;
                y.right = x.child.right;
                x.child.right.left = y;
                x.child.right = y;
            }
            x.degree++;
            y.mark = false;
        }

        private Node<T> mergeLists(Node<T> a, Node<T> b) {
            if (a == null) return b;
            if (b == null) return a;
            Node<T> aNext = a.right;
            Node<T> bPrev = b.left;
            a.right = b;
            b.left = a;
            aNext.left = bPrev;
            bPrev.right = aNext;
            return comparator.compare(a.key, b.key) >= 0 ? a : b;
        }

        private void removeNodeFromList(Node<T> node) {
            node.left.right = node.right;
            node.right.left = node.left;
            node.left = node;
            node.right = node;
        }

        public List<T> getSpilled() {
            return spilled;
        }

        private static class Node<T> {
            T key;
            int degree;
            Node<T> parent;
            Node<T> child;
            Node<T> left;
            Node<T> right;
            boolean mark;

            Node(T key) {
                this.key = key;
                this.degree = 0;
                this.parent = null;
                this.child = null;
                this.left = this;
                this.right = this;
                this.mark = false;
            }
        }
    }
}
