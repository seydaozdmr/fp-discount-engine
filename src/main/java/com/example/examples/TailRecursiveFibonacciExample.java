package com.example.examples;

import com.example.fpcore.TailCall;

import java.util.stream.IntStream;

public final class TailRecursiveFibonacciExample {

    public static void main(String[] args) {
        IntStream.rangeClosed(0, 10)
                .forEach(n -> System.out.println("fib(" + n + ") = " + fibonacci(n)));
    }

    public static long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }

        return fibonacci(n, 0L, 1L).eval();
    }

    private static TailCall<Long> fibonacci(int remaining, long current, long next) {
        if (remaining == 0) {
            return TailCall.done(current);
        }

        return TailCall.call(() -> fibonacci(remaining - 1, next, current + next));
    }
}
