package com.example.fpcore;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FpCoreTest {

    @Test
    void option_and_result_lift_work() {
        Function<Integer, Integer> doubleIt = x -> x * 2;

        Option<Integer> some = Option.some(3);
        Option<Integer> none = Option.none();
        assertEquals(6, Functions.liftOption(doubleIt).apply(some).getOrThrow());
        assertTrue(Functions.liftOption(doubleIt).apply(none).isEmpty());

        Result<Integer> success = Result.success(4);
        assertEquals(8, Functions.liftResult(doubleIt).apply(success).getOrThrow());
    }

    @Test
    void compose_and_curry_work() {
        Function<Integer, Integer> add1 = x -> x + 1;
        Function<Integer, Integer> times3 = x -> x * 3;

        assertEquals(12, Functions.compose(times3, add1).apply(3));
        assertEquals(12, Functions.andThen(add1, times3).apply(3));
        assertEquals(7, Functions.curry(Integer::sum).apply(3).apply(4));
    }

    @Test
    void validation_accumulates_errors() {
        Validation<Integer> invalidA = Validation.invalid("a missing");
        Validation<Integer> invalidB = Validation.invalid("b missing");

        Validation<Integer> v = Validation.map2(invalidA, invalidB, Integer::sum);
        assertTrue(!v.isValid());
        assertEquals(List.of("a missing", "b missing"), v.errors());
    }

    @Test
    void result_sequence_short_circuits_on_failure() {
        List<Result<Integer>> list = List.of(
                Result.success(1),
                Result.failure("boom"),
                Result.success(3)
        );

        Result<List<Integer>> result = Result.sequence(list);
        assertTrue(result.isFailure());
    }

    @Test
    void result_sequence_preserves_order() {
        List<Result<Integer>> list = List.of(Result.success(1), Result.success(2), Result.success(3));
        Result<List<Integer>> result = Result.sequence(list);
        assertEquals(List.of(1, 2, 3), result.getOrThrow());
    }
}
