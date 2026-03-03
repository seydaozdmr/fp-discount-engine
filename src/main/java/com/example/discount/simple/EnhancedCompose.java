package com.example.discount.simple;

import java.util.function.Function;

public class EnhancedCompose {


    public static void main(String[] args) {
        Function<Integer, Integer> triple = x -> x * 3;
        Function<Integer, Integer> square = x -> x * x;
        // Basic compose
        Function<Function<Integer,Integer> , Function<Function<Integer,Integer>, Function<Integer,Integer>>> composed = compose();
        Function<Function<Integer,Integer>, Function<Integer,Integer>> tripleThenSquare = composed.apply(triple);
        Function<Integer, Integer> apply = tripleThenSquare.apply(square);
        System.out.println(apply.apply(3));

        // Higher order compose
        Function<Function<Integer, Integer>, Function<Function<Integer, Integer>, Function<Integer, Integer>>> higherComposed = higherCompose();
        Function<Function<Integer, Integer>, Function<Integer, Integer>> tripleThenSquareHigher = higherComposed.apply(triple);
        Function<Integer, Integer> applyHigher = tripleThenSquareHigher.apply(square);
        System.out.println(applyHigher.apply(3));

    }


    private static <T,U,V> Function<Function<T,U> , Function<Function<U,V>, Function<T,V>>> compose() {
        return tu -> uv-> uv.compose(tu);
    }

    private static <T,U,V> Function<Function<U,V> , Function<Function<T,U>, Function<T,V>>> higherCompose() {
        return x -> y -> z -> x.apply(y.apply(z));
    }
}
