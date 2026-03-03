package com.example.discount.simple;

import java.util.function.Function;

public class PartiallyApply {
    public static void main(String[] args) {
        Function<Integer, Function<Integer, Integer>> sumFunction = x -> y -> x + y;
        Function<Integer, Integer> add5 = partiallyApply(5, sumFunction);
        System.out.println(add5.apply(10));

        Function<Integer, Function<Integer, Integer>> multiplyFunction = x -> y -> x * y;
        Function<Integer, Integer> multiplyBy3 = partiallyApply(10, multiplyFunction);
        System.out.println(multiplyBy3.apply(10));

        Function<Integer, Function<Integer, Integer>> multiplyFunction2 = x -> y -> x * y;
        Function<Integer, Integer> multiplyBy3Second = partiallyApplySecond(10, multiplyFunction2);
        System.out.println(multiplyBy3Second.apply(10));

        Function<String, Function<String,String>> concatFunction = s1 -> s2 -> s1 + s2;
        Function<String, String> greetFunction = partiallyApply("Hello, ", concatFunction);
        System.out.println(greetFunction.apply("World!")); //reusable function
        System.out.println(greetFunction.apply("Seyda! "));
    }

    private static <A,B,C> Function<B,C> partiallyApply(A a, Function<A, Function<B, C>> f) {
        return f.apply(a);
    }

    private static <A,B,C> Function<A,C> partiallyApplySecond(B b, Function<A, Function<B, C>> f) {
        return a -> f.apply(a).apply(b);
    }

}
