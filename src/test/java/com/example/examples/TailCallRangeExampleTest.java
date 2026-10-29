package com.example.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TailCallRangeExampleTest {

    @Test
    void range_returns_values_in_order() {
        assertEquals(List.of(1, 2, 3, 4), TailCallRangeExample.range(1, 5));
    }

    @Test
    void range_returns_empty_when_bounds_are_equal() {
        assertEquals(List.of(), TailCallRangeExample.range(3, 3));
    }

    @Test
    void range_returns_empty_when_start_is_greater_than_end() {
        assertEquals(List.of(), TailCallRangeExample.range(5, 2));
    }

    @Test
    void range_supports_negative_numbers() {
        assertEquals(List.of(-2, -1, 0, 1), TailCallRangeExample.range(-2, 2));
    }

    @Test
    void range_handles_large_inputs_without_growing_the_java_call_stack() {
        List<Integer> result = TailCallRangeExample.range(1, 10_001);

        assertEquals(10_000, result.size());
        assertEquals(1, result.get(0));
        assertEquals(10_000, result.get(result.size() - 1));
    }
}
