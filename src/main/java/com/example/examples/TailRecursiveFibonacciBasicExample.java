package com.example.examples;

import java.math.BigInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TailRecursiveFibonacciBasicExample {

    private static BigInteger fib_ (BigInteger acc1, BigInteger acc2, BigInteger x) {
        if (x.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        } else if (x.equals(BigInteger.ONE)) {
            return acc1.add(acc2);
        } else  {
            return fib_(acc2, acc1.add(acc2), x.subtract(BigInteger.ONE));
        }
    }

    private static BigInteger fibonacci (int x) {
        return fib_(BigInteger.ONE, BigInteger.ZERO, BigInteger.valueOf(x));
    }

    public static void main(String[] args) {
        IntStream stream = IntStream.rangeClosed(1, 100);
        stream.mapToObj(TailRecursiveFibonacciBasicExample::fibonacci).forEach(System.out::println);
    }
}
