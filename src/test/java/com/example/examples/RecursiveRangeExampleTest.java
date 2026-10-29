package com.example.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursiveRangeExampleTest {

    @Test
    void range_returns_values_in_order() {
        assertEquals(List.of(1, 2, 3, 4), RecursiveRangeExample.range(1, 5));
    }

    @Test
    void range_returns_empty_when_bounds_are_equal() {
        assertEquals(List.of(), RecursiveRangeExample.range(3, 3));
    }

    @Test
    void range_returns_empty_when_start_is_greater_than_end() {
        assertEquals(List.of(), RecursiveRangeExample.range(5, 2));
    }

    @Test
    void range_supports_negative_numbers() {
        assertEquals(List.of(-2, -1, 0, 1), RecursiveRangeExample.range(-2, 2));
    }
}
