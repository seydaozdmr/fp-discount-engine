package com.example.fpcore;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void result_map2_fail_fast_but_validation_map2_accumulates_errors() {
        Result<Integer> resultA = Result.failure("a failed");
        Result<Integer> resultB = Result.failure("b failed");

        Result<Integer> resultC = Result.map2(resultA, resultB, a -> b -> a + b);

        assertTrue(resultC.isFailure());
        assertEquals("a failed", resultC.failureCause().getMessage());

        Validation<Integer> validationA = Validation.invalid("a missing");
        Validation<Integer> validationB = Validation.invalid("b missing");

        Validation<Integer> validationC = Validation.map2(validationA, validationB, Integer::sum);
        assertFalse(validationC.isValid());
        assertEquals(
                List.of("a missing", "b missing"), validationC.errors()
        );
    }

    @Test
    void traverse_reduce_returns_failure_when_any_item_fails() {
        List<Integer> inputs = List.of(1, 2, 3);

        Result<List<Integer>> result = Result.traverseReduce(inputs, i-> i==2 ? Result.failure("rule-2 failed") :
                Result.success(i * 10));

        assertTrue(result.isFailure());
        assertEquals("rule-2 failed", result.failureCause().getMessage());
    }

    @Test
    void traverse_reduce_may_still_invoke_mapper_after_failure() {
        List<Integer> inputs = List.of(1, 2, 3);
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger(0);

        Result<List<Integer>> result = Result.traverseReduce(inputs, i -> {
            calls.incrementAndGet();
            return i == 2 ? Result.failure("boom at 2") : Result.success(i);
        });

        assertTrue(result.isFailure());
        assertEquals("boom at 2", result.failureCause().getMessage());

        // Eager evaluation nedeniyle 3 elemanda da mapper çağrılabilir
        assertEquals(3, calls.get());
    }

    @Test
    void traverse_reduce_short_circuit_stops_mapper_calls_after_failure() {
        List<Integer> inputs = List.of(1,2,3);

        AtomicInteger calls = new AtomicInteger(0);
        Result<List<Integer>> result = Result.traverseReduceShortCircuit(inputs, i -> {
            calls.incrementAndGet();
            return i == 2 ? Result.failure("boom at 2") : Result.success(i);
        });
        assertTrue(result.isFailure());
        assertEquals("boom at 2", result.failureCause().getMessage());
        assertEquals(2, calls.get());
     }

     @Test
    void folds_map_filter_reverse_work() {
        List<Integer> inputs = List.of(1, 2, 3, 4, 5);
        assertEquals(List.of(2,4,6,8,10), Folds.map(inputs, x -> x * 2));
        assertEquals(List.of(2,4), Folds.filter(inputs, x -> x  % 2 == 0));
        assertEquals(List.of(5,4,3,2,1), Folds.reverse(inputs));
     }
}
