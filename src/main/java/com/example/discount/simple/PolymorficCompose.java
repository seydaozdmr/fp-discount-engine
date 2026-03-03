package com.example.discount.simple;

import java.util.function.Function;

public class PolymorficCompose {
    public static void main(String[] args) {

        Function<Integer, Integer> triple = x -> x * 3;
        Function<Integer, Integer> square = s -> s * s;


        Integer squareThenTriple = PolymorficCompose.<Integer,Integer,Integer>higherCompose().apply(triple).apply(square).apply(3);
        System.out.println(squareThenTriple);

        Integer tripleThenSquare = PolymorficCompose.<Integer,Integer,Integer>higherAndThen().apply(triple).apply(square).apply(3);
        System.out.println(tripleThenSquare);
    }

    public static <T,U,V> Function<Function<U,V>, Function<Function<T,U>, Function<T,V>>> higherCompose() {
        return uv -> tu -> t -> uv.apply(tu.apply(t));
    }

    public static <T,U,V> Function<Function<T,U>, Function<Function<U,V>, Function<T,V>>> higherAndThen() {
        return tu -> uv -> t -> uv.apply(tu.apply(t));
    }
}
