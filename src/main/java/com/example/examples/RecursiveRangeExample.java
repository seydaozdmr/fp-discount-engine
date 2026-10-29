package com.example.examples;

import com.example.fpcore.CollectionUtils;

import java.util.List;

public final class RecursiveRangeExample {

    public static void main(String[] args) {
        System.out.println(range(1, 10));
    }

    public static List<Integer> range(int startInclusive, int endExclusive) {
        if (startInclusive >= endExclusive) {
            return List.of();
        }

        return CollectionUtils.prepend(startInclusive, range(startInclusive + 1, endExclusive));
    }
}
