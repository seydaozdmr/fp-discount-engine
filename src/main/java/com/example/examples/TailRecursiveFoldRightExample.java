package com.example.examples;

import com.example.fpcore.CollectionUtils;

import java.util.List;
import java.util.function.Function;

public final class TailRecursiveFoldRightExample {

    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Integer sum = foldRight(numbers, 0, value -> acc -> value + acc);
        String expression = foldRight(numbers, "zero", value -> acc -> "(" + value + " + " + acc + ")");

        System.out.println("sum = " + sum);
        System.out.println("expression = " + expression);
    }

    public static <A, B> B foldRight(List<A> list, B identity, Function<A, Function<B, B>> f) {
        Function<B, B> folded = foldRight(list, Function.identity(), f);
        return folded.apply(identity);
    }

    private static <A, B> Function<B, B> foldRight(
            List<A> list,
            Function<B, B> acc,
            Function<A, Function<B, B>> f
    ) {
        if (list.isEmpty()) {
            return acc;
        }

        A head = CollectionUtils.head(list);
        List<A> tail = CollectionUtils.tail(list);
        Function<B, B> nextAcc = value -> acc.apply(f.apply(head).apply(value));

        return foldRight(tail, nextAcc, f);
    }
}
