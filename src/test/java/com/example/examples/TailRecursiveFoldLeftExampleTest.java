package com.example.examples;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TailRecursiveFoldLeftExampleTest {

    @Test
    void fold_left_sums_values_from_left_to_right() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Integer result = TailRecursiveFoldLeftExample.foldLeft(numbers, 0, acc -> value -> acc + value);

        assertEquals(15, result);
    }

    @Test
    void fold_left_preserves_left_associative_order() {
        List<Integer> numbers = List.of(1, 2, 3);

        String result = TailRecursiveFoldLeftExample.foldLeft(numbers, "start", acc -> value -> "(" + acc + " + " + value + ")");

        assertEquals("(((start + 1) + 2) + 3)", result);
    }

    @Test
    void fold_left_handles_large_lists_without_growing_the_java_call_stack() {
        List<Integer> numbers = IntStream.rangeClosed(1, 100_000).boxed().toList();

        Long result = TailRecursiveFoldLeftExample.foldLeft(numbers, 0L, acc -> value -> acc + value);

        assertEquals(5_000_050_000L, result);
    }
}
