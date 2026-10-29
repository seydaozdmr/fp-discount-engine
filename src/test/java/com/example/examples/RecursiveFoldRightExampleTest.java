package com.example.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursiveFoldRightExampleTest {

    @Test
    void fold_right_sums_values() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Integer result = RecursiveFoldRightExample.foldRight(numbers, 0, value -> acc -> value + acc);

        assertEquals(15, result);
    }

    @Test
    void fold_right_applies_values_from_right_to_left() {
        List<Integer> numbers = List.of(1, 2, 3);

        String result = RecursiveFoldRightExample.foldRight(numbers, "zero", value -> acc -> "(" + value + " + " + acc + ")");

        assertEquals("(1 + (2 + (3 + zero)))", result);
    }

    @Test
    void fold_right_returns_identity_for_empty_list() {
        String result = RecursiveFoldRightExample.foldRight(List.<Integer>of(), "zero", value -> acc -> value + " + " + acc);

        assertEquals("zero", result);
    }
}
