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

        Function<String, Integer> strToInt = Integer::parseInt;
        Function<Integer, String> intToStr = x -> "Sayı: " + x;

        Function<String,String> composedString = compose(intToStr, strToInt);

        String result = composedString.apply("44");

        System.out.println(result);

        Function<Double, Integer> doubleToInt = x-> x.intValue();
        Function<Integer, String> intToString = x-> "Number: " + x;

        Function<Double, String> doubleToString = andThen(doubleToInt, intToString);

        String result2 = doubleToString.apply(3.14);
        System.out.println(result2);

    }

    private static <T, U, V> Function<V,U> compose(Function<T,U> f1 , Function<V,T> f2) {
        return x -> f1.apply(f2.apply(x));
    }

    private static <T,U,V> Function<T,V> andThen(Function<T,U> f1, Function<U,V> f2){
        return x-> f2.apply(f1.apply(x));
    }

    private static <T,U,V> Function<T,V> anotherCompose(Function<U,V> f1, Function<T,U> f2) {
        return x-> f1.apply(f2.apply(x));
    }
}
