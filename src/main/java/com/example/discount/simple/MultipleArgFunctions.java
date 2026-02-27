package com.example.discount.simple;

import java.util.function.Function;

public class MultipleArgFunctions {

    public static void main(String[] args) {
        Function<Integer , Function<Integer, Integer>> sumFunction = x-> y -> x + y;

        Function<Integer, Integer> add5 = sumFunction.apply(5);

        System.out.println(add5.apply(10));
    }
}
