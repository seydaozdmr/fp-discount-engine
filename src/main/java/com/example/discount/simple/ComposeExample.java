package com.example.discount.simple;

import java.util.function.Function;

public class ComposeExample {

    public static void main(String[] args) {

        Function<Integer, Integer> triple = new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return integer * 3;
            }
        };

        Function<Integer, Integer> square = new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return integer * integer;
            }
        };

        Function<Integer, Integer> tripleThenSquare = new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return square.apply(triple.apply(integer));
            }
        };

        System.out.println(tripleThenSquare.apply(3));

        Function<Integer, Integer> tripleLambda = x -> x * 3;
        Function<Integer, Integer> squareLambda = x -> x * x;

        Function<Integer, Integer> tripleThenSquareLambda = x -> squareLambda.apply(tripleLambda.apply(x));

        Function<Function<Integer, Integer>, Function<Function<Integer, Integer>, Function<Integer,Integer>>> compose =
                f1 -> f2 -> f3 -> f1.apply(f2.apply(f3));

        Function<Function<Integer,Integer>, Function<Function<Integer,Integer>, Function<Integer,Integer>>> reverseCompose =
                f->g-> x-> g.apply(f.apply(x));

        Function<Integer, Integer> composed = reverseCompose.apply(tripleLambda).apply(squareLambda);

        System.out.println(composed.apply(3));


    }
}
