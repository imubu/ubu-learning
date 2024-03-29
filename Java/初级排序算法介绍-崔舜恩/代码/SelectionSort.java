package com.cipher.algorithm.line;

import static com.cipher.algorithm.line.SortTestHelper.*;

/**
 * 选择排序 O(n^2)
 * i 从 0 开始递增，以第 i 个元素为最小，从 i+1 开始找出比该值更小的元素，与之交换位置。
 * Created by cipher on 2017/9/14.
 */
public  class SelectionSort {

    public static void sort(Comparable[] data) {
        for (int i = 0; i < data.length; i++) {
            // 假设第 i 个元素为最小
            int minIndex = i;
            // 从 i+1 开始找出比该值更小的元素，与之交换位置。
            for (int j = i + 1; j < data.length; j++) {
                if (data[j].compareTo(data[minIndex]) < 0) {
                    minIndex = j;
                }
            }
            swap(data, i, minIndex);
        }
    }

    public static void main(String[] args) {
        Integer[] data = generateRandomArray(10000, 1, 10000);
//        printArray(data);
        testSort(SelectionSort.class, data);
//        printArray(data);
    }

}
