package com.example.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ComposeAllFoldRightExample {

    public static void main(String[] args) {
        List<Function<Integer, Integer>> operations = List.of(
                x -> x + 1,
                x -> x * 2,
                x -> x - 3
        );

        Function<Integer, Integer> composedWithJavaCompose = composeAllWithJavaCompose(operations);
        Function<Integer, Integer> composedViaFoldRight = composeAll(operations);
        Function<Integer, Integer> composedViaFoldLeft = composeAllViaFoldLeft(operations);

        System.out.println("composeAllWithJavaCompose(10) = " + composedWithJavaCompose.apply(10));
        System.out.println("composeAll(10) = " + composedViaFoldRight.apply(10));
        System.out.println("foldRight calculation = plusOne(timesTwo(minusThree(10)))");

        System.out.println("composeAllViaFoldLeft(10) = " + composedViaFoldLeft.apply(10));
        System.out.println("foldLeft calculation = minusThree(timesTwo(plusOne(10)))");

        Function<Integer, Integer> add = y -> y + 1;
        Function<Integer, Integer> fiveHundredAdds = composeAllViaFoldRight(map(range(0, 500), x -> add));

        System.out.println("500 composed increments from 0 = " + fiveHundredAdds.apply(0));
    }

    public static <T> Function<T, T> composeAll(List<Function<T, T>> functions) {
        return composeAllViaFoldRight(functions);
    }

    public static <T> Function<T, T> composeAllWithJavaCompose(List<Function<T, T>> functions) {
        return foldRight(functions, Function.identity(), f -> acc -> f.compose(acc));
    }

    public static <T> Function<T, T> composeAllViaFoldRight(List<Function<T, T>> functions) {
        return value -> foldRight(functions, value, function -> function::apply);
    }

    public static <T> Function<T, T> composeAllViaFoldLeft(List<Function<T, T>> functions) {
        return value -> foldLeft(functions, value, acc -> function -> function.apply(acc));
    }

    public static List<Integer> range(int startInclusive, int endExclusive) {
        List<Integer> result = new ArrayList<>();
        for (int value = startInclusive; value < endExclusive; value++) {
            result.add(value);
        }
        return List.copyOf(result);
    }

    public static <A, B> List<B> map(List<A> list, Function<A, B> f) {
        List<B> result = new ArrayList<>();
        for (A value : list) {
            result.add(f.apply(value));
        }
        return List.copyOf(result);
    }

    public static <A, B> B foldLeft(List<A> list, B identity, Function<B, Function<A, B>> f) {
        B acc = identity;
        for (A value : list) {
            acc = f.apply(acc).apply(value);
        }
        return acc;
    }

    public static <A, B> B foldRight(List<A> list, B identity, Function<A, Function<B, B>> f) {
        return foldRight(list, list.size() - 1, identity, f);
    }

    private static <A, B> B foldRight(List<A> list, int index, B acc, Function<A, Function<B, B>> f) {
        if (index < 0) {
            return acc;
        }

        B nextAcc = f.apply(list.get(index)).apply(acc);
        return foldRight(list, index - 1, nextAcc, f);
    }
}
