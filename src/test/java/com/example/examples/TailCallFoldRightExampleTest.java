package com.example.examples;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TailCallFoldRightExampleTest {

    @Test
    void fold_right_sums_values() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Integer result = TailCallFoldRightExample.foldRight(numbers, 0, value -> acc -> value + acc);

        assertEquals(15, result);
    }

    @Test
    void fold_right_preserves_right_associative_order() {
        List<Integer> numbers = List.of(1, 2, 3);

        String result = TailCallFoldRightExample.foldRight(numbers, "zero", value -> acc -> "(" + value + " + " + acc + ")");

        assertEquals("(1 + (2 + (3 + zero)))", result);
    }

    @Test
    void fold_right_returns_identity_for_empty_list() {
        String result = TailCallFoldRightExample.foldRight(List.<Integer>of(), "zero", value -> acc -> value + " + " + acc);

        assertEquals("zero", result);
    }

    @Test
    void fold_right_matches_recursive_fold_right_for_the_same_inputs() {
        List<Integer> numbers = List.of(1, 2, 3, 4);

        String recursive = RecursiveFoldRightExample.foldRight(numbers, "zero", value -> acc -> "(" + value + " + " + acc + ")");
        String tailCall = TailCallFoldRightExample.foldRight(numbers, "zero", value -> acc -> "(" + value + " + " + acc + ")");

        assertEquals(recursive, tailCall);
    }

    @Test
    void fold_right_handles_large_inputs_without_growing_the_java_call_stack() {
        List<Integer> numbers = IntStream.rangeClosed(1, 100_000).boxed().toList();

        Long result = TailCallFoldRightExample.foldRight(numbers, 0L, value -> acc -> value + acc);

        assertEquals(5_000_050_000L, result);
    }
}
