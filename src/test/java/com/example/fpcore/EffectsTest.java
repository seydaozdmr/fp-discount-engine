package com.example.fpcore;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EffectsTest {

    @Test
    void for_each_or_fail_runs_effect_on_success_and_returns_empty() {
        var seen = new ArrayList<Integer>();

        Result<String> rest = Result.success(42).forEachOrFail(seen::add);

        assertEquals(java.util.List.of(42), seen);
        assertTrue(rest.isEmpty());
    }

    @Test
    void for_each_or_fail_turns_failure_into_its_message_without_running_effect() {
        var seen = new ArrayList<Integer>();
        var errors = new ArrayList<String>();

        Result.<Integer>failure("Division by 0")
              .forEachOrFail(seen::add)
              .forEach(errors::add);

        assertTrue(seen.isEmpty());
        assertEquals(java.util.List.of("Division by 0"), errors);
    }

    @Test
    void for_each_or_fail_does_nothing_on_empty() {
        var seen = new ArrayList<Integer>();
        var errors = new ArrayList<String>();

        Result.<Integer>empty()
              .forEachOrFail(seen::add)
              .forEach(errors::add);

        assertTrue(seen.isEmpty());
        assertTrue(errors.isEmpty());
    }

    @Test
    void list_for_each_applies_effect_in_order() {
        var seen = new ArrayList<Integer>();

        List.list(1, 2, 3).forEach(seen::add);

        assertEquals(java.util.List.of(1, 2, 3), seen);
    }

    @Test
    void list_for_each_is_stack_safe() {
        int[] count = {0};

        List.range(0, 100_000).forEach(i -> count[0]++);

        assertEquals(100_000, count[0]);
    }
}
