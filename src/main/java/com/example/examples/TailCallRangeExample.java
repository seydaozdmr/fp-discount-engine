package com.example.examples;

import com.example.fpcore.CollectionUtils;
import com.example.fpcore.TailCall;

import java.util.List;

public final class TailCallRangeExample {

    public static void main(String[] args) {
        System.out.println(range(1, 10));
    }

    public static List<Integer> range(int startInclusive, int endExclusive) {
        return range(startInclusive, endExclusive, List.of()).eval();
    }

    private static TailCall<List<Integer>> range(int current, int endExclusive, List<Integer> acc) {
        if (current >= endExclusive) {
            return TailCall.done(acc);
        }

        List<Integer> nextAcc = CollectionUtils.append(acc, current);
        return TailCall.call(() -> range(current + 1, endExclusive, nextAcc));
    }
}
