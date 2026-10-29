package com.example.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TailRecursiveFoldRightExampleTest {

    @Test
    void fold_right_sums_values() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Integer result = TailRecursiveFoldRightExample.foldRight(numbers, 0, value -> acc -> value + acc);

        assertEquals(15, result);
    }

    @Test
    void fold_right_preserves_right_associative_order() {
        List<Integer> numbers = List.of(1, 2, 3);

        String result = TailRecursiveFoldRightExample.foldRight(numbers, "zero", value -> acc -> "(" + value + " + " + acc + ")");

        assertEquals("(1 + (2 + (3 + zero)))", result);
    }

    @Test
    void fold_right_returns_identity_for_empty_list() {
        String result = TailRecursiveFoldRightExample.foldRight(List.<Integer>of(), "zero", value -> acc -> value + " + " + acc);

        assertEquals("zero", result);
    }
}
