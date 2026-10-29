package com.example.examples;

import com.example.fpcore.CollectionUtils;

import java.util.List;

public final class TailRecursiveRangeExample {

    public static void main(String[] args) {
        System.out.println(range(1, 10));
    }

    public static List<Integer> range(int startInclusive, int endExclusive) {
        return range(startInclusive, endExclusive, List.of());
    }

    private static List<Integer> range(int current, int endExclusive, List<Integer> acc) {
        if (current >= endExclusive) {
            return acc;
        }

        return range(current + 1, endExclusive, CollectionUtils.append(acc, current));
    }
}
