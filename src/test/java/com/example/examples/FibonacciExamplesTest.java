package com.example.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FibonacciExamplesTest {

    @Test
    void tail_recursive_fibonacci_returns_expected_values() {
        assertEquals(0L, TailRecursiveFibonacciExample.fibonacci(0));
        assertEquals(1L, TailRecursiveFibonacciExample.fibonacci(1));
        assertEquals(1L, TailRecursiveFibonacciExample.fibonacci(2));
        assertEquals(5L, TailRecursiveFibonacciExample.fibonacci(5));
        assertEquals(55L, TailRecursiveFibonacciExample.fibonacci(10));
    }

    @Test
    void tail_recursive_fibonacci_rejects_negative_values() {
        assertThrows(IllegalArgumentException.class, () -> TailRecursiveFibonacciExample.fibonacci(-1));
    }
}
