package com.example.examples;

import com.example.fpcore.TailCall;

import java.util.List;
import java.util.function.Function;

public final class TailRecursiveFoldLeftExample {

    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Integer sum = foldLeft(numbers, 0, acc -> value -> acc + value);
        String joined = foldLeft(numbers, "start", acc -> value -> acc + " -> " + value);

        System.out.println("sum = " + sum);
        System.out.println("joined = " + joined);
    }

    public static <A, B> B foldLeft(List<A> list, B identity, Function<B, Function<A, B>> f) {
        return foldLeft(list, 0, identity, f).eval();
    }

    private static <A, B> TailCall<B> foldLeft(List<A> list, int index, B acc, Function<B, Function<A, B>> f) {
        if (index == list.size()) {
            return TailCall.done(acc);
        }

        B nextAcc = f.apply(acc).apply(list.get(index));
        return TailCall.call(() -> foldLeft(list, index + 1, nextAcc, f));
    }
}
